package com.tailf.jnc;

import com.tailf.jnc.framing.BaseReader;
import com.tailf.jnc.framing.DataReader;
import com.tailf.jnc.framing.Framer;
import com.tailf.jnc.framing.Framing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.connection.channel.direct.Session;

/**
 * A SSH NETCONF transport. Can be used whenever {@link NetconfSession} intends
 * to use SSH for its transport. This class uses the SSHJ
 * implementation.
 * <p>
 * Example:
 *
 * <pre>
 * SSHConnection c = new SSHConnection()
 *           .setHostVerification(&quot;/home/user/.ssh/known_hosts&quot;)
 *           .connect(&quot;127.0.0.1&quot;, 789);
 * c.authenticateWithPassword(&quot;ola&quot;, &quot;secret&quot;);
 * SSHSession ssh = new SSHSession(c);
 * NetconfSession dev1 = new NetconfSession(ssh);
 * </pre>
 *
 */

public class SSHSession implements Transport, AutoCloseable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private SSHConnection connection = null;
    private Session session = null;
    private Session.Subsystem subsys;

    private InputWatchdog watchdog;

    private BufferedReader in;
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private StringBuilder message;
    private final List<IOSubscriber> ioSubscribers;
    protected long readTimeout = 0; // millisecs

    private Framer framer;
    private InputStream subsysInput;
    private OutputStream sessionOutput;

    private interface InputWatchdog {
        void start();
        void watch();
        void done();
        void terminate();
    }

    private class DummyWatchdog implements InputWatchdog {
        public void start() {
            // intentionally empty
        }
        public void watch() {
            // intentionally empty
        }
        public void done() {
            // intentionally empty
        }
        public void terminate() {
            // intentionally empty
        }
    }

    private class TimeoutWatchdog extends Thread implements InputWatchdog {
        private Semaphore semaphore = new Semaphore(0);
        private boolean running = true;

        @Override
        public void watch() {
            semaphore.release();
        }

        @Override
        public void done() {
            semaphore.release();
        }

        @Override
        public void terminate() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            try {
                while (running) {
                    semaphore.acquire();
                    // now the session is reading, watch for timeout
                    if (! semaphore.tryAcquire(readTimeout, TimeUnit.MILLISECONDS)) {
                        log.warn("read timeout, closing session");
                        close();
                        running = false;
                    }
                }
            } catch (InterruptedException e) {
                // log.debug("watchdog interrupted");
            }
        }
    }

    private class SessionDataReader implements DataReader {
        @Override
        public <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf)
            throws IOException {
            // not ideal, but avoiding code duplication
            return readData(rdr, buf, 0, rdr.bufSize(buf));
        }

        @Override
        public <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf,
                                          int offset, int length) throws IOException {
            int read = 0;
            watchdog.watch();
            try {
                read = rdr.read(buf, offset, length);
                if (read == -1) {
                    trace("end of input (-1)");
                    throw new IOException("Session closed");
                } else {
                    ByteBuffer data = rdr.encode(buf, offset, read);
                    // FIXME: this is broken, readData may be called repeatedly
                    // on the same data due to buffering and mark/reset calls
                    for (final IOSubscriber sub: ioSubscribers) {
                        sub.inputRaw(data);
                    }
                }
                return read;
            } finally {
                watchdog.done();
            }
        }
    }

    /**
     * Constructor for SSH session object. This method creates a a new SSh
     * channel on top of an existing connection. SSHSession objects imlement
     * the Transport interface and they are passed into the constructor of the
     * NetconfSession class.
     *
     * @param con an established and authenticated SSH connection
     */
    public SSHSession(SSHConnection con) throws IOException, JNCException {
        this(con, 0);
    }

    /**
     * Constructor with an extra argument for a readTimeout timer.
     *
     * @param con
     * @param readTimeout Time to wait for read. (in milliseconds)
     */
    public SSHSession(SSHConnection con, long readTimeout)
            throws IOException, JNCException {
        this.readTimeout = readTimeout;
        connection = con;

        session = con.client.startSession();
        subsys = session.startSubsystem("netconf");

        subsysInput = subsys.getInputStream();
        sessionOutput = session.getOutputStream();
        setFraming(Framing.END_OF_MESSAGE);
        if (readTimeout > 0) {
            watchdog = new TimeoutWatchdog();
        } else {
            watchdog = new DummyWatchdog();
        }
        watchdog.start();
        message = new StringBuilder();
        ioSubscribers = new ArrayList<IOSubscriber>();
        // hello will be done by NetconfSession
    }
    
    // Sets the framing to accommodate Netconf 1.1
    public void setFraming (Framing f) {
    	framer = f.newSessionFramer(new SessionDataReader(),
                                    subsysInput, sessionOutput);
    }

    /**
     * Return the underlying ssh connection object
     */
    public SSHConnection getSSHConnection() {
        return connection;
    }

    /**
     * Return the readTimeout value that is used to read data from the ssh
     * socket. If a read doesn't complete within the stipulated timeout an
     * INMException is thrown *
     */
    public long getReadTimeout() {
        return readTimeout;
    }

    /**
     * Set the read timeout
     *
     * @param readTimeout timout in milliseconds The readTimeout parameter
     *            affects all read operations. If a timeout is reached, an
     *            INMException is thrown. The socket is not closed.
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Tell whether this transport is ready to be read.
     *
     * @return true if there is something to read, false otherwise. This
     *         function can typically be used to poll a socket and see there is
     *         data to be read. The function will also return true if the
     *         server side has closed its end of the ssh socket. To explictly
     *         just check for that, use the serverSideClosed() method.
     */
    @Override
    public boolean ready() throws IOException {
        return in.ready();
    }

    /**
     * given a live SSHSession, check if the server side has closed it's end of
     * the ssh socket
     */
    public boolean serverSideClosed() throws IOException {
        return session.isEOF();
    }

    /**
     * Reads in "one" reply from the SSH transport input stream. A
     * <em>]]&gt;]]&gt;</em> character sequence is used to separate multiple
     * replies as described in <a target="_top"
     * href="ftp://ftp.rfc-editor.org/in-notes/rfc4742.txt">RFC 4742</a>.
     */
    @Override
    public String readOne() throws IOException, JNCException {
        String frame = framer.parseFrame();
        for (final IOSubscriber sub: ioSubscribers) {
            sub.inputFrame(frame);
        }
        return frame;
    }

    /**
     * Prints an integer (as text) to the output stream.
     *
     * @param iVal Text to send to the stream.
     */
   @Override
    public void print(long iVal) {

        String data = String.valueOf(iVal);
        trace(data);
        message.append(data);
    }

    /**
     * Prints text to the output stream.
     *
     * @param s Text to send to the stream.
     */
    @Override
    public void print(String s) {
        trace(s);
        message.append(s);
    }

    /**
     * Prints an integer (as text) to the output stream. A newline char is
     * appended to end of the output stream.
     *
     * @param iVal Text to send to the stream.
     */
    @Override
    public void println(long iVal) {
        String data = String.valueOf(iVal);
        print(data);
    }

    /**
     * Print text to the output stream. A newline char is appended to end of
     * the output stream.
     *
     * @param s Text to send to the stream.
     */
    @Override
    public void println(String s) {
        trace(s);
        message.append(s).append("\n");
    }

    /**
     * Add an IO Subscriber for this transport. This is useful for tracing the
     * messages.
     *
     * @param s An IOSUbscriber that will be called whenever there is something
     *            received or sent on this transport.
     */
    public void addSubscriber(IOSubscriber s) {
        ioSubscribers.add(s);
    }

    /**
     * Removes an IO subscriber.
     *
     * @param s The IO subscriber to remove.
     */
    public void delSubscriber(IOSubscriber s) {
        for (int i = 0; i < ioSubscribers.size(); i++) {
            final IOSubscriber x = ioSubscribers.get(i);
            if (s.equals(x)) {
                ioSubscribers.remove(i);
                return;
            }
        }
    }

    /**
     * Signals that the message is complete and should be sent to the output
     * transport stream. This method furthermore flushes the transport output
     * stream buffer.
     */
    @Override
    public void flush() throws IOException {
        String messageStr = message.toString();
        framer.sendFrame(messageStr);
        message = new StringBuilder();
    }

    /**
     * Needed by users that need to monitor a session for EOF . This will
     * return the underlying Ganymed SSH Session object.
     *
     * The ganymed Session object has a method waitForCondition() that can be
     * used to check the connection state of an ssh soscket. Assuming a A
     * Session object s:
     *
     * <pre>
     * int conditionSet =
     *     ChannelCondition.TIMEOUT ;amp
     *     ChannelCondition.CLOSED ;amp
     *     ChannelCondition.EOF;
     *     conditionSet = s.waitForCondition(conditionSet, 1);
     *  if (conditionSet != ChannelCondition.TIMEOUT) {
     *      // We know the server closed it's end of the ssh
     *      // socket
     * </pre>
     */

    public Session getSession() {
        return session;
    }

    /**
     * Closes the SSH channnel
     */
    @Override
    public void close() {
        try {
            watchdog.terminate();
            sessionOutput.close();
            subsys.close();
            session.close();
        } catch (IOException e) {
            System.out.println("Exception caught while closing " + e);
        }
    }

    /* help functions */

    public String getDeviceConnectionInfo()
    {
        return getSSHConnection().getClient().getRemoteHostname()
            + ":" + getSSHConnection().getClient().getRemotePort();
    }

    public Collection<IOSubscriber> getIOSubscribers()
    {
        return ioSubscribers;
    }

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private  void trace(String s) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.output("*SSHSession:" + s);
        }
        if (Element.debugLevel >= Element.DEBUG_LEVEL_TRANSPORT) {
            System.err.println("*SSHSession:@" + getDeviceConnectionInfo() + "\n" + s);
        }
    }
}

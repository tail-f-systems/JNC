package com.tailf.jnc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Session;

/**
 * A SSH NETCONF transport. Can be used whenever {@link NetconfSession} intends
 * to use SSH for its transport. This class uses the Ganymed SSH
 * implementation. (<a
 * href="http://www.ganymed.ethz.ch/ssh2/">http://www.ganymed
 * .ethz.ch/ssh2/</a>)
 * <p>
 * Example:
 * 
 * <pre>
 * SSHConnection c = new SSHConnection(&quot;127.0.0.1&quot;, 789);
 * c.authenticateWithPassword(&quot;ola&quot;, &quot;secret&quot;);
 * SSHSession ssh = new SSHSession(c);
 * NetconfSession dev1 = new NetconfSession(ssh);
 * </pre>
 * 
 */

public class SSHSession implements Transport {

    private SSHConnection connection = null;
    private Session session = null;

    private BufferedReader in = null;
    private PrintWriter out = null;
    private final ArrayList<IOSubscriber> ioSubscribers;
    protected long readTimeout = 0; // millisecs

    private static final String endmarker = "]]>]]>";
    private static final int end = endmarker.length() - 1;

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

        session = con.connection.openSession();
        session.startSubSystem("netconf");
        // initStreams

        final InputStream is = session.getStdout();
        final OutputStream os = session.getStdin();
        in = new BufferedReader(new InputStreamReader(is));
        out = new PrintWriter(os, false);
        ioSubscribers = new ArrayList<IOSubscriber>();
        // hello will be done by NetconfSession
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
        if (in.ready()) {
            return true;
        }
        final int conditionSet = session.waitForCondition(0xffffffff, 1);
        if ((conditionSet & ChannelCondition.TIMEOUT) == ChannelCondition.TIMEOUT) {
            // It's a timeout
            return false;
        }
        return true;
    }

    /**
     * given a live SSHSession, check if the server side has closed it's end of
     * the ssh socket
     */
    public boolean serverSideClosed() {
        int conditionSet = ChannelCondition.TIMEOUT & ChannelCondition.CLOSED
                & ChannelCondition.EOF;
        conditionSet = session.waitForCondition(conditionSet, 1);
        if ((conditionSet & ChannelCondition.TIMEOUT) == ChannelCondition.TIMEOUT) {
            // it's a timeout
            return false;
        }
        return true;
    }

    /**
     * If we have readTimeout set, and an outstanding operation was timed out -
     * the socket may still be alive. However since we timed out our read
     * operation and subsequently didn't process the xml data - there may be
     * parts of unprocessed xml data left on the socket. This function reads
     * and throws away all such unprocessed data. An alternative after timeout
     * is of course to close the socket and reconnect.
     * 
     * @return number of discarded characters
     */
    public int readUntilWouldBlock() {
        int ret = 0;
        while (true) {
            try {
                if (!(ready())) {
                    return ret;
                }
                in.read();
                ret++;
            } catch (final IOException e) {
                return ret;
            }
        }
    }

    /**
     * Reads in "one" reply from the SSH transport input stream. A
     * <em>]]&gt;]]&gt;</em> character sequence is used to separate multiple
     * replies as described in <a target="_top"
     * href="ftp://ftp.rfc-editor.org/in-notes/rfc4742.txt">RFC 4742</a>.
     */
    @Override
    public StringBuffer readOne() throws IOException, JNCException {
        final StringWriter wr = new StringWriter();
        int ch;
        while (true) {
            if ((readTimeout > 0) && !in.ready()) { // else we want to block
                final int conditionSet = session.waitForCondition(0xffffffff,
                        readTimeout);
                if ((conditionSet & ChannelCondition.TIMEOUT) == ChannelCondition.TIMEOUT) {
                    // it's a timeout - there is nothing to
                    // read, not even eof
                    throw new JNCException(JNCException.TIMEOUT_ERROR,
                            new Long(readTimeout));
                }
            }

            // If readTimeout /= 0 we're guaranteed to not block
            // If its == 0, we want to block

            ch = in.read();
            if (ch == -1) {
                trace("end of input (-1)");
                throw new IOException("Session closed");
            }

            for (int i=0; i < endmarker.length(); i++) {
                if (ch == endmarker.charAt(i)) {
                    if (i < end) {
                        ch = in.read();
                    } else {
                        for (final IOSubscriber sub : ioSubscribers) {
                            sub.inputFlush(endmarker.substring(0, end));
                        }
                        return wr.getBuffer();
                    }
                } else {
                    subInputChar(wr, endmarker.substring(0, i));
                    subInputChar(wr, ch);
                    break;
                }
            }
        }
    }

    private void subInputChar(StringWriter wr, int ch) {
        wr.write(ch);
        for (int i = 0; i < ioSubscribers.size(); i++) {
            final IOSubscriber sub = ioSubscribers.get(i);
            sub.inputChar(ch);
        }
    }

    private void subInputChar(StringWriter wr, String s) {
        for (int i = 0; i < s.length(); i++) {
            subInputChar(wr, s.charAt(i));
        }
    }

    /**
     * Prints an integer (as text) to the output stream.
     * 
     * @param iVal Text to send to the stream.
     */
    @Override
    public void print(int iVal) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrint(iVal);
        }
        out.print(iVal);
    }

    /**
     * Prints text to the output stream.
     * 
     * @param s Text to send to the stream.
     */
    @Override
    public void print(String s) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrint(s);
        }
        out.print(s);
    }

    /**
     * Prints an integer (as text) to the output stream. A newline char is
     * appended to end of the output stream.
     * 
     * @param iVal Text to send to the stream.
     */
    @Override
    public void println(int iVal) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrintln(iVal);
        }
        out.println(iVal);
    }

    /**
     * Print text to the output stream. A newline char is appended to end of
     * the output stream.
     * 
     * @param s Text to send to the stream.
     */
    @Override
    public void println(String s) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrintln(s);
        }
        out.println(s);
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
            if (s == x) {
                ioSubscribers.remove(i);
                return;
            }
        }
    }

    /**
     * Signals that the final chunk of data has be printed to the output
     * transport stream. This method furthermore flushes the transport output
     * stream buffer.
     * <p>
     * A <em>]]&gt;]]&gt;</em> character sequence is added, as described in <a
     * target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc4742.txt">RFC
     * 4742</a>, to signal that the last part of the reply has been sent.
     */
    @Override
    public void flush() {
        out.print(endmarker);
        out.flush();
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputFlush(endmarker);
        }
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
        session.close();
    }

    /* help functions */

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_TRANSPORT) {
            System.err.println("*SSHSession: " + s);
        }
    }
}

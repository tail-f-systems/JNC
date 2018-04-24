package com.tailf.jnc;


import java.io.*;
import java.util.ArrayList;

/**
 * A TLS NETCONF transport class. Can be used whenever {@link NetconfSession}
 * intends to use tls for its transport.
 * <p>
 * Example:
 *
 * <pre>
 * TLSConnection conn = new TLSConnection(&quot;127.0.0.1&quot;, 2022);
 * TLSSession sess = new TLSSession(conn);
 * sess.addSubscriber(new DefaultIOSubscriber());
 *
 * NetconfSession dev1 = new NetconfSession(sess);
 * </pre>
 */

public class TLSSession implements Transport {

    private TLSConnection connection = null;

    private BufferedReader in = null;
    private PrintWriter out = null;
    private final ArrayList<IOSubscriber> ioSubscribers;
    protected long readTimeout = 0; // millisecs

    private static final String endmarker = "]]>]]>";
    private static final int end = endmarker.length() - 1;

    public TLSSession(TLSConnection con) throws IOException, JNCException {
        this(con, 0);
    }

    public TLSSession(TLSConnection con, long readTimeout)
            throws IOException, JNCException {
        this.readTimeout = readTimeout;
        connection = con;

        final InputStream is = connection.getSocket().getInputStream();
        final OutputStream os = connection.getSocket().getOutputStream();
        in = new BufferedReader(new InputStreamReader(is));
        out = new PrintWriter(os, false);
        ioSubscribers = new ArrayList<IOSubscriber>();
        // hello will be done by NetconfSession
    }

    public TLSConnection getTLSConnection() {
        return connection;
    }

    public boolean ready() throws IOException {
        return in.ready();
    }

    /**
     * Reads in "one" reply from the SSH transport input stream. A
     * <em>]]&gt;]]&gt;</em> character sequence is used to separate multiple
     * replies as described in <a target="_top"
     * href="ftp://ftp.rfc-editor.org/in-notes/rfc4742.txt">RFC 4742</a>.
     */
    public StringBuffer readOne() throws IOException, JNCException {
        final StringWriter wr = new StringWriter();
        int ch;
        while (true) {

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
    public void print(long iVal) {
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
            if (s.equals(x)) {
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
    public void flush() {
        out.print(endmarker);
        out.flush();
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputFlush(endmarker);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (IOException e){

        }
    }

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_TRANSPORT) {
            System.err.println("*SSHSession: " + s);
        }
    }
}

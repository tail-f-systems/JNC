/*    -*- Java -*-
 *
 *  Copyright 2007 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.inm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * A TCP NETCONF transport class.
 * Can be used whenever {@link NetconfSession} intends to use TCP for
 * its transport.
 * <p>
 * <b>Note:</b> TCP is not a standardized way to connect to a NETCONF
 * device. It will only work when connecting to a ConfD agent and
 * should only be used for testing. Use {@link SSHSession} instead.
 * <p>
 * Example:
 * <pre>
 * TCPConnection conn = new TCPConnection("127.0.0.1", 2022);
 * conn.authenticate("ola","500","500", "", "/home/ola", "");
 * TCPSession sess = new TCPSession( conn );
 * sess.addSubscriber( new DefaultIOSubscriber() );
 *
 * NetconfSession dev1 = new NetconfSession( sess );
 * </pre>
 */
public class TCPSession implements Transport {

    private Socket socket;

    private BufferedReader in = null;
    private PrintWriter out = null;

    private ArrayList ioSubscribers;

    /**
     * Creates a new TCP session object.
     * This only works towards ConfD agent since
     * NETCONF/TCP not is a standard transport mechanism.
     * <p>
     * Initial connection string towards ConfD looks like:
     * <code>[Username;127.0.0.1;tcp;UID;GID;SUPLGIDS;DIR;GROUPS;]</code>
     *
     */
    public TCPSession(String host, int port, String username, String uid,
                   String gid, String suplgids, String dir,
                          String groups)
        throws IOException, UnknownHostException, INMException {
        socket = new Socket(host, port);
        // initStreams
        InputStream is = socket.getInputStream();
        OutputStream os= socket.getOutputStream();
        in = new BufferedReader(new InputStreamReader(is));
        out = new PrintWriter(os, false);
        String header = "["+username+";"+host+";tcp;"+uid+";"+gid+";"+
            suplgids+";"+dir+";"+groups+";]";
        trace("sending: "+ header);
        out.println(header);
        ioSubscribers = new ArrayList();
        // hello will be done by NetconfSession
    }


    /**
     * Creates a new TCP session object.
     * This only works towards ConfD agent since
     * NETCONF/TCP not is a standard transport mechanism.
     * <p>
     * Initial connection string towards ConfD looks like:
     * <code>[Username;127.0.0.1;tcp;UID;GID;SUPLGIDS;DIR;GROUPS;]</code>
     *
     */
    public TCPSession(TCPConnection conn)
        throws IOException, UnknownHostException, INMException {
        if (conn.hasSession == true)
            throw new IOException("multiple session not supported");
        conn.hasSession = true;
        socket = conn.socket;

        in = conn.in;
        out = conn.out;
        ioSubscribers= new ArrayList();
        // hello will be done by NetconfSession
    }


    /**
     * Tell whether this transport is ready to be read.
     */
    public boolean ready() throws IOException {
        return in.ready();
    }


    /**
     * Reads in "one" reply from the TCP transport input stream.
     * A <em>]]&gt;]]&gt;</em> character sequence is used to separate
     * multiple replies.
     */
    public StringBuffer readOne() throws IOException {
        StringWriter wr = new StringWriter();
        int ch;
        while (true) {
            ch = in.read();
            if (ch==-1) {
                trace("end of input (-1)");
                // return wr.getBuffer(); // end of input
                throw new IOException("Session closed");
            }
            if (ch == ']') {
                ch= in.read();
                if (ch==']') {
                    ch=  in.read();
                    if (ch== '>') {
                        ch=  in.read();
                        if (ch==']') {
                            ch=  in.read();
                            if (ch==']') {
                                ch=  in.read();
                                if (ch== '>') {
                                    // ']]>]]>' received
                                    // trace("]]>]]> received");
                                    for (int i=0;i<ioSubscribers.size(); i++) {
                                        IOSubscriber sub =
                                            (IOSubscriber)ioSubscribers.get(i);
                                        sub.inputFlush("]]>]]");
                                    }
                                    return wr.getBuffer();
                                } else subInputChar(wr,"]]>]]");
                            } else subInputChar(wr,"]]>]");
                        } else subInputChar(wr,"]]>");
                    } else subInputChar(wr,"]]");
                } else subInputChar(wr,"]");
            }
            subInputChar(wr, ch );
        }
    }


    private void subInputChar(StringWriter wr, int ch) {
        wr.write(ch);
        for (int i=0;i<ioSubscribers.size(); i++) {
            IOSubscriber sub = (IOSubscriber)ioSubscribers.get(i);
            sub.inputChar(ch);
        }
    }
    private void subInputChar(StringWriter wr, String s) {
        for (int i=0; i<s.length(); i++)
            subInputChar(wr, s.charAt(i));
    }


    /**
     * Prints an integer (as text) to the output stream.
     * @param iVal Text to send to the stream.
     */
    public void print(int iVal) {
        for (int i=0;i<ioSubscribers.size(); i++) {
            IOSubscriber sub = (IOSubscriber)ioSubscribers.get(i);
            sub.outputPrint(iVal);
        }
        out.print(iVal);
    }


    /**
     * Prints text to the output stream.
     * @param s Text to send to the stream.
     */
    public void print(String s) {
        for (int i=0;i<ioSubscribers.size(); i++) {
            IOSubscriber sub = (IOSubscriber)ioSubscribers.get(i);
            sub.outputPrint(s);
        }
        out.print(s);
    }


    /**
     * Prints an integer (as text) to the output stream.
     * A newline char is appended to end of the output stream.
     * @param iVal Text to send to the stream.
     */
    public void println(int iVal) {
        for (int i=0;i<ioSubscribers.size(); i++) {
            IOSubscriber sub = (IOSubscriber)ioSubscribers.get(i);
            sub.outputPrintln(iVal);
        }
        out.println(iVal);
    }


    /**
     * Print text to the output stream.
     * A newline char is appended to end of the output stream.
     * @param s Text to send to the stream.
     */
    public void println(String s) {
        for (int i=0;i<ioSubscribers.size(); i++) {
            IOSubscriber sub = (IOSubscriber)ioSubscribers.get(i);
            sub.outputPrintln(s);
        }
        out.println(s);
    }


    /**
     * Add an IO Subscriber for this transport.
     * This is useful for tracing the messages.
     * @param s An IOSUbscriber that will be called whenever
     * there is something received or sent on this transport.
     */
    public void addSubscriber(IOSubscriber s) {
        ioSubscribers.add(s);
    }

    /**
     * Removes an IO subscriber.
     * @param s The IO subscriber to remove.
     */
    public void delSubscriber(IOSubscriber s) {
        for (int i=0; i<ioSubscribers.size(); i++) {
            IOSubscriber x= (IOSubscriber)ioSubscribers.get(i);
            if (s == x) {
                ioSubscribers.remove(i);
                return;
            }
        }
    }



    /**
     * Signals that the final chunk of data has be printed to the output
     * transport stream. This method furthermore flushes the transport
     * output stream buffer.
     * <p>
     * A <em>]]&gt;]]&gt;</em> character sequence is added to signal
     * that the last part of the reply has been sent.
     */
    public void flush() {
        String endmarker = "]]>]]>";
        out.print(endmarker);
        out.flush();
        for (int i=0;i<ioSubscribers.size(); i++) {
            IOSubscriber sub = (IOSubscriber)ioSubscribers.get(i);
            sub.outputFlush(endmarker);
        }
    }


    /**
     * Closes the TCP session/connection.
     */
    public void close() {
        trace("closeSession");
        try {
            socket.close();
        } catch (Exception e) {
        }
    }


    /** ------------------------------------------------------------
     *  help functions
     */

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (Element.debugLevel>=Element.DEBUG_LEVEL_TRANSPORT)
            System.err.println("*TCPSession: "+s);
    }
}

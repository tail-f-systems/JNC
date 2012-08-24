/*    -*- Java -*-
 *
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.jnc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.PrintWriter;

/**
 * A TCP NETCONF connection class. This is a symmetrical class to the
 * {@link SSHConnection}.
 * <p>
 * <b>Note:</b> TCP is not a standardized way to connect to a NETCONF device. It
 * will only work when connecting to a ConfD agent and should only be used for
 * testing. Use {@link SSHConnection} and {@link SSHSession} instead.
 * <p>
 * Example:
 * 
 * <pre>
 * TCPConnection conn = new TCPConnection(&quot;127.0.0.1&quot;, 2022);
 * conn.authenticate(&quot;ola&quot;, &quot;500&quot;, &quot;500&quot;, &quot;&quot;, &quot;/home/ola&quot;, &quot;&quot;);
 * TCPSession sess = new TCPSession(conn);
 * sess.addSubscriber(new DefaultIOSubscriber());
 * NetconfSession dev1 = new NetconfSession(sess);
 * </pre>
 */
public class TCPConnection {

    private String host;
    private int port;

    // package private
    boolean hasSession = false;
    Socket socket;
    BufferedReader in = null;
    PrintWriter out = null;

    /**
     * Creates a new TCP connection object. Connects towards a ConfD NETCONF
     * agent and authenticates.
     * <p>
     * This only works towards ConfD agent since NETCONF/TCP not is a standard
     * transport mechanism.
     * <p>
     * It is provided to be symmetrical to the {@link SSHConnection} class, and
     * should only be used for debugging purposes.
     * <p>
     * Initial authentication string towards ConfD looks like:
     * <code>[Username;127.0.0.1;tcp;UID;GID;SUPLGIDS;DIR;GROUPS;]</code>
     */
    public TCPConnection(String host, int port, String username, String uid,
            String gid, String suplgids, String dir, String groups)
            throws IOException, UnknownHostException, JNCException {
        this(host, port);
        authenticate(username, uid, gid, suplgids, dir, groups);
    }

    /**
     * Creates a new TCP connection object. The connection need to be
     * authenticated before it can be used. See {@link
     * #authenticate(String,String,String,String,String,String) authenticate}
     */
    public TCPConnection(String host, int port) throws IOException,
            UnknownHostException, JNCException {
        trace("created");
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        // initStreams
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        in = new BufferedReader(new InputStreamReader(is));
        out = new PrintWriter(os, false);
    }

    /**
     * Authenticate towards the ConfD NETCONF Agent.
     * <p>
     * The Initial connection string towards ConfD to authenticate looks like:
     * <code>[Username;127.0.0.1;tcp;UID;GID;SUPLGIDS;DIR;GROUPS;]</code>
     * 
     * @param username
     *            The user name
     * @param uid
     * @param gid
     * @param suplgids
     * @param dir
     * @param groups
     */
    public void authenticate(String username, String uid, String gid,
            String suplgids, String dir, String groups) {
        if (host.equals("localhost"))
            host = "127.0.0.1";
        String header = "[" + username + ";" + host + ";tcp;" + uid + ";" + gid
                + ";" + suplgids + ";" + dir + ";" + groups + ";]";
        out.println(header);
    }

    /**
     * Closes the TCP connection.
     */
    public void close() throws IOException {
        trace("close()");
        socket.close();
    }

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_TRANSPORT)
            System.err.println("*TCPConnection: " + s);
    }
    
    /**
     * @return The external host address as a String.
     */
    public String getHost() {
        return host;
    }
    
    /**
     * @return The external host port number of this connection.
     */
    public int getPort() {
        return port;
    }
    
}
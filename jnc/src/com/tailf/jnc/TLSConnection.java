package com.tailf.jnc;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A TLS NETCONF connection class. Can be used whenever {@link NetconfSession}
 * intends to use TLS for its transport.
 * <p>
 * Example:
 *
 * <pre>
 * TLSConnection tls = new TLSConnection(&quot;127.0.0.1&quot;, 2023);
 * TLSSession tr = new TLSSession(tls);
 * NetconfSession dev1 = new NetconfSession(tr);
 * </pre>
 */

public class TLSConnection {

    Socket socket = null;

    /**
     * By default we connect to the IANA registered port for NETCONF which is
     * 830
     *
     * @param host Host or IP address to connect to
     */

    public TLSConnection(String host) throws IOException {
        this(host, 830, 0);
    }

    /**
     * This method establishes an TLS connection to a host.
     *
     * @param host Host name.
     * @param port Port number to connect to.
     */

    public TLSConnection(String host, int port) throws IOException {
        this(host, port, 0);
    }

    /**
     * This method establishes an TLS connection to a host.
     *
     * @param host Host name.
     * @param port Port number to connect to.
     * @param connectTimeout
     */

    public TLSConnection(String host, int port, int connectTimeout) throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        socket = sslSocketFactory.createSocket();
        socket.connect(new InetSocketAddress(host, port), connectTimeout);
    }

    public void close() throws IOException{
        socket.close();
    }

    public Socket getSocket() {
        return socket;
    }
}

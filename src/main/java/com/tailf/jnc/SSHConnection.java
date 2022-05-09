package com.tailf.jnc;

import java.io.File;
import java.io.IOException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.UserAuthException;

/**
 * A SSH NETCONF connection class. Can be used whenever {@link NetconfSession}
 * intends to use SSH for its transport.
 * <p>
 * Example:
 * 
 * <pre>
 * SSHConnection ssh = new SSHConnection(&quot;127.0.0.1&quot;, 2023);
 * ssh.authenticateWithPassword(&quot;ola&quot;, &quot;secret&quot;);
 * SSHSession tr = new SSHSession(ssh);
 * NetconfSession dev1 = new NetconfSession(tr);
 * </pre>
 */
public class SSHConnection {

    SSHClient client = null;

    /**
     * By default we connect to the IANA registered port for NETCONF which is
     * 830
     * 
     * @param host Host or IP address to connect to
     */
    public SSHConnection(String host) throws IOException, JNCException {
        this(host, 830, 0);
    }

    /**
     * This method establishes an SSH connection to a host, once the connection
     * is established it must be authenticated.
     * 
     * @param host Host name.
     * @param port Port number to connect to.
     */
    public SSHConnection(String host, int port) throws IOException,
            JNCException {
        this(host, port, 0);
    }

    /**
     * This method establishes an SSH connection to a host, once the connection
     * is established it must be authenticated.
     * 
     * @param host Host name.
     * @param port Port number to connect to.
     * @param connectTimeout
     */
    public SSHConnection(String host, int port, int connectTimeout)
            throws IOException, JNCException {

        client = new SSHClient();
        client.setTimeout(connectTimeout);
        // TODO: check the host conditionally
        client.addHostKeyVerifier(new net.schmizz.sshj.transport.verification.PromiscuousVerifier());
        client.connect(host, port);
    }

    /**
     * This method establishes an SSH connection to a host, once the connection
     * is established it must be authenticated.
     * 
     * @param host Host name.
     * @param port Port number to connect to.
     * @param connectTimeout Connection timeout timer. Connect the underlying
     *            TCP socket to the server with the given timeout value
     *            (non-negative, in milliseconds). Zero means no timeout.
     * @param kexTimeout Key exchange timeout timer. Timeout for complete
     *            connection establishment (non-negative, in milliseconds).
     *            Zero means no timeout. The timeout counts until the first
     *            key-exchange round has finished.
     * @throws IOException In case of a timeout (either connectTimeout or
     *             kexTimeout) a SocketTimeoutException is thrown.
     *             <p>
     *             An exception may also be thrown if the connection was
     *             already successfully connected (no matter if the connection
     *             broke in the mean time) and you invoke
     *             <code>connect()</code> again without having called
     *             {@link #close()} first.
     */
    public SSHConnection(String host, int port, int connectTimeout,
            int kexTimeout) throws IOException, JNCException {
        client = new SSHClient();
        client.setTimeout(connectTimeout);
        client.getTransport().setTimeoutMs(kexTimeout);
        client.connect(host, port);
    }

    /**
     * TODO: needed?
     */
    SSHClient getClient() {
        return client;
    }

    /**
     * Authenticate with regular username pass.
     * 
     * @param user User name.
     * @param password Password.
     * 
     **/
    public void authenticateWithPassword(String user, String password)
            throws IOException, JNCException {
        try {
            client.authPassword(user, password);
        } catch (UserAuthException e) {
            throw new JNCException(JNCException.AUTH_FAILED, e);
        }
    }

    // /**
    //  * Authenticate with the name of a file containing the private key See
    //  * ganymed docs for full explanation, use null for password if the key
    //  * doesn't have a passphrase.
    //  * 
    //  * @param user User name.
    //  * @param pemFile Fila name.
    //  * @param password Password.
    //  **/
    // public void authenticateWithPublicKeyFile(String user, File pemFile,
    //         String password) throws IOException, JNCException {
    //     if (!client.authPublicKey(user, pemFile, password)) {
    //         throw new JNCException(JNCException.AUTH_FAILED, this);
    //     }
    // }

    // /**
    //  * Authenticate with a private key. See ganymed docs for full explanation,
    //  * use null for password if the key doesn't have a passphrase.
    //  * 
    //  * @param user User name.
    //  * @param pemPrivateKey Private key.
    //  * @param pass Passphrase.
    //  **/
    // public void authenticateWithPublicKey(String user, char[] pemPrivateKey,
    //         String pass) throws IOException, JNCException {
    //     if (!connection.authenticateWithPublicKey(user, pemPrivateKey, pass)) {
    //         throw new JNCException(JNCException.AUTH_FAILED, this);
    //     }
    // }

    /**
     * Closes the SSH session/connection.
     */
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            System.out.println("Exception in close(): " + e);
        }
    }

}

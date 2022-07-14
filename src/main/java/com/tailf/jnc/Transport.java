package com.tailf.jnc;

import java.io.IOException;

/**
 * A NETCONF transport interface. This interface can be used to write custom
 * NETCONF transport mechanisms. The {@link NetconfSession} constructor takes a
 * transport mechanism to be responsible for the actual sending and receiving
 * of NETCONF protocol messages over the wire. {@link SSHSession} and
 * {@link TCPSession} implements the Transport interface.
 *
 * @see SSHSession
 * @see TCPSession
 *
 */
public interface Transport {

    /**
     * Tell whether this transport is ready to be read.
     */
    boolean ready() throws IOException;

    /**
     * Reads "one" reply from the transport input stream.
     */
    StringBuffer readOne() throws IOException, JNCException;

    /**
     * Prints an integer to the transport output stream.
     */
    void print(long i);

    /**
     * Prints a string to the transport output stream.
     */
    void print(String s);

    /**
     * Prints an integer to the transport output stream and an additional line
     * break.
     */
    void println(int i);

    /**
     * Prints a string to the transport output stream and an additional line
     * break.
     */
    void println(String s);

    /**
     * Signals that the final chunk of data has be printed to the output
     * transport stream.
     * <p>
     * This method furthermore flushes the transport output stream buffer.
     */
    void flush();

    /**
     * Closes the Transport session/connection.
     */
    void close();
}

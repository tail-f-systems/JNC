package com.tailf.jnc;

/**
 * This is a default IO subscriber that can be used for tracing, auditing, and
 * logging of messages sent and recived by the transport of the session.
 * <p>
 * This class is provided as an example of how to write your own IO subscriber,
 * and can be used as it is.
 * 
 * @see IOSubscriber
 */
public class DefaultIOSubscriber extends IOSubscriber {

    String devName;

    /**
     * Constructor.
     * 
     * @param devName The name of the device.
     */
    public DefaultIOSubscriber(String devName) {
        super(false); // rawmode = false
        this.devName = devName;
    }

    /**
     * Constructor.
     * 
     * @param devName The name of the device.
     * @param rawmode If true 'raw' text will appear instead of pretty
     *            formatted XML.
     */
    public DefaultIOSubscriber(String devName, boolean rawmode) {
        super(rawmode);
        this.devName = devName;
    }

    /**
     * Will get called as soon as we have input (data which is received).
     * 
     * @param s Text being received
     */
    @Override
    public void input(String s) {
        System.out.println("RECV " + devName);
        System.out.println(s);
    }

    /**
     * Will get called as soon as we have output (data which is being sent).
     * 
     * @param s Text being sent
     */
    @Override
    public void output(String s) {
        System.out.println("SEND " + devName);
        System.out.println(s);
    }
}

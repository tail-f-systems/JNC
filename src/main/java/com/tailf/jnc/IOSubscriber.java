package com.tailf.jnc;

/**
 * The IO subscriber is used for tracing, auditing, and logging of messages
 * which are sent and received over the SSHSession by a NETCONF session.
 * <p>
 * This is an abstract class. For an example of how to use this class see
 * {@link DefaultIOSubscriber}.
 * <p>
 * To register an IO subscriber:
 * 
 * <pre>
 * SSHConnection c = new SSHConnection(&quot;127.0.0.1&quot;, 2022);
 * c.authenticateWithPassword(&quot;adimin&quot;, &quot;pass&quot;);
 * SSHSession session = new SSHSession(c);
 * session.addSubscriber(new DefaultIOSubscriber(&quot;my_device&quot;));
 * NetconfSession nc = new NetconfSession(session);
 * </pre>
 * <p>
 * The above code will install the default IO subscriber which just prints
 * in/out data.
 */
public abstract class IOSubscriber {

    private boolean rawmode;
    private StringBuffer inb;
    private StringBuffer outb;

    /**
     * Constructor.
     * 
     * @param rawmode If true 'raw' text will appear instead of pretty
     *            formatted XML.
     */
    public IOSubscriber(boolean rawmode) {
        inb = new StringBuffer(1024);
        outb = new StringBuffer(1024);
        this.rawmode = rawmode;
    }

    /**
     * Empty constructor. The rawmode, inb and outb fields will be unassigned.
     */
    public IOSubscriber() {
    }

    /**
     * Will get called as soon as we have input (data which is received).
     * 
     * @param s Text being received
     */
    abstract public void input(String s);

    /**
     * Will get called as soon as we have output (data which is being sent).
     * 
     * @param s Text being sent
     */
    abstract public void output(String s);

    void inputChar(int i) {
        final char ch = (char) i;
        inb.append(ch);
        if (ch == '\n' && rawmode) {
            // call usercode
            input(inb.toString());
            inb.setLength(0);
        }
    }

    private void xmlFlush(StringBuffer buf, boolean isInput) {
        String res;
        try {
            final XMLParser p = new XMLParser();
            final Element e = p.parse(buf.toString());
            res = e.toXMLString();
            buf.setLength(0);
        } catch (final Exception e) {
            res = buf.toString();
            buf.setLength(0);
        }
        if (isInput) {
            input(res);
        } else {
            output(res);
        }
    }

    void inputFlush(String endMarker) {
        if (!rawmode) {
            xmlFlush(inb, true);
        } else {
            input(inb.toString() + endMarker + "\n");
            inb.setLength(0);
        }
    }

    void outputFlush(String endMarker) {
        if (!rawmode) {
            xmlFlush(outb, false);
        } else {
            output(outb.toString() + endMarker + "\n");
            outb.setLength(0);
        }
    }

    private void outputChar(char ch) {
        outb.append(ch);
        if (ch == '\n' && rawmode) {
            // call usercode
            output(outb.toString());
            outb.setLength(0);
        }
    }

    void outputPrint(long iVal) {
        final StringBuffer tmp = new StringBuffer(16);
        tmp.append(iVal);
        for (int i = 0; i < tmp.length(); i++) {
            outputChar(tmp.charAt(i));
        }
    }

    void outputPrint(String s) {
        final StringBuffer tmp = new StringBuffer(64);
        tmp.append(s);
        for (int i = 0; i < tmp.length(); i++) {
            outputChar(tmp.charAt(i));
        }
    }

    void outputPrintln(String s) {
        final StringBuffer tmp = new StringBuffer(64);
        tmp.append(s);
        tmp.append('\n');
        for (int i = 0; i < tmp.length(); i++) {
            outputChar(tmp.charAt(i));
        }
    }

    void outputPrintln(int iVal) {
        final StringBuffer tmp = new StringBuffer(16);
        tmp.append(iVal);
        tmp.append('\n');
        for (int i = 0; i < tmp.length(); i++) {
            outputChar(tmp.charAt(i));
        }
    }
}

package com.tailf.jnc;

import java.io.Serializable;

/**
 * This class represents an attribute for an XML element. An attribute belongs
 * to a namespace and has a name and a value. An attribute is typically
 * assigned to a {@link Element} using its setAttr method.
 * <p>
 * Example:
 * 
 * <pre>
 * Element sys = new Element(&quot;http://example.com/config/1.0&quot;, &quot;sys&quot;);
 * sys.setAttr(new Attribute(&quot;enabled&quot;, &quot;yes&quot;));
 * </pre>
 **/

public class Attribute implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String EQUALS_QUOTE = "=\"";
    public static final String QUOTE = "\"";

    /**
     * The Attribute name.
     */
    String name;

    /**
     * The Value of the attribute (if any).
     */
    String value;

    /**
     * Namespace uri
     */
    String ns;

    /**
     * Constructor without namespace or value
     */
    public Attribute(String name) {
        this.name = name;
        value = null; // means not set.
    }

    /**
     * Constructor without namespace
     */
    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     *
     */
    public Attribute(String ns, String name, String value) {
        this.ns = ns;
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the value of the attribute.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the attribute value.
     * 
     * @param value Set the value of the attribute
     */
    public void setValue(String value) {
        trace("setValue: " + name + EQUALS_QUOTE + value + QUOTE);
        this.value = value;
    }

    /**
     * Clones the attribute, returning an exact copy.
     */
    @Override
    public Object clone() {
        return new Attribute(ns, name, value);
    }

    /**
     * Returns a string representation of this Attribute object.
     */
    @Override
    public String toString() {
        return "Attribute{name=" + name + ",ns=" + ns + ",value=" + value + "}";
    }

    /**
     * Returns the XML representation of this XML attribute in the format:
     * prefix:attr="value" The contextnode is used for finding the prefixmap.
     */
    String toXMLString(Element contextnode) {
        // NOTE! Namespace is allowed to be "" for attributes
        if (ns != null && ns.length() > 0) {
            final String prefix = contextnode.nsToPrefix(ns);
            if (prefix == null) {
                return "unknown:" + name + EQUALS_QUOTE + value + QUOTE;
            }
            if (prefix.length() > 0) {
                return prefix + ":" + name + EQUALS_QUOTE + value + QUOTE;
            }
        }
        return name + EQUALS_QUOTE + value + QUOTE;
    }

    /**
     * Encodes the attribute, writing it to the provided out stream. Similar to
     * the toXMLString(), but without the pretty printing. This version of
     * encode allows foreign attributes.
     */
    void encode(Transport out) {
        encode(out, null);
    }

    /**
     * Encodes the attribute, writing it to the provided out stream. Similar to
     * the toXMLString(), but without the pretty printing.
     */
    void encode(Transport out, Element contextnode) {
        // NOTE: Namespace is allowed to be "" for attributes
        if (ns != null && ns.length() > 0) {
            String prefix;
            if (contextnode != null) {
                prefix = contextnode.nsToPrefix(ns);
            } else {
                // use default prefix map
                prefix = Element.defaultPrefixes.nsToPrefix(ns);
            }
            if (prefix == null) {
                if (contextnode != null) {
                    out.print("unknown:");
                }
                out.print(name + EQUALS_QUOTE + value + QUOTE);
                return;
            }
            if (prefix.length() > 0) {
                out.print(prefix + ":" + name + EQUALS_QUOTE + value + QUOTE);
                return;
            }
        }
        out.print(name + EQUALS_QUOTE + value + QUOTE);
    }

    /* help functions */

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_ATTRIBUTE) {
            System.err.println("*Attribute: " + s);
        }
    }
}

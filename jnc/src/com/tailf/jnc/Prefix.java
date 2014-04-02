package com.tailf.jnc;

import java.io.Serializable;

/**
 * This class represents a prefix mapping of a prefix string into a namespace.
 * 
 * For example:
 * <pre>
 *    xmlns:foo='http://foo.com'
 * </pre>
 * 
 * <p>
 * Provides a mapping from prefix "foo" into the namespace "http://foo.com".
 */
public class Prefix extends Attribute implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The namespace the "xmlns" attribute belongs to:
     * "http://www.w3.org/2000/xmlns/".
     */
    static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

    /**
     * qualified name if printed as an xmlns attribute 'xmlns:prefixname'
     */
    String qName;

    /**
     * Constructs a new Prefix object which represents a prefix mapping.
     * 
     * @param name The prefix name
     * @param nsValue the uri namespace that the prefix is mapped to
     */
    public Prefix(String name, String nsValue) {
        super(name, nsValue);
        ns = XMLNS_NAMESPACE;
        if (name.equals("")) {
            qName = "xmlns";
        } else {
            qName = "xmlns:" + name;
        }
    }

    /**
     * Returns a string representation of this prefix. as: xmlns:prefix="uri"
     * or xmlns="uri"
     */
    public String toXMLString() {
        return qName + "=\"" + value + "\"";
    }

    /**
     * Returns a string representation of this Attribute object.
     */
    @Override
    public String toString() {
        return "Prefix{\"" + name + "\", \"" + value + "\"}";
    }

    /**
     * Encodes to XML and send it to the provided stream. Similar to the
     * toXMLString(), but without the pretty printing.
     */
    @Override
    void encode(Transport out) {
        out.print(qName);
        out.print("=\"");
        out.print(value);
        out.print("\"");
    }

}

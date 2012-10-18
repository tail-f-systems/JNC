package com.tailf.jnc;

import java.util.ArrayList;

/**
 * A set of {@link Element} nodes.
 * 
 */
public class NodeSet extends ArrayList<Element> {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new NodeSet which contains the given element.
     */
    public NodeSet(Element node) {
        super();
        add(node);
    }

    /**
     * Construct a new empty NodeSet
     */
    public NodeSet() {
        super();
    }

    /**
     * Gets an element from the node set.
     */
    public Element getElement(int index) {
        return super.get(index);
    }

    /**
     * 'Get' using path expression on nodes within the set. returning new
     * NodeSet
     * 
     * @param pathStr Path string to find nodes within the nodes
     */
    public NodeSet get(String pathStr) throws JNCException {
        final NodeSet result = new NodeSet();
        for (Element e : this) {
            final NodeSet r = e.get(pathStr);
            if (r != null && r.size() > 0) {
                result.addAll(r);
            }
        }
        return result;
    }

    /**
     * 'Get' using path expression on nodes within the set. returning the first
     * Element that matches
     * 
     * @param pathStr Path string to find an element within the nodes
     */
    public Element getFirst(String pathStr) throws JNCException {
        for (int i = 0; i < size(); i++) {
            final Element e = getElement(i);
            final Element r = e.getFirst(pathStr);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    /**
     * @return first element from this node set, or null if none.
     */
    public Element first() {
        return isEmpty() ? null : getElement(0);
    }

    /**
     * @return last element from this node set, or null if none.
     */
    public Element last() {
        return isEmpty() ? null : getElement(size()-1);
    }

    /**
     * Checks if an element is a member of the NodeSet. Elements are compared
     * with the {@link Element#equals(Object) Element.equals} method.
     * 
     * @param x Element to check for
     */
    public boolean isMember(Element x) {
        return findMember(x) != null;
    }

    /**
     * Searches for an element in this NodeSet. Elements are compared
     * with the {@link Element#equals(Object) Element.equals} method.
     * 
     * @param x Element to find
     * @return the found member or null
     */
    public Element findMember(Element x) {
        for (final Element e : this) {
            if (e.equals(x)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Removes a member element from the NodeSet. Members are compared with
     * {@link Element#equals(Object) Element.equals} method.
     * 
     * @param x Removes an element equals to element x.
     * @return <code>true</code> if member was removed. <code>false</code> if not found.
     * 
     */
    public boolean removeMember(Element x) {
        for (int i = 0; i < size(); i++) {
            final Element e = getElement(i);
            if (e.equals(x)) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Formats the NodeSet in XML format.
     * 
     * @return The sub-tree represented as an XML string
     */
    public String toXMLString() {
        StringBuffer s = new StringBuffer();
        for (final Element elem : this) {
            s.append(elem.toXMLString());
        }
        return s.toString();
    }

    /**
     * Encode to XML and send the sequence of elements to the provided stream.
     * 
     * @param out NETCONF transport interface to use
     * @param c NETCONF Capabilities supported
     * @throws JNCException if the
     *             {@link Element#encode(Transport, boolean, Capabilities)
     *             Element.encode} implementation fails, for example due to
     *             missing capability.
     */
    void encode(Transport out, Capabilities c) throws JNCException {
        for (final Element elem : this) {
            elem.encode(out, true, c);
        }
    }

}

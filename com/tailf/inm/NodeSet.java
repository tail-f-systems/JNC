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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A set of {@link Element} nodes.
 *
 */
public class NodeSet extends ArrayList {

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
        return (Element) super.get(index);
    }


    /**
     * 'Get' using path expression on
     * nodes within the set. returning new NodeSet
      * @param pathStr Path string to find nodes within the nodes
     */
    public NodeSet get(String pathStr) throws INMException {
        NodeSet result = new NodeSet();
        for (int i=0;i<size();i++) {
            Element e= getElement(i);
            NodeSet r = e.get(pathStr);
            if (r!=null && r.size()>0)
                result.addAll(r);
        }
        return result;
    }


    /**
     * 'Get' using path expression on
     * nodes within the set. returning the first Element that matches
      * @param pathStr Path string to find an element within the nodes
     */
    public Element getFirst(String pathStr) throws INMException {
        for (int i=0;i<size();i++) {
            Element e= getElement(i);
            Element r = e.getFirst(pathStr);
            if (r!=null) return r;
        }
        return null;
    }



    /**
     * Gets first element from node set.
     */
    public Element first() {
        if (size() > 0)
            return getElement(0);
        return null;
    }


    /**
     * Checks if an element is a member of
     * the NodeSet.
     * Elements are compared with {@link com.tailf.inm.Element#equals(Element) Element.equals} method.
     * @param x Check if x is a member of the NodeSet.
     */
    public boolean isMember(Element x) {
       for (int i=0;i<size();i++) {
           Element e= getElement(i);
           if ( e.equals(x) ) return true;
       }
       return false;
    }


    /**
     * Checks if an element is a member of
     * the NodeSet.
     * Elements are compared with {@link com.tailf.inm.Element#equals(Element) Element.equals} method.
     * @param x Check if x is a member of the NodeSet
     * @return Returns the found member or null
     */
    public Element findMember(Element x) {
        for (int i=0;i<size();i++) {
            Element e= getElement(i);
            if ( e.equals(x) ) return e;
        }
        return null;
    }


    /**
     * Removes a member element from the NodeSet.
     * Members are compared with  {@link com.tailf.inm.Element#equals(Element) Element.equals} method.
     * @param x Removes an element equals to element x.
     * @return 'true' if member was removed. 'false' if not found.
     *
     */
    public boolean removeMember(Element x) {
        for (int i=0;i<size();i++) {
            Element e= getElement(i);
            if ( e.equals(x) ) {
                remove(i);
                return true;
            }
        }
        return false;
    }



    /**
     * This will format the NodeSet in XML format.
     * @return The sub-tree represented as an XML string
     */
    public String toXMLString() {
        String s= new String();
        for (int i=0;i<size();i++) {
            Element elem = getElement(i);
            s=s+ elem.toXMLString();
        }
        return s;
    }

    /**
     * Encode to XML and send the sequence of elements to the
     * provided stream.
     *
     */
    void encode(Transport out, Capabilities c) throws INMException {
        for (int i=0;i<size();i++) {
            Element elem = getElement(i);
            elem.encode( out, true, c );
        }
    }

}

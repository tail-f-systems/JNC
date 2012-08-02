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

package com.tailf.netconfmanager.yang.type;

import com.tailf.netconfmanager.Element;
import com.tailf.netconfmanager.Prefix;
import com.tailf.netconfmanager.yang.YangException;

/**
 * Implements the built-in YANG data type "leafref".
 * 
 * @author emil@tail-f.com
 */
public class Leafref extends Type<Element> {

    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a Leafref object from a String, formatted as described in
     * {@link Leafref#fromString(String)}.
     * 
     * @param s The string.
     * @throws YangException If s is improperly formatted.
     */
    public Leafref(String s) throws YangException {
        super(s);
    }
    
    /**
     * Creates a Leafref object from an Element.
     * 
     * @param value The initial value of the new Leafref object.
     * @throws YangException If an invariant was broken during initialization.
     */
    public Leafref(Element leaf) throws YangException {
        super(leaf);
    }
    
    /**
     * Creates a Leafref object from three strings: Leaf namespace, prefix and
     * argument/identifier.
     *
     * @param ns Leaf module namespace
     * @param prefix Leaf module prefix
     * @param id Leaf argument/identifier
     * @throws YangException If an invariant was broken during initialization.
     */
    public Leafref(String ns, String prefix, String id)
            throws YangException {
        super(ns + " " + prefix + " " + id);
    }

    /**
     * Returns a Leaf element from a String.
     * <p>
     * The string should contain space separated tokens: the Leaf namespace,
     * prefix and argument/identifier.
     * 
     * @param s The string.
     * @return  An Element representing the referenced Leaf, parsed from s.
     * @throws YangException If s is improperly formatted.
     */
    @Override
    protected Element fromString(String s) throws YangException {
        String[] ss = s.split(" ");
        if (ss.length == 3) {
            Element leaf = new Element(ss[0], ss[2]);
            leaf.setPrefix(new Prefix(ss[1], ss[0]));
            return leaf;
        } else {
            throw new YangException(YangException.BAD_VALUE, s);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.type.Type#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof Leafref || obj instanceof Element;
    }

}

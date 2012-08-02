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
 * Implements the built-in YANG data type "identityref".
 * 
 * @author emil@tail-f.com
 */
public class Identityref extends Type<Element> {

    private static final long serialVersionUID = 1L;
    
    /**
     * Creates an Identityref object from a String, formatted as described in
     * {@link Identityref#fromString(String)}.
     * 
     * @param s The string.
     * @throws YangException If s is improperly formatted.
     */
    public Identityref(String s) throws YangException {
        super(s);
    }
    
    /**
     * Creates a YangType object from a Statement.
     * 
     * @param value The initial value of the new YangType object.
     * @throws YangException If an invariant was broken during initialization.
     */
    public Identityref(Element identity) throws YangException {
        super(identity);
    }
    
    /**
     * Creates an Identityref object from three strings: identity
     * argument/identifier, the identity module namespace and its prefix.
     *
     * @param ns identity module namespace
     * @param prefix identity module prefix
     * @param id identity argument/identifier
     * @throws YangException If an invariant was broken during initialization.
     */
    public Identityref(String ns, String prefix, String id)
            throws YangException {
        super(ns + " " + prefix + " " + id);
    }

    /**
     * Returns an identity element from a String.
     * <p>
     * The string should contain space separated tokens: the identity
     * namespace, prefix and argument/identifier.
     * 
     * @param s The string.
     * @return  An Element representing the referenced identity, parsed from s.
     * @throws YangException If s is improperly formatted.
     */
    @Override
    protected Element fromString(String s) throws YangException {
        String[] ss = s.split(" ");
        if (ss.length == 3) {
            Element identity = new Element(ss[0], ss[2]);
            identity.setPrefix(new Prefix(ss[1], ss[0]));
            return identity;
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
        return obj instanceof Identityref || obj instanceof Element;
    }

}

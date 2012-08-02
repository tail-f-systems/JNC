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

import com.tailf.netconfmanager.yang.Statement;
import com.tailf.netconfmanager.yang.YangException;

/**
 * Implements the built-in YANG data type "leafref".
 * 
 * @author emil@tail-f.com
 */
public class Leafref extends Type<Statement> {

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
     * Creates a Leafref object from a Statement.
     * 
     * @param value The initial value of the new Leafref object.
     * @throws YangException If an invariant was broken during initialization.
     */
    public Leafref(Statement identity) throws YangException {
        super(identity);
    }
    
    /**
     * Creates a YangType object from three strings: identity
     * argument/identifier and the identity module namespace and prefix.
     *
     * @param id identity argument/identifier
     * @param ns identity module namespace
     * @param prefix identity module prefix
     * @throws YangException If an invariant was broken during initialization.
     */
    public Leafref(String id, String ns, String prefix)
            throws YangException {
        super(id + " " + ns + " " + prefix);
    }

    /**
     * Returns a top-level Identity Statement from a String.
     * <p>
     * The string should contain space separated tokens, ordered as follows:
     * module name, module namespace, module prefix, identity argument.
     * 
     * @param s The string.
     * @return A Statement representing the referenced leaf, parsed from s.
     * @throws YangException If s is improperly formatted.
     */
    @Override
    protected Statement fromString(String s) throws YangException {
        String[] ss = s.split(" ");
        if (ss.length == 4 || ss.length == 5) {
            Statement module = new Statement("module", ss[0]);
            module.addChild(new Statement("namespace", ss[1]));
            module.addChild(new Statement("prefix", ss[2]));
            Statement res = module.addChild(new Statement("Identity", ss[4]));
            if (ss.length == 5) {
                
            }
            return res;
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
        return obj instanceof Leafref || obj instanceof Statement;
    }

}

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

package com.tailf.confm.yang;

import com.tailf.confm.ConfMException;

/**
 * Implements the built-in YANG data type "identityref".
 * 
 * @author emil@tail-f.com
 */
public class Identityref extends Type<Statement> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -482307288484824804L;
    
    /**
     * Creates a YangType object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If an invariant was broken during initialization,
     *                        or if value could not be parsed from s.
     */
    public Identityref(String s) throws ConfMException {
        super(s);
    }
    
    /**
     * Creates a YangType object from a Statement.
     * 
     * @param value The initial value of the new YangType object.
     * @throws ConfMException If an invariant was broken during initialization.
     */
    public Identityref(Statement stmt) throws ConfMException {
        super(stmt);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.yang.Type#fromString(java.lang.String)
     */
    @Override
    protected Statement fromString(String s) throws ConfMException {
        String[] ss = s.split(" ");
        if (ss.length == 2) {
            return new Statement(ss[0], ss[1]);
        } else {
            throw new ConfMException(ConfMException.BAD_VALUE, s);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.yang.Type#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof Identityref || obj instanceof Statement;
    }

}

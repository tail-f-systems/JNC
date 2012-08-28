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

package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "empty".
 * 
 * @author emil@tail-f.com
 */
public class YangEmpty implements YangType<Void> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangEmpty object.
     */
    public YangEmpty() {
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public YangEmpty clone() {
        return new YangEmpty();
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangType#check()
     */
    @Override
    public void check() throws YangException {
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangEmpty; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangEmpty;
    }
  
    @Override
    public void setValue(String s) throws YangException {
        throw new YangException(YangException.BAD_VALUE, s);
    }

    @Override
    public void setValue(Void value) throws YangException {
        throw new YangException(YangException.BAD_VALUE, value);
    }

    @Override
    public Void getValue() {
        return null; // An empty leaf has no value
    }

}

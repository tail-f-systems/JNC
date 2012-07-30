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

package com.tailf.confm;

import java.io.Serializable;

/**
 * Implements the built-in YANG data type "boolean".
 * 
 * @author emil@tail-f.com
 */
public abstract class YangBoolean extends YangType<Boolean> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -3460008208874981651L;

    /**
     * Creates a YangBoolean object from a String.
     *
     * @param s The string.
     * @throws ConfMException If value is not one of "true" or "false".
     */
    public YangBoolean(String s) throws ConfMException {
        super(s);
    }

    /**
     * Sets the value of this object using a boolean.
     * 
     * @param b The boolean value to set the value of this object to.
     */
    public void setValue(boolean b) {
        value = b;
    }

    /**
     * @return value of this object.
     */
    @Override
    public Boolean getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangType#toString()
     */
    @Override
    public String toString() {
        return Boolean.valueOf(value).toString();
    }

    /**
     * Compares this object with a boolean for equality.
     * 
     * @param b The boolean object to compare with.
     * @return true if value of this object is equal to s; false otherwise.
     */
    public boolean equals(boolean b) {
        return value == b;
    }

    /**
     * Compares this object with an instance of Boolean for equality.
     * 
     * @param b The Boolean object to compare with.
     * @return true if the value of this object is equal to the value of b;
     *         false otherwise.
     */
    public boolean equals(Boolean b) {
        return equals(b.booleanValue());
    }

    /**
     * Compares this object with another object for equality.
     * 
     * @param obj The object to compare with.
     * @return true if obj can be cast to a Boolean or a YangBoolean
     *         and the value of this object is equal to the value of obj;
     *         false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof YangBoolean)
            return equals((YangBoolean) obj);
        else if (obj instanceof Boolean)
            return equals((Boolean) obj);
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangType#check()
     */
    @Override
    public void check() throws ConfMException {
    }

    @Override
    protected Boolean fromString(String s) throws ConfMException {
        s = YangString.wsCollapse(s);
        if (s.equals("true"))
            return true;
        else if (s.equals("false"))
            return false;
        else
            throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

}

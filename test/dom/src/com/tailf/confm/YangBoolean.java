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
public class YangBoolean implements Serializable {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -3460008208874981651L;

    /**
     * The value of this object, of which this class is a wrapper for
     * 
     * @serial
     */
    private boolean value;

    /**
     * Creates a YangBoolean object from a String.
     *
     * @param s The string
     * @throws ConfMException If value is not one of "true" or "false"
     */
    public YangBoolean(String s) throws ConfMException {
        setValue(s);
    }

    /**
     * Creates a YangBoolean object from a boolean.
     * 
     * @param b The boolean to set the value of the new YangBoolean to.
     */
    public YangBoolean(boolean b) {
        setValue(b);
    }

    /**
     * Sets the value of this object using a String.
     *
     * @param s The string
     * @throws ConfMException If value is not one of "true" or "false"
     */
    public void setValue(String s) throws ConfMException {
        s = YangString.wsCollapse(s);
        if (s.equals("true"))
            value = true;
        else if (s.equals("false"))
            value = false;
        else
            throw new ConfMException(ConfMException.BAD_VALUE, this);
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
     * @return value of this object
     */
    public boolean getValue() {
        return value;
    }

    /**
     * @return The value of this object, as a java.lang.String
     */
    @Override
    public String toString() {
        return Boolean.valueOf(value).toString();
    }

    /**
     * Compares this object with a boolean for equality
     * 
     * @param b The boolean object to compare with
     * @return true if value of this object is equal to s; false otherwise
     */
    public boolean equals(boolean b) {
        return value == b;
    }

    /**
     * Compares this object with an other instance of YangBoolean for equality
     * 
     * @param yb The YangBoolean object to compare with
     * @return true if the value of this object is equal to the value of yb;
     *         false otherwise
     */
    public boolean equals(YangBoolean yb) {
        return equals(yb.getValue());
    }

    /**
     * Compares this object with an instance of Boolean for equality
     * 
     * @param b The Boolean object to compare with
     * @return true if the value of this object is equal to the value of b;
     *         false otherwise
     */
    public boolean equals(Boolean b) {
        return equals(b.booleanValue());
    }

    /**
     * Compares this object with another object for equality
     * 
     * @param obj The object to compare with
     * @return true if obj can be cast to a Boolean or a YangBoolean
     *         and the value of this object is equal to the value of obj;
     *         false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof YangBoolean)
            return equals((YangBoolean) obj);
        else if (obj instanceof Boolean)
            return equals((Boolean) obj);
        return false;
    }

}

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

/**
 * Implements the built-in YANG data type "boolean".
 * 
 * @author emil@tail-f.com
 */
public class YangBoolean extends YangType<Boolean> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 6260020042145069756L;

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
     * Creates a YangBoolean object from a boolean.
     * 
     * @param b The boolean to set the value of the new YangBoolean to.
     * @throws ConfMException Never.
     */
    public YangBoolean(boolean b) throws ConfMException {
        super(b);
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

    /**
     * Nop method provided because this class extends the YangType class.
     */
    @Override
    public void check() throws ConfMException {
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
     * Compares this object with another object for equality.
     * 
     * @param obj The object to compare with.
     * @return true if obj can be cast to a Boolean or a YangBoolean
     *         and the value of this object is equal to the value of obj;
     *         false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Boolean)
            return equals(((Boolean) obj).booleanValue());
        assert !canEqual(obj): "obj: " + obj.getClass() + obj;
        return false;
    }

    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangBoolean || obj instanceof Boolean;
    }

}

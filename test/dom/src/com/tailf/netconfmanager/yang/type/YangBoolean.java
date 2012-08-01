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

import com.tailf.netconfmanager.yang.YangException;

/**
 * Implements the built-in YANG data type "boolean".
 * 
 * @author emil@tail-f.com
 */
public class YangBoolean extends Type<Boolean> {

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
     * @throws YangException If value is not one of "true" or "false".
     */
    public YangBoolean(String s) throws YangException {
        super(s);
    }

    /**
     * Creates a YangBoolean object from a boolean.
     * 
     * @param b The boolean to set the value of the new YangBoolean to.
     * @throws YangException Never.
     */
    public YangBoolean(boolean b) throws YangException {
        super(b);
    }

    /**
     * Works much like Boolean.parseBoolean, except that case matters, s is
     * trimmed with wsCollapse prior to parsing, and an exception is thrown if
     * the trimmed string is neither "true" nor "false".
     * 
     * @param s The String.
     * @return true if s matches " *true *", false if s matches " *false *".
     * @throws YangException if s does not match a valid boolean value
     */
    @Override
    protected Boolean fromString(String s) throws YangException {
        s = TypeUtil.wsCollapse(s);
        if (s.equals("true"))
            return true;
        else if (s.equals("false"))
            return false;
        else
            throw new YangException(YangException.BAD_VALUE, this);
    }

    /**
     * Nop method provided because this class extends the YangType class.
     */
    @Override
    public void check() throws YangException {
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangBoolean or java.lang.Boolean;
     *         false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangBoolean || obj instanceof Boolean;
    }

}

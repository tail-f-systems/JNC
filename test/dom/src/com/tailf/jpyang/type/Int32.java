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

package com.tailf.jpyang.type;

import com.tailf.jpyang.ConfMException;

/**
 * Implements the built-in YANG data type "int32".
 * 
 * @author emil@tail-f.com
 */
public class Int32 extends Int<Integer> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 6283234619874649380L;

    /**
     * Creates a YangInt32 object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If value could not be parsed from s.
     */
    public Int32(String s) throws ConfMException {
        super(s);
        setMinMax(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Creates a YangInt32 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt32 object.
     * @throws ConfMException If value does not fit in 8 bits.
     */
    public Int32(Number value) throws ConfMException {
        super(value.intValue());
        setMinMax(Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (!(value instanceof Integer)) {
            ConfMException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jpyang.YangInt#parse(java.lang.String)
     */
    @Override
    protected Integer parse(String s) {
        return Integer.parseInt(s);
    }

}
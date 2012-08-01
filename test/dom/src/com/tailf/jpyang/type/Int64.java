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
 * Implements the built-in YANG data type "int64".
 * 
 * @author emil@tail-f.com
 */
public class Int64 extends Int<Long> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -747862761252258914L;

    /**
     * Creates a YangInt64 object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If value could not be parsed from s.
     */
    public Int64(String s) throws ConfMException {
        super(s);
        setMinMax(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Creates a YangInt64 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt64 object.
     * @throws ConfMException If value does not fit in 8 bits.
     */
    public Int64(Number value) throws ConfMException {
        super(value.longValue());
        setMinMax(Long.MIN_VALUE, Long.MAX_VALUE);
        if (!(value instanceof Long)) {
            ConfMException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jpyang.YangInt#parse(java.lang.String)
     */
    @Override
    protected Long parse(String s) {
        return Long.parseLong(s);
    }

}
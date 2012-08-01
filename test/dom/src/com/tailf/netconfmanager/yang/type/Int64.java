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
 * Implements the built-in YANG data type "int64".
 * 
 * @author emil@tail-f.com
 */
public class Int64 extends Int<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangInt64 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s.
     */
    public Int64(String s) throws YangException {
        super(s);
        setMinMax(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Creates a YangInt64 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt64 object.
     * @throws YangException If value does not fit in 8 bits.
     */
    public Int64(Number value) throws YangException {
        super(value.longValue());
        setMinMax(Long.MIN_VALUE, Long.MAX_VALUE);
        if (!(value instanceof Long)) {
            YangException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected Long parse(String s) {
        return Long.parseLong(s);
    }

}
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
 * Implements the built-in YANG data type "int16".
 * 
 * @author emil@tail-f.com
 */
public class Int16 extends Int<Short> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangInt16 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s.
     */
    public Int16(String s) throws YangException {
        super(s);
        setMinMax(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    /**
     * Creates a YangInt16 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt16 object.
     * @throws YangException If value does not fit in 16 bits.
     */
    public Int16(Number value) throws YangException {
        super(value.shortValue());
        setMinMax(Short.MIN_VALUE, Short.MAX_VALUE);
        if (!(value instanceof Short)) {
            YangException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected Short parse(String s) {
        return Short.parseShort(s);
    }

}
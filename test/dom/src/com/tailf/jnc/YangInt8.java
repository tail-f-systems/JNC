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
 * Implements the built-in YANG data type "int8".
 * 
 * @author emil@tail-f.com
 */
public class YangInt8 extends YangInt<Byte> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangInt8 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s.
     */
    public YangInt8(String s) throws YangException {
        super(s);
        setMinMax(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    /**
     * Creates a YangInt8 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt8 object.
     * @throws YangException If value does not fit in 8 bits.
     */
    public YangInt8(Number value) throws YangException {
        super(value.byteValue());
        setMinMax(Byte.MIN_VALUE, Byte.MAX_VALUE);
        if (!(value instanceof Byte)) {
            YangException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected Byte decode(String s) throws NumberFormatException {
        return Byte.decode(s);
    }

}
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
 * Implements the built-in YANG data type "int16".
 * 
 * @author emil@tail-f.com
 */
public class YangInt16 extends YangInt<Short> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -4230578488815093538L;

    /**
     * Creates a YangInt16 object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If value could not be parsed from s.
     */
    public YangInt16(String s) throws ConfMException {
        super(s);
        setMinMax(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    /**
     * Creates a YangInt16 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt16 object.
     * @throws ConfMException If value does not fit in 16 bits.
     */
    public YangInt16(Number value) throws ConfMException {
        super(value.shortValue());
        setMinMax(Short.MIN_VALUE, Short.MAX_VALUE);
        if (!(value instanceof Short)) {
            ConfMException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangInt#parse(java.lang.String)
     */
    @Override
    protected Short parse(String s) {
        return Short.parseShort(s);
    }

}
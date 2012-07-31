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
 * Implements the built-in YANG data type "int8".
 * 
 * @author emil@tail-f.com
 */
public class YangInt8 extends YangInt<Byte> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 5647834608031218035L;

    /**
     * Creates a YangInt8 object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If value could not be parsed from s.
     */
    public YangInt8(String s) throws ConfMException {
        super(s);
        setMinMax(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    /**
     * Creates a YangInt8 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt8 object.
     * @throws ConfMException If value does not fit in 8 bits.
     */
    public YangInt8(Number value) throws ConfMException {
        super(value.byteValue());
        setMinMax(Byte.MIN_VALUE, Byte.MAX_VALUE);
        if (!(value instanceof Byte)) {
            ConfMException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangInt#parse(java.lang.String)
     */
    @Override
    protected Byte parse(String s) {
        return Byte.parseByte(s);
    }

}
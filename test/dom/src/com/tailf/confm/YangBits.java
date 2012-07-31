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

import java.math.BigInteger;

/**
 * Implements the built-in YANG data type "bits".
 *
 * @author emil@tail-f.com
 */
public abstract class YangBits extends YangInt<BigInteger> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -1772892028531610059L;


    /**
     * Constructor using string arguments.
     * 
     * @param value The value to initialize the object with, as a string.
     * @param mask The bit mask as a string.
     * @throws ConfMException If value does not match mask.
     * @throws NumberFormatException If value or mask are not valid as numbers.
     */
    public YangBits(String value, String mask)
            throws ConfMException {
        this(new BigInteger(value), new BigInteger(mask));
    }

    /**
     * Constructor using BigInteger arguments.
     * 
     * @param value The value to initialize the object with.
     * @param mask The bit mask to initialize the object with.
     * @throws ConfMException If value does not match mask.
     */
    public YangBits(BigInteger value, BigInteger mask)
            throws ConfMException {
        super(value);
        check(mask);
    }

    /**
     * Value setter using string arguments.
     * 
     * @param value The value to set this object's value to, as a string.
     * @param mask The bit mask to use, as a string.
     * @throws ConfMException If value does not match mask.
     * @throws NumberFormatException If value or mask are not valid as numbers.
     */
    public void setValue(String value, String mask)
            throws ConfMException {
        setValue(new BigInteger(value), new BigInteger(mask));
    }

    /**
     * Value setter using BigInteger arguments.
     * 
     * @param value The value to set this object's value to.
     * @param mask The bit mask to use.
     * @throws ConfMException If value does not match mask.
     */
    public void setValue(BigInteger value, BigInteger mask)
            throws ConfMException {
        super.setValue(value);
        check(mask);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangInt#parse(java.lang.String)
     */
    @Override
    protected BigInteger parse(String s) {
        return new BigInteger(s);
    }

    /**
     * Checks that the value space matches mask.
     * @param mask Bit mask.
     * @throws ConfMException if value space does not match mask.
     */
    public void check(BigInteger mask) throws ConfMException {
        boolean fail = mask.or(value).compareTo(mask) == 0;
        ConfMException.throwException(fail, this);
        check();
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangType#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return (obj instanceof YangBits
                || obj instanceof BigInteger
                || obj instanceof String);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangInt#valid(java.lang.Number)
     */
    @Override
    protected boolean valid(Number n) {
        return n instanceof BigInteger;
    }

    /**
     * Performs arithmetic or assignment: this.value &= v.value.
     * 
     * @param v Bits instance to fetch value from.
     */
    public void AND(YangBits v) {
        this.value = this.value.and(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value |= v.value.
     * 
     * @param v Bits instance to fetch value from.
     */
    public void OR(YangBits v) {
        this.value = this.value.or(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value ^= v.value.
     * 
     * @param v Bits instance to fetch value from.
     */
    public void XOR(YangBits v) {
        this.value = this.value.xor(v.getValue());
    }

}

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

import java.math.BigInteger;

import com.tailf.netconfmanager.yang.YangException;

/**
 * Implements the built-in YANG data type "uint64".
 * 
 * @author emil@tail-f.com
 */
public class UInt64 extends Int<BigInteger> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 3163973134001880140L;

    /**
     * Creates a YangUInt64 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s or if it is
     *                        negative or larger than 18446744073709551615.
     */
    public UInt64(String s) throws YangException {
        super(s);
        setMinMax(0, new BigInteger("18446744073709551615"));
        check();
    }

    /**
     * Creates a YangUInt64 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param n The initial value of the new YangUInt64 object.
     * @throws YangException If value is negative or if it is larger than
     *                        18446744073709551615.
     */
    public UInt64(Number n) throws YangException {
        super(TypeUtil.bigDecimalValueOf(n).toBigInteger());
        setMinMax(0, new BigInteger("18446744073709551615"));
        check();
    }

    /**
     * Sets the value of this object using a Number.
     * 
     * @param n The new value to set.
     * @throws YangException If an invariant was broken during assignment or
     *                        if the number has a non-zero fractional part.
     */
    public void setValue(Number n) throws YangException {
        try {
            super.setValue(TypeUtil.bigDecimalValueOf(n).toBigIntegerExact());
        } catch (ArithmeticException e) {
            YangException.throwException(true, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected BigInteger parse(String s) throws NumberFormatException {
        return new BigInteger(s);
    }

    /** ---------- Restrictions ---------- */

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.type.Int#exact(int)
     */
    @Override
    protected void exact(int other) throws YangException {
        BigInteger b = BigInteger.valueOf(other);
        YangException.throwException(!value.equals(b), this);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.type.Int#min(int)
     */
    @Override
    protected void min(int min) throws YangException {
        BigInteger b = BigInteger.valueOf(min);
        YangException.throwException(value.compareTo(b) < 0, this);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.type.Int#max(int)
     */
    @Override
    protected void max(int max) throws YangException {
        BigInteger b = BigInteger.valueOf(max);
        YangException.throwException(value.compareTo(b) > 0, this);
    }

}
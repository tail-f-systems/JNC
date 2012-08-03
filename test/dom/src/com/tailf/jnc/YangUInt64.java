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

import java.math.BigInteger;


/**
 * Implements the built-in YANG data type "uint64".
 * 
 * @author emil@tail-f.com
 */
public class YangUInt64 extends YangInt<BigInteger> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangUInt64 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s or if it is
     *                        negative or larger than 18446744073709551615.
     */
    public YangUInt64(String s) throws YangException {
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
    public YangUInt64(Number n) throws YangException {
        super(Utils.bigDecimalValueOf(n).toBigInteger());
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
            super.setValue(Utils.bigDecimalValueOf(n).toBigIntegerExact());
        } catch (ArithmeticException e) {
            YangException.throwException(true, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected BigInteger parse(String s) throws NumberFormatException {
        return new BigInteger(s);
    }

    /** ---------- Restrictions ---------- */

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangInt#exact(int)
     */
    @Override
    protected void exact(int other) throws YangException {
        BigInteger b = BigInteger.valueOf(other);
        YangException.throwException(!value.equals(b), this);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangInt#min(int)
     */
    @Override
    protected void min(int min) throws YangException {
        BigInteger b = BigInteger.valueOf(min);
        YangException.throwException(value.compareTo(b) < 0, this);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangInt#max(int)
     */
    @Override
    protected void max(int max) throws YangException {
        BigInteger b = BigInteger.valueOf(max);
        YangException.throwException(value.compareTo(b) > 0, this);
    }

}
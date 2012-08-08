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
 * Implements the built-in YANG data type "uint32".
 * 
 * @author emil@tail-f.com
 */
public class YangUInt32 extends YangInt64 {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangUInt32 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s or if it is
     *                        negative or larger than 0xffffffffL.
     */
    public YangUInt32(String s) throws YangException {
        super(s);
        setMinMax(0L, 0xffffffffL);
        check();
    }

    /**
     * Creates a YangUInt32 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param n The initial value of the new YangUInt32 object.
     * @throws YangException If value is negative or larger than 0xffffffffL.
     */
    public YangUInt32(Number n) throws YangException {
        super(n);
        setMinMax(0L, 0xffffffffL);
        check();
    }

    /** ---------- Restrictions ---------- */

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangInt#min(int)
     */
    @Override
    protected void min(int min) throws YangException {
        YangException.throwException(!valid(min), min);
        Utils.restrict(value & 0xffffffffL, min & 0xffffffffL,
                Utils.Operator.GE);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangInt#max(int)
     */
    @Override
    protected void max(int max) throws YangException {
        YangException.throwException(!valid(max), max);
        Utils.restrict(value & 0xffffffffL, max & 0xffffffffL,
                Utils.Operator.LE);
    }

}
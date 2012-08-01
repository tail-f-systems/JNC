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
 * Implements the built-in YANG data type "uint16".
 * 
 * @author emil@tail-f.com
 */
public class UInt16 extends Int32 {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangUInt16 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s or if the
     *                        parsed value is negative or larger than 0xffff.
     */
    public UInt16(String s) throws YangException {
        super(s);
        setMinMax(0, 0xffff);
        check();
    }

    /**
     * Creates a YangUInt16 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangUInt16 object.
     * @throws YangException If value is negative or larger than 0xffff.
     */
    public UInt16(Number value) throws YangException {
        super(value);
        setMinMax(0, 0xffff);
        check();
    }

    /** ---------- Restrictions ---------- */

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.type.Int#min(int)
     */
    @Override
    protected void min(int min) throws YangException {
        TypeUtil.restrict(value & 0xffffffffL, min & 0xffffffffL,
                TypeUtil.Operator.GR);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.type.Int#max(int)
     */
    @Override
    protected void max(int max) throws YangException {
        TypeUtil.restrict(value & 0xffffffffL, max & 0xffffffffL,
                TypeUtil.Operator.LT);
    }

}
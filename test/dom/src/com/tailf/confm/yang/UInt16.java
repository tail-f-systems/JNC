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

package com.tailf.confm.yang;

import com.tailf.confm.ConfMException;

/**
 * Implements the built-in YANG data type "uint16".
 * 
 * @author emil@tail-f.com
 */
public class UInt16 extends Int32 {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 1941352103046738554L;

    /**
     * Creates a YangUInt16 object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If value could not be parsed from s or if the
     *                        parsed value is negative or larger than 0xffff.
     */
    public UInt16(String s) throws ConfMException {
        super(s);
        setMinMax(0, 0xffff);
        check();
    }

    /**
     * Creates a YangUInt16 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangUInt16 object.
     * @throws ConfMException If value is negative or larger than 0xffff.
     */
    public UInt16(Number value) throws ConfMException {
        super(value);
        setMinMax(0, 0xffff);
        check();
    }

}
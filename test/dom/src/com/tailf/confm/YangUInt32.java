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
 * Implements the built-in YANG data type "uint32".
 * 
 * @author emil@tail-f.com
 */
public class YangUInt32 extends YangInt64 {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 5884506721551456162L;

    /**
     * Creates a YangUInt32 object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If value could not be parsed from s or if it is
     *                        negative or larger than 0xffffffffL.
     */
    public YangUInt32(String s) throws ConfMException {
        super(s);
        setMinMax(0L, 0xffffffffL);
        check();
    }

    /**
     * Creates a YangUInt32 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param n The initial value of the new YangUInt32 object.
     * @throws ConfMException If value is negative or larger than 0xffffffffL.
     */
    public YangUInt32(Number n) throws ConfMException {
        super(n);
        setMinMax(0L, 0xffffffffL);
        check();
    }

}
/*    -*- Java -*-
 *
 *  Copyright 2010 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.yang;
import com.tailf.confm.*;
import java.io.Serializable;

/**
 * This class implements the builtin "uint8" YANG datatype.
 */
public class Uint8 extends ShortInteger implements Serializable {
    /**
     * Constructor
     */
    public Uint8(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    private void check() throws ConfMException {
        minInclusive((short)0);
        maxInclusive((short)255);
    }

    public Uint8(short value) throws ConfMException {
        super(value);
        check();
    }

    /**
     * Set a value
     */
    public void setValue(java.lang.String value) throws ConfMException {
        super.setValue(value);
        check();
    }

    public void setValue(short value) throws ConfMException {
        super.setValue(value);
        check();
    }
}

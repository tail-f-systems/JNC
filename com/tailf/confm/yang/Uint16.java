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
 * This class implements the builtin "uint16" YANG datatype.
 */
public class Uint16 extends PlainInteger implements Serializable {
    /**
     * Constructor
     */
    public Uint16(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    private void check() throws ConfMException {
        minInclusive(0);
        maxInclusive(65535);
    }

    public Uint16(int value) throws ConfMException {
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

    public void setValue(int value) throws ConfMException {
        super.setValue(value);
        check();
    }
}

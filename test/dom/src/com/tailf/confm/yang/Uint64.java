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
 * This class implements the builtin "uint64" YANG datatype.
 */
public class Uint64 extends LongInteger implements Serializable {
    /**
     * Constructor
     */
    public Uint64(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    private void check() throws ConfMException {
        minInclusive(0);
        // Does not fit in a native usigned Java long
        // maxInclusive(18446744073709551615);
    }

    public Uint64(long value) throws ConfMException {
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

    public void setValue(long value) throws ConfMException {
        super.setValue(value);
        check();
    }
}

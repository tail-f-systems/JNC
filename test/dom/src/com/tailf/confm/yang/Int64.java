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
 * This class implements the builtin "int64" YANG datatype.
 */
public class Int64 extends LongInteger implements Serializable {
    /**
     * Constructor
     */
    public Int64(java.lang.String value) throws ConfMException {
        super(value);
    }

    public Int64(long value) throws ConfMException {
        super(value);
    }
}

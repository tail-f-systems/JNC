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
 * This class implements the builtin "int16" YANG datatype.
 */
public class Int16 extends ShortInteger implements Serializable {
    /**
     * Constructor
     */
    public Int16(java.lang.String value) throws ConfMException {
        super(value);
    }

    public Int16(short value) throws ConfMException {
        super(value);
    }
}

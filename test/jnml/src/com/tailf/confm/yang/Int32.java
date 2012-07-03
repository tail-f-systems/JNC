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
 * This class implements the builtin "int32" YANG datatype.
 */
public class Int32 extends PlainInteger implements Serializable {
    /**
     * Constructor
     */
    public Int32(java.lang.String value) throws ConfMException {
        super(value);
    }
    
    public Int32(int value) throws ConfMException {	
        super(value);
    }
}

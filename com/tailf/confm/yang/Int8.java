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
 * This class implements the builtin "int8" YANG datatype.
 */
public class Int8 extends PlainInteger implements Serializable {
    /**
     * Constructor
     */
    public Int8(java.lang.String value) throws ConfMException {
        super(value);
    }
    
    public Int8(byte value) throws ConfMException {	
        super(value);
    }
}

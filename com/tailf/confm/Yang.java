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

package com.tailf.confm;
import com.tailf.inm.*;
import java.io.Serializable;

/**
 * This class implements the YANG native data types in the
 * 'urn:ietf:params:xml:ns:yang:yang-types' namespace.
 * <p>
 * The type classes are used by the ConfM generated classes.
 * <p>
 * This class is an encapsulation of all the classes in the
 * 'urn:ietf:params:xml:ns:yang:yang-types' namspace which have been
 * placed in the com.tailf.confm.inet package.  
 */
public class Yang {
    /**
     * This class implements the "uint64" datatype
     */
    static public class Uint64 extends com.tailf.confm.yang.Uint64
      implements Serializable {
        public Uint64(java.lang.String value) 
            throws ConfMException {	
            super(value);
        }
        
        public Uint64(long value) throws ConfMException {	
            super(value);
        }
    }
}

/*    -*- Java -*- 
 * 
 *  Copyright 2007 Tail-F Systems AB. All rights reserved. 
 *
 *  This software is the confidential and proprietary 
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.inet;
import com.tailf.confm.*;
import java.io.Serializable;

/**
 * This class implements the "ipv4-prefix" datatype from the
 * 'urn:ietf:params:xml:ns:yang:inet-types' namespace.
 * <p>
 * A Ipv4Prefix is a synonym for "xs:unsignedLong".
 * Represents an unsigned 64-bit integer.
 */
public class Ipv4Prefix extends com.tailf.confm.xs.UnsignedLong implements Serializable {
    
    public Ipv4Prefix(java.lang.String value) throws ConfMException {	
	super(value);
        check();
    }

    public Ipv4Prefix(long value) throws ConfMException {	
        super(value);
	check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
	super.setValue(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(long value) throws ConfMException {
	super.setValue(value);
        check();
    }

    
    private void check() throws ConfMException {
    }


}

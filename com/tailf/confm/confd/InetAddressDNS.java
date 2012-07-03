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

package com.tailf.confm.confd;
import com.tailf.confm.*;
import java.io.Serializable;

/**
 * This class implements the "inetAddressDNS" datatype from
 * the 'http://tail-f.com/ns/confd/1.0' namespace.
 *
 */
public class InetAddressDNS implements Serializable {
    private String value;    
    
    public InetAddressDNS(String value) throws ConfMException {	
	value = com.tailf.confm.xs.String.wsCollapse(value);
	this.value = value;
	check();
    }

    /**
     * Sets the value.
     */
    public void setValue(String value) throws ConfMException {
	value = com.tailf.confm.xs.String.wsCollapse(value);
	this.value = value;
        check();
    }

    /**
     * Return the value.
     */  
    public String getValue() {
	return value;
    }

    private void check() throws ConfMException {
    }

    public String toString() {
	return value;
    }

    public boolean equals(Object b) {
        if (b instanceof InetAddressDNS)
            return ((InetAddressDNS)b).equals(this.value);
        return false;
    }

    public boolean equals(InetAddressDNS b) {
        return value.equals( b.value );
    }

    public boolean equals(String b) {
        return value.equals( b );
    }

}

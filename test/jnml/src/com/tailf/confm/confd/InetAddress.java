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
 * This class implements the "inetAddress" datatype from
 * the 'http://tail-f.com/ns/confd/1.0' namespace.
 * <p>
 * A value that represents a valid generic Internet address. 
 * Values which adhere to this type must be one of these 
 * types:
 * <ul>
 * <li>inetAddressIPv4
 * <li>inetAddressIPv6
 * <li>inetAddressDNS
 * </ul>
 */
public class InetAddress implements Serializable {

    private Object value;

    /**
     * Constructor
     */    
    public InetAddress(String v) throws ConfMException {	
	try {value = new InetAddressIPv4(v);
	} catch (Exception e1) {
	    try {value = new InetAddressIPv6(v);
	    } catch (Exception e2) {
		value = new InetAddressDNS(v);
	    }
	}
	check();
    }

    public InetAddress(InetAddressIPv4 v)  throws ConfMException {
	value = v;
	check();
    }
    public InetAddress(InetAddressIPv6 v)  throws ConfMException {
	value = v;
	check();
    }
    public InetAddress(InetAddressDNS v)  throws ConfMException {
	value = v;
	check();
    }
    
    
    /**
     * Sets the value.
     */
    public void setValue(String v) throws ConfMException {
	try {value = new InetAddressIPv4(v);
	} catch (Exception e1) {
	    try {value = new InetAddressIPv6(v);
	    } catch (Exception e2) {
		value = new InetAddressDNS(v);
	    }
	}	
	check();
    }

    public void setValue(InetAddressIPv4 v) throws ConfMException {
	value = v;
	check();
    }

    public void setValue(InetAddressIPv6 v) throws ConfMException {
	value = v;
	check();
    }


    public void setValue(InetAddressDNS v) throws ConfMException {
	value = v;
	check();
    }

    public void setValue(Object v) throws ConfMException {
	if (v instanceof InetAddressIPv4) value = v;
	else if (v instanceof InetAddressIPv6) value = v;
	else if (v instanceof InetAddressDNS) value = v;
	else throwException( true, v );
	check();
    }


    /**
     * Return the value space.
     * One of:
     * <ul>
     * <li>inetAddressIPv4
     * <li>inetAddressIPv6
     * <li>inetAddressDNS
     * </ul>     
     */
    public Object getValue() {
	return value;
    }

    public void check() throws ConfMException {
    }
    
    public String toString() {
	return value.toString();
    }

    public boolean equals(Object b) {
        if (b instanceof InetAddress)
            return ((InetAddress)b).equals(this);
        return false;
    }

    public boolean equals(InetAddress b) {
        return value.equals( b.value );
    }


    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object o) throws ConfMException {
	if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,o);
    }
    
}

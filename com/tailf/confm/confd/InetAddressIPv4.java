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
import java.net.InetAddress;

/**
 * This class implements the "inetAddressIPV4" datatype from
 * the 'http://tail-f.com/ns/confd/1.0' namespace.
 *
 */
public class InetAddressIPv4 implements Serializable {
    
    private java.net.InetAddress value;    
    
    public InetAddressIPv4(String v) throws ConfMException {	
	value = parseValue(v);
	check();
    }
    
    public InetAddressIPv4(java.net.InetAddress v) throws ConfMException {	
	value = v;
	check();
    }
    
    /**
     * Sets the value.
     */
    public void setValue(String v) throws ConfMException {
	value = parseValue(v);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.net.InetAddress v) throws ConfMException {
	value = v;
        check();
    }

    /**
     * Return the value space.
     */
    public java.net.InetAddress getValue() {
	return value;
    }

    private void check() throws ConfMException {
    }

    /** parse IP address string into
     * an array of bytes, and create 
     * the InetAddressIPv4 value.
     */        
    private java.net.InetAddress parseValue(String s) 
	throws ConfMException {
	s = com.tailf.confm.xs.String.wsCollapse(s);
	// System.out.println("parsing: "+s);
	byte[] b= s.getBytes();
	byte[] v= new byte[4];
	throwException( b.length ==0, s ); 
	
	int i =0;
	// 1
	throwException(b[i]<'0' | b[i]>'9' ,s);
	int tmp = b[i++]-'0';
	while(i<b.length && b[i]>='0' && b[i]<='9') 
	    tmp = tmp* 10  + b[i++]-'0';
	throwException( tmp > 255, s );
	v[0] = (byte) tmp;
	throwException(b[i++]!='.',s);
	// 2
	throwException(b[i]<'0' | b[i]>'9' ,s);
	tmp = b[i++]-'0';
	while(i<b.length && b[i]>='0' && b[i]<='9') 
	    tmp = tmp* 10  + b[i++]-'0';
	throwException( tmp > 255, s );
	v[1] = (byte) tmp;
	throwException(b[i++]!='.',s);
	// 3
	throwException(b[i]<'0' | b[i]>'9' ,s);
	tmp = b[i++]-'0';
	while(i<b.length && b[i]>='0' && b[i]<='9') 
	    tmp = tmp* 10  + b[i++]-'0';
	throwException( tmp > 255, s );
	v[2] = (byte) tmp;
	throwException(b[i++]!='.',s);
	// 4
	throwException(b[i]<'0' | b[i]>'9' ,s);
	tmp = b[i++]-'0';
	while(i<b.length && b[i]>='0' && b[i]<='9') 
	    tmp = tmp* 10  + b[i++]-'0';
	throwException( tmp > 255, s );
	v[3] = (byte) tmp;
	throwException( i!=b.length,s);
	// getByAddress
	try {
	    return java.net.InetAddress.getByAddress(v);
	} catch (Exception e) {
	    throwException( true, s);
	    return null;
	}
    }
    

    public String toString() {
	String inaddr = value.toString();
	// on format "hostname/address"
	byte[] b = inaddr.getBytes();
	int i=0;
	while(i<b.length && b[i]!='/') i++;
	if (i<b.length) 
	    return inaddr.substring(i+1); 
	return inaddr;
    }
    
    public boolean equals(Object v) {
        if (v instanceof InetAddressIPv4)
            return ((InetAddressIPv4)v).equals(this);
        return false;
    }

    public boolean equals(InetAddressIPv4 v) {
	java.net.InetAddress v1 = value;
	java.net.InetAddress v2 = v.getValue();
	return v1.equals(v2);
    }
    

    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
	if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,this);
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

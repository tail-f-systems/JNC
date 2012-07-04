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
import java.util.ArrayList;
import java.net.InetAddress;

/**
 * This class implements the "inetAddressIPV6" datatype from the
 * 'http://tail-f.com/ns/confd/1.0' namespace.
 * 
 * Examples: (the following are all valid and equivalent)
 * 2001:0db8:0000:0000:0000:0000:1428:57ab 2001:0db8:0000:0000:0000::1428:57ab
 * 2001:0db8:0:0:0:0:1428:57ab 2001:0db8:0:0::1428:57ab 2001:0db8::1428:57ab
 * 2001:db8::1428:57ab
 */
public class InetAddressIPv6 implements Serializable {

    private java.net.InetAddress value;

    public InetAddressIPv6(String v) throws ConfMException {
        value = parseValue(v);
        check();
    }

    public InetAddressIPv6(java.net.InetAddress v) throws ConfMException {
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

    public void check() throws ConfMException {
    }

    /**
     * Parse hexa-decimal IP string into an array of bytes (16 octets) to
     * represent the ipV6 address, and create the InetAddress.
     */
    private java.net.InetAddress parseValue(String s) throws ConfMException {
        s = com.tailf.confm.xs.String.wsCollapse(s);
        // System.out.println("parsing: "+s);
        ArrayList list = new ArrayList();
        byte[] b = s.getBytes();
        boolean doubleColon = false;

        int i = 0;
        while (i < b.length) {

            int tmp = 0;
            for (int x = 0; x < 4; x++) {
                if (b[i] >= '0' && b[i] <= '9')
                    tmp = tmp * 16 + b[i++] - '0';
                else if (b[i] >= 'A' && b[i] <= 'F')
                    tmp = tmp * 16 + b[i++] - 'A' + 10;
                else if (b[i] >= 'a' && b[i] <= 'f')
                    tmp = tmp * 16 + b[i++] - 'a' + 10;
            }
            list.add(new Integer(tmp));
            // :
            if (i < (b.length - 1)) {
                throwException(b[i] != ':', s);
                i++;
                if (b[i] == ':' && doubleColon == false) {
                    doubleColon = true;
                    list.add(new String("::"));
                    i++;
                }
            }
        }
        int j = 0;
        byte[] v = new byte[16];
        for (i = 0; i < list.size(); i++) {
            Object t = list.get(i);
            if (t instanceof Integer) {
                // System.out.println("i="+i+ ", j="+j+" -> "+t);
                int tmp = ((Integer) t).intValue();
                v[j++] = (byte) ((tmp >> 8) & 0xff);
                v[j++] = (byte) (tmp & 0xff);
            } else { // '::' - jump forward
                j = (8 - list.size() + i + 1) * 2;
                // System.out.println("set j="+j);
            }
        }
        try {
            return java.net.InetAddress.getByAddress(v);
        } catch (Exception e) {
            throwException(true, s);
            return null;
        }
    }

    public String toString() {
        String inaddr = value.toString();
        // on format "hostname/address"
        byte[] b = inaddr.getBytes();
        int i = 0;
        while (i < b.length && b[i] != '/')
            i++;
        if (i < b.length)
            return inaddr.substring(i + 1);
        return inaddr;
    }

    private char toDigit(int b) {
        if (b >= 10)
            return (char) (b + 'A' - 10);
        else
            return (char) (b + '0');
    }

    public boolean equals(Object b) {
        if (b instanceof InetAddressIPv6)
            return ((InetAddressIPv6) b).equals(this);
        return false;
    }

    public boolean equals(InetAddressIPv6 v) {
        java.net.InetAddress v1 = value;
        java.net.InetAddress v2 = v.getValue();
        return v1.equals(v2);
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object o) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, o);
    }

}

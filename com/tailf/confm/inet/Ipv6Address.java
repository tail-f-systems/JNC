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

package com.tailf.confm.inet;
import com.tailf.confm.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.net.InetAddress;

/**
 * This class implements the "ipv6-address" datatype from the
 * 'urn:ietf:params:xml:ns:yang:inet-types' namespace.
 *
 * Examples:
 * 2001:0db8:0000:0000:0000:0000:1428:57ab
 * 2001:0db8:0000:0000:0000::1428:57ab
 * 2001:0db8:0:0:0:0:1428:57ab
 * 2001:0db8:0:0::1428:57ab
 * 2001:0db8::1428:57ab
 * 2001:db8::1428:57ab
 *
 * The above addresses are valid and equivalent.
 */
public class Ipv6Address implements Serializable {
    private java.net.InetAddress value;

    /**
     * Constructor
     */
    public Ipv6Address(String stringValue) throws ConfMException {
        value = parseValue(stringValue);
        check();
    }

    private java.net.InetAddress parseValue(String stringValue)
      throws ConfMException {
        stringValue = com.tailf.confm.xs.String.wsCollapse(stringValue);
        ArrayList list = new ArrayList();
        byte[] b = stringValue.getBytes();
        boolean doubleColon = false;
        int i = 0;
        while (i < b.length) {
            int tmp = 0;
            for (int x = 0; x < 4; x++) {
                if (b[i] >= '0' && b[i] <= '9')
                    tmp = tmp*16+b[i++]-'0';
                else if (b[i] >= 'A' && b[i] <= 'F')
                    tmp = tmp*16+b[i++]-'A'+10;
                else if (b[i] >= 'a' && b[i] <= 'f')
                    tmp = tmp*16+b[i++]-'a'+10;
            }
            list.add(new Integer(tmp));
            // :
            if (i < (b.length-1)) {
                throwException(b[i] != ':', stringValue);
                i++;
                if (b[i] == ':' && doubleColon == false) {
                    doubleColon = true;
                    list.add(new String("::"));
                    i++;
                }
            }
        }
        int j=0;
        byte[] v = new byte[16];
        for (i = 0; i < list.size(); i++) {
            Object t = list.get(i);
            if (t instanceof Integer) {
                int tmp =((Integer)t).intValue();
                v[j++] = (byte)((tmp>>8)&0xff );
                v[j++] = (byte)(tmp&0xff );
            } else { // '::' - jump forward
                j = (8-list.size()+i+1)*2;
            }
        }
        try {
            return java.net.InetAddress.getByAddress(v);
        } catch (Exception e) {
            throwException(true, stringValue);
            return null;
        }
    }

    public void check() throws ConfMException {
    }

    public Ipv6Address(java.net.InetAddress value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Set a value
     */
    public void setValue(String stringValue) throws ConfMException {
        value = parseValue(stringValue);
        check();
    }

    public void setValue(java.net.InetAddress value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Get a value
     */
    public java.net.InetAddress getValue() {
        return value;
    }

    /**
     * Convert to a string value
     */
    public String toString() {
        String inaddr = value.toString();
        // on format "hostname/address"
        byte[] b = inaddr.getBytes();
        int i = 0;
        while (i < b.length && b[i] != '/')
            i++;
        if (i < b.length)
            return inaddr.substring(i+1);
        return inaddr;
    }

    private char toDigit(int b) {
        if (b >= 10)
            return (char)(b+'A'-10);
        else
            return (char)(b+'0');
    }

    /**
     * Check for equality
     */
    public boolean equals(Object object) {
        if (object instanceof Ipv6Address)
            return ((Ipv6Address)object).equals(this);
        return false;
    }

    public boolean equals(Ipv6Address value) {
        java.net.InetAddress v1 = this.value;
        java.net.InetAddress v2 = value.getValue();
        return v1.equals(v2);
    }

    /**
     * Internal utilities
     */
    protected void throwException(boolean ok) throws ConfMException {
        if (!ok)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

    protected void throwException(boolean ok, Object object)
      throws ConfMException {
        if (!ok)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, object);
    }
}

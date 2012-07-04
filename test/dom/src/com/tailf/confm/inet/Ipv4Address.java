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
import java.net.InetAddress;

/**
 * This class implements the "ipv4-address" datatype from the
 * 'urn:ietf:params:xml:ns:yang:inet-types' namespace.
 */
public class Ipv4Address implements Serializable {
    private java.net.InetAddress value;

    /**
     * Constructor
     */
    public Ipv4Address(String stringValue) throws ConfMException {
        value = parseValue(stringValue);
        check();
    }

    private java.net.InetAddress parseValue(String stringValue)
            throws ConfMException {
        stringValue = com.tailf.confm.xs.String.wsCollapse(stringValue);
        byte[] b = stringValue.getBytes();
        byte[] v = new byte[4];
        throwException(b.length == 0, stringValue);
        int i = 0;
        // 1
        throwException(b[i] < '0' || b[i] > '9', stringValue);
        int tmp = b[i++] - '0';
        while (i < b.length && b[i] >= '0' && b[i] <= '9')
            tmp = tmp * 10 + b[i++] - '0';
        throwException(tmp > 255, stringValue);
        v[0] = (byte) tmp;
        throwException(b[i++] != '.', stringValue);
        // 2
        throwException(b[i] < '0' || b[i] > '9', stringValue);
        tmp = b[i++] - '0';
        while (i < b.length && b[i] >= '0' && b[i] <= '9')
            tmp = tmp * 10 + b[i++] - '0';
        throwException(tmp > 255, stringValue);
        v[1] = (byte) tmp;
        throwException(b[i++] != '.', stringValue);
        // 3
        throwException(b[i] < '0' || b[i] > '9', stringValue);
        tmp = b[i++] - '0';
        while (i < b.length && b[i] >= '0' && b[i] <= '9')
            tmp = tmp * 10 + b[i++] - '0';
        throwException(tmp > 255, stringValue);
        v[2] = (byte) tmp;
        throwException(b[i++] != '.', stringValue);
        // 4
        throwException(b[i] < '0' || b[i] > '9', stringValue);
        tmp = b[i++] - '0';
        while (i < b.length && b[i] >= '0' && b[i] <= '9')
            tmp = tmp * 10 + b[i++] - '0';
        throwException(tmp > 255, stringValue);
        v[3] = (byte) tmp;
        throwException(i != b.length, stringValue);
        // getByAddress
        try {
            return java.net.InetAddress.getByAddress(v);
        } catch (Exception e) {
            throwException(true, stringValue);
            return null;
        }
    }

    private void check() throws ConfMException {
    }

    public Ipv4Address(java.net.InetAddress value) throws ConfMException {
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
            return inaddr.substring(i + 1);
        return inaddr;
    }

    /**
     * Check for equality
     */
    public boolean equals(Object value) {
        if (value instanceof Ipv4Address)
            return ((Ipv4Address) value).equals(this);
        return false;
    }

    public boolean equals(Ipv4Address value) {
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

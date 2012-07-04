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

package com.tailf.confm.xs;

import com.tailf.confm.*;
import java.io.Serializable;

/**
 * This class implements the "xs:hexBinary" datatype from the
 * 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 * Represents a list of octets. For example: 0FB7 - is the encoding for the
 * 16-bit integer 4023.
 * 
 */
public class HexBinary implements Serializable {

    private byte[] value;

    public HexBinary(java.lang.String v) throws ConfMException {
        value = parseValue(v);
        check();
    }

    public HexBinary(int v) throws ConfMException {
        value = intToBytes((long) v);
        check();
    }

    public HexBinary(long v) throws ConfMException {
        value = intToBytes(v);
        check();
    }

    public HexBinary(byte[] v) throws ConfMException {
        value = v;
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String v) throws ConfMException {
        value = parseValue(v);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(long v) throws ConfMException {
        value = intToBytes(v);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(int v) throws ConfMException {
        value = intToBytes((long) v);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(byte[] v) throws ConfMException {
        value = v;
        check();
    }

    /**
     * Returns the value space.
     */
    public byte[] getValue() {
        return value;
    }

    private void check() {
    }

    /**
     * convert integer to bytes.
     */
    private byte[] intToBytes(long v) throws ConfMException {
        int i = 0;
        throwException(v < 0, new java.lang.Long(v));
        long tmp = v;
        while (tmp > 0) {
            i = i + 1;
            tmp = tmp >> 8;
        }
        byte[] b = new byte[i];
        while (i > 0) {
            b[--i] = (byte) (v & 0xff);
            v = v >> 8;
        }
        return b;
    }

    /**
     * Parse hexa-decimal string
     */
    private byte[] parseValue(java.lang.String s) throws ConfMException {
        s = String.wsCollapse(s);
        byte[] b = s.getBytes();
        throwException(b.length == 0, s);
        throwException((b.length % 2) > 0, s);
        byte[] r = new byte[b.length / 2];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) (parseDigit(b[i * 2], s) * 0x10 + parseDigit(
                    b[i * 2 + 1], s));

            // System.out.println("got "+r[i]);
        }
        return r;
    }

    private int parseDigit(byte v, java.lang.String s) throws ConfMException {
        if (v >= 'a' && v <= 'f')
            return v - 'a' + 10;
        if (v >= 'A' && v <= 'F')
            return v - 'A' + 10;
        if (v >= '0' && v <= '9')
            return v - '0';
        throwException(true, s);
        return 0;
    }

    public java.lang.String toString() {
        java.lang.String s = new java.lang.String();
        for (int i = 0; i < value.length; i++) {
            s = s + toDigit((value[i] >> 4) & 0x0f);
            s = s + toDigit(value[i] & 0x0f);
        }
        return s;
    }

    private char toDigit(int b) {
        // System.out.println("toDigit: "+b);
        if (b >= 10)
            return (char) (b + 'A' - 10);
        else
            return (char) (b + '0');
    }

    public boolean equals(HexBinary v) {
        byte[] v1 = value;
        byte[] v2 = v.getValue();
        if (v1.length != v2.length)
            return false;
        for (int i = 0; i < v1.length; i++)
            if (v1[i] != v2[i])
                return false;
        return true;
    }

    public boolean equals(Object value) {
        if (value instanceof HexBinary)
            return equals((HexBinary) value);
        return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:minLength defines a minimum length measured in number of characters or
     * bytes.
     */
    protected void minLength(int len) throws ConfMException {
        throwException(value.length < len);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of characters or
     * bytes.
     */
    protected void maxLength(int len) throws ConfMException {
        throwException(value.length > len);
    }

    /**
     * xs:totalDigits
     */
    protected void totalDigits(int digits) throws ConfMException {
        throwException(value.length > digits);
    }

    /**
     * xs:enumeration
     */
    protected boolean enumeration(java.lang.String value) {
        if (toString().equals(value))
            return true;
        else
            return false;
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

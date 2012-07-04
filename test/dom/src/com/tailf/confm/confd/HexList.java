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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class implements the "confd:hexList" datatype from the
 * 'http://tail-f.com/ns/confd/1.0' namespace.
 * 
 * Represents a list of colon separated hex octets. For example: 0F:B7 - is the
 * encoding for the 16-bit integer 4023.
 * 
 */
public class HexList implements Serializable {

    private byte[] value;

    public HexList(java.lang.String v) throws ConfMException {
        value = parseValue(v);
        check();
    }

    public HexList(int v) throws ConfMException {
        value = intToBytes((long) v);
        check();
    }

    public HexList(long v) throws ConfMException {
        value = intToBytes(v);
        check();
    }

    public HexList(byte[] v) throws ConfMException {
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
        s = com.tailf.confm.xs.String.wsCollapse(s);
        byte[] b = s.getBytes();
        // throwException( b.length ==0, s );
        ArrayList list = new ArrayList();
        int i = 0;
        while (i < (b.length - 1)) {
            list.add(new Integer(parseDigit(b[i++], s) * 0x10
                    + parseDigit(b[i++], s)));
            if (i < (b.length - 1)) {
                if (b[i] != ':')
                    throwException(true, s);
                i++;
            }
        }
        throwException(i != b.length);
        // make the array
        byte[] v = new byte[list.size()];
        for (i = 0; i < list.size(); i++) {
            v[i] = ((Integer) list.get(i)).byteValue();
        }
        return v;
    }

    private byte parseDigit(byte v, java.lang.String s) throws ConfMException {
        if (v >= 'a' && v <= 'f')
            return (byte) (v - 'a' + 10);
        if (v >= 'A' && v <= 'F')
            return (byte) (v - 'A' + 10);
        if (v >= '0' && v <= '9')
            return (byte) (v - '0');
        throwException(true, s);
        return 0;
    }

    public java.lang.String toString() {
        java.lang.String s = new java.lang.String();
        boolean colon = false;
        for (int i = 0; i < value.length; i++) {
            if (colon)
                s = s + ":";
            colon = true;
            s = s + toDigit((value[i] >> 4) & 0x0f);
            s = s + toDigit(value[i] & 0x0f);
        }
        return s;
    }

    private char toDigit(int b) {
        if (b >= 10)
            return (char) (b + 'A' - 10);
        else
            return (char) (b + '0');
    }

    public boolean equals(HexList v) {
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
        if (value instanceof HexList)
            return equals((HexList) value);
        return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:length defines an exact length measured in number of characters or
     * bytes.
     */
    protected void length(int len) throws ConfMException {
        throwException(value.length != len);
    }

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
     * xs:pattern
     */
    protected void pattern(java.lang.String regex) throws ConfMException {
        try {
            java.lang.String s = toString();
            throwException(!Pattern.matches(regex, s));
        } catch (PatternSyntaxException e) {
            throwException(true, e);
        }
    }

    protected void pattern(java.lang.String[] regexes) throws ConfMException {
        try {
            java.lang.String s = toString();

            for (int i = 0; i < regexes.length; i++)
                if (Pattern.matches(regexes[i], s))
                    return;

            throwException(true);
        } catch (PatternSyntaxException e) {
            throwException(true, e);
        }
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

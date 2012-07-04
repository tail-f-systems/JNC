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

/**
 * This class implements the "confd:octetList" datatype from the
 * 'http://tail-f.com/ns/confd/1.0' namespace.
 * 
 * Represents a list of dot-separated octets. For example: 192.168.0.1.0.1
 * 
 */
public class OctetList implements Serializable {

    private byte[] value;

    public OctetList(java.lang.String v) throws ConfMException {
        value = parseValue(v);
        check();
    }

    public OctetList(byte[] v) throws ConfMException {
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
     * Parse octet list
     */
    private byte[] parseValue(java.lang.String s) throws ConfMException {
        s = com.tailf.confm.xs.String.wsCollapse(s);
        byte[] b = s.getBytes();
        // throwException( b.length ==0, s );
        ArrayList list = new ArrayList();
        int i = 0;
        while (i < b.length) {

            if (b[i] >= '0' && b[i] <= '9') {
                int tmp = b[i++] - '0';
                while (i < b.length && b[i] >= '0' && b[i] <= '9')
                    tmp = tmp * 10 + b[i++] - '0';
                throwException(tmp > 255, s);
                // check if dot
                list.add(new Integer(tmp));
                if (i < (b.length - 1) && b[i] == '.')
                    i++;
            } else
                throwException(true, s);
        }
        // make the array
        byte[] v = new byte[list.size()];
        for (i = 0; i < list.size(); i++) {
            v[i] = ((Integer) list.get(i)).byteValue();
        }
        return v;
    }

    public java.lang.String toString() {
        String s = new String();
        boolean dot = false;
        for (int i = 0; i < value.length; i++) {
            if (dot)
                s = s + ".";
            dot = true;
            s = s + (value[i] & 0xff); // cast to integer first
        }
        return s;
    }

    public boolean equals(OctetList v) {
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
        if (value instanceof OctetList)
            return equals((OctetList) value);
        return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:length defines an exact length measured in number of values.
     * 
     */
    protected void length(int len) throws ConfMException {
        throwException(value.length != len);
    }

    /**
     * xs:minLength defines a minimum length measured in number of values.
     */
    protected void minLength(int len) throws ConfMException {
        throwException(value.length < len);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of values.
     */
    protected void maxLength(int len) throws ConfMException {
        throwException(value.length > len);
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

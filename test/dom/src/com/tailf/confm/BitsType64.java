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

package com.tailf.confm;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * This class implements the "bitsType" datatype from the confspec namespace.
 * <p>
 * This is a type class used by the ConfM generated classes. It represents a
 * 64-bit unsigned integer with flags.
 * 
 */
public class BitsType64 implements Serializable {

    /**
     * Serial version ID
     */
    private static final long serialVersionUID = 9215775924345945977L;

    private long value;

    public BitsType64(String value, long all, String[] smap, int[] imap)
            throws ConfMException {
        this.value = str2long(value, smap, imap);
    }

    public BitsType64(long value, long all, String[] smap, int[] imap)
            throws ConfMException {
        this.value = value;
        check(all, smap, imap);
    }

    /**
     * Sets the value.
     */
    public void setValue(String value, long all, String[] smap, int[] imap)
            throws ConfMException {
        this.value = str2long(value, smap, imap);
    }

    /**
     * Sets the value.
     */
    public void setValue(long value, long all, String[] smap, int[] imap)
            throws ConfMException {
        this.value = value;
        check(all, smap, imap);
    }

    /**
     * Return the value space.
     */
    public long getValue() {
        return value;
    }

    public void check(long all, String[] smap, int[] imap)
            throws ConfMException {
        if ((all | value) != all)
            throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

    public String toString(String[] smap, int[] imap) {
        return long2str(value, smap, imap);
    }

    /**
     * Artihmetic 'or'
     */
    public void OR(BitsType64 v) {
        this.value = this.value | v.getValue();
    }

    /**
     * Arithmetic 'xor'
     */
    public void XOR(BitsType64 v) {
        this.value = this.value ^ v.getValue();
    }

    /**
     * Arithmetic 'and'
     */
    public void AND(BitsType64 v) {
        this.value = this.value & v.getValue();
    }

    /**
     * Compares against an integer
     */
    public boolean equals(long value) {
        if (this.value == value)
            return true;
        else
            return false;
    }

    public boolean equals(BitsType64 value) {
        return value.equals(this.value);
    }

    public boolean equals(Object value) {
        if (value instanceof BitsType64)
            return ((BitsType64) value).equals(this.value);
        return false;
    }

    private long str2long(String v, String[] smap, int[] imap)
            throws ConfMException {
        StringTokenizer st = new StringTokenizer(v);
        long res = 0;
        while (st.hasMoreTokens()) {
            boolean found = false;
            String tok = st.nextToken();
            for (int i = 0; i < smap.length; i++) {
                if (tok.compareTo(smap[i]) == 0) {
                    found = true;
                    res = res | (1 << imap[i]);
                }
            }
            if (!found) {
                throw new ConfMException(ConfMException.BAD_VALUE, tok);
            }
        }
        return res;
    }

    private String long2str(long v, String[] smap, int[] imap) {
        String res = "";
        for (int i = 0; i < imap.length; i++) {
            long mask = 1 << imap[i];
            if (!((v & mask) == 0)) {
                res += smap[i];
                if (i != (imap.length - 1)) {
                    res += " ";
                }
            }
        }
        return res;
    }

}

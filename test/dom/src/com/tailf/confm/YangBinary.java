/*    -*- Java -*-
 *
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm;

import java.io.Serializable;

/**
 * Implements the built-in YANG data type "binary".
 * 
 * @author emil@tail-f.com
 */
public class YangBinary implements Serializable {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -7059091493910236203L;

    /**
     * Buffer to store arbitrary binary data encoded in base 64.
     * 
     * @serial
     */
    private byte[] value;

    /**
     * Creates a YangBinary object from a String. The string is whitespace
     * collapsed before it is encoded.
     * 
     * @param value the String
     */
    public YangBinary(String value) {
        value = YangString.wsCollapse(value);
        this.value = Base64Coder.encodeString(value).getBytes();
    }

    /**
     * Creates a YangBinary object from a base 64 encoded byte buffer.
     * 
     * @param value The base 64 encoded byte buffer
     */
    public YangBinary(byte[] value) {
        this.value = new byte[value.length];
        System.arraycopy(value, 0, this.value, 0, value.length);
    }

    /**
     * Sets the value of this object using a String. The string is whitespace
     * collapsed before it is encoded.
     * 
     * @param value The string
     */
    public void setValue(String value) {
        value = YangString.wsCollapse(value);
        this.value = Base64Coder.encodeString(value).getBytes();
    }

    /**
     * Sets the value of this object by copying a base 64 encoded byte buffer.
     * 
     * @param value The base 64 encoded byte buffer
     */
    public void setValue(byte[] value) {
        this.value = new byte[value.length];
        System.arraycopy(value, 0, this.value, 0, value.length);
    }

    /**
     * @return The value buffer of this object
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * @return The decoded value of this object, as a String
     */
    @Override
    public String toString() {
        return Base64Coder.decodeString(new String(value));
    }

    /** ---------- Restrictions ---------- */

    /**
     * Checks that the value buffer of this object has the specified number of
     * octets/bytes.
     * 
     * @param len The number of octets/bytes to check for
     * @throws ConfMException If value buffer does not have len number of
     *         octets/bytes
     */
    protected void length(int len) throws ConfMException {
        ConfMException.throwException(value.length != len, this);
    }

    /**
     * Checks that the value buffer of this object has less than the specified 
     * number of octets/bytes.
     * 
     * @param len The number of octets/bytes to compare against
     * @throws ConfMException If value buffer does not have less than len
     *         number of octets/bytes
     */
    protected void minLength(int len) throws ConfMException {
        ConfMException.throwException(value.length < len, this);
    }

    /**
     * Checks that the value buffer of this object has more than the specified 
     * number of octets/bytes.
     * 
     * @param len The number of octets/bytes to compare against
     * @throws ConfMException If value buffer does not have more than len
     *         number of octets/bytes
     */
    protected void maxLength(int len) throws ConfMException {
        ConfMException.throwException(value.length > len, this);
    }

    /**
     * Whitespace replace. Replaces all occurrences of #x9 (tab), #xA (line
     * feed), and #xD (CR) with #x20 (space).
     */
    protected void wsReplace() {
        value = YangString.wsReplace(new String(value)).getBytes();
    }

    /**
     * Whitespace replace. Contiguous sequences of 0x20 are collapsed into a
     * single 0x20, and initial and/or final 0x20s are deleted.
     */
    protected void wsCollapse() {
        value = YangString.wsCollapse(new String(value)).getBytes();
    }

}

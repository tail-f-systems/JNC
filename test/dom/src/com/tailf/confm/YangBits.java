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
import java.math.BigInteger;

/**
 * Implements the built-in YANG data type "bits".
 *
 * @author emil@tail-f.com
 */
public abstract class YangBits implements Serializable {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 5882382456815438844L;

    /**
     * The value of this object, of which this class is a wrapper for.
     * 
     * @serial
     */
    private BigInteger value;

    /**
     * Constructor using string arguments.
     * 
     * @param value The value to initialize the object with, as a string.
     * @param mask The bit mask as a string.
     * @throws ConfMException If value does not match mask.
     * @throws NumberFormatException If value or mask are not valid as numbers.
     */
    public YangBits(String value, String mask)
            throws ConfMException {
        this(new BigInteger(value), new BigInteger(mask));
    }

    /**
     * Constructor using BigInteger arguments.
     * 
     * @param value The value to initialize the object with.
     * @param mask The bit mask to initialize the object with.
     * @throws ConfMException If value does not match mask.
     */
    public YangBits(BigInteger value, BigInteger mask)
            throws ConfMException {
        this.value = value;
        check(mask);
    }

    /**
     * Value setter using string arguments.
     * 
     * @param value The value to set this object's value to, as a string.
     * @param mask The bit mask to use, as a string.
     * @throws ConfMException If value does not match mask.
     * @throws NumberFormatException If value or mask are not valid as numbers.
     */
    public void setValue(String value, String mask)
            throws ConfMException {
        setValue(new BigInteger(value), new BigInteger(mask));
    }

    /**
     * Value setter using BigInteger arguments.
     * 
     * @param value The value to set this object's value to.
     * @param mask The bit mask to use.
     * @throws ConfMException If value does not match mask.
     */
    public void setValue(BigInteger value, BigInteger mask)
            throws ConfMException {
        this.value = value;
        check(mask);
    }

    /**
     * @return The value of this object, as a BigInteger.
     * @see java.math.BigInteger
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Checks that the value space matches mask.
     * @param mask Bit mask.
     * @throws ConfMException if value space does not match mask.
     */
    public void check(BigInteger mask) throws ConfMException {
        boolean fail = mask.or(value).compareTo(mask) == 0;
        ConfMException.throwException(fail, this);
    }

    /**
     * @return A string representation of the value of this object.
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Performs arithmetic or assignment: this.value &= v.value.
     * 
     * @param v Bits instance to fetch value from.
     */
    public void AND(YangBits v) {
        this.value = this.value.and(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value |= v.value.
     * 
     * @param v Bits instance to fetch value from.
     */
    public void OR(YangBits v) {
        this.value = this.value.or(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value ^= v.value.
     * 
     * @param v Bits instance to fetch value from.
     */
    public void XOR(YangBits v) {
        this.value = this.value.xor(v.getValue());
    }

    /**
     * Compares against a BigInteger.
     * 
     * @param value The value space to compare against.
     * @return true if equal; false otherwise.
     */
    public boolean equals(BigInteger value) {
        return this.value.compareTo(value) == 0;
    }

    /**
     * Compares against a String.
     * 
     * @param value The value space to compare against, as a string.
     * @return true if equal; false otherwise.
     * @throws NumberFormatException If value is not valid as a number.
     */
    public boolean equals(String value) {
        return equals(new BigInteger(value));
    }

    /**
     * Compares against another Bits instance.
     * 
     * @param bits The Bits intance to compare against.
     * @return true if this object's value space is equal to the value space of
     *         bits; false otherwise.
     */
    public boolean equals(YangBits bits) {
        return equals(bits.getValue());
    }

    /**
     * Compares against an arbitrary object.
     * 
     * @param obj The object to compare against.
     * @return true if obj can be interpreted as a Bits instance or a value
     *         space, and this object's value space is equal to that; false
     *         otherwise.
     * @throws NumberFormatException If value is a String not valid as a number.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BigInteger)
            return equals((BigInteger) obj);
        else if (obj instanceof String)
            return equals((String) obj);
        else if (obj instanceof YangBits)
            return equals((YangBits) obj);
        return false;
    }

}

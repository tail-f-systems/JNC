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
 * @serial 5882382456815438844L
 */
public abstract class Bits implements Serializable {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 5882382456815438844L;

    /**
     * Value space of this object
     */
    private BigInteger value;

    /**
     * Constructor using string arguments
     * @param value The value space as a string
     * @param mask The bit mask as a string
     * @throws ConfMException If value does not match mask
     * @throws NumberFormatException If value or mask are not valid as numbers
     */
    public Bits(String value, String mask)
            throws ConfMException {
        this(new BigInteger(value), new BigInteger(mask));
    }

    /**
     * Constructor using BigInteger arguments
     * @param value The value space
     * @param mask The bit mask
     * @throws ConfMException If value does not match mask
     */
    public Bits(BigInteger value, BigInteger mask)
            throws ConfMException {
        this.value = value;
        check(mask);
    }

    /**
     * Value setter using string arguments
     * @param value The value space to set, as a string
     * @param mask The bit mask as a string
     * @throws ConfMException If value does not match mask
     * @throws NumberFormatException If value or mask are not valid as numbers
     */
    public void setValue(String value, String mask)
            throws ConfMException {
        setValue(new BigInteger(value), new BigInteger(mask));
    }

    /**
     * Value setter using BigInteger arguments
     * @param value The value space to set
     * @param mask The bit mask
     * @throws ConfMException If value does not match mask
     */
    public void setValue(BigInteger value, BigInteger mask)
            throws ConfMException {
        this.value = value;
        check(mask);
    }

    /**
     * @return The value space as a BigInteger
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Checks that the value space matches mask
     * @param mask Bit mask
     * @throws ConfMException if value space does not match mask
     */
    public void check(BigInteger mask) throws ConfMException {
        boolean fail = mask.or(value).compareTo(mask) == 0;
        ConfMException.throwException(fail, this);
    }

    /**
     * @return A string representation of the value space
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Performs arithmetic or assignment: this.value &= v.value
     * @param v Bits instance to fetch value from
     */
    public void AND(Bits v) {
        this.value = this.value.and(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value |= v.value
     * @param v Bits instance to fetch value from
     */
    public void OR(Bits v) {
        this.value = this.value.or(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value ^= v.value
     * @param v Bits instance to fetch value from
     */
    public void XOR(Bits v) {
        this.value = this.value.xor(v.getValue());
    }

    /**
     * Compares against a BigInteger.
     * @param value The value space to compare against
     * @return true if equal, else false
     */
    public boolean equals(BigInteger value) {
        return this.value.compareTo(value) == 0;
    }

    /**
     * Compares against a String.
     * @param value The value space to compare against, as a string
     * @return true if equal, else false
     * @throws NumberFormatException If value is not valid as a number
     */
    public boolean equals(String value) {
        return equals(new BigInteger(value));
    }

    /**
     * Compares against another Bits instance.
     * @param bits The Bits intance to compare against
     * @return true if this object's value space is equal to the value space of
     *         bits; false otherwise.
     */
    public boolean equals(Bits bits) {
        return equals(bits.getValue());
    }

    /**
     * Compares against an arbitrary object.
     * @param obj The object to compare against
     * @return true if obj can be interpreted as a Bits instance or a value
     *         space, and this object's value space is equal to that; false
     *         otherwise.
     * @throws NumberFormatException If value is a String not valid as a number
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BigInteger)
            return equals((BigInteger) obj);
        else if (obj instanceof String)
            return equals((String) obj);
        else if (obj instanceof Bits)
            return equals((Bits) obj);
        return false;
    }

}

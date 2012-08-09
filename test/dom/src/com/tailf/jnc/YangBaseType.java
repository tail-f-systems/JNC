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

package com.tailf.jnc;

import java.math.BigDecimal;

/**
 * Represents a built-in YANG data type.
 * 
 * @author emil@tail-f.com
 */
abstract class YangBaseType<T> implements YangType<T> {

    private static final long serialVersionUID = 1L;

    /**
     * The value of this object, of which this class is a wrapper for.
     * 
     * @serial
     */
    protected T value;
    
    /**
     * Empty constructor for a YangType object. The value will not be
     * initialized when calling this method.
     */
    public YangBaseType() {
    }

    /**
     * Creates a YangType object from a String.
     * 
     * @param s The string.
     * @throws YangException If an invariant was broken during initialization,
     *                        or if value could not be parsed from s.
     */
    public YangBaseType(String s) throws YangException {
        setValue(s);
    }

    /**
     * Creates a YangType object from a value of type T.
     * 
     * @param value The initial value of the new YangType object.
     * @throws YangException If an invariant was broken during initialization.
     */
    public YangBaseType(T value) throws YangException {
        setValue(value);
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangType#setValue(java.lang.String)
     */
    @Override
    public void setValue(String s) throws YangException {
        s = Utils.wsCollapse(s);
        setValue(fromString(s));
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangType#setValue(T)
     */
    @Override
    public void setValue(T value) throws YangException {
        assert !(value instanceof YangType): "Avoid circular value chain";
        this.value = value;
        check();
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangType#getValue()
     */
    @Override
    public T getValue() {
        return value;
    }
    
    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangType#check()
     */
    @Override
    public void check() throws YangException {
        YangException.throwException(getValue() == null, this);
    }

    /**
     * @return The value of this object, as a String.
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Returns a value of type T given a String.
     * <p>
     * Note: This method is non-static since T is a non-static type.
     * 
     * @param s A string representation of a value of type T.
     * @return A T value parsed from s (not this.value!).
     * @throws YangException If s does not contain a parsable T.
     */
    protected abstract T fromString(String s) throws YangException;

    /**
     * Compares this object with another object for equality.
     * 
     * @param obj The object to compare with.
     * @return true if obj can be cast to a comparable type and the value of
     *         this object is equal to the value of obj; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (value == null || !canEqual(obj)) {
            return false;
        }
        if (obj instanceof YangBaseType<?>) {
            YangBaseType<?> other = (YangBaseType<?>) obj;
            if (!other.canEqual(this))
                return false;
            obj = other.getValue();
        }
        if (value instanceof Number && obj instanceof Number) {
            BigDecimal valueNumber = Utils.bigDecimalValueOf((Number) value);
            BigDecimal objNumber = Utils.bigDecimalValueOf((Number) obj);
            return valueNumber.compareTo(objNumber) == 0;
        }
        return value.equals(obj);
    }

    @Override
    public int hashCode() {
        if (value == null) 
            return 0;
        return value.hashCode();
    }

    /** ---------- Restrictions ---------- */

    /**
     * Checks that the value of this object equals or has the same length as
     * the provided other value.
     * 
     * @param other The integer value to compare against.
     * @throws YangException If the comparison does not evaluate to true.
     */
    protected void exact(int other) throws YangException {
        Utils.restrict(this.value, other, Utils.Operator.EQ);
    }

    /**
     * Checks that the value of this object is not smaller than the min-value.
     * 
     * @param min The min-value to compare against.
     * @throws YangException if value is smaller than min.
     */
    protected void min(int min) throws YangException {
        Utils.restrict(value, min, Utils.Operator.GE);
    }

    /**
     * Checks that the value of this object is not larger than the max-value.
     * 
     * @param max The max-value to compare against.
     * @throws YangException if value is larger than max.
     */
    protected void max(int max) throws YangException {
        Utils.restrict(value, max, Utils.Operator.LE);
    }

}

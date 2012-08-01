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

package com.tailf.netconfmanager.yang.type;

import java.math.BigDecimal;

import com.tailf.netconfmanager.yang.YangException;

/**
 * Extended by implementations of the Integer, decimal64 and binary built-in
 * YANG data types.
 * 
 * @author emil@tail-f.com
 */
abstract class Int<T extends Number> extends Type<T> {

    private static final long serialVersionUID = 1L;

    /**
     * A lower bound for the value of this object, or null if none.
     * 
     * @serial
     */
    protected BigDecimal MIN_VALUE = null;
    
    /**
     * An upper bound for the value of this object, or null if none.
     * 
     * @serial
     */
    protected BigDecimal MAX_VALUE = null;
    
    /**
     * Creates a YangInt object from a String.
     * 
     * @param s The string.
     * @param minValue Lower bound for the value of this object.
     * @param maxValue Upper bound for the value of this object.
     * @throws YangException If an invariant was broken during initialization,
     *                        if value could not be parsed from s, or if
     *                        minValue is larger than maxValue.
     */
    public Int(String s)
            throws YangException {
        super(s);
    }

    /**
     * Creates a YangInt object from a value of type T.
     * 
     * @param value The initial value of the new YangInt object.
     * @throws YangException If an invariant was broken during initialization,
     *                        or if minValue is larger than maxValue.
     */
    public Int(T value)
            throws YangException {
        super(value);
        YangException.throwException(!valid(value.longValue()), this);
    }
    
    /**
     * Sets the MIN_VALUE and MAX_VALUE fields of this object.
     * 
     * @param minValue value to set MIN_VALUE to.
     * @param maxValue value to set MAX_VALUE to.
     * @throws YangException If minValue is larger than maxValue.
     */
    protected void setMinMax(Number minValue, Number maxValue)
            throws YangException {
        MIN_VALUE = TypeUtil.bigDecimalValueOf(minValue);
        MAX_VALUE = TypeUtil.bigDecimalValueOf(maxValue);
        if (MIN_VALUE == null || MAX_VALUE == null) {
            return;
        }
        YangException.throwException(MIN_VALUE.compareTo(MAX_VALUE) > 0, this);
    }

    /**
     * @param n A number to check for validity.
     * @return true if n is within this object's value domain; false otherwise.
     */
    protected boolean valid(Number n) {
        if (MIN_VALUE == null && MAX_VALUE == null) {
            return true;
        }
        BigDecimal bd = TypeUtil.bigDecimalValueOf(n);
        boolean res = true;
        if (MIN_VALUE != null) {
            res &= bd.compareTo(MIN_VALUE) >= 0;
        }
        if (MAX_VALUE != null) {
            res &= bd.compareTo(MAX_VALUE) <= 0;
        }
        return res;
    }
    
    /**
     * Checks that the value of this object is not null and valid. Called in 
     * constructors and value setters. Subclasses that have state invariants
     * in addition to those handled by the {@link Int#valid} method should
     * override this method and throw a YangException if such an invariant has
     * been violated.
     * 
     * @throws YangException If the value of this object is null or invalid.
     */
    @Override
    public void check() throws YangException {
        super.check();
        YangException.throwException(!valid(getValue()), this);
    }
    
    /**
     * Returns a value of type T given a String. No implementation should use
     * this.value - this method would be static if Java allowed for abstract
     * static classes.
     * 
     * @param s A string representation of a value of type T.
     * @return A T value parsed from s.
     * @throws NumberFormatException If unable to parse a value of type T.
     */
    protected abstract T parse(String s) throws NumberFormatException;
    
    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.YangType#fromString(java.lang.String)
     */
    @Override
    protected final T fromString(String s) throws YangException {
        try {
            return parse(s);
        } catch (NumberFormatException e) {
            throw new YangException(YangException.BAD_VALUE, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.netconfmanager.yang.YangType#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return (obj instanceof Int
                || obj instanceof Number);
    }

    /** ---------- Restrictions ---------- */

    /**
     * Checks that the value of this object equals another value.
     * 
     * @param value The value to compare against.
     * @throws YangException If comparison does not evaluate to true or if the
     *                        value argument is not {@link Int#valid}.
     */
    @Override
    protected void exact(int value) throws YangException {
        YangException.throwException(valid(value), this);
        super.exact(value);
    }

    /**
     * Checks that the value of this object is not smaller than the min-value.
     * 
     * @param min The min-value to compare against.
     * @throws YangException If value is smaller than min or min is invalid.
     */
    @Override
    protected void min(int min) throws YangException {
        YangException.throwException(valid(min), this);
        super.min(min);
    }

    /**
     * Checks that the value of this object is not larger than the max-value.
     * 
     * @param max The max-value to compare against.
     * @throws YangException If value is larger than max or max is invalid.
     */
    @Override
    protected void max(int max) throws YangException {
        YangException.throwException(valid(max), this);
        super.min(max);
    }

}

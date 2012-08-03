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
abstract class YangType<T> implements java.io.Serializable {

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
    public YangType() {
    }

    /**
     * Creates a YangType object from a String.
     * 
     * @param s The string.
     * @throws YangException If an invariant was broken during initialization,
     *                        or if value could not be parsed from s.
     */
    public YangType(String s) throws YangException {
        setValue(s);
    }

    /**
     * Creates a YangType object from a value of type T.
     * 
     * @param value The initial value of the new YangType object.
     * @throws YangException If an invariant was broken during initialization.
     */
    public YangType(T value) throws YangException {
        setValue(value);
    }

    /**
     * Sets the value of this object using a String.
     * 
     * @param s A string containing the new value to set.
     * @throws YangException If an invariant was broken during assignment, or
     *                        if value could not be parsed from s.
     */
    public void setValue(String s) throws YangException {
        s = Utils.wsCollapse(s);
        setValue(fromString(s));
    }

    /**
     * Sets the value of this object using a value of type T.
     * 
     * @param value The new value to set.
     * @throws YangException If an invariant was broken during assignment.
     */
    public void setValue(T value) throws YangException {
        assert !(value instanceof YangType): "Avoid circular value chain";
        this.value = value;
        check();
    }

    /**
     * @return The value of this object.
     */
    public T getValue() {
        return value;
    }

    /**
     * Checks that the value of this object is not null. Called in constructors
     * and value setters. Subclasses that have state invariants should extend
     * this method and throw a YangException if such an invariant has been
     * violated.
     * 
     * @throws YangException If the value of this object is null.
     */
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
     * Note: No implementation should use this.value - this method would be
     * static if Java allowed for abstract static classes.
     * 
     * @param s A string representation of a value of type T.
     * @return A T value parsed from s.
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
        if (obj instanceof YangType<?>) {
            YangType<?> other = (YangType<?>) obj;
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
    
    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj type is compatible; false otherwise.
     */
    public abstract boolean canEqual(Object obj);

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
        Utils.restrict(value, min, Utils.Operator.GR);
    }

    /**
     * Checks that the value of this object is not larger than the max-value.
     * 
     * @param max The max-value to compare against.
     * @throws YangException if value is larger than max.
     */
    protected void max(int max) throws YangException {
        Utils.restrict(value, max, Utils.Operator.LT);
    }

}

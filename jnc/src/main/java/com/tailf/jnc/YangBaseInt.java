package com.tailf.jnc;

import java.math.BigDecimal;

/**
 * Extended by implementations of the Integer, decimal64 and binary built-in
 * YANG data types.
 * 
 * @author emil@tail-f.com
 */
abstract class YangBaseInt<T extends Number> extends YangBaseType<T> {

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
     * Creates a YangBaseInt object from a String.
     * 
     * @param s The string.
     * @param minValue Lower bound for the value of this object.
     * @param maxValue Upper bound for the value of this object.
     * @throws YangException If an invariant was broken during initialization,
     *             if value could not be parsed from s, or if minValue is
     *             larger than maxValue.
     */
    public YangBaseInt(String s) throws YangException {
        super(s);
    }

    /**
     * Creates a YangBaseInt object from a value of type T.
     * 
     * @param value The initial value of the new YangBaseInt object.
     * @throws YangException If an invariant was broken during initialization,
     *             or if minValue is larger than maxValue.
     */
    public YangBaseInt(T value) throws YangException {
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
        MIN_VALUE = Utils.bigDecimalValueOf(minValue);
        MAX_VALUE = Utils.bigDecimalValueOf(maxValue);
        if (MIN_VALUE == null || MAX_VALUE == null) {
            return;
        }
        YangException
                .throwException(MIN_VALUE.compareTo(MAX_VALUE) > 0, this);
    }

    /**
     * @param n A number to check for validity.
     * @return true if n is within this object's value domain; false otherwise.
     */
    protected boolean valid(Number n) {
        if (MIN_VALUE == null && MAX_VALUE == null) {
            return true;
        }
        final BigDecimal bd = Utils.bigDecimalValueOf(n);
        boolean res = true;
        if (MIN_VALUE != null) {
            res = bd.compareTo(MIN_VALUE) >= 0;
        }
        if (MAX_VALUE != null) {
            res &= bd.compareTo(MAX_VALUE) <= 0;
        }
        return res;
    }

    /**
     * Checks that the value of this object is not null and valid. Called in
     * constructors and value setters. Subclasses that have state invariants in
     * addition to those handled by the {@link YangBaseInt#valid} method should
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
    protected abstract T decode(String s) throws NumberFormatException;

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.yang.YangType#fromString(java.lang.String)
     */
    @Override
    protected final T fromString(String s) throws YangException {
        try {
            return decode(s);
        } catch (final NumberFormatException e) {
            throw new YangException(YangException.BAD_VALUE, e);
        }
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * <p>
     * If a subclass of this class overrides the equals method, this method
     * should be overridden as well.
     * 
     * @param obj Object to compare type with.
     * @return true if obj type is compatible; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return (obj instanceof YangBaseInt);
    }

    /** ---------- Restrictions ---------- */

    /**
     * Checks that the value of this object equals another value.
     * 
     * @param value The value to compare against.
     * @throws YangException If comparison does not evaluate to true or if the
     *             value argument is not {@link YangBaseInt#valid}.
     */
    protected void exact(Number value) throws YangException {
        YangException.throwException(!valid(value), value);
        Utils.restrict(this.value, value, Utils.Operator.EQ);
    }

    /**
     * Checks that the value of this object is not smaller than the min-value.
     * 
     * @param min The min-value to compare against.
     * @throws YangException If value is smaller than min or min is invalid.
     */
    protected void min(Number min) throws YangException {
        YangException.throwException(!valid(min), min);
        Utils.restrict(value, min, Utils.Operator.GE);
    }

    /**
     * Checks that the value of this object is not larger than the max-value.
     * 
     * @param max The max-value to compare against.
     * @throws YangException If value is larger than max or max is invalid.
     */
    protected void max(Number max) throws YangException {
        YangException.throwException(!valid(max), max);
        Utils.restrict(value, max, Utils.Operator.LE);
    }

}

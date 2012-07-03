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

package com.tailf.confm.xs;
import com.tailf.confm.*;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class implements the "xs:integer" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 * Arbitrary big signed integer.
 * We do not support arbitrary big integers,
 * this integer is limited to 64 bits.
 *
 */
public class Integer implements Serializable {

    private long value;

    public Integer(java.lang.String value) {
        value = String.wsCollapse(value);
        this.value = java.lang.Long.parseLong(value);
        check();
    }

    public Integer(long value) {
        this.value = value;
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        this.value = java.lang.Long.parseLong(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(long value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Gets the value space.
     */
    public long getValue() {
        return value;
    }

    private void check() {
    }

    public java.lang.String toString() {
        return new java.lang.Long(value).toString();
    }

    public boolean equals(long value) {
        if (this.value == value) return true;
        else return false;
    }

    public boolean equals(Integer value) {
        return value.equals( this.value );
    }

    public boolean equals(Object value) {
        if (value instanceof Integer) {
            return ((Integer)value).equals(this.value);
        }
        return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:minInclusive defines a minimum value that can be reached.
     */
    protected void minInclusive(long restriction) throws ConfMException {
        throwException(value < restriction);
    }

    /**
     * xs:minExclusive defines a minimum value that cannot be reached.
     */
    protected void minExclusive(long restriction) throws ConfMException {
        throwException(value <= restriction);
    }

    /**
     * xs:maxExclusive defines a maximum value that cannot be reached.
     */
    protected void maxInclusive(long restriction) throws ConfMException {
        throwException(value > restriction);
    }

    /**
     * xs:maxExclusive defines a minimum value that cannot be reached.
     */
    protected void maxExclusive(long restriction) throws ConfMException {
        throwException(value >= restriction);
    }

    /**
     * xs:minLength defines a minimum length measured in number of characters
     * or bytes.
     */
    protected void minLength(int len) throws ConfMException {
        throwException( toString().length() < len);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of characters
     * or bytes.
     */
    protected void maxLength(int len) throws ConfMException {
        throwException( toString().length() > len);
    }

    /**
     * xs:fractionDigits
     */
    protected void fractionDigits(int digits)
        throws ConfMException {
        // compare against the lexical value representation
        throwException(Decimal.numFractionDigits(toString()) > digits);
    }

    /**
     * xs:totaDigits
     */
    protected void totalDigits(int digits)
        throws ConfMException {
        // compare against the lexical value representation
        throwException(Decimal.numTotalDigits(toString()) > digits);
    }

    /**
     * xs:enumeration
     */
    protected boolean enumeration(long value) {
        if (this.value == value) return true;
        else return false;
    }

    /**
     * xs:pattern
     */
    protected void pattern(java.lang.String regex) throws ConfMException {
        try {
            java.lang.String s = new java.lang.Long(value).toString();
            throwException(!Pattern.matches(regex, s));
        } catch (PatternSyntaxException e) {
            throwException( true, e );
        }
    }

    protected void pattern(java.lang.String[] regexes) throws ConfMException {
        try {
            java.lang.String s = new java.lang.Long(value).toString();

            for (int i = 0; i < regexes.length; i++)
                if (Pattern.matches(regexes[i], s))
                    return;

            throwException(true);
        } catch (PatternSyntaxException e) {
            throwException(true, e);
        }
    }

    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
        if (!v) return;
    throw new ConfMException(ConfMException.BAD_VALUE,this);
    }

    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object o) throws ConfMException {
        if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,o);
    }
}

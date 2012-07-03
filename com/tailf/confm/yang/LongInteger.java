/*    -*- Java -*-
 *
 *  Copyright 2010 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.yang;
import com.tailf.confm.*;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This is an abstract long integer class to be used by the integer
 * types  defined in the 'urn:ietf:params:xml:ns:yang:yang-types'
 * namespace, e.g. int64.
 */
abstract class LongInteger implements Serializable {
    private long value;

    /**
     * Constructor
     */
    public LongInteger(java.lang.String stringValue) {
        value = parseValue(stringValue);
        check();
    }

    private long parseValue(java.lang.String stringValue) {
        stringValue = com.tailf.confm.xs.String.wsCollapse(stringValue);
        return Long.parseLong(stringValue);
    }

    private void check() {
    }

    public LongInteger(long value) {
        this.value = value;
        check();
    }

    /**
     * Set a value
     */
    public void setValue(java.lang.String stringValue) throws ConfMException {
        value = parseValue(stringValue);
        check();
    }

    public void setValue(long value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Get a value
     */
    public long getValue() {
        return value;
    }

    /**
     * Convert to a string value
     */
    public java.lang.String toString() {
        return new Long(value).toString();
    }

    /**
     * Check for equality
     */
    public boolean equals(Object object) {
        if (object instanceof Integer) {
            return ((Integer)object).equals(this.value);
        }
        return false;
    }

    public boolean equals(long value) {
        if (this.value == value)
            return true;
        else
            return false;
    }

    /**
     * xs:minInclusive defines a minimum value that can be reached
     */
    protected void minInclusive(long value) throws ConfMException {
        throwException(this.value < value);
    }

    /**
     * xs:minExclusive defines a minimum value that cannot be reached
     */
    protected void minExclusive(long value) throws ConfMException {
        throwException(this.value <= value);
    }

    /**
     * xs:maxExclusive defines a maximum value that cannot be reached
     */
    protected void maxInclusive(long value) throws ConfMException {
        throwException(this.value > value);
    }

    /**
     * xs:maxExclusive defines a minimum value that cannot be reached
     */
    protected void maxExclusive(long value) throws ConfMException {
        throwException(this.value >= value);
    }

    /**
     * xs:minLength defines a minimum length measured in number of
     * characters or bytes
     */
    protected void minLength(int n) throws ConfMException {
        throwException(toString().length() < n);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of
     * characters or bytes
     */
    protected void maxLength(int n) throws ConfMException {
        throwException(toString().length() > n);
    }

    /**
     * xs:fractionDigits
     */
    /* FIXME
    protected void fractionDigits(int digits) throws ConfMException {
        throwException(
            com.tailf.confm.xs.Decimal.numFractionDigits(toString()) > digits);
    }
    */

    /**
     * xs:totalDigits
     */
    /* FIXME
    protected void totalDigits(int digits) throws ConfMException {
        throwException(
            com.tailf.confm.xs.Decimal.numTotalDigits(toString()) > digits);
    }
    */

    /**
     * xs:enumeration
     */
    protected boolean enumeration(long value) {
        if (this.value == value)
            return true;
        else
            return false;
    }

    /**
     * xs:pattern
     */
    protected void pattern(java.lang.String regex) throws ConfMException {
        try {
            java.lang.String s = new Long(value).toString();
            throwException(!Pattern.matches(regex, s));
        } catch (PatternSyntaxException e) {
            throwException(true, e);
        }
    }

    protected void pattern(java.lang.String[] regexes) throws ConfMException {
        try {
            java.lang.String s = new Long(value).toString();
            for (int i = 0; i < regexes.length; i++)
                if (Pattern.matches(regexes[i], s))
                    return;
            throwException(true);
        } catch (PatternSyntaxException e) {
            throwException(true, e);
        }
    }

    /**
     * Internal utilities
     */
    protected void throwException(boolean ok) throws ConfMException {
        if (!ok)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

    protected void throwException(boolean ok, Object object)
      throws ConfMException {
        if (!ok)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, object);
    }
}

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

/**
 * This class implements the "xs:float" datatype from the
 * 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 */
public class Float implements Serializable {

    private float value;

    public Float(java.lang.String value) {
        value = String.wsCollapse(value);
        this.value = java.lang.Float.parseFloat(value);
        check();
    }

    public Float(float value) {
        this.value = value;
        check();
    }

    public Float(double value) throws ConfMException {
        throwException(value > java.lang.Float.MAX_VALUE);
        throwException(value < java.lang.Float.MIN_VALUE);
        this.value = (float) value;
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        this.value = java.lang.Float.parseFloat(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(float value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Gets the value.
     */
    public float getValue() {
        return value;
    }

    private void check() {
    }

    public java.lang.String toString() {
        return new java.lang.Float(value).toString();
    }

    public boolean equals(float value) {
        return new java.lang.Float(value)
                .equals(new java.lang.Float(this.value));
    }

    public boolean equals(Float value) {
        return value.equals(this.value);
    }

    public boolean equals(Object value) {
        if (value instanceof Float)
            return ((Float) value).equals(this.value);
        return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:minInclusive defines a minimum value that can be reached.
     */
    protected void minInclusive(float restriction) throws ConfMException {
        throwException(value < restriction);
    }

    /**
     * xs:minExclusive defines a minimum value that cannot be reached.
     */
    protected void minExclusive(float restriction) throws ConfMException {
        throwException(value <= restriction);
    }

    /**
     * xs:maxExclusive defines a maximum value that cannot be reached.
     */
    protected void maxInclusive(float restriction) throws ConfMException {
        throwException(value > restriction);
    }

    /**
     * xs:maxExclusive defines a minimum value that cannot be reached.
     */
    protected void maxExclusive(float restriction) throws ConfMException {
        throwException(value >= restriction);
    }

    /**
     * xs:minLength defines a minimum length measured in number of characters or
     * bytes.
     */
    protected void minLength(int len) throws ConfMException {
        throwException(toString().length() < len);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of characters or
     * bytes.
     */
    protected void maxLength(int len) throws ConfMException {
        throwException(toString().length() > len);
    }

    /**
     * xs:fractionDigits
     */
    protected void fractionDigits(int digits) throws ConfMException {
        throwException(Decimal.numFractionDigits(toString()) > digits);
    }

    /**
     * xs:totalDigits
     */
    protected void totalDigits(int digits) throws ConfMException {
        throwException(Decimal.numTotalDigits(toString()) > digits);
    }

    /**
     * xs:enumeration
     */
    protected boolean enumeration(float value) {
        if (this.value == value)
            return true;
        else
            return false;
    }

    protected boolean enumeration(double value) {
        if (this.value == value)
            return true;
        else
            return false;
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

}

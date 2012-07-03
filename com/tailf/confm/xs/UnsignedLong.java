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
import java.math.BigInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class implements the "xs:unsignedLong" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 * Since unsigned 64 bit integers are not possible
 * to represent in Java, a BigInteger is used.
 */

public class UnsignedLong implements Serializable {

    private BigInteger value;
    public static java.lang.String maxValue = "18446744073709551615";

    public UnsignedLong(java.lang.String value) throws ConfMException {
        this.value = new BigInteger(value);
        check();
    }

    public UnsignedLong(long value) throws ConfMException {
        this.value = new BigInteger(value + "");
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        this.value = new BigInteger(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(long value) throws ConfMException {
        this.value = new BigInteger(value + "");
        check();
    }

    public void setValue(BigInteger value) throws ConfMException {
        this.value = value;
        check();
    }

    public BigInteger getValue() {
        return value;
    }

    public java.lang.String toString() {
        return value.toString();
    }

    private void check() throws ConfMException {
        if (this.value.compareTo(new BigInteger("0")) < 0) {
            throw new ConfMException(ConfMException.BAD_VALUE,this);
        }
        if (this.value.compareTo(new BigInteger(UnsignedLong.maxValue)) > 0) {
            throw new ConfMException(ConfMException.BAD_VALUE,this);
        }
    }

    public boolean equals(long value) {
        return (this.value.compareTo(
                    new BigInteger(value + "")) == 0);
    }

    public boolean equals(Object value) {
        if (value instanceof BigInteger) {
            return (((BigInteger)value).compareTo(this.value) == 0);
        }
        else if (value instanceof Long)
            return (this.value.compareTo(
                        new BigInteger(value + "")) == 0);
        else if (value instanceof Int)
            return (this.value.compareTo(
                        new BigInteger(value + "")) == 0);
        else if ( value instanceof UnsignedLong) {
            UnsignedLong l = (UnsignedLong)value;
            return (this.value.compareTo(l.getValue()) == 0);
        }
        else
            return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:minInclusive defines a minimum value that can be reached.
     */
    protected void minInclusive(long restriction)
        throws ConfMException {
        minInclusive(restriction + "");
    }
    protected void minInclusive(java.lang.String restriction)
        throws ConfMException {
        throwException(
            this.value.compareTo(new BigInteger(restriction)) < 0);
    }

    /**
     * xs:minExclusive defines a minimum value that cannot be reached.
     */
    protected void minExclusive(long restriction)
        throws ConfMException {
        minExclusive(restriction + "");
    }
    protected void minExclusive(java.lang.String restriction)
        throws ConfMException {
        int cmp = this.value.compareTo(new BigInteger(restriction));
        throwException(cmp == 0 || cmp < 0);
    }

    /**
     * xs:maxExclusive defines a maximum value that cannot be reached.
     */
    protected void maxInclusive(long restriction)
        throws ConfMException {
        maxInclusive(restriction + "");
    }
    protected void maxInclusive(java.lang.String restriction)
        throws ConfMException {
        throwException(
            this.value.compareTo(new BigInteger(restriction)) > 0);
    }

    /**
     * xs:maxExclusive defines a minimum value that cannot be reached.
     */
    protected void maxExclusive(long restriction)
        throws ConfMException {
        maxExclusive(restriction + "");
    }
    protected void maxExclusive(java.lang.String restriction)
        throws ConfMException {
        int cmp = this.value.compareTo(new BigInteger(restriction));
        throwException(cmp == 0 || cmp > 0);
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
     * xs:pattern
     */
    protected void pattern(java.lang.String regex) throws ConfMException {
        try {
            java.lang.String s = value.toString();
            throwException(!Pattern.matches(regex, s));
        } catch (PatternSyntaxException e) {
            throwException( true, e );
        }
    }

    protected void pattern(java.lang.String[] regexes) throws ConfMException {
        try {
            java.lang.String s = value.toString();

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
    protected void throwException(boolean v, Object o) throws ConfMException {
        if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,o);
    }

}

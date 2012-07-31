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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Implements the built-in YANG data type "decimal64".
 * 
 * @author emil@tail-f.com
 */
public class YangDecimal64 extends YangType<BigDecimal> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 4461074766110277807L;
    
    /**
     * The number of decimals allowed in value.
     * 
     * @serial
     */
    private int fractionDigits;
    
    /**
     * The largest value allowed.
     * 
     * @serial
     */
    private BigDecimal maxValue;

    /**
     * The smallest value allowed.
     * 
     * @serial
     */
    private BigDecimal minValue;

    /**
     * Creates a YangDecimal64 object from a String.
     * 
     * @param value The string.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     * @throws NumberFormatException If value is not a valid representation of
     *                               a java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    public YangDecimal64(String value, int fractionDigits)
            throws ConfMException {
        this(new BigDecimal(value), fractionDigits);
    }

    /**
     * Creates a YangDecimal64 object from a double.
     * 
     * @param value The double.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     * @throws NumberFormatException If val is infinite or NaN.
     * @see java.math.BigDecimal
     */
    public YangDecimal64(double value, int fractionDigits)
            throws ConfMException {
        setValue(new BigDecimal(value), fractionDigits);
    }

    /**
     * Creates a YangDecimal64 object from a BigDecimal.
     * 
     * @param value The BigDecimal.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     */
    public YangDecimal64(BigDecimal value, int fractionDigits)
            throws ConfMException {
        setValue(value, fractionDigits);
    }

    /**
     * Sets the value of this object using a String.
     * 
     * @param value The string.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     * @throws NumberFormatException If value is not a valid representation of
     *                               a java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    public void setValue(String value, int fractionDigits)
            throws ConfMException {
        setValue(new BigDecimal(value), fractionDigits);
    }

    /**
     * Sets the value of this object using a double.
     * 
     * @param value The double.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     * @throws NumberFormatException If value is not a valid representation of
     *                               a java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    public void setValue(double value, int fractionDigits)
            throws ConfMException {
        setValue(new BigDecimal(value), fractionDigits);
    }

    /**
     * Sets the value of this object using a BigDecimal.
     * 
     * @param value The BigDecimal.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     */
    public void setValue(BigDecimal value, int fractionDigits)
            throws ConfMException {
        BigDecimal pow63 = new BigDecimal("2.0").pow(63);
        maxValue = pow63.add(BigDecimal.ONE).movePointLeft(fractionDigits);
        minValue = pow63.negate().movePointLeft(fractionDigits);
        this.value = value;
        this.fractionDigits = fractionDigits;
        check();
    }

    /**
     * @return The fractionDigits value of this object.
     */
    public int getFractionDigits() {
        return fractionDigits;
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangType#toString()
     */
    @Override
    public String toString() {
        return value.toPlainString();
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangType#fromString(java.lang.String)
     */
    @Override
    protected BigDecimal fromString(String s) throws ConfMException {
        return new BigDecimal(s);
    }

    /**
     * Checks that the value of this object does not violate any invariants.
     * 
     * @throws ConfMException If fractionDigits is not in [1, 18] or if value
     *                        of this object is not in [minValue, maxValue].
     */
    @Override
    public void check() throws ConfMException {
        // Check that the fraction-digits arguments value is within bounds
        boolean withinBounds = fractionDigits < 1 || fractionDigits > 18;
        ConfMException.throwException(withinBounds, this);
        
        // Check that value is not too large or too small
        ConfMException.throwException(value.compareTo(maxValue) > 0, this);
        ConfMException.throwException(value.compareTo(minValue) < 0, this);
    }

    /**
     * Compares this object with an instance of BigDecimal for equality, taking
     * a fractionDigits integer argument into account.
     * 
     * @param bd The BigDecimal object to compare with.
     * @param fractionDigits Number of decimals allowed in value.
     * @return true if the value of this object is equal to the value of bd and
     *         the fractionDigits value of this object is equal to the 
     *         fractionDigits argument; false otherwise.
     */
    public boolean equals(BigDecimal bd, int fractionDigits) {
        boolean sameValue = value.compareTo(bd) == 0;
        boolean samefractionDigits = this.fractionDigits == fractionDigits;
        return sameValue && samefractionDigits;
    }

    /**
     * Compares this object with an instance of BigDecimal for equality.
     * 
     * @param bd The BigDecimal object to compare with.
     * @return true if the value of this object is equal to the value of bd;
     *         false otherwise.
     */
    public boolean equals(BigDecimal bd) {
        return equals(bd, fractionDigits);
    }

    /**
     * Compares this object with an instance of BigInteger for equality, taking
     * a fractionDigits integer argument into account.
     * 
     * @param bi The BigInteger object to compare with.
     * @param fractionDigits Number of decimals allowed in value.
     * @return true if the value of this object is equal to the value of bi and
     *         the fractionDigits value of this object is equal to the 
     *         fractionDigits argument; false otherwise.
     */
    public boolean equals(BigInteger bi, int fractionDigits) {
        return equals(new BigDecimal(bi), fractionDigits);
    }

    /**
     * Compares this object with an instance of BigInteger for equality.
     * 
     * @param bi The BigInteger object to compare with.
     * @return true if the value of this object is equal to the value of bi;
     *         false otherwise.
     */
    public boolean equals(BigInteger bi) {
        return equals(bi, fractionDigits);
    }

    /**
     * Compares this object with an instance of String for equality, taking
     * a fractionDigits integer argument into account.
     * 
     * @param s The String object to compare with.
     * @param fractionDigits Number of decimals allowed in value.
     * @return true if the value of this object is equal to the value of s and
     *         the fractionDigits value of this object is equal to the 
     *         fractionDigits argument; false otherwise.
     * @throws NumberFormatException If s is not a valid representation of a 
     *                               java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    public boolean equals(String s, int fractionDigits) {
        return equals(new BigDecimal(s), fractionDigits);
    }

    /**
     * Compares this object with an instance of String for equality.
     * 
     * @param s The String object to compare with.
     * @return true if the value of this object is equal to the value of s;
     *         false otherwise.
     * @throws NumberFormatException If s is not a valid representation of a 
     *                               java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    public boolean equals(String s) {
        return equals(s, fractionDigits);
    }

    /**
     * Compares this object with an instance of Double for equality, taking
     * a fractionDigits integer argument into account.
     * 
     * @param d The Double object to compare with.
     * @param fractionDigits Number of decimals allowed in value.
     * @return true if the value of this object is equal to the value of d and
     *         the fractionDigits value of this object is equal to the 
     *         fractionDigits argument; false otherwise.
     * @throws NumberFormatException If value of d is infinite or NaN.
     */
    public boolean equals(Number n, int fractionDigits) {
        return equals(new BigDecimal(n.doubleValue()), fractionDigits);
    }

    /**
     * Compares this object with an instance of Double for equality.
     * 
     * @param d The Double object to compare with.
     * @return true if the value of this object is equal to the value of d;
     *         false otherwise.
     * @throws NumberFormatException If value of d is infinite or NaN.
     */
    public boolean equals(Number n) {
        return equals(n, fractionDigits);
    }

    /**
     * Compares this object with an other instance of YangDecimal64 for
     * equality. The fractionDigits field is considered in the comparison.
     * 
     * @param yd64 The YangDecimal64 object to compare with
     * @return true if the value of this object is equal to the value of yd64;
     *         false otherwise.
     */
    public boolean equals(YangDecimal64 yd64) {
        return equals(yd64.getValue(), yd64.getFractionDigits());
    }

    /**
     * Compares this object with another object for equality. Comparisons are
     * only made if obj can be cast to a YangDecimal64, BigDecimal, BigInteger,
     * String or Double. All other objects are considered not equal.
     * <p>
     * If obj can be cast to a YangDecimal64, its fractionDigits field is
     * considered in the comparison.
     * 
     * @param obj The object to compare with
     * @return true if the value of this object is equal to the value of obj;
     *         false otherwise.
     * @throws NumberFormatException If obj can be cast to String but is not a
     *                               valid representation of a 
     *                               java.math.BigDecimal, or if obj can be
     *                               cast to Double but is infinite or NaN.
     * @see java.math.BigDecimal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof YangDecimal64) {
            return equals((YangDecimal64) obj);
        } else if (obj instanceof BigDecimal) {
            return equals((BigDecimal) obj);
        } else if (obj instanceof BigInteger) {
            return equals((BigInteger) obj);
        } else if (obj instanceof Number) {
            return equals((Number) obj);
        } else if (obj instanceof String) {
            return equals((String) obj);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangType#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return (obj instanceof YangDecimal64
                || obj instanceof Number
                || obj instanceof String);
    }

    /** ---------- Restrictions ---------- */

    /**
     * Checks that value of this object is exactly that of bd.
     * 
     * @param bd BigDecimal to check equality against.
     * @throws ConfMException If not equal.
     */
    protected void exactValue(BigDecimal bd) throws ConfMException {
        ConfMException.throwException(equals(bd), this);
    }

    /**
     * Checks that value of this object is not smaller than bd.
     * 
     * @param bd BigDecimal to compare against.
     * @throws ConfMException If bd is larger than value of this object.
     */
    protected void minValue(BigDecimal bd) throws ConfMException {
        ConfMException.throwException(value.compareTo(bd) < 0, this);
    }

    /**
     * Checks that value of this object is not larger than bd.
     * 
     * @param bd BigDecimal to compare against.
     * @throws ConfMException If bd is smaller than value of this object.
     */
    protected void maxValue(BigDecimal bd) throws ConfMException {
        ConfMException.throwException(value.compareTo(bd) > 0, this);
    }

}
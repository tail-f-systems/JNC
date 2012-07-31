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

/**
 * Implements the built-in YANG data type "decimal64".
 * 
 * @author emil@tail-f.com
 */
public class YangDecimal64 extends YangInt<BigDecimal> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 8486689004714954100L;
    
    /**
     * The number of decimals allowed in value.
     * 
     * @serial
     */
    private Integer fractionDigits = null;

    /**
     * Creates a YangDecimal64 object from a String.
     * 
     * @param s The string.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     * @throws NumberFormatException If value is not a valid representation of
     *                               a java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    public YangDecimal64(String s, int fractionDigits)
            throws ConfMException {
        super(s);
        this.fractionDigits = fractionDigits;
        setMinMax();
    }

    /**
     * Creates a YangDecimal64 object from a Number. 
     * 
     * @param n The Number to initialize the value of this object with.
     * @param fractionDigits Number of decimals allowed in value.
     * @throws ConfMException If n is too small or too large with regard to
     *                        the fractionDigits argument.
     */
    public YangDecimal64(Number n, int fractionDigits) throws ConfMException {
        super(YangTypeUtil.bigDecimalValueOf(n));
        this.fractionDigits = fractionDigits;
        setMinMax();
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
     * Sets the value of this object using a BigDecimal.
     * 
     * @param value The BigDecimal.
     * @param fractionDigits Number of decimals allowed.
     * @throws ConfMException If value is too small or too large with regard to
     *                        the fractionDigits argument.
     */
    public void setValue(Number n, int fractionDigits) throws ConfMException {
        BigDecimal value = YangTypeUtil.bigDecimalValueOf(n);
        super.setValue(value);
        this.fractionDigits = fractionDigits;
        setMinMax();
        check();
    }

    /**
     * Sets the MIN_VALUE and MAX_VALUE fields of this object.
     * 
     * @throws ConfMException If the fractionDigits field is not set.
     */
    private void setMinMax() throws ConfMException {
        ConfMException.throwException(fractionDigits == null, this);
        BigDecimal pow63 = new BigDecimal("2.0").pow(63);
        BigDecimal minValue = pow63.negate().movePointLeft(fractionDigits);
        pow63 = pow63.add(BigDecimal.ONE);
        BigDecimal maxValue = pow63.movePointLeft(fractionDigits);
        setMinMax(minValue, maxValue);
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
        
        // Check value bounds using parent check method
        super.check();
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.confm.YangInt#parse(java.lang.String)
     */
    @Override
    protected BigDecimal parse(String s) throws NumberFormatException {
        return new BigDecimal(s);
    }

}
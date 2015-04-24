package com.tailf.jnc;

import java.math.BigDecimal;

/**
 * Implements the built-in YANG data type "decimal64".
 * 
 * @author emil@tail-f.com
 */
public class YangDecimal64 extends YangBaseInt<BigDecimal> {

    private static final long serialVersionUID = 1L;

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
     * @param fractionDigits [1, 18], Number of decimals allowed in value.
     * @throws YangException If value is too small or too large with regard to
     *             the number of decimals.
     * @throws NumberFormatException If value is not a valid representation of
     *             a java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    public YangDecimal64(String s, int fractionDigits) throws YangException {
        super(s);
        this.fractionDigits = fractionDigits;
        setMinMax();
        check();
    }

    /**
     * Creates a YangDecimal64 object from a Number.
     * 
     * @param n The Number to initialize the value of this object with.
     * @param fractionDigits [1, 18], Number of decimals allowed in n.
     * @throws YangException If n is too small or too large with regard to the
     *             fractionDigits argument.
     */
    public YangDecimal64(Number n, int fractionDigits) throws YangException {
        super(Utils.bigDecimalValueOf(n));
        this.fractionDigits = fractionDigits;
        setMinMax();
    }

    /**
     * Sets the value of this object using a String.
     * 
     * @param value The string.
     * @throws YangException If value is too small or too large with regard to
     *             the fractionDigits of this YangDecimal.
     * @throws NumberFormatException If value is not a valid representation of
     *             a java.math.BigDecimal.
     * @see java.math.BigDecimal
     */
    @Override
    public void setValue(String value)
            throws YangException {
        setValue(new BigDecimal(value));
    }

    /**
     * Sets the value of this object using a Number.
     * 
     * @param n The Number.
     * @throws YangException If value is too small or too large with regard to
     *             the fractionDigits of this YangDecimal.
     */
    public void setValue(Number n) throws YangException {
        final BigDecimal value = Utils.bigDecimalValueOf(n);
        super.setValue(value);
        check();
    }

    /**
     * Sets the MIN_VALUE and MAX_VALUE fields of this object.
     * 
     * @throws YangException If the fractionDigits field is not set.
     */
    private void setMinMax() throws YangException {
        YangException.throwException(fractionDigits == null, this);
        BigDecimal pow63 = new BigDecimal("2.0").pow(63);
        final BigDecimal minValue = pow63.negate().movePointLeft(
                fractionDigits);
        pow63 = pow63.subtract(BigDecimal.ONE);
        final BigDecimal maxValue = pow63.movePointLeft(fractionDigits);
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
     * 
     * @see com.tailf.jnc.yang.YangType#toString()
     */
    @Override
    public String toString() {
        return value.toPlainString();
    }

    /**
     * Checks that the value of this object does not violate any invariants.
     * 
     * @throws YangException If fractionDigits is not in [1, 18] or if value of
     *             this object is not in [minValue, maxValue].
     */
    @Override
    public void check() throws YangException {
        // Check that the fraction-digits arguments value is within bounds
        final boolean outsideBounds = fractionDigits != null
                && (fractionDigits < 1 || fractionDigits > 18);
        YangException.throwException(outsideBounds, this);

        // Check value bounds using parent check method
        super.check();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected BigDecimal decode(String s) throws NumberFormatException {
        return new BigDecimal(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseInt#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangDecimal64;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof YangDecimal64) {
            final YangDecimal64 other = (YangDecimal64) obj;
            try {
                exact(other.getValue());
                return (fractionDigits.equals(other.fractionDigits) && other
                        .canEqual(this));
            } catch (final Exception e) {
            } // Different/null value, or can't equal
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#hashCode()
     */
    @Override
    public int hashCode() {
        final int hash = super.hashCode();
        return (fractionDigits == null) ? hash : (hash << fractionDigits);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangDecimal64 cloneShallow() throws YangException {
        return new YangDecimal64(toString(), fractionDigits);
    }

}
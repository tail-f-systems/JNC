package com.tailf.jnc;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Implements the built-in YANG data type "uint64".
 * 
 * @author emil@tail-f.com
 */
public class YangUInt64 extends YangBaseInt<BigInteger> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangUInt64 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s or if it is
     *             negative or larger than 18446744073709551615.
     */
    public YangUInt64(String s) throws YangException {
        super(s);
        setMinMax(0, new BigInteger("18446744073709551615"));
        check();
    }

    /**
     * Creates a YangUInt64 object from a Number.
     * 
     * @param n The initial value of the new YangUInt64 object.
     * @throws YangException If value is negative, larger than
     *             18446744073709551615 or rounding is necessary.
     */
    public YangUInt64(Number n) throws YangException {
        this(n.toString());
    }

    /**
     * Sets the value of this object using a Number.
     * 
     * @param n The new value to set.
     * @throws YangException If an invariant was broken during assignment or if
     *             the number has a non-zero fractional part.
     */
    public void setValue(Number n) throws YangException {
        try {
            super.setValue(Utils.bigDecimalValueOf(n).toBigIntegerExact());
        } catch (final ArithmeticException e) {
            YangException.throwException(true, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseInt#decode(java.lang.String)
     */
    @Override
    protected BigInteger decode(String s) throws NumberFormatException {
        try {
            return new BigDecimal(s).toBigIntegerExact();
        } catch (final ArithmeticException e) {
            throw new NumberFormatException(e.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangUInt64 cloneShallow() throws YangException {
        return new YangUInt64(toString());
    }

}
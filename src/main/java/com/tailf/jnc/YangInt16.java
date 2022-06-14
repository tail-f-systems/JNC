package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "int16".
 * 
 * @author emil@tail-f.com
 */
public class YangInt16 extends YangBaseInt<Short> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangInt16 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s.
     */
    public YangInt16(String s) throws YangException {
        super(s);
        setMinMax(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    /**
     * Creates a YangInt16 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt16 object.
     * @throws YangException If value does not fit in 16 bits.
     */
    public YangInt16(Number value) throws YangException {
        super(value.shortValue());
        setMinMax(Short.MIN_VALUE, Short.MAX_VALUE);
        if (!(value instanceof Short)) {
            YangException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected Short decode(String s) throws NumberFormatException {
        return Short.decode(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangInt16 cloneShallow() throws YangException {
        return new YangInt16(toString());
    }

}
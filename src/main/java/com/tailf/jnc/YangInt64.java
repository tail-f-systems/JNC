package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "int64".
 * 
 * @author emil@tail-f.com
 */
public class YangInt64 extends YangBaseInt<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangInt64 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s.
     */
    public YangInt64(String s) throws YangException {
        super(s);
        setMinMax(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Creates a YangInt64 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangInt64 object.
     * @throws YangException If value does not fit in 8 bits.
     */
    public YangInt64(Number value) throws YangException {
        super(value.longValue());
        setMinMax(Long.MIN_VALUE, Long.MAX_VALUE);
        if (!(value instanceof Long)) {
            YangException.throwException(!valid(value), this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.yang.YangInt#parse(java.lang.String)
     */
    @Override
    protected Long decode(String s) throws NumberFormatException {
        return Long.decode(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangInt64 cloneShallow() throws YangException {
        return new YangInt64(toString());
    }

}
package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "uint8".
 * 
 * @author emil@tail-f.com
 */
public class YangUInt8 extends YangInt16 {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangUInt8 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s or if the
     *             parsed value is negative or larger than 0xff.
     */
    public YangUInt8(String s) throws YangException {
        super(s);
        setMinMax(0, 0xff);
        check();
    }

    /**
     * Creates a YangUInt8 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param value The initial value of the new YangUInt8 object.
     * @throws YangException If value is negative or larger than 0xff.
     */
    public YangUInt8(Number value) throws YangException {
        super(value);
        setMinMax(0, 0xff);
        check();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangUInt8 cloneShallow() throws YangException {
        return new YangUInt8(toString());
    }

}
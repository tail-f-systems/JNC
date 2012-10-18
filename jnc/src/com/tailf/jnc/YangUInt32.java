package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "uint32".
 * 
 * @author emil@tail-f.com
 */
public class YangUInt32 extends YangInt64 {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangUInt32 object from a String.
     * 
     * @param s The string.
     * @throws YangException If value could not be parsed from s or if it is
     *             negative or larger than 0xffffffffL.
     */
    public YangUInt32(String s) throws YangException {
        super(s);
        setMinMax(0L, 0xffffffffL);
        check();
    }

    /**
     * Creates a YangUInt32 object from a Number. This may involve rounding or
     * truncation.
     * 
     * @param n The initial value of the new YangUInt32 object.
     * @throws YangException If value is negative or larger than 0xffffffffL.
     */
    public YangUInt32(Number n) throws YangException {
        super(n);
        setMinMax(0L, 0xffffffffL);
        check();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangUInt32 cloneShallow() throws YangException {
        return new YangUInt32(toString());
    }

}
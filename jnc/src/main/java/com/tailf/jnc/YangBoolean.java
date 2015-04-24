package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "boolean".
 * 
 * @author emil@tail-f.com
 */
public class YangBoolean extends YangBaseType<Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangBoolean object from a String.
     * 
     * @param s The string.
     * @throws YangException If value is not one of "true" or "false".
     */
    public YangBoolean(String s) throws YangException {
        super(s);
    }

    /**
     * Creates a YangBoolean object from a boolean.
     * 
     * @param b The boolean to set the value of the new YangBoolean to.
     * @throws YangException Never.
     */
    public YangBoolean(boolean b) throws YangException {
        super(b);
    }

    /**
     * Works much like Boolean.parseBoolean, except that case matters, s is
     * trimmed with wsCollapse prior to parsing, and an exception is thrown if
     * the trimmed string is neither "true" nor "false".
     * 
     * @param s The String.
     * @return true if s matches " *true *", false if s matches " *false *".
     * @throws YangException if s does not match a valid boolean value
     */
    @Override
    protected Boolean fromString(String s) throws YangException {
        s = Utils.wsCollapse(s);
        if (s.equals("true")) {
            return true;
        } else if (s.equals("false")) {
            return false;
        } else {
            throw new YangException(YangException.BAD_VALUE, this);
        }
    }

    /**
     * Nop method provided because this class extends the YangBaseType class.
     */
    @Override
    public void check() throws YangException {
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangBoolean or java.lang.Boolean;
     *         false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangBoolean;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangBoolean cloneShallow() throws YangException {
        return new YangBoolean(toString());
    }

}

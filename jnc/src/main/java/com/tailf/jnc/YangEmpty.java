package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "empty".
 * 
 * @author emil@tail-f.com
 */
public class YangEmpty implements YangType<YangEmpty> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangEmpty object.
     */
    public YangEmpty() {
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public YangEmpty clone() {
        return new YangEmpty();
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangType#check()
     */
    @Override
    public void check() throws YangException {
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangEmpty; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangEmpty;
    }

    /**
     * This method doesn't do anything, but is part of the interface
     * 
     * @param s ignored
     * @throws YangException always, since this object is immutable
     */
    @Override
    public void setValue(String s) throws YangException {
        // throw new YangException(YangException.BAD_VALUE, s);
    }

    /**
     * This method doesn't do anything, but is part of the interface
     * 
     * @param value ignored
     * @throws YangException always, since this object is immutable
     */
    @Override
    public void setValue(YangEmpty value) throws YangException {
        // throw new YangException(YangException.BAD_VALUE, value);
    }

    /**
     * @return this object, since an empty leaf is it's own value.
     */
    @Override
    public YangEmpty getValue() {
        return this;
    }
    
    /**
     * @return An empty string.
     */
    @Override
    public String toString() {
        return "";
    }

}

package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "string".
 * 
 * @author emil@tail-f.com
 */
public class YangString extends YangBaseString {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangString object from a java.lang.String.
     * 
     * @param value The Java String.
     * @throws YangException If an invariant was broken during assignment.
     */
    public YangString(String value) throws YangException {
        super(value);
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangString; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangString || obj instanceof String;
    }

}

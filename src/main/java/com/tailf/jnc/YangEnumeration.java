package com.tailf.jnc;

import java.util.Arrays;

/**
 * Implements the built-in YANG data type "enumeration".
 * <p>
 * An enumeration checker method is provided.
 * 
 * @author emil@tail-f.com
 */
public class YangEnumeration extends YangBaseString {

    private static final long serialVersionUID = 1L;

    /**
     * An array of the allowed names, ordered as in the YANG module.
     */
    private String[] enums;
    
    /**
     * Get the allowed type names for this enumeration.
     *
     * @return A string array with the enum names
     */
    protected String[] enums() {
        return enums;
    };

    /**
     * Creates an YangEnumeration object given an enum (as a String) and an
     * array of the allowed enum names.
     * 
     * @param value The enum name
     * @param enums The allowed type names of the enumeration.
     * @throws YangException If an invariant was broken during assignment.
     */
    public YangEnumeration(String value, String[] enums) throws YangException {
        super(value);
        if (value.isEmpty()) {
            YangException.throwException(true, "empty string in enum value");
        } else if (enums == null || enums.length == 0) {
            YangException.throwException(true, "no enum names provided");
        }
        pattern("[^ ]|[^ ].*[^ ]");  // Leading and trailing spaces not allowed
        this.enums = enums;
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangBaseType#check()
     */
    @Override
    public void check() throws YangException {
        if (enums == null) {
            return;  // Premature check
        }
        super.check();
        boolean found = false;
        for (String enumName : enums) {
            found |= value.equals(enumName);
        }
        YangException.throwException(!found, "\"" + value + "\" not valid" +
        		"enum name");
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangEnumeration or
     *         java.lang.String; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangEnumeration;
    }
    
    /**
     * Compares this enumeration with another object for equality.
     * 
     * @param obj The object to compare with.
     * @return true if obj is an enumeration with same value and enum names;
     *         false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return (canEqual(obj)
                && java.util.Arrays.equals(enums, ((YangEnumeration)obj).enums)
                && super.equals(obj));
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangBaseType#hashCode()
     */
    @Override
    public int hashCode() {
        final int hash = super.hashCode();
        return (enums == null) ? hash : (hash + Arrays.hashCode(enums));
    }
    
    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangBaseString#cloneShallow()
     */
    @Override
    protected YangEnumeration cloneShallow() throws YangException {
        return new YangEnumeration(value, enums);
    }

}

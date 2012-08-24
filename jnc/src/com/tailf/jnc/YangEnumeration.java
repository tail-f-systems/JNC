/*    -*- Java -*-
 *
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.jnc;

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
     * Creates an YangEnumeration object from a java.lang.String.
     * 
     * @param value The Java String.
     * @throws YangException If an invariant was broken during assignment.
     */
    public YangEnumeration(String value) throws YangException {
        super(value);
        if (value.isEmpty()) {
            YangException.throwException(true, "empty string");
        }
        pattern("[^ ]|[^ ].*[^ ]");
    }

    /**
     * Checks if value is equal to this object's value, interpreted as an enum.
     * 
     * @param value An enum value candidate, as a String.
     * @return true if value of this object is equal to value; false otherwise.
     */
    protected boolean enumeration(String value) {
        return this.value.equals(value);
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

}
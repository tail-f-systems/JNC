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

package com.tailf.confm.yang;

/**
 * Implements the built-in YANG data type "enumeration".
 * <p>
 * White space collapse and replace methods, regexp pattern matchers, an
 * enumeration checker method and length assertion methods are provided.
 * 
 * @author emil@tail-f.com
 */
public class Enumeration extends JString {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -2361951707621016957L;

    /**
     * Creates a YangString object from a java.lang.String.
     * 
     * @param value The Java String.
     */
    public Enumeration(String value) {
        super(value);
    }

    /**
     * Checks if value is equal to this object's value, interpreted as an enum.
     * 
     * @param value An enum value candidate, as a String.
     * @return true if value of this object is equal to value; false otherwise.
     */
    protected boolean enumeration(String value) {
        return equals(value);
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of Enumeration or java.lang.String;
     *         false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof Enumeration || obj instanceof String;
    }

}
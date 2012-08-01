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

package com.tailf.netconfmanager.yang.type;

/**
 * Implements the built-in YANG data type "enumeration".
 * <p>
 * An enumeration checker method is provided.
 * 
 * @author emil@tail-f.com
 */
public class Enumeration extends BaseString {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an Enumeration object from a java.lang.String.
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
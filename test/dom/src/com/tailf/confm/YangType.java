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

package com.tailf.confm;

import java.io.Serializable;

/**
 * Represents a built-in YANG data type.
 * 
 * @author emil@tail-f.com
 */
public abstract class YangType<T extends Comparable<T>> implements Serializable {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 1283676367920670186L;

    /**
     * The value of this object, of which this class is a wrapper for.
     * 
     * @serial
     */
    protected T value;
    
    /**
     * Empty constructor for a YangType object. The value will not be
     * initialized when calling this method.
     */
    public YangType() {
    }
    
    /**
     * Creates a YangType object from a String.
     * 
     * @param s The string.
     * @throws ConfMException If an invariant was broken during initialization.
     * @throws IllegalArgumentException if value could not be parsed from s.
     */
    public YangType(String s) 
            throws ConfMException, IllegalArgumentException {
        setValue(s);
    }

    /**
     * Creates a YangType object from a value of type T.
     * 
     * @param value The initial value of the new YangType object.
     * @throws ConfMException If an invariant was broken during initialization.
     */
    public YangType(T value) throws ConfMException {
        setValue(value);
    }

    /**
     * Sets the value of this object using a String.
     * 
     * @param s A string containing the new value to set.
     * @throws ConfMException If an invariant was broken during assignment.
     * @throws IllegalArgumentException if value could not be parsed from s.
     */
    public void setValue(String s) 
            throws ConfMException, IllegalArgumentException {
        s = wsCollapse(s);
        setValue(fromString(s));
    }

    /**
     * Sets the value of this object using a value of type T.
     * 
     * @param value The new value to set.
     * @throws ConfMException If an invariant was broken during assignment.
     */
    public void setValue(T value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * @return The value of this object.
     */
    public T getValue() {
        return value;
    }

    /**
     * Called in constructors and value setters.
     * 
     * @throws ConfMException If an invariant has been violated by assigning to
     *                        the value of this object.
     */
    public abstract void check() throws ConfMException;

    /**
     * @return The value of this object, as a String.
     */
    @Override
    public abstract String toString();

    /**
     * @param s A string representation of a value of type T.
     * @throws NumberFormatException If s does not contain a parsable T.
     * @return A T value parsed from s.
     */
    protected abstract T fromString(String s);

    /**
     * Compares this object with an other instance of YangType for equality.
     * 
     * @param yt The YangType object to compare with.
     * @return true if the value of this object is equal to the value of yt;
     *         false otherwise.
     */
    public boolean equals(YangType<T> yt) {
        return equals(yt.getValue());
    }

    /**
     * Compares this object with another object for equality.
     * 
     * @param obj The object to compare with
     * @return true if obj can be cast to a comparable class and the value of
     *         this object is equal to the value of obj; false otherwise.
     */
    @Override
    public abstract boolean equals(Object obj);

    /** ---------- Restrictions ---------- */

    /**
     * Checks that the value of this object is not smaller than the min-value.
     * 
     * @param min The min-value to compare against.
     * @throws ConfMException if value is smaller than min.
     * @throws ClassCastException If type of min prevents it from being
     *                            compared to the value of this object.
     */
    protected void min(T min) throws ConfMException {
        ConfMException.throwException(value.compareTo(min) > 0, this);
    }

    /**
     * Checks that the value of this object is not larger than the max-value.
     * 
     * @param max The max-value to compare against.
     * @throws ConfMException if value is larger than max.
     * @throws ClassCastException If type of max prevents it from being
     *                            compared to the value of this object.
     */
    protected void max(T max) throws ConfMException {
        ConfMException.throwException(value.compareTo(max) < 0, this);
    }
    
    /* ---------- static methods ---------- */

    /**
     * Whitespace collapse. Contiguous sequences of 0x20 are collapsed into a
     * single #x20, and initial and/or final #x20s are deleted.
     * <p>
     * This method is used by most other data types to collapse Strings from
     * the XML parser.
     *
     * @param value The string to collapse.
     * @return The collapsed string.
     */
    public static String wsCollapse(String value) {
        // Collapse multiple spaces into single spaces
        String res = value.replaceAll(" +", " ");
        
        // Remove any leading and/or trailing space
        int startOffset = res.startsWith(" ") ? 1 : 0;
        int stopOffset = res.length() > 1 && res.endsWith(" ") ? -1 : 0;
        return res.substring(startOffset, res.length() + stopOffset);
    }

    /**
     * Whitespace replace. Replaces whitespaces with spaces.
     *
     * @param value The String to replace whitespaces in.
     * @return a copy of value with all characters matching "[\t\n\r]"
     *         replaced by " " (a blank).
     */
    public static String wsReplace(String value) {
        return value.replaceAll("[\t\n\r]", " ");
    }

}

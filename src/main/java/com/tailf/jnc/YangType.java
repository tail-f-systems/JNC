package com.tailf.jnc;

public interface YangType<T> extends java.io.Serializable {

    /**
     * Sets the value of this object using a String.
     * 
     * @param s A string containing the new value to set.
     * @throws YangException If an invariant was broken during assignment, or
     *             if value could not be parsed from s.
     */
    void setValue(String s) throws YangException;

    /**
     * Sets the value of this object using a value of type T.
     * 
     * @param value The new value to set.
     * @throws YangException If an invariant was broken during assignment.
     */
    void setValue(T value) throws YangException;

    /**
     * @return The value of this object.
     */
    T getValue();

    /**
     * Checks that the value of this object is not null. Called in constructors
     * and value setters. Subclasses that have state invariants should extend
     * this method and throw a YangException if such an invariant has been
     * violated.
     * 
     * @throws YangException If the value of this object is null.
     */
    void check() throws YangException;

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj type is compatible; false otherwise.
     */
    boolean canEqual(Object obj);

    /**
     * Clones this object and its value.
     * 
     * @return A deep clone of this object.
     */
    YangType<T> clone();

}

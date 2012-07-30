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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implements the built-in YANG data type "string".
 * <p>
 * White space collapse and replace methods, regexp pattern matchers, an
 * enumeration checker method and length assertion methods are provided.
 * 
 * @author emil@tail-f.com
 */
public class YangString extends YangType<String> {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -7382018276731616249L;

    /**
     * Creates a YangString object from a java.lang.String.
     * 
     * @param value The Java String.
     */
    public YangString(String value) {
        setValue(value);
    }

    /**
     * Sets the value of this object using a java.lang.String.
     * 
     * @param value The Java String.
     */
    @Override
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return The value of this object, as a java.lang.String.
     */
    @Override
    public String toString() {
        return getValue();
    }

    /**
     * Identity method provided because this class extends the YangType class.
     * 
     * @param s A string.
     * @return s.
     */
    @Override
    protected String fromString(String s) {
        return s;
    }

    /**
     * Nop method provided because this class extends the YangType class.
     */
    @Override
    public void check() {
    }

    /**
     * Compares this object with a java.lang.String for equality.
     * 
     * @param s The java.lang.String object to compare with.
     * @return true if value of this object is equal to s; false otherwise.
     */
    public boolean equals(String s) {
        return value.equals(s);
    }

    /**
     * Compares this object with another object for equality.
     * 
     * @param obj The object to compare with
     * @return false, since any object which can be equal to this object should
     *         call either the equals(String) method or the 
     *         equals(YangType<String>) method.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return equals((String) obj);
        }
        assert !canEqual(obj): "obj: " + obj.getClass() + obj;
        return false;
    }

    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangString || obj instanceof String;
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

    /* ---------- Restrictions ---------- */

    /**
     * Checks that a regular expression matches the value of this object.
     * 
     * @param regex The regular expression.
     * @throws ConfMException If regexp has a syntax error or does not match.
     */
    protected void pattern(String regex) throws ConfMException {
        pattern(new String[] {regex});
    }

    /**
     * Checks that a set of regular expressions match the value of this object.
     * 
     * @param regexes The regular expressions.
     * @throws ConfMException If any regexp in regexes has a syntax error or
     *         does not match.
     */
    protected void pattern(String[] regexes) throws ConfMException {
        Object opaqueData = this;
        boolean matches = true;
        try {
            for (int i = 0; i < regexes.length; i++)
                if (!(matches = Pattern.matches(regexes[i], value)))
                    break;
        } catch (PatternSyntaxException e) {
            opaqueData = e;
            matches = false;
        }
        ConfMException.throwException(!matches, opaqueData);
    }

    /**
     * Checks that the value of this object has specified length.
     * 
     * @param len The length to check for.
     * @throws ConfMException if value does not have len.
     */
    protected void length(int len) throws ConfMException {
        ConfMException.throwException(value.length() != len, this);
    }

    /**
     * Checks that the value of this object is longer than specified length.
     * 
     * @param len The length to compare against.
     * @throws ConfMException if value is not longer than len.
     */
    protected void min(int len) throws ConfMException {
        ConfMException.throwException(value.length() < len, this);
    }

    /**
     * Checks that the value of this object is shorter than specified length.
     * 
     * @param len The length to compare against.
     * @throws ConfMException if value is not shorter than len.
     */
    protected void max(int len) throws ConfMException {
        ConfMException.throwException(value.length() > len, this);
    }

    /**
     * Raises a ConfMException regardless of what min is, since the min value
     * of a string is generally undefined.
     * 
     * @param min Ignored.
     * @throws ConfMException always.
     */
    @Override
    protected void min(String min) throws ConfMException {
        ConfMException.throwException(true, this);
    }

    /**
     * Raises a ConfMException regardless of what max is, since the max value
     * of a string is generally undefined.
     * 
     * @param max Ignored.
     * @throws ConfMException always.
     */
    @Override
    protected void max(String max) throws ConfMException {
        ConfMException.throwException(true, this);
    }

    /**
     * Whitespace replace. Replaces all occurrences of #x9 (tab), #xA (line
     * feed), and #xD (CR) with #x20 (space).
     */
    protected void wsReplace() {
        value = wsReplace(value);
    }

    /**
     * Whitespace replace. Contiguous sequences of 0x20 are collapsed into a
     * single 0x20, and initial and/or final 0x20s are deleted.
     */
    protected void wsCollapse() {
        value = wsCollapse(value);
    }

}
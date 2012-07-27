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
public class YangString implements Serializable {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -4524001592063916576L;

    /**
     * The value of this object, of which this class is a wrapper for
     * 
     * @serial
     */
    private String value;

    /**
     * Creates a YangString object from a java.lang.String
     * 
     * @param value The Java String
     */
    public YangString(String value) {
        this.value = value;
    }

    /**
     * Sets the value of this object using a java.lang.String
     * 
     * @param value The Java String
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return The value of this object
     */
    public String getValue() {
        return value;
    }

    /**
     * @return The value of this object, as a java.lang.String
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Compares this object with a java.lang.String for equality
     * 
     * @param s The java.lang.String object to compare with
     * @return true if value of this object is equal to s; false otherwise
     */
    public boolean equals(String s) {
        return value.equals(s);
    }

    /**
     * Compares this object with an other instance of YangString for equality
     * 
     * @param ys The YangString object to compare with
     * @return true if the value of this object is equal to the value of ys;
     *         false otherwise
     */
    public boolean equals(com.tailf.confm.YangString ys) {
        return equals(ys.value);
    }

    /**
     * Compares this object with another object for equality
     * 
     * @param obj The object to compare with
     * @return true if obj can be cast to a YangString or a java.lang.String
     *         and the value of this object is equal to the value of obj;
     *         false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof com.tailf.confm.YangString) {
            return equals((com.tailf.confm.YangString) obj);
        } else if (obj instanceof String) {
            return equals((String) obj);
        }
        return false;
    }

    /* ---------- Restrictions ---------- */

    /**
     * Checks that a regular expression matches the value of this object
     * 
     * @param regex The regular expression
     * @throws ConfMException If regexp has a syntax error or does not match
     */
    protected void pattern(String regex) throws ConfMException {
        String[] regexes = {regex};
        pattern(regexes);
    }

    /**
     * Checks that a set of regular expressions match the value of this object
     * 
     * @param regexes The regular expressions
     * @throws ConfMException If any regexp in regexes has a syntax error or
     *         does not match
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
     * Checks that the value of this object has specified length
     * 
     * @param len The length to check for
     * @throws ConfMException if value does not have len
     */
    protected void length(int len) throws ConfMException {
        ConfMException.throwException(value.length() != len, this);
    }

    /**
     * Checks that the value of this object is longer than specified length
     * 
     * @param len The length to compare against
     * @throws ConfMException if value is not longer than len
     */
    protected void minLength(int len) throws ConfMException {
        ConfMException.throwException(value.length() < len, this);
    }

    /**
     * Checks that the value of this object is shorter than specified length
     * 
     * @param len The length to compare against
     * @throws ConfMException if value is not shorter than len
     */
    protected void maxLength(int len) throws ConfMException {
        ConfMException.throwException(value.length() > len, this);
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

    /**
     * Checks if value is equal to this object's value, interpreted as an enum
     * 
     * @param value An enum value candidate, as a String
     * @return true if value of this object is equal to value; false otherwise
     */
    protected boolean enumeration(String value) {
        return this.value.equals(value);
    }
    
    /* ---------- static methods ---------- */

    /**
     * Whitespace collapse. Contiguous sequences of 0x20 are collapsed into a
     * single #x20, and initial and/or final #x20s are deleted.
     * <p>
     * This method is used by most other data types to collapse Strings from
     * the XML parser.
     *
     * @param value The string to collapse
     * @return The collapsed string
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

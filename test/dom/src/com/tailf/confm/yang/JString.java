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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.tailf.confm.ConfMException;

/**
 * Implements the built-in YANG data type "string".
 * <p>
 * White space collapse and replace methods, regexp pattern matchers, an
 * enumeration checker method and length assertion methods are provided.
 * 
 * @author emil@tail-f.com
 */
public class JString extends Type<String> {

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
    public JString(String value) {
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
    public void check() throws ConfMException {
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangString or java.lang.String;
     *         false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof JString || obj instanceof String;
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
     * Whitespace replace. Replaces all occurrences of #x9 (tab), #xA (line
     * feed), and #xD (CR) with #x20 (space).
     */
    protected void wsReplace() {
        value = TypeUtil.wsReplace(value);
    }

    /**
     * Whitespace replace. Contiguous sequences of 0x20 are collapsed into a
     * single 0x20, and initial and/or final 0x20s are deleted.
     */
    protected void wsCollapse() {
        value = TypeUtil.wsCollapse(value);
    }

}
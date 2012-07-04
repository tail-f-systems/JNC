/*    -*- Java -*-
 *
 *  Copyright 2007 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.xs;

import com.tailf.confm.*;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class implements the "xs:string" datatype from the
 * 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 */
public class String implements Serializable {

    private java.lang.String value;

    public String(java.lang.String value) {
        this.value = value;
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Gets the value.
     */
    public java.lang.String getValue() {
        return value;
    }

    private void check() {
    }

    public java.lang.String toString() {
        return value;
    }

    public boolean equals(java.lang.String b) {
        return value.equals(b);
    }

    public boolean equals(com.tailf.confm.xs.String b) {
        return value.equals(b.value);
    }

    public boolean equals(Object b) {
        if (b instanceof com.tailf.confm.xs.String) {
            return equals((com.tailf.confm.xs.String) b);
        }
        return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:pattern
     */
    protected void pattern(java.lang.String regex) throws ConfMException {
        try {
            throwException(!Pattern.matches(regex, value));
        } catch (PatternSyntaxException e) {
            throwException(true, e);
        }
    }

    protected void pattern(java.lang.String[] regexes) throws ConfMException {
        try {
            for (int i = 0; i < regexes.length; i++)
                if (Pattern.matches(regexes[i], value))
                    return;

            throwException(true);
        } catch (PatternSyntaxException e) {
            throwException(true, e);
        }
    }

    /**
     * xs:length
     */
    protected void length(int len) throws ConfMException {
        throwException(value.length() != len);
    }

    /**
     * xs:minLength defines a minimum length measured in number of characters or
     * bytes.
     */
    protected void minLength(int len) throws ConfMException {
        throwException(value.length() < len);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of characters or
     * bytes.
     */
    protected void maxLength(int len) throws ConfMException {
        throwException(value.length() > len);
    }

    /**
     * xs:whiteSpace replace. Replaces all occurances of #x9 (tab), #xA (line
     * feed), and #xD (CR) with #x20 (space).
     */
    protected void wsReplace() {
        value = wsReplace(value);
    }

    /**
     * xs:whiteSpace collapse. Contiguous sequences of 0x20 are collapsed into a
     * single #x20, and initial and/or final #x20s are deleted.
     */
    protected void wsCollapse() {
        value = wsCollapse(value);
    }

    /**
     * xs:enumeration
     */
    protected boolean enumeration(java.lang.String value) {
        if (this.value.equals(value))
            return true;
        else
            return false;
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object o) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, o);
    }

    /** ---------- package private --------- */

    /**
     * xs:whiteSpace replace. Replaces all occurances of #x9 (tab), #xA (line
     * feed), and #xD (CR) with #x20 (space).
     */
    static java.lang.String wsReplace(java.lang.String value) {
        byte[] s = value.getBytes();
        byte[] r = new byte[s.length];
        int j = 0;
        for (int i = 0; i < s.length; i++) {
            switch (s[i]) {
            case 0xA:
            case 0xD:
            case 0x9:
                r[j++] = 0x20;
                break;
            default:
                r[j++] = s[i];
            }
        }
        return new java.lang.String(r, 0, j);
    }

    /**
     * xs:whiteSpace collapse. Contiguous sequences of 0x20 are collapsed into a
     * single #x20, and initial and/or final #x20s are deleted.
     * <p>
     * This method is used by most all other data types to collapse Strings that
     * come from the XML parser.
     */
    public static java.lang.String wsCollapse(java.lang.String value) {
        // collapse will always do replace first
        value = wsReplace(value);
        byte[] s = value.getBytes();

        // remove 0x20 at beginning and end
        int i = 0;
        while (i < s.length && s[i] == 0x20)
            i++;
        int n = s.length - 1;
        while (n >= 0 && s[n] == 0x20)
            n--;
        s = new java.lang.String(s, i, n - i + 1).getBytes();

        // collapse multiple 0x20
        byte[] r = new byte[s.length];
        boolean allow_space = true;
        int j = 0;
        for (i = 0; i < s.length; i++) {
            if (s[i] == 0x20) {
                if (allow_space) {
                    r[j++] = s[i];
                    allow_space = false;
                }
            } else {
                r[j++] = s[i];
                allow_space = true;
            }
        }
        return new java.lang.String(r, 0, j);
    }

}

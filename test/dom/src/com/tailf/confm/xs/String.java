/*    -*- Java -*-
 *
 *  Copyright 2007-2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.xs;

import com.tailf.confm.*;
import static com.tailf.confm.ConfMException.throwException;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class implements the "xs:string" datatype from the
 * 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 * White space collapse and replace methods, regexp pattern matchers, length
 * assertion methods and an enumeration checker method are provided.
 */
public class String implements Serializable {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = -5494289433972914667L;
    
    private java.lang.String value;

    public String(java.lang.String value) {
        this.value = value;
        check();
    }

    public void setValue(java.lang.String value) throws ConfMException {
        this.value = value;
        check();
    }

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
        } else if (b instanceof java.lang.String) {
            return equals((java.lang.String) b);
        }
        return false;
    }

    /**
     * xs:whiteSpace collapse. Contiguous sequences of 0x20 are collapsed into a
     * single #x20, and initial and/or final #x20s are deleted.
     * <p>
     * This method is used by most all other data types to collapse Strings that
     * come from the XML parser.
     */
    public static java.lang.String wsCollapse(java.lang.String value) {
        java.lang.String res = new java.lang.String(value);
        
        // Collapse multiple spaces into single spaces
        res.replaceAll(" +", " ");
        
        // Remove any leading and/or trailing space
        if (res.length() > 0) {
            boolean hasLeadingSpace = res.charAt(0) == ' ';
            boolean hasTrailingSpace = res.charAt(res.length() - 1) == ' ';
            if (hasLeadingSpace && hasTrailingSpace) {
                return res.substring(1, res.length()-1);
            } else if (hasLeadingSpace) {
                return res.substring(1, res.length());
            } else if (hasTrailingSpace) {
                return res.substring(0, res.length()-1);
            }
        }
        return res;
    }

    /**
     * xs:whiteSpace replace. Returns a copy of value with all characters
     * matching "[\t\n\r]" replaced by " " (a blank).
     */
    public static java.lang.String wsReplace(java.lang.String value) {
        java.lang.String res = new java.lang.String(value);
        res.replaceAll("[\t\n\r]", " ");
        return res;
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:pattern
     */
    protected void pattern(java.lang.String regex) throws ConfMException {
        java.lang.String[] regexes = {regex};
        pattern(regexes);
    }

    protected void pattern(java.lang.String[] regexes) throws ConfMException {
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
        throwException(!matches, opaqueData);
    }

    /**
     * xs:length
     */
    protected void length(int len) throws ConfMException {
        throwException(value.length() != len, this);
    }

    /**
     * xs:minLength defines a minimum length measured in number of characters or
     * bytes.
     */
    protected void minLength(int len) throws ConfMException {
        throwException(value.length() < len, this);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of characters or
     * bytes.
     */
    protected void maxLength(int len) throws ConfMException {
        throwException(value.length() > len, this);
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
        return this.value.equals(value);
    }

}

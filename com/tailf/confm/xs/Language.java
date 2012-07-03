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

/**
 * This class implements the "xs:language" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 * Derivate from "xs:token".
 */
public class Language extends Token implements Serializable {

    public Language(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        super.setValue(value);
        check();
    }

    /**
     * From RFC 1766:
     *
     *  The syntax of this tag in RFC-822 EBNF is:
     *  Language-Tag = Primary-tag *( "-" Subtag )
     *  Primary-tag = 1*8ALPHA
     *  Subtag = 1*8ALPHA
     *
     */
    private void check() throws ConfMException {
        byte[] b=  getValue().getBytes();
        int i=0;
        while (i<b.length) {
            int len=0;
            while (i<b.length &&
                   ((b[i]>='a' && b[i]<='z') ||
                    (b[i]>='A' && b[i]<='Z'))) { i++; len++; }
            if (len==0 || len>8)
                throwException( true );

            if (i<b.length) {
                if (b[i++] != '-')
                    throwException( true );
            }
        }
    }

    /**
     * Overrride equals to not check upper or lower cases.
     *
     */
    public boolean equals(java.lang.String b) {
        java.lang.String s1= b.toLowerCase();
        java.lang.String s2= getValue().toLowerCase();
        return s1.equals(s2);
    }

    public boolean equals(Language b) {
        java.lang.String s1= b.getValue().toLowerCase();
        java.lang.String s2= getValue().toLowerCase();
        return s1.equals(s2);
    }

    public boolean equals(Object b) {
        if (b instanceof Language) {
            return equals( (Language)b );
        }
        return false;
    }

}

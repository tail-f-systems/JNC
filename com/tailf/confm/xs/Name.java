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
 * This class implements the "xs:Name" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 * Derivate from "xs:token".
 */
public class Name extends Token implements Serializable {

    public Name(java.lang.String value) throws ConfMException {
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

    private void check() throws ConfMException {
        java.lang.String value = getValue();
        // restriction that is starts with a letter or ":" or "-"
        startsWithLetterOrColonOrDash( value );
        // no spaces or commas allowed
        noSpacesOrCommasAllowed( value );
    }

    /**
     * Restriction that says that the Name should
     * start with a letter or ":" colon or "-" dash.
     *
     **/
    private void startsWithLetterOrColonOrDash(java.lang.String value)
        throws ConfMException {
        byte[] s= value.getBytes();
        byte b = s[0];
        if ( b>='a' && b<='z') return;
        if ( b>='A' && b<='Z') return;
        if ( b=='-' || b==':') return;
        throwException( true );
    }

}

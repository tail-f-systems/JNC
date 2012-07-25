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
 * This class implements the "xs:NCName" datatype from the
 * 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 * Derivate from "xs:Name".
 */
public class NCName extends Name implements Serializable {

    public NCName(java.lang.String value) throws ConfMException {
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
        // noColons allowed
        noColonsAllowed(getValue());
    }

    /**
     * Restriction that says that no colons are allowed.
     * 
     **/
    private void noColonsAllowed(java.lang.String value) throws ConfMException {
        byte[] s = value.getBytes();
        for (int i = 0; i < s.length; i++) {
            if (s[i] == ':')
                ConfMException.throwException(true, this);
        }
    }

}

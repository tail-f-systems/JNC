/*    -*- Java -*-
 *
 *  Copyright 2009 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.confd;
import com.tailf.confm.*;
import java.io.Serializable;

// THIS CLASS IS REALLY A PLACEHOLDER. IT WAS NOT IMPLEMENTED
// BY OLA AND THERE IS NO TIME TO AD IT RIGHT NOW EITHER.

/**
 * This class implements the "confd:AESCFB128EncryptedString" datatype from
 * the 'http://tail-f.com/ns/confd/1.0' namespace.
 */
public class AESCFB128EncryptedString implements Serializable {
    private String value;

    public AESCFB128EncryptedString(String value) throws ConfMException {
        value = com.tailf.confm.xs.String.wsCollapse(value);
        this.value = parseValue(value);
        check();
    }

    /**
     * Sets the value
     */
    public void setValue(String value) throws ConfMException {
        value = com.tailf.confm.xs.String.wsCollapse(value);
        this.value = parseValue(value);
        check();
    }

    /**
     * Returns the value
     */
    public String getValue() {
        return toString();
    }

    /**
     * Verifies value
     */
    private void check() throws ConfMException {
    }

    /**
     * Parses a AESCFB128EncryptedString String
     */
    private String parseValue(String value) {
        return value;
    }

    /**
     *
     */
    public String toString() {
        return value;
    }

    /**
     * Compares two AESCFB128EncryptedStrings for equality
     */
    public boolean equals(AESCFB128EncryptedString x) {
        return value.equals(x);
    }

    /**
     * Compares two AESCFB128EncryptedStrings for equality
     */
    public boolean equals(Object x) {
        if (x instanceof AESCFB128EncryptedString)
            return value.equals((AESCFB128EncryptedString)x);
        else
            return false;
    }
}

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
 * This class implements the "xs:base64Binary" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 * base64Binary represents Base64-encoded arbitrary binary data.
 *
 */
public class Base64Binary implements Serializable {

    private byte[] value;

    public Base64Binary(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        this.value = Base64Coder.encodeString(value).getBytes();
        check();
    }

    public Base64Binary(byte[] value) throws ConfMException {
        this.value = value;
        check();
    }


    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        this.value = Base64Coder.encodeString(value).getBytes();
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(byte[] value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Returns the value space.
     */
    public byte[] getValue() {
        return value;
    }


    /**
     * Check the value.
     * Put restrictions on the value here.
     */
    private void check() throws ConfMException {
    }



    public java.lang.String toString() {
        return Base64Coder.decodeString(new java.lang.String(value));
    }



    /** ---------- Restrictions ---------- */

    /**
     * xs:whiteSpace replace
     */
    public void wsReplace() {
        value = String.wsReplace(new java.lang.String(value)).getBytes();
    }

    /**
     * xs:whiteSpace collapse
     */
    public void wsCollapse() {
        value = String.wsCollapse(new java.lang.String(value)).getBytes();
    }

}

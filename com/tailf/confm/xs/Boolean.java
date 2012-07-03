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
 * This class implements the "xs:boolean" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 */
public class Boolean implements Serializable {

    private boolean value;

    public Boolean(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        if (value.equals("true"))  this.value = true;
        else if (value.equals("false")) this.value = false;
        else
            throw new ConfMException(ConfMException.BAD_VALUE, this);
        check();
    }

    public Boolean(boolean value) {
        this.value = value;
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        if (value.equals("true"))  this.value = true;
        else if (value.equals("false")) this.value = false;
        else
            throw new ConfMException(ConfMException.BAD_VALUE, this);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(boolean value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Gets the value space.
     */
    public boolean getValue() {
        return value;
    }

    /**
     * Check the value.
     * Put restrictions on the value here.
     */
    private void check() {
    }

    public java.lang.String toString() {
        return new java.lang.Boolean(value).toString();
    }

    public boolean equals(boolean value) {
        if (this.value == value) return true;
        else return false;
    }

    public boolean equals(Boolean value) {
        return value.equals( this.value );
    }

    public boolean equals(Object value) {
        if (value instanceof Boolean)
            return ((Boolean)value).equals(this.value);
        return false;
    }

}

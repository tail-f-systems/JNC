/*    -*- Java -*-
 *
 *  Copyright 2010 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.inet;
import com.tailf.confm.*;
import java.io.Serializable;

enum IpVersionValue { unknown, ipv4, ipv6 }

/**
 * This class implements the "ip-version" datatype from the
 * 'urn:ietf:params:xml:ns:yang:inet-types' namespace.
 */
public class IpVersion implements Serializable {
    private IpVersionValue value;

    /**
     * Constructor
     */
    public IpVersion(String stringValue) throws ConfMException {
        value = parseValue(stringValue);
    }

    private IpVersionValue parseValue(java.lang.String stringValue)
      throws ConfMException {
        if (stringValue.equals("unknown"))
            return IpVersionValue.unknown;
        else if (stringValue.equals("ipv4"))
            return IpVersionValue.ipv4;
        else if (stringValue.equals("ipv6"))
            return IpVersionValue.ipv4;
        else
            throwException(true, stringValue);

        return null;
    }

    private void check() {
    }

    public IpVersion(IpVersionValue value) {
        this.value = value;
    }

    /**
     * Set a value
     */
    public void setValue(String stringValue) throws ConfMException {
        parseValue(stringValue);
    }

    public void setValue(IpVersionValue value) {
        this.value = value;
    }

    /**
     * Get a value
     */
    public IpVersionValue getValue() {
        return value;
    }

    /**
     * Convert to a string value
     */
    public java.lang.String toString() {
        return value.toString();
    }

    /**
     * Check for equality
     */

    public boolean equals(Object object) {
        if (object instanceof IpVersionValue) {
            return ((IpVersionValue)object).equals(this.value);
        }
        return false;
    }

    public boolean equals(IpVersionValue value) {
        if (this.value == value)
            return true;
        else
            return false;
    }

    /**
     * Internal utilities
     */
    protected void throwException(boolean ok) throws ConfMException {
        if (!ok)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

    protected void throwException(boolean ok, Object object)
      throws ConfMException {
        if (!ok)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, object);
    }
}

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

/**
 * This class implements the "ip-address" datatype from the
 * 'urn:ietf:params:xml:ns:yang:inet-types' namespace.
 * <p>
 * A value that represents a valid v4 or v6 network address. Values which adhere
 * to this type must be one of these types:
 * <ul>
 * <li>ipv4-address
 * <li>ipv6-address
 * </ul>
 */
public class IpAddress implements Serializable {
    private Object value;

    /**
     * Constructor
     */
    public IpAddress(String stringValue) throws ConfMException {
        try {
            value = new Ipv4Address(stringValue);
        } catch (Exception e) {
            value = new Ipv6Address(stringValue);
        }
        check();
    }

    private void check() throws ConfMException {
    }

    public IpAddress(Ipv4Address value) throws ConfMException {
        this.value = value;
        check();
    }

    public IpAddress(Ipv6Address value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Set a value
     */
    public void setValue(String stringValue) throws ConfMException {
        try {
            value = new Ipv4Address(stringValue);
        } catch (Exception e) {
            value = new Ipv6Address(stringValue);
        }
        check();
    }

    public void setValue(Ipv4Address value) throws ConfMException {
        this.value = value;
        check();
    }

    public void setValue(Ipv6Address value) throws ConfMException {
        this.value = value;
        check();
    }

    public void setValue(Object object) throws ConfMException {
        if (object instanceof Ipv4Address)
            value = object;
        else if (object instanceof Ipv6Address)
            value = object;
        else
            throwException(true, object);
        check();
    }

    /**
     * Get a value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Convert to a string value
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Check for equality
     */
    public boolean equals(Object object) {
        if (object instanceof IpAddress)
            return ((IpAddress) object).equals(this);
        return false;
    }

    public boolean equals(IpAddress value) {
        return this.value.equals(value.value);
    }

    /**
     * Internal utilities
     */
    protected void throwException(boolean ok, Object object)
            throws ConfMException {
        if (!ok)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, object);
    }
}

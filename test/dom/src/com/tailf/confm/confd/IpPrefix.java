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

package com.tailf.confm.confd;

import com.tailf.confm.*;
import java.io.Serializable;

/**
 * This class implements the "ipPrefix" datatype from the
 * 'http://tail-f.com/ns/confd/1.0' namespace.
 */
public class IpPrefix implements Serializable {
    private Object value;

    /**
     * Constructor
     */
    public IpPrefix(String v) throws ConfMException {
        try {
            value = new Ipv4Prefix(v);
        } catch (Exception e1) {
            value = new Ipv6Prefix(v);
        }
        check();
    }

    public IpPrefix(Ipv4Prefix v) throws ConfMException {
        value = v;
        check();
    }

    public IpPrefix(Ipv6Prefix v) throws ConfMException {
        value = v;
        check();
    }

    /**
     * Set a value
     */
    public void setValue(Object v) throws ConfMException {
        if (v instanceof Ipv4Prefix)
            value = v;
        else if (v instanceof Ipv6Prefix)
            value = v;
        else
            throwException(true, v);
        check();
    }

    public void setValue(String v) throws ConfMException {
        try {
            value = new Ipv4Prefix(v);
        } catch (Exception e1) {
            value = new Ipv6Prefix(v);
        }
        check();
    }

    public void setValue(Ipv4Prefix v) throws ConfMException {
        value = v;
        check();
    }

    public void setValue(Ipv6Prefix v) throws ConfMException {
        value = v;
        check();
    }

    public Object getValue() {
        return value;
    }

    public void check() throws ConfMException {
    }

    public String toString() {
        return value.toString();
    }

    public boolean equals(Object b) {
        if (b instanceof IpPrefix)
            return ((IpPrefix) b).equals(this);
        return false;
    }

    public boolean equals(IpPrefix b) {
        return value.equals(b.value);
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object o) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, o);
    }
}

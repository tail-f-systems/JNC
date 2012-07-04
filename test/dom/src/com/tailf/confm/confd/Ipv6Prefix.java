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

package com.tailf.confm.confd;

import com.tailf.confm.*;
import java.io.Serializable;

/**
 * This class implements the "ipv6Prefix" datatype from the
 * 'http://tail-f.com/ns/confd/1.0' namespace.
 * <p>
 * The ipv6Prefix type represents an IPv6 address prefix. The prefix length is
 * given by the number following the slash character and must be less than or
 * equal 128.
 * <p>
 * A prefix length value of n corresponds to an IP address mask which has n
 * contiguous 1-bits from the most significant bit (MSB) and all other bits set
 * to 0.
 * <p>
 * The IPv6 address should have all bits that do not belong to the prefix set to
 * zero.
 * <p>
 * An example: 2001:DB8::1428:57AB/125
 * 
 * 
 */
public class Ipv6Prefix implements Serializable {

    private InetAddressIPv6 ipaddr;
    private int masklen;

    /**
     * Constructor
     */
    public Ipv6Prefix(String s) throws ConfMException {
        String v = com.tailf.confm.xs.String.wsCollapse(s);
        try {
            int slashAt = v.indexOf('/');
            ipaddr = new InetAddressIPv6(v.substring(0, slashAt));
            masklen = Integer.parseInt(v.substring(slashAt + 1));
        } catch (Exception e) {
            throwException(true, v);
        }
        check();
    }

    public Ipv6Prefix(InetAddressIPv6 ipaddr, int masklen)
            throws ConfMException {
        this.ipaddr = ipaddr;
        this.masklen = masklen;
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(String s) throws ConfMException {
        String v = com.tailf.confm.xs.String.wsCollapse(s);
        try {
            int slashAt = v.indexOf('/');
            ipaddr = new InetAddressIPv6(v.substring(0, slashAt));
            masklen = Integer.parseInt(v.substring(slashAt + 1));
        } catch (Exception e) {
            throwException(true, v);
        }
        check();
    }

    public void setValue(InetAddressIPv6 ipaddr, int masklen)
            throws ConfMException {
        this.ipaddr = ipaddr;
        this.masklen = masklen;
        check();
    }

    /**
     * Return the value space. But since there are two values (ipaddr, and
     * masklen) a String combining them both are returned.
     */
    public String getValue() {
        return toString();
    }

    public void check() throws ConfMException {
        throwException(masklen < 0 || masklen > 128);
    }

    public String toString() {
        return ipaddr.toString() + "/" + masklen;
    }

    public boolean equals(Object b) {
        if (b instanceof Ipv6Prefix)
            return ((Ipv6Prefix) b).equals(this);
        return false;
    }

    public boolean equals(Ipv6Prefix b) {
        if (masklen == b.masklen)
            return ipaddr.equals(b.ipaddr);
        return false;
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, toString());
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

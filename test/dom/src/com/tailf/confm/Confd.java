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

package com.tailf.confm;

import com.tailf.inm.*;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * This class implements the ConfD data types from the
 * 'http://tail-f.com/ns/confd/1.0' namespace. The type classes are used by the
 * ConfM generated classes.
 * <p>
 * This class is an encapsulation of all the classes in the
 * 'http://tail-f.com/ns/confd/1.0' namespace which have been placed in the
 * com.tailf.confm.confd package.
 * 
 * 
 */
public class Confd {

    /**
     * This class implements the "Counter32" datatype.
     * 
     */
    static public class Counter32 extends com.tailf.confm.confd.Counter32
            implements Serializable {

        public Counter32(String value) throws ConfMException {
            super(value);
        }

        public Counter32(int value) throws ConfMException {
            super(value);
        }

        public Counter32(long value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "Counter64" datatype.
     * 
     */
    static public class Counter64 extends com.tailf.confm.confd.Counter64
            implements Serializable {

        public Counter64(String value) throws ConfMException {
            super(value);
        }

        public Counter64(long value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class inplements an existance element.
     */
    static public class Exists extends com.tailf.confm.confd.Exists implements
            Serializable {
        public Exists() throws ConfMException {
            super();
        }
    }

    /**
     * This class implements the "Gauge32" datatype.
     * 
     */
    static public class Gauge32 extends com.tailf.confm.confd.Gauge32 implements
            Serializable {

        public Gauge32(String value) throws ConfMException {
            super(value);
        }

        public Gauge32(int value) throws ConfMException {
            super(value);
        }

        public Gauge32(long value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "HexList" datatype.
     * 
     */
    static public class HexList extends com.tailf.confm.confd.HexList implements
            Serializable {

        public HexList(String value) throws ConfMException {
            super(value);
        }

        public HexList(long value) throws ConfMException {
            super(value);
        }

        public HexList(int value) throws ConfMException {
            super(value);
        }

        public HexList(byte[] value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "inetAddress" datatype.
     * 
     */
    static public class InetAddress extends com.tailf.confm.confd.InetAddress
            implements Serializable {

        public InetAddress(String value) throws ConfMException {
            super(value);
        }

        public InetAddress(com.tailf.confm.confd.InetAddressIPv4 value)
                throws ConfMException {
            super(value);
        }

        public InetAddress(com.tailf.confm.confd.InetAddressIPv6 value)
                throws ConfMException {
            super(value);
        }

        public InetAddress(com.tailf.confm.confd.InetAddressDNS value)
                throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "inetAddressDNS" datatype.
     * 
     */
    static public class InetAddressDNS extends
            com.tailf.confm.confd.InetAddressDNS implements Serializable {

        public InetAddressDNS(String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "inetAddressIP" datatype.
     */
    static public class InetAddressIP extends
            com.tailf.confm.confd.InetAddressIP implements Serializable {

        public InetAddressIP(String value) throws ConfMException {
            super(value);
        }

        public InetAddressIP(com.tailf.confm.confd.InetAddressIPv4 value)
                throws ConfMException {
            super(value);
        }

        public InetAddressIP(com.tailf.confm.confd.InetAddressIPv6 value)
                throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "inetAddressIPV4" datatype.
     * 
     */
    static public class InetAddressIPv4 extends
            com.tailf.confm.confd.InetAddressIPv4 implements Serializable {

        public InetAddressIPv4(String value) throws ConfMException {
            super(value);
        }

        public InetAddressIPv4(java.net.InetAddress value)
                throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "inetAddressIPV6" datatype.
     * 
     */
    static public class InetAddressIPv6 extends
            com.tailf.confm.confd.InetAddressIPv6 implements Serializable {

        public InetAddressIPv6(String value) throws ConfMException {
            super(value);
        }

        public InetAddressIPv6(java.net.InetAddress value)
                throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "InetPortNumber" datatype.
     * 
     */
    static public class InetPortNumber extends
            com.tailf.confm.confd.InetPortNumber implements Serializable {

        public InetPortNumber(String value) throws ConfMException {
            super(value);
        }

        public InetPortNumber(int value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "ipv4Prefix" datatype.
     * 
     */
    static public class Ipv4Prefix extends com.tailf.confm.confd.Ipv4Prefix
            implements Serializable {

        public Ipv4Prefix(String value) throws ConfMException {
            super(value);
        }

        public Ipv4Prefix(com.tailf.confm.confd.InetAddressIPv4 ipaddr,
                int masklen) throws ConfMException {
            super(ipaddr, masklen);
        }
    }

    /**
     * This class implements the "ipv6Prefix" datatype.
     * 
     */
    static public class Ipv6Prefix extends com.tailf.confm.confd.Ipv6Prefix
            implements Serializable {

        public Ipv6Prefix(String value) throws ConfMException {
            super(value);
        }

        public Ipv6Prefix(com.tailf.confm.confd.InetAddressIPv6 ipaddr,
                int masklen) throws ConfMException {
            super(ipaddr, masklen);
        }
    }

    /**
     * This class implements the "OctetList" datatype.
     * 
     */
    static public class OctetList extends com.tailf.confm.confd.OctetList
            implements Serializable {

        public OctetList(String value) throws ConfMException {
            super(value);
        }

        public OctetList(byte[] value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "OID" datatype.
     * 
     */
    static public class OID extends com.tailf.confm.confd.OID implements
            Serializable {

        public OID(String value) throws ConfMException {
            super(value);
        }

        public OID(int[] value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "size" datatype.
     * 
     */
    static public class Size extends com.tailf.confm.confd.Size implements
            Serializable {

        public Size(String value) throws ConfMException {
            super(value);
        }
    }

}

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

package com.tailf.netconfmanager.yang;

import com.tailf.netconfmanager.Element;
import com.tailf.netconfmanager.Tagpath;

/**
 * The SchemaNode class is used to represent individual nodes in the schema tree.
 * Each generated ConfM namespace gets its schema, as a set of SchemaNode objects
 * generated in its top class All the SchemaNode's for a namespace are put into
 * hashtable that is held by the SchemaTree class.
 */

public class SchemaNode {

    public Tagpath tagpath;
    public String namespace;

    public int primitive_type; // C_*
    public int min_occurs;
    public int max_occurs;
    public String[] children;

    public String[] keys;
    public long flags; // CS_NODE_*
    public String desc;
    public RevisionInfo[] revInfo;

    public static int CS_NODE_IS_DYN = (1 << 0);
    public static int CS_NODE_IS_WRITE = (1 << 1);
    public static int CS_NODE_IS_CDB = (1 << 2);
    public static int CS_NODE_IS_ACTION = (1 << 3);
    public static int CS_NODE_IS_PARAM = (1 << 4);
    public static int CS_NODE_IS_RESULT = (1 << 5);
    public static int CS_NODE_IS_NOTIF = (1 << 6);
    public static int CS_NODE_IS_CASE = (1 << 7);

    // Same names and values that we use in the
    // confd device side C libraries

    public static int C_BUF = 5; // (xs:string ...)
    public static int C_INT8 = 6; // (xs:byte)
    public static int C_INT16 = 7; // (xs:short)
    public static int C_INT32 = 8; // (xs:int)
    public static int C_INT64 = 9; // (xs:integer)
    public static int C_UINT8 = 10; // (xs:unsignedByte)
    public static int C_UINT16 = 11; // (xs:unsignedShort)
    public static int C_UINT32 = 12; // (xs:unsignedInt)
    public static int C_UINT64 = 13; // (xs:unsignedLong)
    public static int C_DOUBLE = 14; // (xs:float,xs:double)
    public static int C_IPV4 = 15; // (confd:inetAddressIPv4)
    public static int C_IPV6 = 16; // (confd:inetAddressIPv6)
    public static int C_BOOL = 17; // (xs:boolean)
    public static int C_QNAME = 18; // (xs:QName)
    public static int C_DATETIME = 19; // (xs:dateTime)
    public static int C_DATE = 20; // (xs:date)
    public static int C_GYEARMONTH = 21; // (xs:gYearMonth)
    public static int C_GYEAR = 22; // (xs:gYear)
    public static int C_TIME = 23; // (xs:time)
    public static int C_GDAY = 24; // (xs:gDay)
    public static int C_GMONTHDAY = 25; // (xs:gMonthDay)
    public static int C_GMONTH = 26; // (xs:gMonth)
    public static int C_DURATION = 27; // (xs:duration)
    public static int C_ENUM_HASH = 28; // (string enumerations)
    public static int C_BIT32 = 29; // (bitsType size 32)
    public static int C_BIT64 = 30; // (bitsType size 64)
    public static int C_LIST = 31; // (xs:list)

    public static int C_OBJECTREF = 34; // (confd:objectRef)
    public static int C_UNION = 35; // (xs:union)
    public static int C_OID = 38; // (confd:oid)
    public static int C_BINARY = 39; // (xs:hexBinary ...)
    public static int C_IPV4PREFIX = 40; // (confd:ipv4Prefix)
    public static int C_IPV6PREFIX = 41; // (confd:ipv6Prefix)

    public SchemaNode() {
    }

    // print as /foo/bar style
    public String toString() {
        return "SchemaNode{" + tagpath + "}";
    }

    /*
     * Given an Element, find the SchemaNode, i.e. the schema class for the data
     * element
     */

    public static SchemaNode get(Element e) {
        return SchemaTree.lookup(e.namespace, e.tagpath());
    }

}

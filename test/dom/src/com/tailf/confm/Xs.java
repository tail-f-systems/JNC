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

import java.io.Serializable;

/**
 * This class implements the XML schema data types. the
 * 'http://www.w3.org/2001/XMLSchema' namespace. The type classes are used by
 * the ConfM generated classes.
 * <p>
 * This class is an encapsulation of all the classes in the
 * 'http://www.w3.org/2001/XMLSchema' namespace which have been placed in the
 * com.tailf.confm.xs package.
 * 
 * 
 */
public class Xs {

    /**
     * This class implements the "xs:string" datatype.
     * 
     */
    static public class String extends com.tailf.confm.xs.String implements
            Serializable {

        public String(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:normalizedString" datatype.
     * 
     */
    static public class NormalizedString extends
            com.tailf.confm.xs.NormalizedString implements Serializable {

        public NormalizedString(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:token" datatype.
     * 
     */
    static public class Token extends com.tailf.confm.xs.Token implements
            Serializable {

        public Token(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:Name" datatype.
     * 
     */
    static public class Name extends com.tailf.confm.xs.Name implements
            Serializable {

        public Name(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:NMTOKEN" datatype.
     * 
     */
    static public class NMTOKEN extends com.tailf.confm.xs.NMTOKEN implements
            Serializable {

        public NMTOKEN(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:NCName" datatype.
     * 
     */
    static public class NCName extends com.tailf.confm.xs.NCName implements
            Serializable {

        public NCName(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:ID" datatype.
     * 
     */
    static public class ID extends com.tailf.confm.xs.ID implements
            Serializable {

        public ID(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:language" datatype.
     * 
     */
    static public class Language extends com.tailf.confm.xs.Language implements
            Serializable {

        public Language(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:boolean" datatype.
     * 
     */
    static public class Boolean extends com.tailf.confm.xs.Boolean implements
            Serializable {

        public Boolean(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Boolean(boolean value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:byte" datatype. Represents an 8-bit signed
     * integer.
     * 
     */
    static public class Byte extends com.tailf.confm.xs.Byte implements
            Serializable {

        public Byte(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Byte(byte value) throws ConfMException {
            super(value);
        }

        public Byte(int value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:short" datatype. Represents a 16-bit signed
     * integer.
     * 
     */
    static public class Short extends com.tailf.confm.xs.Short implements
            Serializable {

        public Short(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Short(short value) throws ConfMException {
            super(value);
        }

        public Short(int value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:int" datatype. Represents a 32-bit signed
     * integer.
     * 
     */
    static public class Int extends com.tailf.confm.xs.Int implements
            Serializable {

        public Int(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Int(int value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:long" datatype. Represents a 64-bit signed
     * integer.
     * 
     */
    static public class Long extends com.tailf.confm.xs.Long implements
            Serializable {

        public Long(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Long(long value) throws ConfMException {
            super(value);
        }

    }

    /**
     * This class implements the "xs:integer" datatype. Arbitrary big signed
     * integer. We do not support arbitrary big integers, this integer is
     * limited to 64 bits.
     * 
     */
    static public class Integer extends com.tailf.confm.xs.Integer implements
            Serializable {

        public Integer(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Integer(long value) throws ConfMException {
            super(value);
        }

        public Integer(int value) throws ConfMException {
            super(value);
        }

    }

    /**
     * This class implements the "xs:unsignedByte" datatype. Represents an
     * unsigned 8-bit integer.
     * 
     */
    static public class UnsignedByte extends com.tailf.confm.xs.UnsignedByte
            implements Serializable {

        public UnsignedByte(java.lang.String value) throws ConfMException {
            super(value);
        }

        public UnsignedByte(short value) throws ConfMException {
            super(value);
        }

        public UnsignedByte(int value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:unsignedShort" datatype. Represents an
     * unsigned 16-bit integer.
     * 
     */
    static public class UnsignedShort extends com.tailf.confm.xs.UnsignedShort
            implements Serializable {

        public UnsignedShort(java.lang.String value) throws ConfMException {
            super(value);
        }

        public UnsignedShort(int value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:unsignedInt" datatype. Represents an
     * unsigned 32-bit integer.
     * 
     */
    static public class UnsignedInt extends com.tailf.confm.xs.UnsignedInt
            implements Serializable {

        public UnsignedInt(java.lang.String value) throws ConfMException {
            super(value);
        }

        public UnsignedInt(long value) throws ConfMException {
            super(value);
        }

    }

    /**
     * This class implements the "xs:unsignedLong" datatype. Arbitrary big
     * unsigned integer. We do not support arbitrary big integers. Represented
     * with a 64-bit signed (long) integer. (may not fit)
     */
    static public class UnsignedLong extends com.tailf.confm.xs.UnsignedLong
            implements Serializable {

        public UnsignedLong(java.lang.String value) throws ConfMException {
            super(value);
        }

        public UnsignedLong(long value) throws ConfMException {
            super(value);
        }

    }

    /**
     * This class implements the "xs:decimal" datatype.
     * 
     */
    static public class Decimal extends com.tailf.confm.xs.Decimal implements
            Serializable {

        public Decimal(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Decimal(double value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:float" datatype.
     * 
     */
    static public class Float extends com.tailf.confm.xs.Float implements
            Serializable {

        public Float(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Float(float value) throws ConfMException {
            super(value);
        }

        public Float(double value) throws ConfMException {
            super(value);
        }

    }

    /**
     * This class implements the "xs:double" datatype.
     * 
     */
    static public class Double extends com.tailf.confm.xs.Double implements
            Serializable {

        public Double(java.lang.String value) throws ConfMException {
            super(value);
        }

        public Double(double value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:positiveInteger" datatype. Represents a
     * positive integer.
     * 
     */
    static public class PositiveInteger extends
            com.tailf.confm.xs.PositiveInteger implements Serializable {

        public PositiveInteger(java.lang.String value) throws ConfMException {
            super(value);
        }

        public PositiveInteger(long value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:nonPositiveInteger" datatype. Represents a
     * non-positive integer.
     * 
     */
    static public class NonPositiveInteger extends
            com.tailf.confm.xs.NonPositiveInteger implements Serializable {

        public NonPositiveInteger(java.lang.String value) throws ConfMException {
            super(value);
        }

        public NonPositiveInteger(long value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:negativeInteger" datatype. Represents a
     * negative integer.
     * 
     */
    static public class NegativeInteger extends
            com.tailf.confm.xs.NegativeInteger implements Serializable {

        public NegativeInteger(java.lang.String value) throws ConfMException {
            super(value);
        }

        public NegativeInteger(long value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:nonNegativeInteger" datatype. Represents a
     * non-negative integer.
     * 
     */
    static public class NonNegativeInteger extends
            com.tailf.confm.xs.NonNegativeInteger implements Serializable {

        public NonNegativeInteger(java.lang.String value) throws ConfMException {
            super(value);
        }

        public NonNegativeInteger(long value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:duration" datatype.
     * 
     */
    static public class Duration extends com.tailf.confm.xs.Duration implements
            Serializable {

        public Duration(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:dateTime" datatype.
     * 
     */
    static public class DateTime extends com.tailf.confm.xs.DateTime implements
            Serializable {

        public DateTime(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:date" datatype.
     * 
     */
    static public class Date extends com.tailf.confm.xs.Date implements
            Serializable {

        public Date(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:gYearMonth" datatype.
     * 
     */
    static public class GYearMonth extends com.tailf.confm.xs.GYearMonth
            implements Serializable {

        public GYearMonth(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:gYear" datatype.
     * 
     */
    static public class GYear extends com.tailf.confm.xs.GYear implements
            Serializable {

        public GYear(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:time" datatype.
     * 
     */
    static public class Time extends com.tailf.confm.xs.Time implements
            Serializable {

        public Time(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:gMonth" datatype.
     * 
     */
    static public class GMonth extends com.tailf.confm.xs.GMonth implements
            Serializable {

        public GMonth(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:gMonthDay" datatype.
     * 
     */
    static public class GMonthDay extends com.tailf.confm.xs.GMonthDay
            implements Serializable {

        public GMonthDay(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:gDay" datatype.
     * 
     */
    static public class GDay extends com.tailf.confm.xs.GDay implements
            Serializable {

        public GDay(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:QName" datatype.
     * 
     */
    static public class QName extends com.tailf.confm.xs.QName implements
            Serializable {

        public QName(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:anyURI" datatype.
     * 
     */
    static public class AnyURI extends com.tailf.confm.xs.AnyURI implements
            Serializable {

        public AnyURI(java.lang.String value) throws ConfMException {
            super(value);
        }
    }

    /**
     * This class implements the "xs:HexBinary" datatype.
     * 
     */
    static public class HexBinary extends com.tailf.confm.xs.HexBinary
            implements Serializable {

        public HexBinary(java.lang.String value) throws ConfMException {
            super(value);
        }

        public HexBinary(int value) throws ConfMException {
            super(value);
        }

        public HexBinary(long value) throws ConfMException {
            super(value);
        }

        public HexBinary(byte[] value) throws ConfMException {
            super(value);
        }

    }

}

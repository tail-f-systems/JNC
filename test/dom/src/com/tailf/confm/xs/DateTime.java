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
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This class implements the "xs:dateTime" datatype from the
 * 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 * 
 * 
 */
public class DateTime implements Serializable {

    private boolean neg = false;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    private int secs;
    private int secs_fraction; // in milliseconds
    private int secs_fraction_digits;
    private boolean timezoned = false;
    private boolean tz_neg = false;
    private int tz_hh;
    private int tz_mm;

    /**
     *
     */
    public DateTime() {
    }

    /**
     * Constructor for xs:dateTime from a String in the format
     * "CCYY-MM-DDThh:mm:ss". For example: "2000-04-01T21:32:52",
     * "2000-04-01T21:32:52+02:00", "2000-04-01T21:32:52Z",
     * "2000-04-01T21:32:52.12679",
     * 
     */
    public DateTime(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        parseValue(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        value = String.wsCollapse(value);
        parseValue(value);
        check();
    }

    /**
     * Gets the value.
     */
    public java.lang.String getValue() {
        return toString();
    }

    private void check() throws ConfMException {
        throwException(month > 12);
        throwException(month < 1);
        throwException(day > 31);
        throwException(day < 1);
        throwException(hour > 23);
        throwException(hour < 0);
        throwException(min > 59);
        throwException(min < 0);
        throwException(secs >= 60);
        throwException(secs < 0);
    }

    public java.lang.String toString() {
        java.lang.String s = new java.lang.String();
        if (neg)
            s = s + "-";
        s = Date.four_digits(year) + "-" + Date.two_digits(month) + "-"
                + Date.two_digits(day) + "T" + Date.two_digits(hour) + ":"
                + Date.two_digits(min) + ":" + Date.two_digits(secs);
        if (secs_fraction > 0 && secs_fraction_digits > 0) {
            int mmm = secs_fraction / 100;
            int mm = (secs_fraction - (100 * mmm)) / 10;
            int m = (secs_fraction - (100 * mmm) - (10 * mm));
            java.lang.String fr = "" + mmm + mm + m;
            s = s + "." + fr;
        }
        if (timezoned)
            s = s + Date.timezone_toString(tz_neg, tz_hh, tz_mm);
        return s;
    }

    /**
     * Checks if the value space of two DateTime objects are equal.
     */
    public boolean equals(DateTime value) {
        Calendar v1 = getCalendar();
        Calendar v2 = value.getCalendar();
        // System.out.println("v1= "+ v1.getTimeInMillis());
        // System.out.println("v2= "+ v2.getTimeInMillis());
        if (v1.getTimeInMillis() == v2.getTimeInMillis())
            return true;
        return false;
    }

    /**
     * Checks if the value space of two DateTime objects are equal.
     */
    public boolean equals(Object value) {
        if (value instanceof DateTime)
            return ((DateTime) value).equals(this);
        return false;
    }

    /**
     *
     */
    public int compareTo(DateTime value) {
        return toString().compareTo(value.toString());
    }

    /**
     * Returns a Calendar object from the DateTime.
     * 
     */
    public Calendar getCalendar() {
        Calendar c = new GregorianCalendar();
        c.clear();
        c.set(year, month - 1, day, hour, min, secs);
        // System.out.println("secs_fraction= "+secs_fraction);
        c.set(Calendar.MILLISECOND, secs_fraction);
        if (timezoned) {
            int zone_offset = tz_hh * 60 * 60 * 1000 + tz_mm * 60 * 1000;
            if (tz_neg)
                zone_offset = -zone_offset;
            c.set(Calendar.ZONE_OFFSET, zone_offset);
        }
        return c;
    }

    /**
     * Returns the current time, for this DateTime object.
     * 
     */
    public java.util.Date getTime() {
        return getCalendar().getTime();
    }

    /**
     * Help method to parse and set the value.
     */
    private void parseValue(java.lang.String value) throws ConfMException {
        neg = false;
        byte[] b = value.getBytes();
        try {
            int i = 0;
            if (b[i] == '-') {
                neg = true;
                i++;
            }
            year = Date.parseDigit(b[i++]);
            year = year * 10 + Date.parseDigit(b[i++]);
            year = year * 10 + Date.parseDigit(b[i++]);
            year = year * 10 + Date.parseDigit(b[i++]);
            while (b[i] >= '0' && b[i] <= '9')
                year = year * 10 + b[i++] - '0';
            throwException(b[i++] != '-', value);
            month = Date.parseDigit(b[i++]);
            month = 10 * month + Date.parseDigit(b[i++]);
            throwException(b[i++] != '-', value);
            day = Date.parseDigit(b[i++]);
            day = day * 10 + Date.parseDigit(b[i++]);
            throwException(b[i++] != 'T', value);
            hour = Date.parseDigit(b[i++]);
            hour = hour * 10 + Date.parseDigit(b[i++]);
            throwException(b[i++] != ':', value);
            min = Date.parseDigit(b[i++]);
            min = min * 10 + Date.parseDigit(b[i++]);
            throwException(b[i++] != ':', value);
            secs = Date.parseDigit(b[i++]);
            secs = secs * 10 + Date.parseDigit(b[i++]);
            secs_fraction_digits = 0;
            secs_fraction = 0;
            if (i < b.length && b[i] == '.') {
                i++;
                int value_pos = 100; // millisecs
                while (i < b.length && b[i] >= '0' && b[i] <= '9'
                        && secs_fraction_digits < 3) {
                    secs_fraction_digits++;
                    secs_fraction = secs_fraction + value_pos * (b[i++] - '0');
                    value_pos = value_pos / 10;
                }
                // ignore extra numbers in the fraction part
                while (i < b.length && b[i] >= '0' && b[i] <= '9')
                    i++;
                //
                throwException(secs_fraction_digits == 0, value);
            }
            timezoned = false;
            tz_neg = false;
            tz_hh = 0;
            tz_mm = 0;
            if (i < b.length) {
                // timezone also
                timezoned = true;
                if (b[i] == 'Z') {
                    i++;
                    throwException(i != b.length, value);
                } else if (b[i] == '+' || b[i] == '-') {
                    if (b[i] == '-')
                        tz_neg = true;
                    i++;
                    tz_hh = Date.parseDigit(b[i++]);
                    tz_hh = tz_hh * 10 + Date.parseDigit(b[i++]);
                    throwException(b[i++] != ':', value);
                    tz_mm = Date.parseDigit(b[i++]);
                    tz_mm = tz_mm * 10 + Date.parseDigit(b[i++]);
                    throwException(i != b.length, value);
                } else
                    throwException(true, value);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throwException(true, value);
        }
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:minInclusive defines a minimum value that can be reached.
     */
    protected void minInclusive(DateTime restriction) throws ConfMException {
        throwException(compareTo(restriction) < 0);
    }

    /**
     * xs:minExclusive defines a minimum value that cannot be reached.
     */
    protected void minExclusive(DateTime restriction) throws ConfMException {
        throwException(compareTo(restriction) <= 0);
    }

    /**
     * xs:maxExclusive defines a maximum value that cannot be reached.
     */
    protected void maxInclusive(DateTime restriction) throws ConfMException {
        throwException(compareTo(restriction) > 0);
    }

    /**
     * xs:maxExclusive defines a minimum value that cannot be reached.
     */
    protected void maxExclusive(DateTime restriction) throws ConfMException {
        throwException(compareTo(restriction) >= 0);
    }

    /**
     * xs:enumeration
     */
    protected boolean enumeration(java.lang.String value) {
        if (toString().equals(value))
            return true;
        else
            return false;
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, this);
    }

    /**
     * Assert that the value is 'false' Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object value)
            throws ConfMException {
        if (!v)
            return;
        throw new ConfMException(ConfMException.BAD_VALUE, value);
    }

}

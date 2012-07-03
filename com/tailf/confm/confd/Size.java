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
 * This class implements the "confd:size" datatype from
 * the 'http://tail-f.com/ns/confd/1.0' namespace.
 * <p>
 * A value that represents a number of bytes.
 * An example could be S1G8M7K956B;  meaning
 * 1GB+8MB+7KB+956B = 1082138556 bytes.
 * The value must start with an S. Any byte magnifier can be
 * left out, i.e. S1K1B equals 1025 bytes. The order is significant though,
 * i.e. S1B56G is not a valid byte size.
 * The size value space is as for xs:unsignedLong.
 *
 */
public class Size implements Serializable {

    private long value;

    public Size(String value) throws ConfMException {
        value = com.tailf.confm.xs.String.wsCollapse(value);
        this.value = parseValue(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(String value) throws ConfMException {
        value = com.tailf.confm.xs.String.wsCollapse(value);
        this.value = parseValue(value);
        check();
    }

    /**
     * Sets the size in bytes.
     */
    public void setValue(long value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Return the value.
     */
    public String getValue() {
        return toString();
    }

    /**
     * Return the size in bytes.
     */
    public long longValue() {
        return value;
    }

    private void check() throws ConfMException {
        throwException( value < 0 );
    }

    /**
     * Parse a Size String into a long value.
     *
     */
    private long parseValue(String value) throws ConfMException {
        byte[] b = value.getBytes();
        int num=0;
        long ack=0L;
        // Order is 'G', 'M', 'K', 'B'
        // order 'G'=1, 'M'=2, 'K'=3, 'B'=4.
        int order = 1;
        int i =0;
        if (b[i++] != 'S') throwException(true, value);
        for (;i<b.length;i++) {
            if (b[i]>='0' && b[i]<='9') {
                num=num*10+ b[i]-'0';
            } else if (b[i]=='G' && order<=1) {
                order= 2;
                ack= ack + num*1073741824L;
                num=0;
            } else if (b[i]=='M' && order<=2) {
                order=3;
                ack= ack + num*1048576L;
                num=0;
            } else if (b[i]=='K' && order<=3) {
                order=4;
                ack= ack + num*1024L;
                num=0;
            } else if (b[i]=='B' && order<=4) {
                order=5;
                ack= ack + (long) num;
                num=0;
            } else throwException( true, value );
        }
        if (num!=0) throwException( true, value );
        return ack;
    }

    /**
     *
     */
    public String toString() {

        long ack = this.value;

        // 'G'
        long g = ack / 1073741824L;
        ack = ack - 1073741824L*g;

        // 'M'
        long m = ack / 1048576L;
        ack = ack - 1048576L*m;

        // 'K'
        long k = ack / 1024L;
        ack = ack - 1024L*k;

        // 'B'
        long b = ack;

        String s = new String("S");
        if (g>0)  s = s + g + "G";
        if (m>0)  s = s + m + "M";
        if (k>0)  s = s + k + "K";
        if (b>0)  s = s + b + "B";

        return s;
    }

    /**
     * Compares two Sizes for equality.
     */
    public boolean equals(Size x) {
        if (value == x.value) return true;
        return false;
    }

    /**
     * Compares two Sizes for equality.
     */
    public boolean equals(Object x) {
        if (x instanceof Size)
            return equals((Size)x);
        return false;
    }

    /** ---------- Restrictions ---------- */

    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
        if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,this);
    }

    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object value) throws ConfMException {
        if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,value);
    }

}

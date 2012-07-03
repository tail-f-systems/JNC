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
 * This class implements the "xs:QName" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 */
public class QName implements Serializable {

    private java.lang.String prefixValue;
    private java.lang.String nameValue;

    public QName(java.lang.String value)
        throws ConfMException {
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

    private void check() {
    }

    public java.lang.String toString() {
        return prefixValue + ":" + nameValue;
    }

    public boolean equals(QName b) {
        if (prefixValue.equals(b.prefixValue))
            if (nameValue.equals(b.nameValue))
                return true;
        return false;
    }

    public boolean equals(Object b) {
        if (b instanceof com.tailf.confm.xs.QName) {
            return equals( (com.tailf.confm.xs.QName) b);
        }
        return false;
    }


    private void parseValue(java.lang.String value)
        throws ConfMException {
        value = String.wsReplace(value);
        value = String.wsCollapse(value);

        byte[] b= value.getBytes();
        // noSpacesOrCommasAllowed
        for (int i=0;i<b.length;i++) {
            if ( b[i]==' ' || b[i]==',')
                throwException( true, value );
        }
        // split at colon
        int i=0;
        while(i<b.length && b[i]!=':') i++;
        throwException( i==b.length, value );

        prefixValue = value.substring(0,i++);
        throwException( i==b.length, value );

        nameValue = value.substring(i,b.length);

        // no more colons please
        i++;
        while(i<b.length)
            throwException( b[i++]==':', value );
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

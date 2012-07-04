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
import java.lang.reflect.Constructor;

/**
 * This class implements the "xs:union" datatype from the
 * 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 * Represents a union of different sub-types.
 * 
 */
public abstract class Union implements Serializable {

    private Object value;

    abstract protected java.lang.String[] memberTypes();

    public Union() {
    }

    /**
     * Will try to construct from a string. each union type at a time.
     */
    public Union(java.lang.String value) throws ConfMException {
        parseValue(value);
        check();
    }

    /**
     * Construct union containing the give object. Must be one of the union
     * types.
     */
    public Union(Object value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Try to set the value from string.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        parseValue(value);
        check();
    }

    /**
     * Sets the union value to the given object. Must be one of the union types.
     */
    public void setValue(Object value) throws ConfMException {
        this.value = value;
        check();
    }

    /**
     * Return the union value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * check the the value is one of the union types.
     */
    private void check() throws ConfMException {
        // TODO
    }

    /**
     * Help method to parse the value from a string.
     */
    private void parseValue(java.lang.String value) throws ConfMException {
        java.lang.String[] mtypes = memberTypes();
        for (int i = 0; i < mtypes.length; i++) {
            try {
                Class cl = Class.forName(mtypes[i]);
                Constructor c = cl
                        .getConstructor(new Class[] { java.lang.String.class });
                Object o = c.newInstance(new Object[] { value });
                this.value = o;
                return;
            } catch (Exception e) {
                // try next memberType, until we succeed
            }
        }
        throw new ConfMException(ConfMException.BAD_VALUE, value);
    }

    public java.lang.String toString() {
        return value.toString();
    }

    public boolean equals(Object value) {
        if (value instanceof Union) {
            try {
                return this.value.equals(((Union) value).value);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
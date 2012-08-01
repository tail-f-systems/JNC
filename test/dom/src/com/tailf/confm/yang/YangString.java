/*    -*- Java -*-
 *
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.yang;

/**
 * Implements the built-in YANG data type "string".
 * 
 * @author emil@tail-f.com
 */
public class YangString extends BaseString {

    /**
     * Generated serial version UID, to be changed if this class is modified in
     * a way which affects serialization. Please see:
     * http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678
     */
    private static final long serialVersionUID = 7197127324640511939L;

    /**
     * Creates a YangString object from a java.lang.String.
     * 
     * @param value The Java String.
     */
    public YangString(String value) {
        super(value);
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangString or java.lang.String;
     *         false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangString || obj instanceof String;
    }

}
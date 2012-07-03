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

package com.tailf.confm.inet;
import com.tailf.confm.*;
import java.io.Serializable;

/**
 * This class implements the "port-number" datatype from the
 * 'urn:ietf:params:xml:ns:yang:inet-types' namespace.
 * <p>
 * A PortNumber is a synonym for "xs:unsignedLong".
 * Represents an unsigned 64-bit integer.
 */
public class PortNumber extends com.tailf.confm.xs.UnsignedLong implements Serializable {

    public PortNumber(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    public PortNumber(long value) throws ConfMException {
        super(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
        super.setValue(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(long value) throws ConfMException {
        super.setValue(value);
        check();
    }


    private void check() throws ConfMException {
    }


}

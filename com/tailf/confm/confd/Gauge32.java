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
 * This class implements the "confd:Gauge32 datatype from
 * the 'http://tail-f.com/ns/confd/1.0' namespace.
 * <p>
 * A Gauge32 is a synonym for "xs:unsignedInt".
 * Represents an unsigned 32-bit integer.
 * They are inherited from the SNMP world for
 * flexibility in modelling.
 *
 */
public class Gauge32 extends com.tailf.confm.xs.UnsignedInt implements Serializable {

    public Gauge32(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    public Gauge32(long value) throws ConfMException {
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

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
 * This class implements the "confd:Decimal64 datatype from the
 * 'http://tail-f.com/ns/confd/1.0' namespace.
 * <p>
 * A Decimal64 is a synonym for "xs:unsignedLong". Represents an unsigned 64-bit
 * integer.
 * 
 */
public class Decimal64 extends com.tailf.confm.xs.UnsignedLong implements
        Serializable {

    public Decimal64(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    public Decimal64(long value) throws ConfMException {
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

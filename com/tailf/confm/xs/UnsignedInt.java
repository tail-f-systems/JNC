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
 * This class implements the "xs:unsignedInt" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 * Represents an unsigned 32-bit integer.
 *
 */
public class UnsignedInt extends Integer implements Serializable {

    public UnsignedInt(java.lang.String value) throws ConfMException {
        super(value);
        check();
    }

    public UnsignedInt(long value) throws ConfMException {
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
        minInclusive(0);
        maxInclusive( 0x0ffffffffL );
    }

}

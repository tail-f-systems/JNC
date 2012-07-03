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
import com.tailf.inm.*;
import java.io.Serializable;

/**
 * This class implements the "inetPortNumber" datatype from
 * the 'http://tail-f.com/ns/confd/1.0' namespace.
 *
 */
public class InetPortNumber
    extends com.tailf.confm.xs.UnsignedShort implements Serializable {

    public InetPortNumber(String value) throws ConfMException {
        super(value);
        check();
    }

    public InetPortNumber(int value) throws ConfMException {
        super(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(String value) throws ConfMException {
        super.setValue(value);
        check();
    }

    /**
     * Sets the value.
     */
    public void setValue(int value) throws ConfMException {
        super.setValue(value);
        check();
    }

    public void check() throws ConfMException {
    }

}

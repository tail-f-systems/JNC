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
 * This class implements the "xs:double" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 */
public class Double extends Decimal implements Serializable {
    
    public Double(java.lang.String value) throws ConfMException {	
	super(value);
        check();
    }

    public Double(double value) throws ConfMException {	
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
    public void setValue(double value) throws ConfMException {
        super.setValue(value);
	check();
    }

    /**
     * Check the value.
     * Put restrictions on the value here.
     */
    private void check() throws ConfMException {
    }

}

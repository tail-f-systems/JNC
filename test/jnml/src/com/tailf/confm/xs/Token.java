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
 * This class implements the "xs:token" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 */
public class Token extends NormalizedString implements Serializable {
    
    public Token(java.lang.String value) throws ConfMException {	
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


    private void check() throws ConfMException {        
        wsCollapse();
    }
    
    
    /**
     * Restriction that says that the String must 
     * have no spaces or commas.
     *
     * Used by xs:Name and xs:NMTOKEN
     **/
    protected void noSpacesOrCommasAllowed(java.lang.String value) 
	throws ConfMException { 
	byte[] s= value.getBytes();
	for (int i=0;i<s.length;i++) {
	    if ( s[i]==' ' || s[i]==',')  
		throwException( true );
	}
    }

    
    
}

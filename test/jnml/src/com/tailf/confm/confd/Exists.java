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
 * This class implements an existance element. 
 *
 */
public class Exists implements Serializable {

    public Exists() throws ConfMException {	
    }
    
    public java.lang.String toString() {
	return "";
    }
    
    public boolean equals(Exists value) {
	if (value != null) return true;
	else return false;
    }

    public boolean equals(Object value) {
        if (value == null) return false;
	if (value instanceof Exists) 
            return true;
	return false;
    }

}

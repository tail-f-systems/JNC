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
 * This class implements the "xs:list from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 * 
 * Represents a list of items
 *
 */
public abstract class List implements Serializable {
    
    private Object[] values;
    
    abstract public java.lang.String itemType();
    
    public List() {
    }
    
    /**
     * Construct list from string of space separated values
     */
    public List(java.lang.String value) throws ConfMException {	
	value = String.wsCollapse(value);
	parseValue(value);
	check();
    }

    /**
     * Construct list from array of values
     */ 
    public List(Object[] vals) throws ConfMException {
	this.values = vals;
	check();
    }

    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
	value = String.wsCollapse(value);
	parseValue(value);
	check();
    }
    
    /**
     * Sets the value.
     */
    public void setValue(Object[] vals) throws ConfMException {
	this.values = vals;
	check();
    }
    
    /**
     * Returns an array of values.
     */
    public Object[] getValue() {
	return values;
    }
    
    
    private void check() {
    	// check that itemType is correct first?
	// TODO
    }


    public java.lang.String toString() {
	java.lang.String s = new java.lang.String();
	boolean space= false;
	for (int i=0;i<values.length;i++)  {
	    if (space) s=s+" ";
	    space=true;	    
	    s= s + values[i];
	}
	return s;
    }

    
    public boolean equals(Object value) {	
	if (value instanceof List) {
	    List list2= (List) value;
	    if (list2.values.length != this.values.length) return false;
	    
	    for (int i=0;i<values.length;i++) {
		if ( ! this.values[i].equals(list2.values[i]))
		    return false;
	    }
	    return true;
	}
	return false;
    }    

    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
	if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,this);
    }

    /** ---------- Restrictions ---------- */

    /**
     * xs:maxLength defines a maximum length measured in number of characters
     * or bytes.
     */
    protected void maxLength(int len) throws ConfMException {
        throwException( values.length > len);
    }

    /**
     * Help method to parse value from String
     *
     */
    private void parseValue(java.lang.String value)
	throws ConfMException {
	try {
	    Class cl= Class.forName( itemType() );
	    Constructor c= cl.getConstructor( new Class[] { java.lang.String.class });
	    // Collapse string first
	    java.lang.String s2= String.wsCollapse(value);
	    // tokenize value
	    java.lang.String[] toks= s2.split(" ");
	    values = new Object[ toks.length ];
	    for (int i=0;i<toks.length;i++) {
		Object o = c.newInstance( new Object[] { toks[i] } );
		values[i]= o;
	    }
	} catch (Exception e) {
	    throw new ConfMException(ConfMException.BAD_VALUE,this);
	}	
    }
    
}

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
 * This class implements the "xs:decimal" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 */
public class Decimal implements Serializable {

    private double value;    
    
    public Decimal(java.lang.String value) {	
	value = String.wsCollapse(value);
	this.value = java.lang.Double.parseDouble(value);
        check();
    }
    
    public Decimal(double value) {	
	this.value = value;
	check();
    }
    
    /**
     * Sets the value.
     */
    public void setValue(java.lang.String value) throws ConfMException {
	value = String.wsCollapse(value);
	this.value = java.lang.Double.parseDouble(value);
	check();
    }

    /**
     * Sets the value.
     */
    public void setValue(double value) throws ConfMException {
        this.value = value;
	check();
    }

    /**
     * Gets the value space.
     */
    public double getValue() {
	return value;
    }


    private void check() {
    }

    public java.lang.String toString() {
	return new java.lang.Double(value).toString();
    }

    public boolean equals(double value) {
	if (this.value == value) return true;
	else return false;
    }

    public boolean equals(Decimal value) {
	return value.equals( this.value );
    }
    
    public boolean equals(Object value) {
	if (value instanceof Decimal) 
	    return ((Decimal)value).equals(this.value);
	return false;
    }


    /** ---------- Restrictions ---------- */

    /**
     * xs:minInclusive defines a minimum value that can be reached.     
     */
    protected void minInclusive(double restriction) throws ConfMException {
	throwException(value < restriction);
    }    

    /**
     * xs:minExclusive defines a minimum value that cannot be reached.     
     */
    protected void minExclusive(double restriction) throws ConfMException {
	throwException(value <= restriction);
    }
        
    /**
     * xs:maxExclusive defines a maximum value that cannot be reached.     
     */
    protected void maxInclusive(double restriction) throws ConfMException {
	throwException(value > restriction);
    }

    /**
     * xs:maxExclusive defines a minimum value that cannot be reached.     
     */
    protected void maxExclusive(double restriction) throws ConfMException {
	throwException(value >= restriction);
    }

    /**
     * xs:minLength defines a minimum length measured in number of characters
     * or bytes.
     */
    protected void minLength(int len) throws ConfMException {
	throwException( toString().length() < len);
    }

    /**
     * xs:maxLength defines a maximum length measured in number of characters
     * or bytes.
     */
    protected void maxLength(int len) throws ConfMException {
	throwException( toString().length() > len);
    }
    
    /**
     * xs:fractionDigits
     */
    protected void fractionDigits(int digits) 
        throws ConfMException {
        throwException(numFractionDigits(toString()) > digits);
    }

    /**
     * xs:totalDigits
     */
    protected void totalDigits(int digits) 
        throws ConfMException {
        throwException(numTotalDigits(toString()) > digits);
    }

    /**
     * xs:enumeration
     */
    protected boolean enumeration(double value) {
	if (this.value == value) return true;
	else return false;
    }

    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
	if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,this);
    }

    
    /** ---------- package private --------- */

    /**
     * count xs:fractionDigits
     * uses the lexical value representation
     */
    static int numFractionDigits(java.lang.String lexical) {
        byte[] s= lexical.getBytes();
        int cpos= lexical.indexOf('.');
        if (cpos==-1) {
            // no '.' in String
            return 0;
        }
        int p= cpos+1;
        int last_known= cpos;
        while( p<s.length ) {
            if (s[p]!='0') last_known= p;
            p++;
        }
        int fraction_digits= last_known-cpos;
        return fraction_digits;        
    }


    /**
     * count xs:totalDigits
     * uses the lexical value representation
     */
    static int numTotalDigits(java.lang.String lexical) {
        byte[] s= lexical.getBytes();
        int totalDigits=0;
        int i=0;
        if (s[i]=='-') i++;
        while(i<s.length && s[i]!='0') i++;
        while(i<s.length && s[i]!='.') { i++; totalDigits++; }
        int cpos= i;
        int last_known= i;
        i++;
        while( i<s.length ) {
            if (s[i]!='0') last_known= i;
            i++;
        }
        totalDigits = totalDigits + last_known - cpos;
        return totalDigits;
    }
    
}

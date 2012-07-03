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
import java.text.NumberFormat;

/**
 * This class implements the "xs:duration" datatype from
 * the 'http://www.w3.org/2001/XMLSchema' namespace.
 *
 * 
 *
 */
public class Duration implements Serializable {

    private boolean negative = false;
    private int years;
    private int months;
    private int days;
    private int hours;
    private int mins;
    private int secs;
    private int secs_fraction;
    private int secs_fraction_digits;
    
    public Duration(java.lang.String value) throws ConfMException {		
	value = String.wsCollapse(value);
	parseValue(value);
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
     * Gets the value.
     */
    public java.lang.String getValue() {
	return toString();
    }

    private void check() {
    }
    
    /**
     * Parse a Size String into a long value.
     *
     */ 
    private void parseValue(java.lang.String value) throws ConfMException {	
	byte[] b = value.getBytes();
	
	int num=0;
	// order 'P'=0, 'Y'=1, 'M'=2, 'D'=3, 'T'=4, 'H'=5, 'Min'=6, 'S'=7
	int i =0;
	if (b[i]== '-') { negative = true; i++; }
	if (b[i++]!='P') 
	    throwException( true, value );
	
	int order = 1;
	
	while( i<b.length && order<=4 ) {
	    if (b[i]>='0' && b[i]<='9') {
		num=num*10+ b[i]-'0';
	    } else if (b[i]=='Y' && order<=1) {
		order= 2;
		years = num;
		num=0;
	    } else if (b[i]=='M' && order<=2) {
		order=3;
		months = num;
		num=0;
	    } else if (b[i]=='D' && order<=3) {
		order=4;
		days= num;
		num=0;
	    } else if (b[i]=='T' && order<=4) {
		order=5;
		if (num!=0) throwException( true, value );
	    } else throwException( true, value );
	    i++;
	}
	while( i<b.length ) {
	    if (b[i]>='0' && b[i]<='9') {
		num=num*10+ b[i]-'0';
	    } else if (b[i]=='H' && order<=5) {
		order= 6;
		hours = num;
		num=0;
	    } else if (b[i]=='M' && order<=6) {
		order=7;
		mins = num;
		num=0;
	    } else if (b[i]=='S' && order<=7) {
		order=8;
		secs= num;
		secs_fraction_digits=0;
		secs_fraction=0;
		num=0;
	    } else if (b[i]=='.') {
		order = 8;
		secs = num;
		num = 0;
		int fraction = 0;
		secs_fraction_digits =0;
		i++;
		int value_pos = 1000000; // microsecs
		while (i<b.length && b[i]>='0' && b[i]<='9' && secs_fraction_digits<6) {
		    secs_fraction_digits++;		    
		    secs_fraction= secs_fraction + value_pos * ( b[i++] - '0');
		    value_pos = value_pos / 10;
		} 
		// mask off, and ignore extra numbers
		while (i<b.length && b[i]>='0' && b[i]<='9') i++;		
		if (b[i]!='S') throwException( true, value );		
	    } else  {
		throwException( true, value );
	    }
	    i++;
	}   
	if (num != 0) 
	    throwException( true, value );
    }


    /**
     * Formats the duration into a String.
     *
     */
    public java.lang.String toString() {
	java.lang.String s =  new java.lang.String();
	if (negative) s = s + "-P";
	else s = s + "P";

	if (years > 0) s = s + years + "Y";
	if (months >0) s = s + months + "M";
	if (days >0 )  s = s + days + "D";
	
	if (hours >0 || mins>0 || secs>0 || secs_fraction>0 )
	    {
		s = s + "T";
		if (hours>0) s= s+ hours + "H";
		if (mins>0) s= s+ mins+ "M";
		if (secs>0 || secs_fraction>0) {
		    s = s + secs;
		    if (secs_fraction >0 && secs_fraction_digits>0) {
			java.lang.String fr = new java.lang.Integer(secs_fraction).toString();
			fr = fr.substring(0,secs_fraction_digits);
			s = s + "." + fr;
		    }
		    s = s + "S";
		} 
	    }	
	return s;
    }
    
    
    /**
     * Compares two Durations for equality.
     */
    public boolean equals(Duration value) {
	if (this.negative == value.negative && 
	    this.years == value.years &&
	    this.months == value.months && 
	    this.days == value.days && 
	    this.hours == value.hours && 
	    this.mins == value.mins && 
	    this.secs == value.secs) 
	    return true;
	else return false;
    }

    
    public boolean equals(Object value) {
	if (value instanceof Duration) 
	    return ((Duration)value).equals(this);
	return false;
    }
    

    /** ---------- Restrictions ---------- */

    
    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v) throws ConfMException {
	if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,this);
    }
    
    /**
     * xs:enumeration
     */
    protected boolean enumeration(java.lang.String value) {
	if ( toString().equals(value)) return true;
	else return false;
    }
    
    /**
     * Assert that the value is 'false'
     * Throw an ConfMException otherwise
     */
    protected void throwException(boolean v, Object value) throws ConfMException {
	if (!v) return;
        throw new ConfMException(ConfMException.BAD_VALUE,value);
    }
    

}

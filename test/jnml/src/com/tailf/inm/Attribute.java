/**
 *  Copyright 2007 Tail-F Systems AB. All rights reserved. 
 *
 *  This software is the confidential and proprietary 
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.inm;
import java.io.Serializable;

/**
 * This class represents an attribute for an XML element. An attribute
 * belongs to a namespace and has a name and a value. An
 * attribute is typically assigned to a {@link Element} using its
 * setAttr method.
 * <p>
 * Example:
 * <pre>
 * Element sys = new Element("http://example.com/config/1.0", "sys");
 * sys.setAttr(new Attribute("enabled", "yes"));
 * </pre>
 **/

public class Attribute implements Serializable {
    /**
     * The Attribute name.
     */
    String name;

    /** 
     * The Value of the attribute (if any).
     */
    String value;

    /**
     * Namespace uri
     */
    String ns;

    /**
     *
     */
    public Attribute(String name) {
	this.name= name;
	this.value = null; // means not set.
    }

    /**
     *
     */
    public Attribute(String name, String value) {
	this.name= name;
	this.value = value;
    }
    
    /** 
     *
     */
    public Attribute(String ns,String name,String value) {
	this.ns= ns;
	this.name= name;
	this.value= value;
    }

    /**
     * Returns the value of the attribute.
     */ 
    public String getValue() {
	return value;
    }
    
    /**
     * Sets the attribute value.     
     * @param value Set the value of the attribute
     */
    public void setValue(String value) {
	trace("setValue: "+name+"=\""+value+"\"");
	this.value = value;
    }

    /**
     * Clones the attribute, returning an exact copy.
     */
    public Object clone() {
	return new Attribute(ns,name,value);
    }
    
    /**
     * Returns a string representation of this Attribute object. 
     */
    public String toString() {
	return new String("Attribute{name="+name+
			  ",ns="+ns+
			  ",value="+value+"}");
    }
    
    /**
     * Returns the XML representation of this XML attribute 
     * in the format: 
     *     prefix:attr="value"
     * The contextnode is used for finding the prefixmap.
     */
    String toXMLString(Element contextnode) {
	// NOTE! Namespace is allowed to be "" for attributes
	if (ns!=null && ns.length()>0 ) {
	    String prefix = contextnode.nsToPrefix(ns);
	    if (prefix==null) 
		return "unknown:"+name+"=\""+value+"\"";
	    if (prefix.length()>0) 
		return prefix + ":" +  name + "=\""+ value + "\"";
	}
	return name + "=\""+ value + "\"";
    }

    /**
     * Encodes the attribute, writing it to the 
     * provided out stream.
     * Similar to the toXMLString(), but without
     * the pretty printing.
     * This version of encode allows foreign attributes.
     */
    void encode(Transport out) {
	encode(out, null);
    }

    /**
     * Encodes the attribute, writing it to the 
     * provided out stream.
     * Similar to the toXMLString(), but without
     * the pretty printing.
     */
    void encode(Transport out, Element contextnode) {
	// NOTE: Namespace is allowed to be "" for attributes
	if (ns!=null && ns.length()>0) {
	    String prefix=null;
	    if (contextnode!=null)
		prefix = contextnode.nsToPrefix(ns);
	    else  // use default prefix map
		prefix = Element.defaultPrefixes.nsToPrefix(ns);
	    if (prefix==null) {
		if (contextnode!=null)
		    out.print("unknown:");
		out.print(name);
		out.print("=\"");
		out.print(value);
		out.print("\"");
		return;
	    }
	    if (prefix.length()>0) {
		out.print(prefix);
		out.print(":");
		out.print(name);
		out.print("=\"");
		out.print(value);
		out.print("\"");
		return;
	    }
	}
	out.print(name);
	out.print("=\"");
	out.print(value);
	out.print("\"");
    }
    
   /** ------------------------------------------------------------
     *  help functions
     */

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
	if (Element.debugLevel>=Element.DEBUG_LEVEL_ATTRIBUTE) 
	    System.err.println("*Attribute: "+s);
    }
}

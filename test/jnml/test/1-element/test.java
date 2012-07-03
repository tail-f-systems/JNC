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

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import com.tailf.inm.*;

public class test {
    public static int testnr = 0;
    public static int pass = 0;
    public static int fail = 0;
    public static int skip = 0;
    public static boolean processed = false;

    static public void main(String args[]) {
	int i, th, usid;

	System.err.println("---------------------------------------");
	System.err.println("Java NETCONF Manager Library tests");
	System.err.println("---------------------------------------");

	TestCase[] tests = new TestCase[] {

	    /************************************************************
	     * Test 0
	     * readFile
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {		    
		    String fname = "simple_init.xml";
		    Element tree = new XMLParser().readFile(fname);
		    print(fname+": \n" + tree.toString());
		    print(fname+": \n" + tree.toXMLString());
		}
	    },

	    /************************************************************
	     * Test 1
	     * readFile
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    String fname = "aaa_init.xml";
		    Element tree = new XMLParser().readFile(fname);
		    print(fname+": \n" + tree.toString());
		    print(fname+": \n" + tree.toXMLString());
		}
	    },

	    /************************************************************
	     * Test 2
	     * readFile
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    String fname = "community_init.xml";
		    Element tree = new XMLParser().readFile(fname);
		    print(fname+": \n" + tree.toString());
		    print(fname+": \n" + tree.toXMLString());
		}
	    },

	    /************************************************************
	     * Test 3
	     * Element.create
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    // Element.setDebugLevel(5);
		    Element t = Element.create("http://tailf.com.ns","/hosts/host");
		    t.createPath(  new Prefix("ola","http://ola"), "host/ola:kalle/ip[name='kalle',foo=42, bar=2.3]/bar");
		    t.createPath( Element.CREATE_MERGE,"host/ola:kalle/ola:ip[name='kalle'][foo=42][bar=2.3, @myattr=1]");
		    //t.createPath( Element.CREATE_MERGE,"host/ola:kalle/ola:ip[name='kalle',foo=42, bar=2.3]/ola:bar/zoo/you");
		    //t.createPath( Element.CREATE_NEW,"host/ola:kalle/ola:ip[name='kalle',foo=42, bar=2.3]");
		    //t.createPath( Element.CREATE_MERGE,"host/ola:kalle/ola:ip[name='kalle',foo=42, bar=2.4]");
		    //t.createPath( new Prefix("nisse","http://nisse"), "host/ola:kalle/ola:kaka");
		    //t.createPath( new Prefix("nisse","http://nisse1"), "host/nisse:kalle/nisse:ip[name='kalle',foo=42, bar=2.3]");
		    //t.createPath("host/kalle/ola:ip[name='kalle',foo=42, bar=2.4]");
		    print("MY TREE: "+ t.toString());	
		    print("MY XML TREE: \n"+ t.toXMLString());		    
		}
	    },

	    /************************************************************
	     * Test 4
	     * Element.create
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Element t = Element.create("http://tailf.com.ns","/hosts/");
		    Element host_kalle= Element.create("http://tailf.com.ns","host[name='kalle',ip='127.0.0.1']");
		    Element host_peter= Element.create("http://tailf.com.ns","host[name='peter'][ip='192.168.0.1']");
		    Element host_sara= Element.create("http://tailf.com.ns","host[name='sara', ip='192.168.0.1']");
		    print("insertFirst: "+ t.insertFirst(host_kalle));
		    print("insertFirst: "+ t.insertFirst(host_sara));
		    print("insertLast: " + t.insertLast(host_peter));
		    print("MY TREE: "+ t.toString());	
		    print("MY XML TREE: \n"+ t.toXMLString());
		}
	    },
		    
		    
	    /************************************************************
	     * Test 5
	     * Element.create
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    // Element.setDebugLevel(3);
		    Element t = Element.create("http://tailf.com.ns","/hosts/");
		    Element kalle= t.createPath("host[name='kalle', ip='127.0.0.1', @member='yes']");
		    // kalle.setAttr("hungry","yes");
		    t.markDelete();
		    // t.setPrefix( new Prefix("xc",Element.NETCONF_NAMESPACE));
		    // t.removePrefix("xc");
		    // t.removeMarks();
		    print("MY TREE: "+ t.toString());	
		    print("MY XML TREE: \n"+ t.toXMLString());
		}			    
	    }
	    
	};
	
	
	/************************************************************
	 * Run the tests
	 */

	int from = 0;
	int to   = tests.length;

	// from = 21;
	// to = 22;

	test.testnr = from;

	for(i = from ; i < to ; i++) 
	    tests[i].doit();

	/************************************************************
	 * Summary 
	 */
	System.err.println("---------------------------------------");
	System.err.println("  Passed:  " + test.pass);
	System.err.println("  Failed:  " + test.fail);
	System.err.println("  Skipped: " + test.skip);
	System.err.println("---------------------------------------");
	if (test.fail == 0)
	    System.exit(0);
	else
	    System.exit(1);
    }


    private static void testStart() {
	test.testnr++;
	test.processed=false;
	System.err.print("Test "+test.testnr+": ");
    }

    private static void passed() {
	if (!test.processed) {
	    test.processed = true;
	    test.pass++;
	    System.err.println("passed");
	}
    }

    private static void skipped() {
	if (!test.processed) {
	    test.processed = true;
	    test.skip++;
	    System.err.println("skipped");
	}
    }


    private static void failed(Exception e) {
	if (!test.processed) {
	    test.fail++;
	    test.processed = true;
	    System.err.println("failed");
	    System.err.println("    '"+e.toString()+"'");
	    e.printStackTrace();
	}
    }

    private static void failed(String reason) {
	if (!test.processed) {
	    test.fail++;
	    test.processed = true;
	    System.err.println("failed");
	    System.err.println("    '"+reason+"'");
	}
    }
}


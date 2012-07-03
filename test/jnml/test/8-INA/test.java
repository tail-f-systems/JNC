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
	System.err.println("Juniper NETCONF test ");
	System.err.println("---------------------------------------");

	TestCase[] tests = new TestCase[] {

	    /************************************************************
	     * Test 0
	     * connect to a real juniper router.
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    TCPSession tcp = new TCPSession("192.168.1.74", 7788,"admin", "501",
						       "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tcp );       
		    
		    // Test capas
		    print("Test Capabilities:");
		    print( sess.getCapabilities().toXMLString());

		    Element config = sess.getConfig().first();		    
		    print("Got config= "+ config.toXMLString());
		    
		    Element filter = Element.create("http://tail-f.com/ns/confspec/1.0","/config/servers/server");
		    NodeSet server = sess.getConfig( filter );
		    print("found servers: "+ server.toXMLString());
		    
		    print("Close session");
		    sess.closeSession(); 
		}			    
	    },

	};


	/************************************************************
	 * Run the tests
	 */

	int from = 0;
	int to   = tests.length;

	// from = 1;
	// to = 2;

	test.testnr = from;

	for(i = from ; i < to ; i++) 
	    tests[i].doit();
		    /* TCP */	

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


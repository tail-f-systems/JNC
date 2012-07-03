/*    -*- Java -*- 
 * 
 *  Copyright 2007 Tail-f Systems AB. All rights reserved. 
 *
 *  This software is the confidential and proprietary 
 *  information of Tail-f Systems AB.
 *
 *  $Id$
 *
 */


import java.io.*;
import java.net.*;

public abstract class TestCase {

    /**********************************************************************
     * The test case goes here
     */

    public abstract void test() throws Exception;

    /**********************************************************************
     * Redefine to return true if the test should be skipped
     */
    public boolean skip() {
	return false;
    }

    public void doit() {
	if (this.skip()) {
	    testStart();
	    skipped();
	    return;
	}
	    
	try {
	    testStart();
	    test();
	    passed();
	} catch (Exception e) {
	    failed(e);
	}
    }
    
    /**********************************************************************
     * Reporting functions
     */

    public static void print(String s) {
	System.out.println(s);
    }
    
    public static void testStart() {
	test.processed=false;
	System.err.println("TEST "+test.testnr+": ");
	test.testnr++;
    }

    public static void passed() {
	if (!test.processed) {
	    test.processed = true;
	    test.pass++;
	    System.err.println("PASSED");
	}
    }

    public static void skipped() {
	if (!test.processed) {
	    test.processed = true;
	    test.skip++;
	    System.err.println("SKIPPED");
	}
    }


    public static void failed(Exception e) {
	if (!test.processed) {
	    test.fail++;
	    test.processed = true;
	    System.err.println("FAILED");
	    System.err.println("    '"+e.toString()+"'");
	    e.printStackTrace();
	}
    }

    public static void failed(String reason) {
	if (!test.processed) {
	    test.fail++;
	    test.processed = true;
	    System.err.println("FAILED");
	    System.err.println("    '"+reason+"'");
	}
    }
}

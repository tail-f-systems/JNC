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

import com.tailf.confm.*;
import com.tailf.inm.*;

import simple.*;

public class test {
    public static int testnr = 0;
    public static int pass = 0;
    public static int fail = 0;
    public static int skip = 0;
    public static boolean processed = false;


    public static void oscmd(String s)  {
        System.out.println("oscmd(" + s + ");");
        try {
            Process p=Runtime.getRuntime().exec(s); 
            BufferedReader in=new BufferedReader(
                new InputStreamReader(p.getInputStream())); 
            String line = null;  
            while ((line = in.readLine()) != null) {  
                System.out.println(line);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        System.out.println("oscmd Done"); 
    }  

    

    static public void main(String args[]) {
	int i, th, usid;

	try {
	    Simple.enable();
	} catch (Exception e) {
	    System.err.println(e);
	    return;
	}

	System.err.println("---------------------------------------");
	System.err.println("Java NETCONF Manager Library tests");
	System.err.println("---------------------------------------");

	TestCase[] tests = new TestCase[] {

	    /************************************************************
	     * Test 0
	     * getConfig,no keys set
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
                    Simple.enable();
                    Zapp z = new Zapp();
                    z.addFoo();

                    // No keys are set                    
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr = new SSHSession(ssh);		    
		    NetconfSession sess= new NetconfSession( tr , new com.tailf.confm.XMLParser());
                    NodeSet reply = sess.getConfig(NetconfSession.RUNNING, z);
		    print("got "+ reply.toXMLString());	
                    
                    z = (Zapp)reply.first();
                    ElementChildrenIterator it = z.fooIterator();
                    while (it.hasNext()) {
                        Foo f = (Foo)it.next();
                        System.out.println("Foo " + f.toXMLString());
                    }

		    sess.closeSession();
                    
		}
	    },

	    /************************************************************
	     * Test 1
	     * getConfig, two keys are set
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
                    Simple.enable();

                    Zapp z = new Zapp();
                    z.addFoo("app1", "1");

                    // No keys are set                    
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr = new SSHSession(ssh);		    
		    NetconfSession sess= new NetconfSession( tr,new com.tailf.confm.XMLParser() );
                    NodeSet reply = sess.getConfig(NetconfSession.RUNNING, z);
                    //NodeSet reply = sess.getConfig(NetconfSession.RUNNING);
		    print("got "+ reply.toXMLString());	

                    z = (Zapp)reply.first();
                    ElementChildrenIterator it = z.fooIterator();
                    while (it.hasNext()) {
                        Foo f = (Foo)it.next();
                        System.out.println("Foo " + f.toXMLString());
                    }

		    sess.closeSession();
                
                    
		}
	    },

	    /************************************************************
	     * Test 1
	     * getConfig, one key is set
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
                    Simple.enable();

                    Zapp z = new Zapp();
                    Foo foo = z.addFoo();
                    foo.setBarValue("1"); // partial key set
                    
                    // No keys are set                    
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr = new SSHSession(ssh);		    
		    NetconfSession sess= new NetconfSession( tr, new com.tailf.confm.XMLParser() );
                    NodeSet reply = sess.getConfig(NetconfSession.RUNNING, z);
                    //NodeSet reply = sess.getConfig(NetconfSession.RUNNING);
		    print("got "+ reply.toXMLString());	

                    z = (Zapp)reply.first();
                    ElementChildrenIterator it = z.fooIterator();
                    while (it.hasNext()) {
                        Foo f = (Foo)it.next();
                        System.out.println("Foo " + f.toXMLString());
                    }

		    sess.closeSession();
                    
		}
	    }

	};        	
	
	/************************************************************
	 * Run the tests
	 */

	int from = 0;
	int to   = tests.length;
	
	// from =14;
	// to = to -1;

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


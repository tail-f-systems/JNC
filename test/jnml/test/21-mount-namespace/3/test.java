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
import com.tailf.confm.*;
import movik.sys.*;
import movik.cae.*;

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

	System.err.println("---------------------------------------");
	System.err.println("Java NETCONF Manager Library tests");
	System.err.println("---------------------------------------");

        try {
            Movik_cae_node.enable();
            Movik_system.enable();
        }
        catch (Exception e) {
            failed("can't enable()");
        }

	TestCase[] tests = new TestCase[] {


	    /************************************************************
	     * Test 0
	     * mountNamespace test
	     */
	    new TestCase() {
	        public void test() throws Exception {		

                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    NetconfSession sess =
                        new NetconfSession(tr, new com.tailf.confm.XMLParser());

                    

                    NodeSet reply = sess.getConfig(new Node());
                    Node node = (Node) reply.first();
                    System.out.println("Node = " + node.toXMLString());
                    node.deleteOperationalMode();
                    Application a = node.addApplication(
                        new Application(
                            new NameType("FOO")));
                    a.setOperStatusValue("enable");
                    movik.sys.OperationalMode m = node.addOperationalMode();
                    m.setStandardValue(
                        new SignalingStandardType("3gpp"));
                    System.out.println("Node2 = " + node.toXMLString());
                    sess.editConfig(node);


                    reply = sess.getConfig(new Node());
                    node = (Node) reply.first();
                    System.out.println("Node3 = " + node.toXMLString());
                }
            }

        };
        	
	
	/************************************************************
	 * Run the tests
	 */

	int from = 0;
	int to   = tests.length;

        to = 1;

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


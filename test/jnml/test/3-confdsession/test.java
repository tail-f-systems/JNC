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
	     * TCP, ConfDSession, withDefaults, get
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
                    TCPConnection conn = new TCPConnection( "127.0.0.1", 2023 );                    
                    conn.authenticate("admin","501","501","","/Users/ola", "");
		    TCPSession tr = new TCPSession( conn );

                    try {
                        TCPSession tr2 = new TCPSession( conn );
                        failed("should have failed. multiple sessions not supported");
                    } catch (IOException e) {
                        
                    }
                    
                    tr.addSubscriber( new DefaultIOSubscriber("my_device") );
                    
		    ConfDSession sess= new ConfDSession( tr );   
		    sess.setWithDefaults(true);
		    print("with-defaults support: "+ sess.getCapabilities().hasWithDefaults());

		    if (!sess.getCapabilities().hasWithDefaults())
			failed("session does not support :with-defaults capability!");
		    
		    if (!sess.getCapabilities().hasTransactions())
			failed("session does not support :transactions capability!");
	
		    if (!sess.getCapabilities().hasActions())
			failed("session does not support :actions capability!");
		    
		    NodeSet config = sess.get("hosts");
		    NodeSet all_host = config.get("host");
		    print("Got config= "+ all_host.toXMLString());
		    sess.closeSession(); 
		}			    
	    },


	    /************************************************************
	     * Test 1
	     * TCP, ConfDSession Transactions
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    ConfDSession sess= new ConfDSession( tr );       
		    Element sf = Element.create("http://tail-f.com/ns/simple/1.0","/hosts");
		    Element host = sf.createPath("host[name='pzzt@ip.com', numberOfServers=33, enabled='true']");
		    host.markCreate();
		    Element vega = sf.createPath("host[name='vega@tail-f.com',numberOfServers=42,enabled='false']");
		    vega.markReplace();
		    Element saturn = sf.createPath("host[name='saturn@tail-f.com',numberOfServers=49]");
		    saturn.markMerge();
		    print("my tree: "+sf.toXMLString());
		    // now read the config. to save and restore for later
		    Element old_config= sess.getConfig("hosts").first();
		    // now lets do a transaction
		    print("startTransaction");
		    sess.startTransaction(ConfDSession.RUNNING);
		    print("writing the data");
		    sess.editConfig(ConfDSession.RUNNING, sf );
		    print("prepareTransaction");
		    sess.prepareTransaction();
		    print("commitTransaction");
		    sess.commitTransaction();
		    // check running
		    print("checking config");
		    Element pzzt= sess.getConfig(ConfDSession.RUNNING,"hosts/host[name='pzzt@ip.com']").first();
		    if (pzzt == null)
			failed("expected pzzt entry to exist in running");
		    // write back the old config.		    
		    old_config.markReplace();
		    print("writing back old config to RUNNING: \n"+old_config.toXMLString());
		    sess.editConfig(NetconfSession.RUNNING, old_config);
		    sess.closeSession(); 		    
		}			    
	    },

	    /************************************************************
	     * Test 2
	     * TCP, ConfDSession - abortTransaction
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    ConfDSession sess= new ConfDSession( tr );       
		    Element sf = Element.create("http://tail-f.com/ns/simple/1.0","/hosts");
		    Element host = sf.createPath("host[name='pzzt@ip.com', numberOfServers=33, enabled='true']");
		    host.markCreate();
		    Element vega = sf.createPath("host[name='vega@tail-f.com',numberOfServers=42,enabled='false']");
		    vega.markReplace();
		    Element saturn = sf.createPath("host[name='saturn@tail-f.com',numberOfServers=49]");
		    saturn.markMerge();
		    print("my tree: "+sf.toXMLString());
		    // now read the config. to save and restore for later
		    Element old_config= sess.getConfig("hosts").first();
		    // now lets do a transaction
		    print("startTransaction");
		    sess.startTransaction(ConfDSession.RUNNING);
		    print("writing the data");
		    sess.editConfig(ConfDSession.RUNNING, sf );
		    print("abortTransaction");
		    sess.abortTransaction();
		    // check running
		    print("checking config");
		    Element pzzt= sess.getConfig(ConfDSession.RUNNING,"hosts/host[name='pzzt@ip.com']").first();
		    if (pzzt != null)
			failed("expected pzzt entry should not exist in running");
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


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
		    SSHConnection ssh = new SSHConnection("152.14.12.12", 22);
		    ssh.authenticateWithPassword( "mbj", "mortuta42");
		    SSHSession tr = new SSHSession(ssh);		    
		    tr.addSubscriber( new DefaultIOSubscriber("juniper") );
		    NetconfSession sess= new NetconfSession( tr );       
		    
		    // Test capas
		    print("Test Capabilities:");
		    print("hasValidate() --> "+sess.hasValidate());
		    print("hasConfirmedCommit() -->"+sess.hasConfirmedCommit());
		    print("hasCandidate() --> "+sess.hasCandidate());
		    print("hasUrl() --> "+sess.hasUrl());
		    print("urlSchemes:");
		    String[] prots = sess.urlSchemes();
		    for (int i=0;i<prots.length;i++) 
			print("   "+prots[i]);
		    print("");
		    
		    // get config "/system"
		    Element t = Element.create("http://xml.juniper.net/xnm/1.1/xnm","/configuration/system");
		    print("getConfig: "+ t.toXMLString());
		    Element config = sess.getConfig( t ).first();		    
		    print("Got config= "+ config.toXMLString());
		 
		    // reset candidate!
		    print("reset CANDIDATE");
		    sess.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
		    
		    // get config "/snmp"
		    t = Element.create("http://xml.juniper.net/xnm/1.1/xnm","/configuration/snmp");
		    print("getConfig: "+t.toXMLString());
		    config = sess.getConfig( t ).first();		    
		    print("Got config= "+ config.toXMLString());

		    // print the attributes at configuration
		    print("Attributes at config: ");
		    Attribute[] attrs = config.getAttrs();
		    for(int i=0; i<attrs.length; i++)
			print("  "+attrs[i]);
		    
		    print("");
		    // Update local tree
		    Element trapGroup = config.getFirst("snmp/trap-group/name");		    
		    print("trapGroup = "+trapGroup.value);		    
		    trapGroup.setValue("yyyyyyyyyyyy");
		    trapGroup.markReplace();
		    print("Updated config: "+config.toXMLString());
		    
		    // editConfig CANDIDATE
		    print("editConfig");
		    sess.editConfig(NetconfSession.CANDIDATE, config);
		    
		    // getConfig CANDIDATE
		    print("getConfig: CANDIDATE "+t.toXMLString());
		    config = sess.getConfig(NetconfSession.CANDIDATE, t ).first();	
		    print("Got config= "+ config.toXMLString());
		    
		    // Investigate the unknown attribute in trap-group
		    trapGroup= config.get("snmp/trap-group").first();
		    print("Get trap-group: "+trapGroup);
		    print("Attributes at trap-group: ");
		    Attribute inact = trapGroup.getAttr("inactive");
		    print("inactive prefix= "+inact);
		
		    // send back again
		    // editConfig CANDIDATE
		    print("editConfig");
		    sess.editConfig(NetconfSession.CANDIDATE, config);
		    
		    // print("validate1");
		    // sess.validate(NetconfSession.CANDIDATE);
		    
		    print("confirmedCommit!");
		    sess.confirmedCommit(10);
		    
		    for (int i=1;i<10;i++) {
			print("wait 15 secs....."+i);
			Thread.sleep(15000);			
			// getConfig RUNNING
			print("getConfig: RUNNING "+t.toXMLString());
			config = sess.getConfig(NetconfSession.RUNNING, t ).first();	
			print("Got config= "+ config.toXMLString());
		    }

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


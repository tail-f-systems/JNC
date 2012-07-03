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

// import com.tailf.inm.xs.*;

public class test {
    public static int testnr = 0;
    public static int pass = 0;
    public static int fail = 0;
    public static int skip = 0;
    public static boolean processed = false;

    static public void main(String args[]) {
	int i, th, usid;

	System.err.println("---------------------------------------");
	System.err.println("    Read and Write XML file test");
	System.err.println("---------------------------------------");
	
	TestCase[] tests = new TestCase[] {


	    /************************************************************
	     * Test 0
	     * Construct and access generated simple classes
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    
		    // create hosts1 tree
		    Hosts hosts1 = new Hosts();
		    Host kalle1 = hosts1.addHost( new Host("kalle"));
		    kalle1.setEnabledValue("true");
		    kalle1.setNumberOfServersValue("12");
		    kalle1.setEncryptionValue("sha256");
		    kalle1.setAddressValue("192.168.0.1");
		    kalle1.setMtuValue("1024");
		    kalle1.setFlagsValue(new MyBitsType( MyBitsType.DIRTY | MyBitsType.COMPRESSED ));		    
		    Servers kalle1_servers = kalle1.addServers();
		    kalle1.setBaudRateValue("9600");
		    kalle1.setSecPortValue("5001");
		    kalle1.setSecPort2Value("2100");
		    kalle1.setGzzValue( new GzzType( new SecPortType("5002")));
		    kalle1.setAlistValue("12 14 35 42 105");
		    HighPriorityServer hi_prio= kalle1.addHighPriorityServer();		    
		    hi_prio.setIpValue("127.0.0.1");
		    hi_prio.setPortValue("4001");
		    LowPriorityServer lo_prio = kalle1.addLowPriorityServer();
		    lo_prio.setIpValue("10.10.0.1");
		    lo_prio.setPortValue("4002");		    
		    Server server = kalle1_servers.addServer();
		    server.setIpValue("192.168.10.1");
		    server.setPortValue("1025");
		    server.setIdValue("server_id1");
		    server.setQosValue("123");		    
		    print("hosts1: \n"+ hosts1.toXMLString());
		    
		    // Enable
		    // 
		    Simple.enable();

		    String filename= "hosts1.config";
		    print("Writing FILE: "+filename);
		    hosts1.writeFile( filename );
		    print("Reading FILE (as ELEMENT): "+filename);
		    Element h1 = Element.readFile( filename );
		    print("h1 = "+ h1.toXMLString());
		    
		    print("Reading FILE (as Hosts): "+filename);
		    Container h2= (Container) Container.readFile( filename );
		    print("h2 = "+h2.toXMLString());
		    
		    print("h2.getClass() -> "+h2.getClass());		    
		    print("hosts1.equals(h2) --> "+hosts1.equals(h2));

		    print("hosts1.getValue() -> '"+hosts1.getValue()+"'");
		    print("h2.getValue() -> '"+h2.getValue()+"'");		    
		    print("h1.getValue() --> "+h1.getValue());
		    
		    print("Compare: hosts1.compare(h2) --> "+hosts1.compare(h2));
		    
		    print("CHECKSYNC!");
		    if (! hosts1.checkSync(h2)) {
			print("FAILED!");
			print("sync: "+ hosts1.sync(h2).toXMLString());
			failed("Checksyncing with file is false!");
		    }
		    else print("---> Checksync is OK!");
		}
	    }
		
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


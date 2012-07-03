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
import wdmpon.*;

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

    

    public static void run(NetconfSession sess, Msp m) throws Exception {
        Msp filter = new Msp();
        NodeSet reply = sess.getConfig(filter);
        Msp atConfd = (Msp) reply.first();

        Container d0 = Container.syncMerge(atConfd, m);
        sess.editConfig(d0);
        
        reply = sess.getConfig(filter);
        Msp atConfd2 = (Msp) reply.first();
        

        Container d1 = Container.syncMerge(atConfd2 ,atConfd);
        sess.editConfig(d1);

        reply = sess.getConfig(filter);
        Msp atConfd3 = (Msp) reply.first();
        
        if (Container.checkSync(atConfd, atConfd3) != true) 
            failed(m.toXMLString());
    }

        



    static public void main(String args[]) {
	int i, th, usid;

	System.err.println("---------------------------------------");
	System.err.println("Java NETCONF Manager Library tests");
	System.err.println("---------------------------------------");

        Wdmpon.enable();

	TestCase[] tests = new TestCase[] {


	    new TestCase() {
	        public void test() throws Exception {		
                    com.tailf.confm.XMLParser p = 
                        new com.tailf.confm.XMLParser();
                    
                    Msp m1 = (Msp)Container.readFile("x7");
                    Msp m2 = (Msp)Container.readFile("x8");
                    Container d0 = Container.syncMerge(m1 , m2);
                    System.out.println("diff " + d0.toXMLString());
                    Container d1 = Container.syncMerge(m2, m1);
                    System.out.println("diff2 " + d1.toXMLString());

                    // m1 = (Msp)Container.readFile("x1");
                    // m2 = (Msp)Container.readFile("x2");
                    // d0 = Container.syncMerge(m1 , m2);
                    // System.out.println("diff " + d0.toXMLString());
                    // d1 = Container.syncMerge(m2, m1);
                    // System.out.println("diff2 " + d1.toXMLString());


                    // m1 = (Msp)Container.readFile("x5");
                    // m2 = (Msp)Container.readFile("x6");
                    // d0 = Container.syncMerge(m1 , m2);
                    // System.out.println("diff " + d0.toXMLString());
                    // d1 = Container.syncMerge(m2, m1);
                    // System.out.println("diff2 " + d1.toXMLString());

                    
                }
            },


	    new TestCase() {
	        public void test() throws Exception {		

                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    NetconfSession sess =
                        new NetconfSession(tr, new com.tailf.confm.XMLParser());


                    NodeSet reply = sess.getConfig(new Msp());
                    Msp atConfd = (Msp) reply.first();
                    Msp c  = (Msp)atConfd.clone();
                    if (!Container.isNotElem(atConfd))
                        failed("is elem");
                    if (!Container.isNotElem(c))
                        failed("c is elem");

                    c.jinterface.getEthernet("a/8/0").addDisable();
                    System.out.println("c = " + c.toXMLString());

                    Container d0 = Container.syncMerge(atConfd, c);
                    System.out.println("diff " + d0.toXMLString());
                    Container d1 = Container.syncMerge(c, atConfd);
                    System.out.println("diff2 " + d1.toXMLString());
                }
            },


	    /************************************************************
	     * Test 0
	     * sync tests
	     */
	    new TestCase() {
	        public void test() throws Exception {		

                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    NetconfSession sess =
                        new NetconfSession(tr, new com.tailf.confm.XMLParser());


                    Msp mspp = new Msp();
                    Jinterface j = new Jinterface();
                    Ethernet e = new Ethernet("eth0");
                    Ethernet e1 = new Ethernet("eth1");
                    e1.addVlan("999");
                    mspp.addJinterface(j);
                    j.addEthernet(e);
                    j.addEthernet(e1);
                    EthernetVlan vlan = new EthernetVlan(44);
                    e.addVlan(vlan);
                    run(sess, mspp);

                    NodeSet reply = sess.getConfig(new Msp());
                    Msp atConfd = (Msp) reply.first();

                    atConfd.jinterface.addEthernet((Ethernet)e1.clone());
                    //run(sess, atConfd);


                }
            },

	    /************************************************************
	     * Test 1
	     * sync tests
	     */
	    new TestCase() {
		public void test() throws Exception {		
                    Msp filter = new Msp();
                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    NetconfSession sess =
                        new NetconfSession(tr, new com.tailf.confm.XMLParser());
                    NodeSet reply = sess.getConfig(filter);
                    System.out.println("GOT " + reply.toXMLString());
                    Msp morig = (Msp) reply.first();
                    Msp m = (Msp)morig.clone();
                    EthernetQos q = new EthernetQos();
                    q.setSchedulerValue("aaa");
                    Ethernet e = m.jinterface.getEthernet("1/1/3");
                    e.addQos(q);
                    EthernetVlan vlan = e.getVlan("3504");
                    vlan.setVidValue("2222");
                    e.deleteVlan("400");
                    e.deletePluggableModule();
                    Container d0 = Container.syncMerge(morig, m);
                    System.out.println("SEND " + d0.toXMLString());
                    //sess.editConfig(d0);
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


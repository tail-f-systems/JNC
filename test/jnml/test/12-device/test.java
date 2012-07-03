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
import simple.*;
import msp.*;
import notif.*;

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

    

    public static void run(NetconfSession sess, Mspp m) throws Exception {
        Mspp filter = new Mspp();
        NodeSet reply = sess.getConfig(filter);
        Mspp atConfd = (Mspp) reply.first();

        Container d0 = Container.syncMerge(atConfd, m);
        sess.editConfig(d0);
        
        reply = sess.getConfig(filter);
        Mspp atConfd2 = (Mspp) reply.first();
        

        Container d1 = Container.syncMerge(atConfd2 ,atConfd);
        sess.editConfig(d1);

        reply = sess.getConfig(filter);
        Mspp atConfd3 = (Mspp) reply.first();
        
        if (Container.checkSync(atConfd, atConfd3) != true) 
            failed(m.toXMLString());
    }

        



    static public void main(String args[]) {
	int i, th, usid;

	System.err.println("---------------------------------------");
	System.err.println("Java NETCONF Manager Library tests");
	System.err.println("---------------------------------------");

	try {
	    Simple.enable();
	    Msp.enable();
            Notif.enable();
	} catch (Exception e) {
	    System.out.println(e);
	    return;
	}

	TestCase[] tests = new TestCase[] {





	    /************************************************************
	     * Test 4
	     * read a notif
	     */
	    new TestCase() {

		public void test() throws Exception {		

                    DeviceUser duser = new DeviceUser(
                        "admin", "admin", "admin");
                    Device dev = new Device("mydev", duser, "127.0.0.1", 2022);
                    dev.connect("admin", 4000);
                    dev.newSession("cfg");

                    ConfDSession sess = dev.getSession("cfg");
                    
                    System.out.println("Reading a notif ");

		    NodeSet reply = sess.getStreams();
		    System.out.println("got streams:"+ reply.toXMLString());	
                    sess.createSubscription("interface");
                    Element e = sess.receiveNotification();
                    
                    System.out.println("got notif:"+ e.toXMLString());	
                    NodeSet ns = e.getChildren();

                    //System.out.println("XX " + ns.getElement(1));
                    LinkUp lup = (LinkUp)ns.getElement(1);
                    if (lup.getIfNameValue().toString().compareTo("eth2") != 0)
                        failed("expect eth2");
                    //System.out.println("Lup " + lup.toXMLString());


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


                    Mspp mspp = new Mspp();
                    Jinterface j = new Jinterface();
                    Ethernet e = new Ethernet("eth0");
                    Ethernet e1 = new Ethernet("eth1");
                    e1.addVlan("999");
                    mspp.addJinterface(j);
                    j.addEthernet(e);
                    j.addEthernet(e1);
                    Vlan vlan = new Vlan(44);
                    e.addVlan(vlan);
                    run(sess, mspp);

                    NodeSet reply = sess.getConfig(new Mspp());
                    Mspp atConfd = (Mspp) reply.first();

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
                    Mspp filter = new Mspp();
                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    NetconfSession sess =
                        new NetconfSession(tr, new com.tailf.confm.XMLParser());
                    NodeSet reply = sess.getConfig(filter);
                    System.out.println("GOT " + reply.toXMLString());
                    Mspp morig = (Mspp) reply.first();
                    Mspp m = (Mspp)morig.clone();
                    Qos q = new Qos();
                    q.setSchedulerValue("aaa");
                    Ethernet e = m.jinterface.getEthernet("1/1/3");
                    e.addQos(q);
                    Vlan vlan = e.getVlan("3504");
                    vlan.setVidValue(2222);
                    e.deleteVlan("400");
                    e.deletePluggableModule();
                    Container d0 = Container.syncMerge(morig, m);
                    System.out.println("SEND " + d0.toXMLString());
                    //sess.editConfig(d0);
                }
            },



	    /************************************************************
	     * Test 2
	     * sync tests
	     */
	    new TestCase() {
		public void test() throws Exception {		
                    Hosts filter = new Hosts();
                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    NetconfSession sess =
                        new NetconfSession(tr, new com.tailf.confm.XMLParser());
                    NodeSet reply = sess.getConfig(filter);
                    System.out.println("GOT " + reply.toXMLString());
                    Hosts h = (Hosts)reply.first();
                    Hosts h2 = (Hosts)h.clone();
                    Container d0 = Container.sync(h, h2);
                    System.out.println("d0 = " + d0);
                    if (d0 != null)
                        failed("Should be null");
                    Host host  = h2.getHost("saturn@tail-f.com");
                    System.out.println("HOST " + host.toXMLString());
                    host.setEnabledValue(false);

                    d0 = Container.sync(h, h2);
                    System.out.println("d0 = " + d0.toXMLString());

                    sess.editConfig(d0);
                    


                }
            },

                    





	    /************************************************************
	     * Test 3
	     * getConfig, subtreefilter
	     */
	    new TestCase() {

		public void test() throws Exception {		
                    DeviceUser duser = new DeviceUser(
                        "admin", "admin", "admin");
                    Device dev = new Device("mydev", duser, "127.0.0.1", 2022);
                    dev.setDefaultReadTimeout(10000);
                    dev.connect("admin", 4000);
                    dev.newSession("cfg");
                    dev.setReadTimeout("cfg", 10000);
                    
                    ConfDSession csess = dev.getSession("cfg");
                    
                    /* Now get everything within 10 secs */
                    Element e =dev.getSession("cfg").getConfig("/hosts/host").first(); 
                    System.out.println("GOT " + e.toXMLString());
                    dev.setReadTimeout("cfg", 1);
                    
                    // Check that there is no data to read now
                    if(dev.getSSHSession("cfg").ready()) {
                        throw new Exception("ready err");
                    }
                    
                    // check that server doesn't think it's closed
                    if(dev.getSSHSession("cfg").serverSideClosed())
                        throw new Exception("server not closed");
                

                    
                    try {
                        e =dev.getSession("cfg").getConfig("/").
                            first();                         
                        System.out.println("got reply in 1 ms ");
                        throw new Exception("too fast");
                    }
                    catch (Exception ex) {
                        System.out.println("Expected Exception " + ex);
                    }
                    

                    // Now we have an unclear situation, with data
                    // That's still there to be read
                    // We need to clear out all garbage data
                    // before we read anything here
                    
                    
                    dev.setReadTimeout("cfg", 10000);
                    
                    // clean up
                    //int junk = dev.getSSHSession("cfg").readUntilWouldBlock();
                    dev.getSSHSession("cfg").readOne();
                    //System.out.println("discarded " + junk);

                    // now this should work
                    dev.getSession("cfg").getConfig("/");

                    // Now stop confd
                    oscmd("make stop");

                    if (!dev.getSSHSession("cfg").serverSideClosed())
                        throw new Exception("server side should be closed");

                    // Now restart confd
                    oscmd("make cstart");
                    
                    // should still be closed
                    if (!dev.getSSHSession("cfg").serverSideClosed())
                        throw new Exception("server side should be closed2");
                    
                    
                    // Let's reconnect
                    
                    dev.close();
                    dev.connect("admin", 4000);
                    dev.newSession("cfg");

                    if (dev.getSSHSession("cfg").serverSideClosed())
                        throw new Exception("server side should not be closed");
                    // now this should work
                    dev.getSession("cfg").getConfig("/");
                    

                }
            }
            


        };
        	
	
	/************************************************************
	 * Run the tests
	 */

	int from = 0;
        //int from = tests.length -1;
	int to   = tests.length;

        //to = 1;

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














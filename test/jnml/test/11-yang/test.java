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
import yang.Counter32;
import inet.*;
import com.tailf.inm.*;
import com.tailf.confm.*;
import types.*;

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
	     * getConfig, subtreefilter
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {		
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );
		    // getConfig
		    Element subtreeFilter = Element.create("http://tail-f.com/ns/simple","/hosts");
		    NodeSet reply = sess.getConfig(subtreeFilter);
		    print("got "+ reply.toXMLString());	
		    sess.closeSession();     
		}
	    },

	    /************************************************************
	     * Test 1
	     * TCP, get, subtreefilter
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );
		    // get
		    Element subtreeFilter = Element.create("http://tail-f.com/ns/simple","/hosts");
		    NodeSet reply = sess.get(subtreeFilter);
		    print("got "+ reply.toXMLString());	
		    sess.closeSession(); 
		}
	    },
	    
	    /************************************************************
	     * Test 2
	     * SSH, get subtreefilter
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* SSH */	
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr = new SSHSession(ssh);		    
		    NetconfSession sess= new NetconfSession( tr );
		    Element subtreeFilter = Element.create("http://tail-f.com/ns/simple","/hosts");
		    NodeSet reply = sess.get(subtreeFilter);
		    print("got "+ reply.toXMLString());	
		    sess.closeSession(); 	
		}
	    },

	    /************************************************************
	     * Test 3
	     * TCP, getConfig, xpath filter
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );
		    // get
		    NodeSet reply = sess.getConfig("/hosts/host");
		    print("got "+ reply.toXMLString());	
		    sess.closeSession(); 
		}
	    },
		    
		    
	    /************************************************************
	     * Test 4
	     * TCP, editConfig-> create
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );
		    // editConfig
		    Element hosts = Element.create("http://tail-f.com/ns/simple","/hosts");
		    Element host= hosts.createPath("host[name='martin']");
		    host.createChild("enabled","false");
		    host.createChild("numberOfServers","42");
		    host.markCreate();
		    if (!sess.getCapabilities().hasValidate())
			failed("session does not support the :validate capability");
		    print("validating config");
		    sess.validate(hosts);
		    print("editConfig");
		    print("hosts= "+hosts.toXMLString());
		    sess.editConfig(hosts);
		    sess.closeSession(); 
		}			    
	    },

	    /************************************************************
	     * Test 5
	     * TCP, getconfig, subtreefilter,editconfig -> delete
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );       
		    // create subtree filter
		    Element filter = Element.create("http://tail-f.com/ns/simple","/hosts");
		    filter.createPath("host[name='martin']");
		    print("filter= "+filter.toXMLString());	
		    Element hosts = sess.getConfig(filter).first();
		    print("Got hosts= "+hosts.toXMLString());
		    hosts.markDelete("host[name='martin']");
		    print("Mark delete gives: hosts= "+hosts.toXMLString());
		    sess.editConfig(hosts);
		    sess.closeSession(); 
		}			    
	    },

	    /************************************************************
	     * Test 6
	     * TCP, NetconfSession commit
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );       
		    Element sf = Element.create("http://tail-f.com/ns/simple","/hosts");
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
		    print("locking CANDIDATE");
		    sess.lock(NetconfSession.CANDIDATE);
		    print("reset CANDIDATE, by copyConfig");
		    sess.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
		    print("writing to CANDIDATE");
		    sess.editConfig(NetconfSession.CANDIDATE, sf );
		    print("commit");
		    sess.commit();
		    print("checking config");
		    Element pzzt= sess.getConfig(NetconfSession.RUNNING,"hosts/host[name='pzzt@ip.com']").first();
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
	     * Test 7
	     * TCP, NetconfSession confirmed commit
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );       
		    Element sf = Element.create("http://tail-f.com/ns/simple","/hosts");
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
		    print("locking CANDIDATE");
		    sess.lock(NetconfSession.CANDIDATE);
		    print("reset CANDIDATE, by copyConfig");
		    sess.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
		    print("writing to CANDIDATE");
		    sess.editConfig(NetconfSession.CANDIDATE, sf );
		    sess.confirmedCommit(60);
		    print("sleeping 4 secs");
		    Thread.sleep(4000);
		    sess.commit();
		    // write back the old config.		    
		    old_config.markReplace();
		    print("writing back old config to RUNNING: \n"+old_config.toXMLString());
		    sess.editConfig(NetconfSession.RUNNING, old_config);
		    sess.closeSession(); 		    
		}			    
	    },

	    /************************************************************
	     * Test 8
	     * TCP, NetconfSession confirmed commit - ROLLBACK
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );       
		    Element sf = Element.create("http://tail-f.com/ns/simple","/hosts");
		    Element host = sf.createPath("host[name='pzzt@ip.com', numberOfServers=33, enabled='true']");
		    host.markCreate();
		    Element vega = sf.createPath("host[name='vega@tail-f.com',numberOfServers=42,enabled='false']");
		    vega.markReplace();
		    Element saturn = sf.createPath("host[name='saturn@tail-f.com',numberOfServers=49]");
		    saturn.markMerge();
		    print("my tree: "+sf.toXMLString());
		    // now lets do a transaction
		    print("locking CANDIDATE");
		    sess.lock(NetconfSession.CANDIDATE);
		    print("reset CANDIDATE, by copyConfig");
		    sess.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
		    print("writing to CANDIDATE");
		    sess.editConfig(NetconfSession.CANDIDATE, sf );
		    sess.confirmedCommit(10);
		    print("sleeping 15 secs - Server should roll back to old config");
		    Thread.sleep(15000);		    
		    print("checking config");
		    Element hosts = sess.getConfig("hosts").first();
		    print( hosts.toXMLString());
		    NodeSet pzzt= hosts.get("host[name='pzzt@ip.com']");
		    if (pzzt.size() >0)
			failed("pzzt must not be there");
		    sess.closeSession(); 		    
		}			    
	    },


	    /************************************************************
	     * Test 9
	     * TCP, lock, unlock
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr1 = new TCPSession("127.0.0.1", 2023, "admin", "501","501", "", "/Users/ola", "");
		    NetconfSession sess1= new NetconfSession( tr1 );       
		    /* SSH */
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr2 = new SSHSession(ssh);		    
		    NetconfSession sess2= new NetconfSession( tr2 );  
		    // candidate
		    print("locking CANDIDATE");
		    sess1.lock(NetconfSession.CANDIDATE);
		    Thread.sleep(1000);
		    print("unlocking CANDIDATE");
		    sess1.unlock(NetconfSession.CANDIDATE);
		    // running
		    print("locking RUNNING");
		    sess1.lock(NetconfSession.RUNNING);
		    Thread.sleep(1000);
		    print("unlocking RUNNING");
		    sess1.unlock(NetconfSession.RUNNING);
		    sess1.closeSession(); 
		}			    
	    },


	    /************************************************************
	     * Test 10
	     * TCP, lock, unlock, killSession
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr1 = new TCPSession("127.0.0.1", 2023, "admin", "501","501", "", "/Users/ola", "");
		    NetconfSession sess1= new NetconfSession( tr1 );       
		    /* SSH */
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr2 = new SSHSession(ssh);		  
		    NetconfSession sess2= new NetconfSession( tr2 );  
		    // candidate
		    print("locking CANDIDATE");
		    sess1.lock(NetconfSession.CANDIDATE);
		    Thread.sleep(1000);
		    print("session 2 tries to take lock");
		    try {
			sess2.lock(NetconfSession.CANDIDATE);
		    } catch (INMException e) {
			Element err = (Element) e.opaqueData;
			Element error_tag = err.get("self::rpc-reply/rpc-error[error-tag='lock-denied']").first();
			print("got error-tag: "+error_tag.toXMLString());
			if (error_tag==null) 
			    failed("expected 'lock-denied'");
		    }
		    print("session 1 releases lock");
		    sess1.unlock(NetconfSession.CANDIDATE);
		    print("session 2 successfully takes lock now");
		    sess2.lock(NetconfSession.CANDIDATE);
		    Thread.sleep(1000);
		    print("session 1 kills session 2");
		    sess1.killSession( sess2.sessionId );
		    print("session 1 tries to take lock");
		    sess1.lock(NetconfSession.CANDIDATE);
		    sess1.closeSession();
		}			    
	    },
	    

	    /************************************************************
	     * Test 11
	     * TCP, :url capability, deleteConfig
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501","501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );       
		    print(":url-capability = "+ sess.getCapabilities().hasUrl());
		    print(" url-schemes: ");
		    String[] urlSchemes= sess.getCapabilities().getUrlSchemes();
		    for(int i=0;i<urlSchemes.length;i++) 
			print("  *"+urlSchemes[i]);
		    if (!sess.getCapabilities().hasUrl()) 
			failed("session does not support the :url capability");
		    print("checking vega");
		    Element vega = sess.getConfig("hosts/host[name='vega@tail-f.com']").first();
		    if (vega==null) 
			failed("vega does not exist in config initially");
		    print("copy-config running to 'backup-running.xml' file");
		    sess.copyConfig(NetconfSession.RUNNING,"file:///backup-running.xml");
		    print("deleting vega");
		    Element del_vega= Element.create("http://tail-f.com/ns/simple","hosts/host[name='vega@tail-f.com']");
		    del_vega.markDelete();
		    sess.editConfig(del_vega);
		    print("config is now deleted");
		    vega = sess.getConfig("hosts/host[name='vega@tail-f.com']").first();
		    if (vega!=null) 
			failed("vega does still exist. config was not deleted.");
		    Thread.sleep(5000);
		    print("restoring");
		    sess.copyConfig("file:///backup-running.xml",NetconfSession.RUNNING);
		    print("checking ");
		    vega = sess.getConfig("hosts/host[name='vega@tail-f.com']").first();
		    if (vega==null) 
			failed("vega does not exist. config was not properly restored.");
		    print("deleteting config file: backup-running.xml");
		    sess.deleteConfig("file:///backup-running.xml");
		    sess.closeSession();
		}			    
	    },


	    /************************************************************
	     * Test 12
	     * SSH, getConfig, many
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    /* SSH */	
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr = new SSHSession(ssh);		  
		    NetconfSession sess= new NetconfSession( tr );
		    Element subtreeFilter = Element.create("http://tail-f.com/ns/simple","/hosts");
		    print("Reading hosts!");
		    Element reply = sess.get(subtreeFilter).first();
		    print("got "+ reply + "  numberOfHosts= "+ reply.getChildren().size());	
		    // create many
		    reply = null;
		    Element hosts = Element.create("http://tail-f.com/ns/simple","/hosts");
		    hosts.markReplace();
		    // int n = 100;
		    int n = 20;  // gives 20000
		    for (int j=0; j<n;j++) {
			print("building: X + "+j+" * 1000");
			for (int i=0; i<1000; i++) {
			    hosts.createPath(Element.CREATE_NEW,"host[name='"+(i+1000*j)+
					     "', enabled='true', numberOfServers=0]");
			}
		    }
		    print("Writing large config= "+ hosts + " numberOfChildren= "+ hosts.getChildren().size());
		    sess.editConfig( hosts );
		    hosts = null;  // garbage collect please
		    print("Config is now written! (sleep 3000)");
		    Thread.sleep(3000);
		    print("Will now read back all elements!");
		    Element config= sess.getConfig(subtreeFilter).first();
		    print("got: "+config+ " numberOfChildren="+ config.getChildren().size());
		    print("now delete them - cleanup");
		    subtreeFilter.markDelete();
		    sess.editConfig( subtreeFilter );
		    print("Done!");
		    sess.closeSession(); 	
		}
	    },


	    /************************************************************
	     * Test 13
	     * partialLock
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Transport tr1 = new TCPSession("127.0.0.1", 2023, "admin", "501","501", "", "/Users/ola", "");
		    NetconfSession sess1= new NetconfSession( tr1 );
		    Transport tr2 = new TCPSession("127.0.0.1", 2023, "admin", "501","501", "", "/Users/ola", "");
		    NetconfSession sess2= new NetconfSession( tr2 );
		    
		    print("Creating hosts");
		    Element hosts = Element.create("http://tail-f.com/ns/simple","/hosts");
		    hosts.markReplace();
		    int n = 1;
		    for (int j=0; j<n;j++) {
			for (int i=0; i<10; i++) {
			    hosts.createPath(Element.CREATE_NEW,"host[name='"+(i+1000*j)+
					     "', enabled='true', numberOfServers=0]");
			}
		    }
		    print("Writing config= "+ hosts + " numberOfChildren= "+ hosts.getChildren().size());
		    sess1.editConfig( hosts );
		    print("Will now read back all elements!");
		    Element config= sess1.getConfig( "/hosts" ).first();
		    print("got: "+config.toXMLString());
		    // take partial lock
		    int lockId = sess1.lockPartial("/hosts");		    
		    print("lockId= "+lockId);
		    // fail to take lock from sess2
		    try {
			print("try to take lock from second session");
			sess2.lockPartial("/hosts");
			failed("partial lock should not be possible to take from second session");
		    } catch (Exception e) {
			print("ok - failed to take lock from second session"+ e);
		    }
		    print("Release the lock");
		    sess1.unlockPartial(lockId);
		    print("Done!");
		    sess1.closeSession(); 	
		}
	    },


	    /************************************************************
	     * Test 14
	     * error handling
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {		
		    /* TCP */	
		    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
						    "501", "", "/Users/ola", "");
		    NetconfSession sess= new NetconfSession( tr );
		    // getConfig
		    Element subtreeFilter = Element.create("http://tail-f.com/ns/simple_strange/","/strange_host");
		    try {
			sess.editConfig(subtreeFilter);
			failed("expected error, got 'ok' from bad editConfig");
		    } catch (INMException e) {
			if (e.rpcErrors==null)
			    failed("expected rpc-error");
			RpcError re= e.rpcErrors[0];
			print("got: "+re);
		    }
		    sess.closeSession();     
		}
	    },

	    /************************************************************
	     * Test 15
	     * SSH, dyn top level
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
                    /* SSH */	
		    SSHConnection ssh = new SSHConnection("127.0.0.1", 2022);
		    ssh.authenticateWithPassword( "admin", "admin");
		    Transport tr = new SSHSession(ssh);		  
		    NetconfSession sess= new NetconfSession( tr );
		    Element subtreeFilter = Element.create("http://tail-f.com/ns/simple","/storage");
		    print("Reading storage!");
		    Element reply = sess.get(subtreeFilter).first();
		    print("got "+ reply);;	
		    // create a few
		    reply = null;
		    Element storage = Element.create("http://tail-f.com/ns/simple","/storage[alias='kalle',type='foo']");
		    storage.markCreate();
		    if (!sess.getCapabilities().hasValidate())
			failed("session does not support the :validate capability");
		    print("validating config");
		    sess.validate(storage);
		    print("editConfig");
		    print("storage= "+storage.toXMLString());
		    sess.editConfig(storage);

		    print("Reading storage!");
		    reply = sess.get(subtreeFilter).first();
		    print("got "+ reply);;	

		    sess.closeSession(); 
		}			    
	    },

        /************************************************************
         * Test 16
         * getConfig and edit of YANG types
         */
        new TestCase() {
            // public boolean skip() { return true; }
            public void test() throws Exception {           
                /* TCP */   
                Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
                                              "501", "", "/Users/ola", "");
                NetconfSession sess= new NetconfSession( tr );
                Element subtreeFilter =
                    Element.create("http://tail-f.com/ns/types", "/types");
                // getConfig
                NodeSet reply = sess.getConfig(subtreeFilter);
                print("got1 "+ reply.toXMLString()); 
                // editConfig
                Types t = new Types();
                //
                t.addBuiltin();
                t.builtin.setJbooleanValue(false);
                //
                YangTypes yangTypes = t.addYangTypes();
                yang.Counter32 counter32;
                counter32 = new yang.Counter32("33");
                // WHY???
                //counter32 = new yang.Counter32(33L);
                t.yangTypes.setCounter32Value(counter32);
                //
                t.addInetTypes();
                t.inetTypes.setIpv4AddressValue("1.2.3.4");
                //
                t.addInlineTypes();
                t.inlineTypes.addServers();
                // WHY???
                /*
                t.inlineTypes.servers.addServer(new Server("bongo"));
                t.inlineTypes.servers.addServer(
                    new com.tailf.confm.xs.String("bongo"));
                */
                //
                sess.editConfig(t);
                reply = sess.getConfig(subtreeFilter);
                print("got2 "+ reply.toXMLString());
                sess.closeSession();     
            }
        }
    };
	
	/************************************************************
	 * Run the tests
	 */

	int from = 0;
	int to   = tests.length;
	
	//from =15;
	//to = 17;

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


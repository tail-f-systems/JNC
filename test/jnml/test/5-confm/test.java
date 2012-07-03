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
import com.tailf.confm.confd.*;
import com.tailf.inm.*;
import simple.*;
import multi.*;

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
    System.err.println("    ConfM tests");
    System.err.println("---------------------------------------");

    try {
	Simple.enable();
	Multi.enable();
    } catch (INMException e) {
	failed("enabling error: "+e);
	return;
    }


    TestCase[] tests = new TestCase[] {
        /************************************************************
         * RT3556
         */
        new TestCase() {
            // public boolean skip() { return true; }
            public void test() throws Exception {
                Rt3556 root = new Rt3556();
                root.setFooValue("1");
                root.setFooValue("2");
                try {
                    root.setFooValue("3");
                } catch (ConfMException e) {
                    if (e.errorCode != ConfMException.BAD_VALUE)
                        failed("errorCode mismatch: "+e.errorCode);
                }
            }
        },
        /************************************************************
         * RT3254
         */
        new TestCase() {
            // public boolean skip() { return true; }
            public void test() throws Exception {
                Rt3254 root = new Rt3254();
                root.setFooValue("-1");
                root.setBarValue("1");
            }
        },
      /************************************************************
       * RT3363
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
          Transport tr =
            new TCPSession("127.0.0.1", 2023, "admin", "501",
                           "501", "", "/Users/ola", "");
          NetconfSession sess =
            new NetconfSession(tr, new com.tailf.confm.XMLParser());
            
          // Get current config with subtree filter
          Element subtreeFilter = new Rt3363();
          Element.create("http://tail-f.com/ns/simple/1.0", "/rt3363");
          NodeSet reply = sess.getConfig(subtreeFilter);
          Rt3363 currentConfig = (Rt3363)reply.first();
          print("**** current config:\n "+currentConfig.toXMLString()); 
          NodeSet children = currentConfig.msp.platform.getChildren();
          print(children.getElement(0).toString());

          if (!children.getElement(0).getChildren().getElement(0).getValue().toString().equals("a0"))
              failed("SHOULD BE EQUAL1");

          if (!children.getElement(1).getChildren().getElement(0).getValue().toString().equals("b0"))
              failed("SHOULD BE EQUAL2");

          if (!children.getElement(2).getChildren().getElement(0).getValue().toString().equals("a1"))
              failed("SHOULD BE EQUAL3");

          if (!children.getElement(3).getChildren().getElement(0).getValue().toString().equals("b1"))
              failed("SHOULD BE EQUAL4");

          if (!children.getElement(4).getChildren().getElement(0).getValue().toString().equals("c1"))
              failed("SHOULD BE EQUAL5");

          if (!children.getElement(5).getChildren().getElement(0).getValue().toString().equals("d1"))
              failed("SHOULD BE EQUAL6");

          if (!children.getElement(6).getChildren().getElement(0).getValue().toString().equals("a2"))
              failed("SHOULD BE EQUAL7");

          if (!children.getElement(7).getChildren().getElement(0).getValue().toString().equals("b2"))
              failed("SHOULD BE EQUAL8");

          if (!children.getElement(8).getChildren().getElement(0).getValue().toString().equals("c2"))
              failed("SHOULD BE EQUAL9");

          sess.closeSession();           
        }
      },

      // test 1
      /************************************************************
       * RT2870
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
          Rt2870 rt2870 = new Rt2870();
          Msp msp = rt2870.addMsp();
          SpanningTree st = new SpanningTree();
          msp.addSpanningTree(st);
          Mst mst = new Mst(22);
          mst.setForceVersionValue("foo");
          st.addMst(mst);

          int i = 0;
          while (i++ < 2) {
            Instance instance = new Instance(i);
            instance.setPriorityValue("42");
            mst.addInstance(instance);
            Vlans vlans = new Vlans("33");
            instance.addVlans(vlans);
          }

          mst.setMaxAgeValue("1");
          print("**** order config:\n "+rt2870.toXMLString());
        }
      },
            
      // test 2
      /************************************************************
       * RT2852
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {

          print("**** 1");

          Transport tr =
            new TCPSession("127.0.0.1", 2023, "admin", "501",
                           "501", "", "/Users/ola", "");

          print("**** 2");



          NetconfSession sess =
            new NetconfSession(tr, new com.tailf.confm.XMLParser());
         
          print("**** 3");


          // Get current config with subtree filter
          Element subtreeFilter = new Rt2852();

          print("**** 4");


          Element.create("http://tail-f.com/ns/simple/1.0",
                         "/rt2852");

          print("**** 5");

          NodeSet reply = sess.getConfig(subtreeFilter);
                    
          print("**** 6");

          Rt2852 currentConfig = (Rt2852)reply.first();

          print("**** 7");

          print("**** current config:\n "+
                currentConfig.toXMLString()); 
                    
          // Save current config
          Rt2852 oldConfig = (Rt2852)currentConfig.clone();
                    
          print("**** 8");

          // Update current config
          QueueProfile qp = currentConfig.getQueueProfile("bb");
          qp.setNumberOfQueuesValue("8");
          print("**** new config:\n "+currentConfig.toXMLString());

          print("**** 9");
                    
          // Extract sync
          Element sync = oldConfig.sync(currentConfig);


          print("**** 9a: "+sync);


          print("**** sync config:\n "+sync.toXMLString());

          print("**** 10");

          // Apply sync
          sess.editConfig(sync);
                    
          print("**** 11");

          // Get config again
          NodeSet afterSync = sess.getConfig(subtreeFilter);
          print("**** after sync:\n "+afterSync.toXMLString());

          print("**** 12");

          // Assert
          if (!Container.checkSync(currentConfig,
                                   (Rt2852)afterSync.first()))
            failed("not equal:\n"+currentConfig.toXMLString()+"\nand\n"+afterSync.toXMLString());

          print("**** 20");

                    
          sess.closeSession();           
        }
      },

      // test 3
      /************************************************************
       * multi
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
          Transport tr =
            new TCPSession("127.0.0.1", 2023, "admin", "501",
                           "501", "", "/Users/ola", "");
          NetconfSession sess =
            new NetconfSession(tr, new com.tailf.confm.XMLParser());
         
          // this is just here to make it possible to
          // run the test many times

          try {
            Foo f1 = new Foo("key1");
            f1.markDelete();
            sess.editConfig(f1);
          } catch (Exception ee) {
            ;
          }

          NodeSet reply = sess.getConfig();
          NodeSet origfoos = new NodeSet();
          NodeSet newfoos = new NodeSet();
          print("multi config: " + reply.toXMLString());
          for (int i =0; i<reply.size(); i++) {
            Element e = (Element)reply.get(i);
            if (e instanceof Foo) {
              System.out.println("FOO " + e.toXMLString());
              origfoos.add(e);
              newfoos.add(e);
            }
          }
                    
          Foo f = new Foo("key1");
          f.setFintValue(77);
          newfoos.add(f);
                    
          boolean b = Container.checkSync(origfoos, newfoos);
          System.out.println("bb = " + b);
          if (b) failed("Should be differenet");
          NodeSet diff = Container.sync(origfoos, newfoos);
          System.out.println("DIFF " + diff.toXMLString());

          // So let's send that edit-config away
          sess.editConfig(diff);
          reply = sess.getConfig();

          // pick out the foos again
          origfoos = new NodeSet();
          for (int i =0; i<reply.size(); i++) {
            Element e = (Element)reply.get(i);
            if (e instanceof Foo) {
              System.out.println("FOO " + e.toXMLString());
              origfoos.add(e);
            }
          }
                    
          // origfoos and newfoos should be equal
          b = Container.checkSync(origfoos, newfoos);
          System.out.println("bb2 = " + b);
          if (!b) failed("should be eq");
          b = Container.checkSync(newfoos, origfoos);
          System.out.println("bb3 = " + b);
          if (!b) failed("should be eq");
        }
      },

      /************************************************************
       * Test 4
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
        
          // create hosts2 subtree
          Hosts hosts2 = new Hosts();
          Host kalle2 = hosts2.addHost( new Host("kalle"));
          kalle2.setEnabledValue("true");
          print("hosts2: \n"+ hosts2.toXMLString());
        
          // create hosts3 subtree
          Hosts hosts3 = new Hosts();
          Host lisa = hosts3.addHost( new Host("lisa"));
          print("hosts3: \n"+ hosts3.toXMLString());
        
          // equals on hosts
          if (! hosts1.equals(hosts2)) 
            failed("equals failed. hosts1 != hosts2");
          if (! hosts1.equals(hosts3)) 
            failed("equals failed. hosts1 != hosts3");
        
          // equals on host level
          if (! kalle1.equals(kalle2)) 
            failed("equals failed. kalle1 != kalle2");
          if (! kalle1.equals(lisa)) 
            failed("equals failed. kalle1 == lisa (they should both be host)");
        
          // compare hosts
          if ( hosts1.compare(hosts1) != 0)
            failed("compare hosts1 with (self) hosts1 failed");       
          if (  hosts1.compare(hosts2) !=0 )
            failed("compare hosts1 with hosts2 is not 0");
          if ( hosts1.compare(hosts3) != 0 )
            failed("compare hosts1 with hosts3 is not 0");      
        
          // compare host
          if ( kalle1.compare(kalle1) != 0) 
            failed("compare kalle1 with kalle1 (self) is not 0"); 
          if ( kalle1.compare(kalle2) != 1) 
            failed("compare kalle1 with kalle2 is not 1"); 
          if ( kalle1.compare(lisa) != -1) 
            failed("compare kalle1 with lisa is not -1");
        
        
          // checkSync 
          if (! Container.checkSync(hosts1, hosts1)) 
            failed("checkSync hosts1, hosts1 failed");
          if ( Container.checkSync(hosts1, hosts2)) 
            failed("checkSync hosts1, hosts2 failed");
          if ( Container.checkSync(hosts1, hosts3)) 
            failed("checkSync hosts1, hosts1 failed");
        
          // sync
          Element sync1= Container.sync(hosts1, hosts1); 
          print("sync between hosts1 and hosts1 (self)");
          if (sync1 != null) 
            failed("sync between hosts1 and hosts1 (self) should be null");
        
          Element sync2= Container.sync(hosts1, hosts2);
          print("sync between hosts1 and hosts2");
          print( sync2.toXMLString() );
        
          Element sync3= Container.sync(hosts1, hosts3);
          print("sync between hosts1 and hosts3");
          print( sync3.toXMLString() );
        
        }
      },
      
      /************************************************************
       * Test 5
       * More sync and checkSync
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
        
          // create hosts1 tree
          Hosts hosts1 = new Hosts();
          Host lisa1 = hosts1.addHost( new Host("lisa"));
          Host kalle1 = hosts1.addHost( new Host("kalle"));
          Host kaka1 = hosts1.addHost( new Host("kaka"));
          print("hosts1: \n"+ hosts1.toXMLString());
        
          // create hosts2 subtree
          Hosts hosts2 = new Hosts();       
          Host kalle2 = hosts2.addHost( new Host("kalle"));
          kalle2.setEnabledValue("true");
          Host lisa2 = hosts2.addHost( new Host("lisa"));
          print("hosts2: \n"+ hosts2.toXMLString());
        
          print("checkSync");
          // checkSync
          if ( ! hosts1.checkSync( hosts1 ))
            failed("checkSync hosts1, hosts1 (self) failed");
          if ( hosts1.checkSync( hosts2 ))
            failed("checkSync hosts1, hosts2 failed");
        
        
          // inspect
          NodeSet unA = new NodeSet();
          NodeSet unB = new NodeSet();
          NodeSet chA = new NodeSet();
          NodeSet chB = new NodeSet();
          Container.inspect(hosts1, hosts2,unA,unB,chA,chB);
        
          print("--------------------");
          print("INSPECT");
          print("uniqueA = "+ unA.toXMLString());
          print("uniqueB = "+ unB.toXMLString());
          print("changedA = "+ chA.toXMLString());
          print("changedB = "+ chB.toXMLString());
        
          print("--------------------");
          // sync
          print("sync: hosts1, hosts2");
          Element sync= hosts1.sync(hosts2);
          print( sync.toXMLString() );
        }
      },


      /************************************************************
       * Test 6
       * More sync and checkSync (deep sync)
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
        
          // create hosts1 tree
          Hosts hosts1 = new Hosts();
          Host lisa1 = hosts1.addHost( new Host("lisa"));
          Servers lisa_servers1= lisa1.addServers();        
          lisa_servers1.addServer(new Server("192.168.0.1",6400L));
          lisa_servers1.addServer(new Server("10.10.10.1",6500L));
          Host kalle1 = hosts1.addHost( new Host("kalle"));
          Host kaka1 = hosts1.addHost( new Host("kaka"));
          print("hosts1: \n"+ hosts1.toXMLString());
        
          // create hosts2 subtree
          Hosts hosts2 = new Hosts();       
          Host kalle2 = hosts2.addHost( new Host("kalle"));
          kalle2.setEnabledValue("true");
          Host lisa2 = hosts2.addHost( new Host("lisa"));
          Servers lisa_servers2= lisa2.addServers();        
          lisa_servers2.addServer(new Server("192.168.0.1",6400L));
          lisa_servers2.addServer(new Server("10.10.10.1",6501L));
          print("hosts2: \n"+ hosts2.toXMLString());
        
          print("checkSync");
          // checkSync
          if ( ! hosts1.checkSync( hosts1 ))
            failed("checkSync hosts1, hosts1 (self) failed");
          if ( hosts1.checkSync( hosts2 ))
            failed("checkSync hosts1, hosts2 failed");
        
        
          // inspect
          NodeSet unA = new NodeSet();
          NodeSet unB = new NodeSet();
          NodeSet chA = new NodeSet();
          NodeSet chB = new NodeSet();
          Container.inspect(hosts1, hosts2,unA,unB,chA,chB);
        
          print("--------------------");
          print("INSPECT");
          print("uniqueA = "+ unA.toXMLString());
          print("uniqueB = "+ unB.toXMLString());
          print("changedA = "+ chA.toXMLString());
          print("changedB = "+ chB.toXMLString());
        
          print("--------------------");
          // sync
          print("sync: hosts1, hosts2");
          Element sync= hosts1.sync(hosts2);
          print( sync.toXMLString() );

          print("----------------------------------------");
          hosts1.setManaValue("12");
        
          // inspect
          unA = new NodeSet();
          unB = new NodeSet();
          chA = new NodeSet();
          chB = new NodeSet();
          Container.inspect(hosts1, hosts2,unA,unB,chA,chB);
        
          print("--------------------");
          print("INSPECT");
          print("uniqueA = "+ unA.toXMLString());
          print("uniqueB = "+ unB.toXMLString());
          print("changedA = "+ chA.toXMLString());
          print("changedB = "+ chB.toXMLString());
        
          print("--------------------");
          // sync
          print("sync: hosts1, hosts2");
          sync= hosts1.sync(hosts2);
          print( sync.toXMLString() );
        }
      },
            
      /************************************************************
       * Test 7
       * Test the Existance element
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
                    
          // create hosts1 tree
          print("Creating hosts1\n");
          Hosts hosts1 = new Hosts();
          Host karin1 = hosts1.addHost( new Host("karin"));
          karin1.setDisabledValue( new Confd.Exists() );
          print("hosts1: \n"+ hosts1.toXMLString());

          print("Removing 'disabled'\n");                    
          karin1.unsetDisabledValue();
          print("hosts1: \n"+ hosts1.toXMLString());

          print("Setting 'disabled'\n");
          karin1.setDisabledValue( new Confd.Exists());
          print("hosts1: \n"+ hosts1.toXMLString());
                    
          print("Creating hosts2\n");
          // create hosts2 tree
          Hosts hosts2 = new Hosts();
          Host karin2 = hosts2.addHost( new Host("karin"));
          karin2.addDisabled();
          print("hosts2: \n"+ hosts2.toXMLString());
                    
          print("inspect:\n");
          // inspect
          NodeSet unA = new NodeSet();
          NodeSet unB = new NodeSet();
          NodeSet chA = new NodeSet();
          NodeSet chB = new NodeSet();
          Container.inspect(hosts1, hosts2,unA,unB,chA,chB);
        
          print("--------------------");
          print("INSPECT");
          print("uniqueA = "+ unA.toXMLString());
          print("uniqueB = "+ unB.toXMLString());
          print("changedA = "+ chA.toXMLString());
          print("changedB = "+ chB.toXMLString());        
          print("--------------------");                    
                    
          print("Unset value in hosts1\n");
          karin1.unsetDisabledValue();
          print("inspect:\n");
          // inspect
          unA = new NodeSet();
          unB = new NodeSet();
          chA = new NodeSet();
          chB = new NodeSet();
          Container.inspect(hosts1, hosts2,unA,unB,chA,chB);
        
          print("--------------------");
          print("INSPECT");
          print("uniqueA = "+ unA.toXMLString());
          print("uniqueB = "+ unB.toXMLString());
          print("changedA = "+ chA.toXMLString());
          print("changedB = "+ chB.toXMLString());        
          print("--------------------");                    

          // sync
          print("sync: hosts1, hosts2");
          Element sync= hosts1.sync(hosts2);
          print( sync.toXMLString() );                    
                                            
        }
      },

      /************************************************************
       * Test 8
       * Klackes...
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
          Hosts h = new Hosts();
          A a = h.addA(new A());
          B b = h.addB(new B());
          a.addAs(new As("11"));
          b.addAs(new BAs("12"));
          System.out.println("HH " + h.toXMLString());

          Simple.enable();
          // Now we need to parse that string
          com.tailf.confm.XMLParser p = new com.tailf.confm.XMLParser();

          Hosts hh = (Hosts)p.parse(h.toXMLString());
          System.out.println("ZZa"  + hh.toXMLString());

        }
      },

      /************************************************************
       * Test 9
       * Test that we can parse trees that contain reserved words
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
          Hosts h = new Hosts();
          Reservedwords r = h.addReservedwords();
          Jif j = r.addJif();
          j.setJinterfaceValue("kalle");
          Jpublic pub = j.addJpublic();
          JpublicJinterface i2 = pub.addJinterface();
          Jnew nn = i2.addJnew();
          nn.setFoobarValue(44);
          System.out.println("HH " + h.toXMLString());

          Simple.enable();
          // Now we need to parse that string
          com.tailf.confm.XMLParser p = 
            new com.tailf.confm.XMLParser();

          Hosts hh = (Hosts)p.parse(h.toXMLString());
          System.out.println("ZZa"  + hh.toXMLString());


          MyBitsType2 bits = new MyBitsType2(MyBitsType2.DIRTY2);
          bits =  new MyBitsType2(MyBitsType2.DIRTY2 | 
                                  MyBitsType2.COMPRESSED2);


          // test that we can parse trees with bitsType
                    
          // create hosts1 tree
          Hosts hosts1 = new Hosts();
          Host kalle1 = hosts1.addHost( new Host("kalle"));
          kalle1.setEnabledValue("true");
          kalle1.setNumberOfServersValue("12");
          kalle1.setEncryptionValue("sha256");
          kalle1.setAddressValue("192.168.0.1");
          kalle1.setMtuValue("1024");
          kalle1.setFlagsValue(
            new MyBitsType( 
              MyBitsType.DIRTY | MyBitsType.COMPRESSED ));
          kalle1.setFlags2Value(
            new MyBitsType2( 
              MyBitsType2.DIRTY2 | MyBitsType2.COMPRESSED2 ));
          System.out.println("BITS0"  + hosts1.toXMLString());
          p = new com.tailf.confm.XMLParser();

          hh = (Hosts)p.parse(hosts1.toXMLString());
          System.out.println("BITS1"  + hh.toXMLString());
                    

          String real = "<hosts xmlns=\"http://tail-f.com/ns/simple/1.0\" xmlns:simple=\"http://tail-f.com/ns/simple/1.0\">   <host>     <name>kalle</name>      <enabled>true</enabled>      <flags>compressed</flags>      <mtu>1024</mtu>      <encryption>sha256</encryption>      <address>192.168.0.1</address>      <numberOfServers>12</numberOfServers>   </host></hosts>";
          p = new com.tailf.confm.XMLParser();
          hh = (Hosts)p.parse(real);
          System.out.println("BITS2"  + hh.toXMLString());


                    


        }
      },




      /************************************************************
       * Test 10
       * Test unsigned long
       */
      new TestCase() {
        // public boolean skip() { return true; }
        public void test() throws Exception {
          Hosts h = new Hosts();

          h.setUlongValue("18446744073709551615");
          //h.setUlongValue(456);
                    
          h.setUlong2Value(1);
          try {
            h.setUlong2Value(111);
            throw new Exception("should not work");
          } catch (Exception eee) {
          }

          System.out.println("HH " + h.toXMLString());

          Simple.enable();
          // Now we need to parse that string
          com.tailf.confm.XMLParser p = new com.tailf.confm.XMLParser();

          Hosts hh = (Hosts)p.parse(h.toXMLString());
          System.out.println("ZZa"  + hh.toXMLString());

        }
      },

      // Test 11, rt 3538
      new TestCase() {
          public void test() throws Exception {
              System.out.println("Test 10");

              Rt3538 rt = new Rt3538();
              Rt3538Msp msp = new Rt3538Msp();
              Md md = new Md(1);
              md.setFormatValue("none");
              md.addFaultAlarms();
              msp.addMd(md);
              rt.addMsp(msp);

              System.out.println("RT = " + rt.toXMLString());
              
              // Now the above should be identical to what we have on the 
              // agent
              
              Rt3538 filter = new Rt3538();
              TCPSession tr =
                  new TCPSession("127.0.0.1", 2023, "admin", "501",
                                 "501", "", "/Users/ola", "");
	      //tr.addSubscriber(new DefaultIOSubscriber("conny2"));

              NetconfSession s =
                  new NetconfSession(tr, new com.tailf.confm.XMLParser());
	      NodeSet reply = s.getConfig(NetconfSession.RUNNING, filter);
	      //NodeSet reply2 = s.getConfig(NetconfSession.RUNNING);
	      Rt3538 deviceConfig = (Rt3538)reply.first();

              Exists e = deviceConfig.msp.getMd("1").getFaultAlarmsValue();
              System.out.println("Exists = <" + e + ">");
              if (e == null) {
                  failed("Should exist ");
              }
              if (!deviceConfig.checkSync(rt)) {
                  failed("should be equal");
              }
          }
      },

        new TestCase() {
            public void test() throws Exception {
                System.out.println("Test 12");
                
                DeviceUser deviceUser = new DeviceUser
                    ("default", "admin", "admin");
                
                int timeout = 5000;
                String ipAddr = "127.0.0.1";

                Device device = new Device(ipAddr, deviceUser, ipAddr, 2022);
                device.connect("default", timeout);

                // create new sessions
                device.newSession("abc");
                

                device.newSession("xyz");

                // close session
                device.closeSession("xyz");

                // Check whether session still exists
                System.out.println("Session exists after closeSession: "
                                + device.hasSession("xyz"));

                // re-create new session
                device.newSession("xyz");

                // confirm new session exists
                System.out.println("Session after renewSession: "
                                + device.hasSession("xyz"));

            }
        }




    };
  
 
    /************************************************************
     * Run the tests
     */

    int from = 0;
    int to   = tests.length;

      
//    from = 14;
    //  to = 15;


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

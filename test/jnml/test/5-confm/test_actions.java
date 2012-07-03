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

public class test_actions {
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
	} catch (INMException e) {
	    failed("enabling error: "+e);
	    return;
	}
        
        TestCase[] tests = new TestCase[] {
            new TestCase() {
                public void test() throws Exception {
                    Actions actions = new Actions();
                    Halt halt = actions.addHalt();
                    TCPSession tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    tr.addSubscriber(new DefaultIOSubscriber("my_device"));
                    ConfDSession s =
                        new ConfDSession(tr, new com.tailf.confm.XMLParser());
                    Element result = s.action(actions);
                    if (!result.name.equals("ok"))
                        failed("not ok");
                    // test an action in a dynamic elem as well
                    actions = new Actions();
                    Dyn dyn = actions.addDyn("foo");
                    dyn.addHalt();
                    result = s.action(actions);
                    print("**** result:\n "+result.toXMLString()); 
                    if (!result.name.equals("ok"))
                        failed("not ok");
                }
            },

            new TestCase() {
                public void test() throws Exception {
                    Actions actions = new Actions();
                    Shutdown shutdown = actions.addShutdown();
                    shutdown.setMessageValue("foo");
                    Options options = shutdown.addOptions();
                    options.setRebootAfterShutdownValue(true);

                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    ConfDSession s =
                        new ConfDSession(tr, new com.tailf.confm.XMLParser());
                    Element result = s.action(actions);
                    if (!result.name.equals("ok"))
                        failed("not ok");
                }
            },

            new TestCase() {
                public void test() throws Exception {
                    Actions actions = new Actions();
                    SetSystemClock setSystemClock = actions.addSetSystemClock();
                    setSystemClock.setClockSettingsValue("2002-10-10T17:00:00Z");
                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    ConfDSession s =
                        new ConfDSession(tr, new com.tailf.confm.XMLParser());
                    Element result = s.action(actions);
                    print("**** result:\n "+result.toXMLString()); 
                    if (!result.name.equals("data"))
                        failed("not ok");
                    NodeSet dataChildren = result.getChildren();
                    actions = (Actions)dataChildren.getElement(0);
                    setSystemClock = (SetSystemClock)actions.setSystemClock;
                    if (!setSystemClock.getSystemClockValue().toString().equals("2001-11-21T11:10:09Z"))
                        failed("no date");
                }
            }
        };
        
        /************************************************************
         * Run the tests
         */

        int from = 0;
        int to   = tests.length;

      
        //from = 4;
        //to = 5;


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

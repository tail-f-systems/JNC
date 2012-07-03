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
import mountpoint.*;

// import com.tailf.inm.xs.*;

public class test_misc {
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
            Mountpoint.enable();
        } catch (INMException e) {
            failed("enabling error: "+e);
            return;
        }
        
        TestCase[] tests = new TestCase[] {
            new TestCase() {
                public void test() throws Exception {
                    Transport tr =
                        new TCPSession("127.0.0.1", 2023, "admin", "501",
                                       "501", "", "/Users/ola", "");
                    NetconfSession sess =
                        new NetconfSession(tr, new com.tailf.confm.XMLParser());
                    
                    // Get current config with subtree filter
                    Element subtreeFilter = new MountpointTop();
                    NodeSet reply = sess.getConfig(subtreeFilter);
                    MountpointTop mt = (MountpointTop)reply.first();
                    print("**** current config1:\n "+mt.toXMLString());
                    // Create new config
                    MountpointTop mt2 = new MountpointTop();
                    mt2.addMountpoint();
                    mt2.mountpoint.addBar().addBingo().setBazValue("miffo");
                    //
                    mt2.addAugmentpoint();
                    mt2.augmentpoint.addA().setBValue(42);
                    //
                    mt2.addStructuredType1().addBappelsin().setGubbadjavelValue("m");
                    mt2.addStructuredType2().addBappelsin2().setGubbadjavel2Value("m");
                    print("**** current config2:\n "+mt2.toXMLString());
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

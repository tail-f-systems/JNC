package math;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

import com.tailf.confm.*;
import com.tailf.inm.*;


public class Main {
    private static class Test {
        public Test() {
            init();
        }
        
        public Test(Subscriber s) {
            init(s);
        }
        

        private Device dev;
        private DeviceUser duser;
        
        private void init() {
            String emsUserName = "bobby";
            duser = new DeviceUser(emsUserName, "admin", "admin");
            dev = new Device("mydev", duser, "localhost", 2022);
            
            try {
                dev.connect(emsUserName);
                dev.newSession("cfg");
            } catch (IOException e0) {
                System.err.println("Can't connect");
                System.exit(1);
            } catch (INMException e1) {
                System.err.println("Can't authenticate" + e1);
                System.exit(1);
            }
        }
        
        private void init(Subscriber sub) {
            String emsUserName = "bobby";
            duser = new DeviceUser(emsUserName, "admin", "admin");
            dev = new Device("mydev", duser, "localhost", 2022);
            try {
                dev.connect(emsUserName);
                dev.newSession(sub, "cfg");
            } catch (IOException e0) {
                System.err.println("Can't connect");
                System.exit(1);
            } catch (INMException e1) {
                System.err.println("Can't authenticate" + e1);
                System.exit(1);
             }
        }
        
        void getConfig() throws IOException, INMException{
            NodeSet reply =
                dev.getSession("cfg").getConfig(NetconfSession.RUNNING);
            System.out.println(reply.first());
        }

        void rpcMath2() throws Exception {
            Math.enable();
            Math2 math2 = new Math2();
            Add add = math2.addAdd();
            add.setOperandValue(2);
            add.setOperandValue(5);
            System.out.println("Data = " +  math2.toXMLString());
            NodeSet reply = dev.getSession("cfg").callRpc(math2);
            System.out.println("Reply = " +  reply.toXMLString());
        }
        
        void actionMath2() throws Exception {
            Math.enable();
            Actions actions = new Actions();
            ActionsMath2 math2 = actions.addMath2();
            Math2Add add = math2.addAdd();
            add.setOperandValue(2);
            add.setOperandValue(4);
            System.out.println("Data = " +  actions.toXMLString());
            Element reply = dev.getSession("cfg").action(actions);
            System.out.println("Reply = " +  reply.toXMLString());
        }
    }

    static public int NUMTESTS = 2;
    
    static void runTest(Test t, int n) throws Exception {
        System.out.println("TEST " + n);
        switch (n) {
        case 1:
            t.rpcMath2();
            break;
        case 2:
            t.actionMath2();
            break;
        default:
            System.out.println("bad testno");
            System.exit(1);
        }
    }
    
    static public void main(String args[]) {
        System.out.println("In MAIN");
        int n = -1;
        boolean trace = false;
        try {
            for (int i=0; i<args.length; i++) {
                if (args[i].equals("-n")) {
                    try {
                        n = Integer.parseInt(args[++i]);
                    } catch (Exception e) {
                        System.err.println("Usage  : Main -n <int> [-t]");
                        System.exit(1);
                    }
                }
                else if (args[i].equals("-t")) {
                    trace = true;
                }
                else {
                    System.err.println("Usage  : Main -n <int> [-t]");
                    System.exit(1);
                }
            }
            
            Test t = new Test();
            if (trace) 
                t = new  Test(new Subscriber("mydev"));
            if (n == -1) {
                for (int i=1; i< (NUMTESTS+1); i++) {
                    runTest(t, i);
                }
                System.out.println("OK");
            }
            else {
                runTest(t, n);
            }
        }
        catch (Exception e) {
            System.out.println("ERROR " + e);
            e.printStackTrace();
        }
    }
}

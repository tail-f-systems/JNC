
package dhcp;

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
        
        public Test(IOSubscriber sLeft, IOSubscriber sRight) {
            init(sLeft, sRight);
        }


// M3_BEGIN init
        private Device left, right;
        private DeviceUser duser;

        private void init() {
            duser = new DeviceUser("bobby", "admin", "secret");
            left = new Device("left", duser, "localhost", 2022);
            right = new Device("right", duser, "localhost", 2023);
            try {
                left.connect("bobby");
                right.connect("bobby");
                left.newSession("cfg");
                right.newSession("cfg");
            } catch (IOException e0) {
                System.err.println("Can't connect" + e0);
                System.exit(1);
            } catch (INMException e1) {
                System.err.println("Can't authenticate" + e1);
                System.exit(1);
            }
        }
// M3_END init


// M3_BEGIN subscriberinit

        private void init(IOSubscriber subLeft, IOSubscriber subRight) {
            duser = new DeviceUser("bobby", "admin", "secret");
            left = new Device("left", duser, "localhost", 2022);
            right = new Device("right", duser, "localhost", 2023);
            try {
                left.connect("bobby");
                right.connect("bobby");
                left.newSession(subLeft, "cfg");
                right.newSession(subRight, "cfg");
            } catch (IOException e0) {
                System.err.println("Can't connect");
                System.exit(1);
            } catch (INMException e1) {
                System.err.println("Can't authenticate" + e1);
                System.exit(1);
            }
        }
// M3_END subscriberinit



// M3_BEGIN getConfig
        Dhcp getConfig(Device d) throws IOException,INMException{
             Dhcp filter = new Dhcp();
             NetconfSession s = d.getSession("cfg");
             NodeSet reply = s.getConfig(NetconfSession.RUNNING, filter);
             Dhcp config = (Dhcp)reply.first();
             return config;
        }
        
        void getConfig() throws IOException,INMException{
            System.out.println(getConfig(left).toXMLString());
        }

// M3_END getConfig
        

// M3_BEGIN getLogLevel        
        Loglevel getLogLevel() throws IOException,INMException{
            Dhcp config = getConfig(left);
            Loglevel level = config.getLogFacilityValue();
            return level;
        }
// M3_END getLogLevel        


// M3_BEGIN getLogLevel2        
        Loglevel getLogLevel2() throws IOException,INMException{
            Dhcp filter = new Dhcp();
            filter.addLogFacility();
            NetconfSession s = left.getSession("cfg");
            NodeSet reply = s.getConfig(NetconfSession.RUNNING, filter);
            Dhcp config = (Dhcp)reply.first();
            return config.getLogFacilityValue();
        }
// M3_END getLogLevel2        

        void listNetworks() throws IOException,INMException {
            listNetworks(left);
        }

// M3_BEGIN listNetworks
        void listNetworks(Device dev) 
            throws IOException,INMException {
            Dhcp filter = new Dhcp();
            filter.addSubNets();
            NetconfSession session = dev.getSession("cfg");
            NodeSet reply = session.getConfig(NetconfSession.RUNNING, filter);
            Dhcp config = (Dhcp)reply.first();
            ElementChildrenIterator iter = config.sharedNetworks.sharedNetworkIterator();
            while (iter.hasNext()) {
                SubNet s = (SubNet)iter.next();
                System.out.println("NETWORK" + s.getNetValue() + 
                                   "/" + s.getMaskValue());
            }
        }
// M3_END listNetworks


        // used to make a device out of sync to check the
        // sync code
        void makeUnsync(Device dev) throws INMException, IOException {
            Loglevel lev;
            Dhcp config = getConfig(dev);
            SubNet s = new SubNet();
	    s.setNetValue("10.3.0.0");
	    s.setMaskValue("255.255.0.0");
            addNetWork(left, s);
            if (config.getLogFacilityValue().toString().equals("mail"))
                lev = new Loglevel("mail");
            else
                lev = new Loglevel("kern");
            Dhcp d = new Dhcp();
            d.setLogFacilityValue(lev);
            dev.getSession("cfg").editConfig(NetconfSession.RUNNING, d);
        }
        


        void checkSync()  throws IOException,INMException {
            Dhcp dhcp =  getConfig(left);
            boolean b = checkSync(dhcp, left);

            makeUnsync(left);
            boolean b2 = checkSync(dhcp, left);
            // revert back
            makeSync(dhcp, left);
            boolean b3 = checkSync(dhcp, left);
            System.out.println(b);
            System.out.println(b2);
            System.out.println(b3);
        }

// M3_BEGIN checkSync        
        boolean checkSync(Dhcp dbConfig, Device d)  
            throws IOException,INMException {
            Dhcp filter = new Dhcp();
            NodeSet reply = d.getSession("cfg").getConfig(NetconfSession.RUNNING, 
                                                   filter);
            Dhcp deviceConfig = (Dhcp)reply.first();
            boolean b = Container.checkSync(dbConfig, deviceConfig);
            return b;
        }
// M3_END checkSync        



        void makeSync()  throws IOException,INMException {
            // This tescode first make the left device out of sync
            Dhcp config = getConfig(left);
            
            SubNet s = new SubNet();
	    s.setNetValue("10.3.220.0");
	    s.setMaskValue("255.255.0.0");
            addNetWork(left, s);
           
            Loglevel lev = new Loglevel("mail");
            Dhcp d = new Dhcp();
            d.setLogFacilityValue(lev);
            left.getSession("cfg").editConfig(NetconfSession.RUNNING, d);

            makeSync(config, left);
            boolean b = checkSync(config, left);
            System.out.println(b);
        }
        

// M3_BEGIN makeSync
        void makeSync(Dhcp dbConfig, Device d) throws IOException,INMException {
            if (!checkSync(dbConfig, d)) {
                Dhcp deviceConfig = getConfig(d);
                Container toSend = deviceConfig.sync(dbConfig);
                System.out.println("DIFF=" + toSend.toXMLString());
                d.getSession("cfg").editConfig(NetconfSession.RUNNING, toSend);
            }
        }

// M3_END makeSync

        
// M3_BEGIN addNetWork

        void addNetWork() throws IOException,INMException {
            SubNet s = new SubNet();
	    s.setNetValue("10.3..0");
	    s.setMaskValue("255.255.0.0");
            addNetWork(left, s);
            addNetWork(right, (SubNet)s.clone());
        }
        
        void addNetWork(Device dev, SubNet subNet) 
            throws IOException,INMException {
            Dhcp dhcp = new Dhcp();
            dhcp.addSubNets().addSubNet(subNet);
            dev.getSession("cfg").editConfig(NetconfSession.RUNNING, dhcp);
        }
        
// M3_END addNetWork


        
// M3_BEGIN addNetWork2

        void addNetWork2() throws INMException {
            SubNet s = new SubNet();
	    s.setNetValue("10.3.0.0");
	    s.setMaskValue("255.255.0.0");
            addNetWork2(left, s);
            addNetWork2(right, (SubNet) s.clone());
        }
        
        void addNetWork2(Device dev, SubNet subNet) throws INMException {
            if (dev.getConfig("cfg") == null)
                dev.setConfig("cfg", new Dhcp());
            Dhcp dhcp = (Dhcp)dev.getConfig("cfg");
            dhcp.addSubNets().addSubNet(subNet);
        }
        
// M3_END addNetWork2



// M3_BEGIN commitAll
        void commitAll() throws Exception {
            ArrayList devs = new ArrayList();
            devs.add(left);
            devs.add(right);
            commitAll(devs);
        }
        
        void resetList(ArrayList devs) throws Exception {
            try {
                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    reset(d);
                }
            } catch (Exception e) {
                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    d.closeSession("cfg");
                }
                throw (e);
            }
        }

        ArrayList filterDevs(ArrayList devs) {
            ArrayList updatedDevs = new ArrayList();
            for (int i=0; i<devs.size(); i++) {
                 Device d = (Device)devs.get(i);
                 if (d.getConfig("cfg") != null)
                     updatedDevs.add(d);
            }
            return updatedDevs;
        }


        void commitAll(ArrayList devs0) throws Exception {
            ArrayList devs = filterDevs(devs0);
            resetList(devs);
            try {
                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    d.getSession("cfg").editConfig(NetconfSession.CANDIDATE, 
                                                   d.getConfig("cfg"));
                    d.getSession("cfg").validate(NetconfSession.CANDIDATE);
                }
                for (int i=0; i<devs.size(); i++) {
                    ((Device)devs.get(i)).getSession("cfg").commit();
                }

                // If we're here all is ok - unlock everything
                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    try {
                        d.getSession("cfg").unlock(NetconfSession.CANDIDATE);
                        d.getSession("cfg").unlock(NetconfSession.RUNNING);
                    }
                    catch (Exception e) {
                        // ignore
                    }
                }
            }
            catch (Exception e1) {
                // abort by ending all sessions
                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    d.closeSession("cfg");
                }
                throw (e1);
            }
        }

// M3_END commitAll



// M3_BEGIN commitAll2

        void commitAll2() throws Exception {
            ArrayList devs = new ArrayList();
            devs.add(left);
            devs.add(right);
            commitAll2(devs);
        }


        void commitAll2(ArrayList devs0) throws Exception {
            ArrayList devs = filterDevs(devs0);
            resetList(devs);
            try {
                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    d.getSession("cfg").editConfig(NetconfSession.CANDIDATE, 
                                                   d.getConfig("cfg"));
                }
                for (int i=0; i<devs.size(); i++) {
                    ((Device)devs.get(i)).getSession("cfg").confirmedCommit(100);
                }
                
                // If we're here all is ok - confirm  everything  

                 for (int i=0; i<devs.size(); i++) {
                    ((Device)devs.get(i)).getSession("cfg").commit();
                }


                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    try {
                        d.getSession("cfg").unlock(NetconfSession.CANDIDATE);
                        d.getSession("cfg").unlock(NetconfSession.RUNNING);
                    }
                    catch (Exception e) {
                        // ignore
                    }
                }
            }
            catch (Exception e1) {
                // abort by ending sessions
                for (int i=0; i<devs.size(); i++) {
                    Device d = (Device)devs.get(i);
                    d.closeSession("cfg");
                }
                throw (e1);
            }
        }

// M3_END commitAll2



        void setLogFacility() throws IOException, INMException {
            setLogFacility("kern");
        }


// M3_BEGIN setLogFacility
        void setLogFacility(String lev) throws IOException, INMException {
            setLogFacility(new Loglevel(lev));
        }
        void setLogFacility(Loglevel lev)  throws IOException, INMException {
            Dhcp d = new Dhcp();
            d.setLogFacilityValue(lev);
            System.out.println(d.toXMLString());
            left.getSession("cfg").editConfig(NetconfSession.RUNNING, d);
            right.getSession("cfg").editConfig(NetconfSession.RUNNING, d);
        }
// M3_END setLogFacility
        



        void safeSetLogFacility()   throws IOException, INMException {
            safeSetLogFacility(new Loglevel("mail"));
        }
        


// M3_BEGIN reset
        void reset(Device dev)   throws IOException, INMException {
            NetconfSession s = dev.getSession("cfg");
            s.discardChanges();
            s.lock(NetconfSession.CANDIDATE);
            s.lock(NetconfSession.RUNNING);
            s.copyConfig(NetconfSession.RUNNING, 
                         NetconfSession.CANDIDATE);
        }
// M3_END reset

// M3_BEGIN safeSetLogFacility
        void safeSetLogFacility(Loglevel l)  throws IOException,INMException {
            reset(left);
            reset(right);
            try {
                Dhcp d = new Dhcp();
                NetconfSession ls = left.getSession("cfg");
                NetconfSession rs = right.getSession("cfg");

                d.setLogFacilityValue(l);
                ls.editConfig(NetconfSession.CANDIDATE, d);
                rs.editConfig(NetconfSession.CANDIDATE, d);
                
                ls.commit();
                rs.commit();
                
                try {
                    ls.unlock(NetconfSession.CANDIDATE);
                    ls.unlock(NetconfSession.RUNNING);
                    rs.unlock(NetconfSession.CANDIDATE);
                    rs.unlock(NetconfSession.RUNNING);

                } catch (Exception e) {
                    return;
                }
            } catch (IOException e0) {
                left.closeSession("cfg");
                right.closeSession("cfg");
                throw(e0);
            } catch (INMException e1) {
                left.closeSession("cfg");
                right.closeSession("cfg");
                throw(e1);
            }
        }

// M3_END safeSetLogFacility

        void safeSetLogFacility2()   throws IOException, INMException {
            safeSetLogFacility2(new Loglevel("kern"));
        }

// M3_BEGIN safeSetLogFacility2
        void safeSetLogFacility2(Loglevel l)  throws IOException,INMException {
            try {
                reset(left);
                reset(right);
            } catch (INMException ie) {
                left.closeSession("cfg"); right.closeSession("cfg"); 
                throw (ie);
            }
            catch (IOException ioe) {
                left.closeSession("cfg"); right.closeSession("cfg"); 
                throw (ioe);
            }
            
            try {
                Dhcp d = new Dhcp();
                NetconfSession ls = left.getSession("cfg");
                NetconfSession rs = right.getSession("cfg");
                d.setLogFacilityValue(l);
                ls.editConfig(NetconfSession.CANDIDATE, d);
                rs.editConfig(NetconfSession.CANDIDATE, d);
                ls.validate(NetconfSession.CANDIDATE);
                rs.validate(NetconfSession.CANDIDATE);
                ls.commit();
                rs.commit();
                
                try {
                    ls.unlock(NetconfSession.CANDIDATE);
                    ls.unlock(NetconfSession.RUNNING);
                    rs.unlock(NetconfSession.CANDIDATE);
                    rs.unlock(NetconfSession.RUNNING);

                } catch (Exception e) {
                    return;
                }
            } catch (IOException e0) {
                left.closeSession("cfg"); right.closeSession("cfg");
                throw(e0);
            } catch (INMException e1) {
                left.closeSession("cfg"); right.closeSession("cfg");
                throw(e1);
            }
        }

// M3_END safeSetLogFacility2



// M3_BEGIN testCapas
        boolean testCapas() {
            return (testCapas(left) &&  testCapas(right));
        }

        boolean testCapas(Device d) {
	    NetconfSession session = d.getSession("cfg");
            if (! (session.getCapabilities().hasCandidate() &&
		   session.getCapabilities().hasConfirmedCommit())) 
                return false;
            if (! session.getCapabilities().hasCapability(Dhcp.NAMESPACE)) {
                return false;
            }
            return true;
        }
// M3_END testCapas

        void saveDevice() throws IOException, INMException {
            saveDevice(left);
        }

// M3_BEGIN saveDevice
        void saveDevice(Device d) throws IOException, INMException {
            Dhcp config = getConfig(d);
            FileOutputStream fos = new FileOutputStream( d.name + ".SER" );
            ObjectOutputStream outStream = new ObjectOutputStream( fos );
            outStream.writeObject(config);
            outStream.flush();
            outStream.close();
        }
// M3_END saveDevice





    }


    static public int NUMTESTS = 13;

    static void runTest(Test t, int n) throws Exception {
        System.out.println("TEST " + n);
        switch (n) {
        case 1:
            t.getConfig();
            break;
        case 2:
            t.testCapas();
            break;
        case 3:
            System.out.println(t.getLogLevel2().toString());
            break;
        case 4:
            t.getLogLevel2();
            break;
        case 5:
            t.checkSync();
            break;
        case 6:
	    // FIXME
            //t.listNetworks();
            break;
        case 7:
            t.setLogFacility();
            break;
        case 8:
            t.safeSetLogFacility();
            break;
        case 9:
            t.makeSync();
            break;
        case 10:
	    // FIXME
            //t.addNetWork();
            break;
        case 11:
            t.addNetWork2();
            break;
        case 12:
            t.safeSetLogFacility2();
            break;
        case 13:
            t.saveDevice();
            break;

        default:
            System.out.println("bad testno");
            System.exit(1);
        }
    }

        
    static public void main(String args[]) {
        System.out.println("In MAIN");
	try {
	    Dhcp.enable();
	} catch (Exception e) {
	    System.exit(1);
	}
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
                t = new  Test(new DefaultIOSubscriber("left"),
                              new DefaultIOSubscriber("right"));
            if (n == -1) {
                for (int i=1; i<(NUMTESTS+1); i++) {
                    runTest(t, i);
                }
                System.out.println("ALLTESTOK");
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


            

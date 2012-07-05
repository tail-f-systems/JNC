package app;

import java.io.IOException;
import java.util.ListIterator;

import com.tailf.confm.*;
import com.tailf.inm.*;

import gen.Mini;

public class Main {

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
    
    private static void printNodeSet(NodeSet nset) {
        ListIterator<Element> iter = nset.listIterator();
        while (iter.hasNext()) {
            Element e = iter.next();
            System.out.println(e);
            System.out.println(e.getValue());
            if (e.hasChildren()) {
                printNodeSet(e.getChildren());
            }
        }
    }
    
    Element updateConfig(Element config) throws IOException,INMException {
        return updateConfig(dev, config);
    }

    Element updateConfig(Device d, Element config) throws IOException,INMException {
        d.getSession("cfg").editConfig(config);
        // Inspect the updated RUNNING configuration
        return getConfig(d);
    }
    
    private Element getConfig(Device d) throws IOException, INMException{
        Mini.enable();
        ConfDSession session = d.getSession("cfg");
        NodeSet reply = session.getConfig(NetconfSession.RUNNING);
        // printNodeSet(reply);
        Element h = (Element)reply.first();
        return h;
    }
    
    public Element getConfig() throws IOException,INMException{
        return getConfig(dev);
    }
    
    public int run() {
        try {
            Mini.enable();
        } catch (INMException e) {
            System.err.println("Schema file not found.");
            return -1;
        }
        return 0;
    }

    /**
     * @param args Ignored
     * @throws INMException 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException, INMException {
        Main main = new Main();
        main.init();
        Element config = main.getConfig();
        
        gen.L l = (gen.L)config;
        System.out.println("Initial   config:\n" + l.toXMLString());
        
        gen.L k = (gen.L)l.clone();
        k.addK();
        
        Element config2 = main.updateConfig(k);
        System.out.println("Resulting config:\n" + config2.toXMLString());
    }

}

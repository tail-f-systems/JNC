package app;

import java.io.IOException;
import java.util.ListIterator;

import com.tailf.confm.*;
import com.tailf.inm.*;

import gen.Mini;

public class Main {

    private Device dev;
    private DeviceUser duser;
    
    public Main() {
        this.init();
    }
    
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
    
    NodeSet editConfig(Element config) throws IOException,INMException {
        return editConfig(dev, config);
    }

    private NodeSet editConfig(Device d, Element config) throws IOException,INMException {
        d.getSession("cfg").editConfig(config);
        // Inspect the updated RUNNING configuration
        return getConfig(d);
    }
    
    private NodeSet getConfig(Device d) throws IOException, INMException {
        ConfDSession session = d.getSession("cfg");
        NodeSet reply = session.getConfig(NetconfSession.RUNNING);
        return reply;
    }
    
    public NodeSet getConfig() throws IOException,INMException{
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
        Mini.enable();
        main.init();
        gen.L l = (gen.L)main.getConfig().first();
        System.out.println("Initial config:\n" + l.toXMLString());
        
        // gen.L k = (gen.L)l.clone();
        // System.out.println(l.getKValue());
        l.setKValue("test2");
        // System.out.println(l.getKValue());
        
        Element config2 = main.editConfig(l).first();
        System.out.println("Resulting config:\n" + config2.toXMLString());
        
        l.setKValue("test");
        
        Element config3 = main.editConfig(l).first();
        System.out.println("Changed back to original config:\n" + config3.toXMLString());
    }

}

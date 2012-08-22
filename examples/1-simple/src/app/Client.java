package app;

import java.io.IOException;

import com.tailf.jnc.ConfDSession;
import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.ElementChildrenIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.YangException;
import com.tailf.jnc.YangString;
import com.tailf.jnc.YangUInt32;

import gen.simple.Simple;
import gen.simple.Hosts;
import gen.simple.hosts.Host;

public class Client {

    private Device dev;
    private DeviceUser duser;

    public Client() {
        this.init();
    }

    private void init() {
        String emsUserName = "bobby";
        String ip = "localhost";
        duser = new DeviceUser(emsUserName, "admin", "admin");
        dev = new Device("mydev", duser, ip, 2022);

        try {
            dev.connect(emsUserName);
            dev.newSession("cfg");
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        } catch (JNCException e) {
            System.err.println("Can't authenticate " + e);
            System.exit(1);
        }
    }

    NodeSet editConfig(Element config) throws IOException, JNCException {
        return editConfig(dev, config);
    }

    private NodeSet editConfig(Device d, Element config) throws IOException,
            JNCException {
        d.getSession("cfg").editConfig(config);
        // Inspect the updated RUNNING configuration
        return getConfig(d);
    }

    private NodeSet getConfig(Device d) throws IOException, JNCException {
        ConfDSession session = d.getSession("cfg");
        NodeSet reply = session.getConfig(NetconfSession.RUNNING);
        return reply;
    }

    public NodeSet getConfig() throws IOException, JNCException {
        return getConfig(dev);
    }
    
    /**
     * Gets the first configuration element in configs with name "hosts".
     * 
     * @param configs Set of device configuration data.
     * @return First hosts configuration, or null if none present.
     */
    public static Hosts getHostsConfig(NodeSet configs) {
        Element hostsConfig = configs.first();
        if (!hostsConfig.name.equals("hosts")) {
            hostsConfig = null;
            for (Element config : configs) {
                if (config.name.equals("hosts")) {
                    hostsConfig = config;
                }
            }
        }
        return (Hosts)hostsConfig;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Client client = new Client();
        Simple.enable();
        client.init();
        NodeSet configs = client.getConfig();
        
        // Get (first) config with name "hosts"
        Hosts hostsConfig = getHostsConfig(configs);

        System.out.println("Initial config:\n" + hostsConfig.toXMLString());
        
        // Increment number of servers at each host by one
        ElementChildrenIterator hostIterator = hostsConfig.hostIterator();
        while (hostIterator.hasNext()) {
            Host h = (Host) hostIterator.next();
            
            // Internal representation of numberOfServers
            YangUInt32 numberOfServers = h.getNumberOfServersValue();
            
            // Set value of the YangUInt32 with a Java long
            numberOfServers.setValue(numberOfServers.getValue() + 1);
            
            // Set value in local config
            h.setNumberOfServersValue(numberOfServers);
        }

        // Verify the change
        System.out.print("NumberOfServers incremented: ");
        hostIterator = hostsConfig.hostIterator();
        while (hostIterator.hasNext()) {
            Host h = (Host) hostIterator.next();
            // Use get methods for leaf name and numberOfServersValue
            System.out.print("(" + h.getNameValue() + ", " +
                    h.getNumberOfServersValue() + "), ");
        }
        System.out.println();
        
        // Get "enabled"-value although not always there
        hostIterator = hostsConfig.hostIterator();
        while (hostIterator.hasNext()) {
            Host h = (Host) hostIterator.next();
            System.out.println(h.getEnabledValue());
        }

//        System.out.println("Incremented:\n" + hostsConfig.toXMLString());
        
        // Add a new host
        YangString hostName = new YangString("uppsala");
        Host uppsala = new Host(hostName);
        uppsala.setEnabledValue(false);
        uppsala.setNumberOfServersValue(0);
        try {
            hostsConfig.addHost(uppsala);
            System.out.println("Host " + hostName + " added: OK");
        } catch (YangException e) {
            System.out.println("Cannot add host " + hostName + ": Fail");
        }
        
        // Add one with same name (key collision!)
        try {
            hostsConfig.addHost(hostName);
            System.out.println("Host " + hostName + " added twice: Fail");
        } catch (YangException e) {
            System.out.println("Cannot add host " + hostName + " twice: OK");
        }

//        System.out.println("After collision:\n" + hostsConfig.toXMLString());
        
        // Element config2 = client.editConfig(l).first();
        // System.out.println("Resulting config:\n" + config2.toXMLString());
        //
        // l.setRefLeafValue("test");
        //
        // Element config3 = client.editConfig(l).first();
        // System.out.println("Changed back to original config:\n" +
        // config3.toXMLString());
    }

}

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
        
        // Clone a backup configuration for rollback purposes
        Element backup = hostsConfig.clone();

        String configAsXML = hostsConfig.toXMLString();
        System.out.println("Initial config:\n" + configAsXML);
        System.out.println("HashCode of the string: " + configAsXML.hashCode());
        
        // Increment number of servers at each host by one
        ElementChildrenIterator hostIterator = hostsConfig.hostIterator();
        while (hostIterator.hasNext()) {
            Host h = (Host) hostIterator.next();
            
            // Internal representation of numberOfServers
            YangUInt32 numberOfServers = h.getNumberOfServersValue();
            
            // Set value of the with a Java long
            numberOfServers.setValue(numberOfServers.getValue() + 1);
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
        
        // Get "enabled"-value, using default value when not present
        System.out.print("Hosts enable status: ");
        hostIterator = hostsConfig.hostIterator();
        while (hostIterator.hasNext()) {
            Host h = (Host) hostIterator.next();
            System.out.print("(" + h.getNameValue() + ", " +
                    h.getEnabledValue() + "), ");
        }
        System.out.println();
        
        // Add a new host
        YangString hostName = new YangString("uppsala");
        Host uppsala = new Host(hostName);
        uppsala.setEnabledValue(false);
        uppsala.setNumberOfServersValue(0);
        try {
            hostsConfig.addHost(uppsala);
            System.out.println("Host " + hostName + " added: OK");
        } catch (YangException e) {
            System.err.println("Cannot add host " + hostName + ": Fail");
        }
        
        // Try to add one with same name (key collision!)
        try {
            hostsConfig.addHost(hostName);
            System.err.println("Host " + hostName + " added twice: Fail");
        } catch (YangException e) {
            System.out.println("Cannot add host " + hostName + " twice: OK");
        }
        
        // Fill the host list with MAX_ELEMENTS entries
        final int MAX_ELEMENTS = 64;
        int spaceLeft = MAX_ELEMENTS - hostsConfig.getChildren().size();
        if (spaceLeft <= 0) {
            System.err.println("No space for more host list entries! FAIL");
        }
        for(int i = 0; i < spaceLeft; i++) {
            hostsConfig.addHost("host" + i);
        }
        if (hostsConfig.getChildren().size() == MAX_ELEMENTS) {
            System.out.println("Host list contains " + MAX_ELEMENTS +
                    " entries: OK");
        } else {
            System.err.println("Fill list to limit: FAIL (is limit 0?)");
        }

        // Try to add more than max-elements number of hosts
        try {
            hostsConfig.addHost("anotherhost");
            System.err.println("More than " + MAX_ELEMENTS +
                    " hosts added: Fail");
        } catch (YangException e) {
            System.out.println("Cannot add more than " + MAX_ELEMENTS +
                    " hosts: OK");
        }
        
        // Delete the added host list entries
        for(int i = 0; i < spaceLeft; i++) {
            hostsConfig.deleteHost("host" + i);
        }
        if (spaceLeft == MAX_ELEMENTS - hostsConfig.getChildren().size()) {
            System.out.println("Restore number of hosts: OK");
        } else {
            System.err.println("Restore number of hosts: FAIL");
        }

        // Change name (key) of sthlm host to stockholm
        Host sthlm = hostsConfig.getHost("sthlm");
        sthlm.setNameValue("stockholm");
        try {
            hostsConfig.getHost("sthlm"); // This should now raise an exception
            System.err.println("sthlm host should not exist: FAIL");
        } catch (YangException e) {
            hostsConfig.getHost("stockholm"); // This should be ok
            System.out.println("sthlm host changed name to stockholm: OK");
        }

        // Edit the remote configuration and get the new one
        NodeSet configs2 = client.editConfig(hostsConfig);
        
        // Get the new (changed) config with name "hosts"
        hostsConfig = getHostsConfig(configs2);
        String configAsXML2 = hostsConfig.toXMLString();
//        System.out.println("New config:\n" + configAsXML2);
        System.out.println("HashCode of the new configuration: " +
                configAsXML2.hashCode());
        
        // Change back to original config
        client.editConfig(backup);
        NodeSet configs3 = client.getConfig();
        hostsConfig = getHostsConfig(configs3);
        String configAsXML3 = hostsConfig.toXMLString();
        System.out.println("Rollback config hashCode:" +
                configAsXML3.hashCode());
    }

}

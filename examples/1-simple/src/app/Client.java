package app;

import java.io.IOException;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.ElementChildrenIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.YangElement;
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
        NetconfSession session = d.getSession("cfg");
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
        YangElement backup = hostsConfig.clone();

        String configAsXML = hostsConfig.toXMLString();
        System.out.println("Initial config:\n" + configAsXML);
        System.out.println("HashCode of the config XML string: " +
                configAsXML.hashCode());
        
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
        
        // Get diff between "backup" and "hostsConfig"
        NodeSet toKeep1 = new NodeSet();
        NodeSet toDelete1 = new NodeSet();
        NodeSet toKeep2 = new NodeSet();
        NodeSet toDelete2 = new NodeSet();
        YangElement.getDiff(backup, hostsConfig,
                toKeep1, toDelete1, toKeep2, toDelete2);
        System.out.print("Hosts changed or unique to backup: ");
        for (NodeSet toKeep : new NodeSet[] {toKeep1, toKeep2}) {
            for (Element elem : toKeep) {
                if (elem instanceof Host) {
                    Host host = (Host) elem;
                    System.out.print("(" + host.getNameValue() + ", " +
                            host.getNumberOfServersValue() + "), ");
                }
            }
        }
        System.out.println();
        System.out.print("Hosts changed or unique to hostsConfig: ");
        for (NodeSet toDelete : new NodeSet[] {toDelete1, toDelete2}) {
            for (Element elem : toDelete) {
                if (elem instanceof Host) {
                    Host host = (Host) elem;
                    System.out.print("(" + host.getNameValue() + ", " +
                            host.getNumberOfServersValue() + "), ");
                }
            }
        }
        System.out.println();
        
        // Clear remote host config
        hostsConfig.markDelete();
        NodeSet configs3 = client.editConfig(hostsConfig);
        hostsConfig = getHostsConfig(configs3);
        if (hostsConfig == null) {
            System.out.println("Cleared the remote hosts config: OK");
        } else {
            System.out.println("Clear the remote hosts config: FAIL");
        }
        
        // Change back to original config
        client.editConfig(backup);
        NodeSet configs4 = client.getConfig();
        hostsConfig = getHostsConfig(configs4);
        String configAsXML4 = hostsConfig.toXMLString();
        if (configAsXML4.equals(configAsXML)) {
            System.out.println("Rollback config same as first one: OK");
        } else {
            System.out.println("Different rollback config: FAIL");
        }
        System.out.println("Rollback config XML string hashCode: " +
                configAsXML4.hashCode());

        // Cleanup
        client.dev.close();
    }

}

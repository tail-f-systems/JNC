package app;

import java.io.IOException;

import notif.Notif;
import notif.Interfaces;
import notif.interfaces.JInterface;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.ElementChildrenIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.YangElement;
import com.tailf.jnc.YangUInt32;

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
     * Gets the first configuration element in configs with name "c".
     * 
     * @param configs Set of device configuration data.
     * @return First c configuration, or null if none present.
     */
    public static Interfaces getInterfacesConfig(NodeSet configs) {
        Element interfacesConfig = configs.first();
        if (!interfacesConfig.name.equals("interfaces")) {
            interfacesConfig = null;
            for (Element config : configs) {
                if (config.name.equals("interfaces")) {
                    interfacesConfig = config;
                }
            }
        }
        return (Interfaces)interfacesConfig;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Client client = new Client();
        Notif.enable();
        client.init();
        NodeSet configs = client.getConfig();

        // Buffered reader used to pause execution
        java.io.InputStreamReader isr =
                new java.io.InputStreamReader(System.in);
        java.io.BufferedReader br = new java.io.BufferedReader(isr);
        
        // Get (first) config with name "c"
        Interfaces interfacesConfig = getInterfacesConfig(configs);
        
        // Clone a backup configuration for rollback purposes
        YangElement backup = interfacesConfig.clone();

        String configAsXML = interfacesConfig.toXMLString();
        System.out.println("Initial config:\n" + configAsXML);
        System.out.println("HashCode of the config XML string: " +
                configAsXML.hashCode());
        
        ElementChildrenIterator lIterator = interfacesConfig.interface_Iterator();
        while (lIterator.hasNext()) {
            JInterface h = (JInterface) lIterator.next();
            
            // Internal representations of ifIndex key
            YangUInt32 ifIndexValue = (YangUInt32)h.getIfIndexValue();
            
            // Increment the ifIndex value by one
            ifIndexValue.setValue(ifIndexValue.getValue() + 1L);
        }

        // Edit the remote configuration and get the new one
        NodeSet configs2 = client.editConfig(interfacesConfig);
        
        // Pause (Read line)
        br.readLine();
        
        // Get the new (changed) config with name "c"
        interfacesConfig = getInterfacesConfig(configs2);
        String configAsXML2 = interfacesConfig.toXMLString();
//        System.out.println("New config:\n" + configAsXML2);
        System.out.println("HashCode of the new configuration: " +
                configAsXML2.hashCode());
        
        // Get diff between "backup" and "interfacesConfig"
        NodeSet toKeep1 = new NodeSet();
        NodeSet toDelete1 = new NodeSet();
        NodeSet toKeep2 = new NodeSet();
        NodeSet toDelete2 = new NodeSet();
        YangElement.getDiff(backup, interfacesConfig,
                toKeep1, toDelete1, toKeep2, toDelete2);
        System.out.print("C changed or unique to backup: ");
        for (NodeSet toKeep : new NodeSet[] {toKeep1, toKeep2}) {
            for (Element elem : toKeep) {
                if (elem instanceof JInterface) {
                    JInterface l = (JInterface) elem;
                    System.out.print("(" + l.getIfIndexValue() + "), ");
                }
            }
        }
        System.out.println();
        System.out.print("C changed or unique to interfacesConfig: ");
        for (NodeSet toDelete : new NodeSet[] {toDelete1, toDelete2}) {
            for (Element elem : toDelete) {
                if (elem instanceof JInterface) {
                    JInterface l = (JInterface) elem;
                    System.out.print("(" + l.getIfIndexValue() + "), ");
                }
            }
        }
        System.out.println();
        
        // Clear remote interfaces config
        interfacesConfig.markDelete();
        NodeSet configs3 = client.editConfig(interfacesConfig);
        interfacesConfig = getInterfacesConfig(configs3);
        if (interfacesConfig == null) {
            System.out.println("Cleared the remote c config: OK");
        } else {
            System.out.println("Clear the remote c config: FAIL");
        }

        // Pause (Read line)
        br.readLine();
        
        // Change back to original config
        client.editConfig(backup);
        NodeSet configs4 = client.getConfig();
        interfacesConfig = getInterfacesConfig(configs4);
        String configAsXML4 = interfacesConfig.toXMLString();
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

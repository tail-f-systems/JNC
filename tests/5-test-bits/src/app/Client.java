package app;

import java.io.IOException;

import bits.C;
import bits.Bits;
import bits.c.L;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.ElementChildrenIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.YangBits;
import com.tailf.jnc.YangElement;

public class Client {

    private Device dev;

    public Client() {
        this.init();
    }

    private void init() {
        String emsUserName = "bobby";
        String ip = "localhost";
        DeviceUser duser = new DeviceUser(emsUserName, "admin", "admin");
        dev = new Device("mydev", duser, ip, 2022);

        try {
            dev.connect(emsUserName, 2000, false);
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
        return session.getConfig(NetconfSession.RUNNING);
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
    public static C getCConfig(NodeSet configs) {
        Element cConfig = configs.first();
        if (!"c".equals(cConfig.name)) {
            cConfig = null;
            for (Element config : configs) {
                if ("c".equals(config.name)) {
                    cConfig = config;
                }
            }
        }
        return (C)cConfig;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Client client = new Client();
        Bits.enable();
        client.init();
        NodeSet configs = client.getConfig();

        // Get (first) config with name "c"
        C cConfig = getCConfig(configs);

        // Clone a backup configuration for rollback purposes
        YangElement backup = cConfig.clone();

        String configAsXML = cConfig.toXMLString();
        System.out.println("Initial config:\n" + configAsXML);
        System.out.println("HashCode of the config XML string: " +
                configAsXML.hashCode());

        ElementChildrenIterator lIterator = cConfig.lIterator();
        while (lIterator.hasNext()) {
            L h = (L) lIterator.next();

            // Internal representations of key1 and key2
            YangBits key1Value = h.getKey1Value();
            YangBits key2Value = h.getKey2Value();
        }

        // Edit the remote configuration and get the new one
        NodeSet configs2 = client.editConfig(cConfig);

        // Get the new (changed) config with name "c"
        cConfig = getCConfig(configs2);
        String configAsXML2 = cConfig.toXMLString();
//        System.out.println("New config:\n" + configAsXML2);
        System.out.println("HashCode of the new configuration: " +
                configAsXML2.hashCode());

        // Get diff between "backup" and "cConfig"
        NodeSet toKeep1 = new NodeSet();
        NodeSet toDelete1 = new NodeSet();
        NodeSet toKeep2 = new NodeSet();
        NodeSet toDelete2 = new NodeSet();
        YangElement.getDiff(backup, cConfig,
                toKeep1, toDelete1, toKeep2, toDelete2);
        System.out.print("C changed or unique to backup: ");
        for (NodeSet toKeep : new NodeSet[] {toKeep1, toKeep2}) {
            for (Element elem : toKeep) {
                if (elem instanceof L) {
                    L l = (L) elem;
                    System.out.print("(" + l.getKey1Value() + ", " +
                            l.getKey2Value() + "), ");
                }
            }
        }
        System.out.println();
        System.out.print("C changed or unique to cConfig: ");
        for (NodeSet toDelete : new NodeSet[] {toDelete1, toDelete2}) {
            for (Element elem : toDelete) {
                if (elem instanceof L) {
                    L l = (L) elem;
                    System.out.print("(" + l.getKey1Value() + ", " +
                            l.getKey2Value() + "), ");
                }
            }
        }
        System.out.println();

        // Clear remote l config
        cConfig.markDelete();
        NodeSet configs3 = client.editConfig(cConfig);
        cConfig = getCConfig(configs3);
        if (cConfig == null) {
            System.out.println("Cleared the remote c config: OK");
        } else {
            System.out.println("Clear the remote c config: FAIL");
        }

        // Change back to original config
        client.editConfig(backup);
        NodeSet configs4 = client.getConfig();
        cConfig = getCConfig(configs4);
        String configAsXML4 = cConfig.toXMLString();
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

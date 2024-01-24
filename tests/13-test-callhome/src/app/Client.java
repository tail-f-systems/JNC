package app;

import java.io.IOException;

import callhome.C;
import callhome.Callhome;
import callhome.c.L;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.ElementChildrenIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.YangElement;
import com.tailf.jnc.YangEnumeration;
import com.tailf.jnc.YangException;
import com.tailf.jnc.YangString;

public class Client {

    private Device dev;

    public Client() {
        this.init();
    }

    private void init() {
        String emsUserName = "bobby";
        String ip = "localhost";

        try {
            System.out.println("accepting connections");
            dev = new Device.CallHome("mydev")
                .setWaitTimeout(10000)
                .waitForCallHome();
            dev.addUser(new DeviceUser(emsUserName, "admin", "admin"));
            dev.authenticate(emsUserName);
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
        Callhome.enable();
        Client client = new Client();
        NodeSet configs = client.getConfig();

        // Get (first) config with name "c"
        C cConfig = getCConfig(configs);

        // Clone a backup configuration for rollback purposes
        YangElement backup = cConfig.clone();

        String configAsXML = cConfig.toXMLString();
        System.out.println("Initial config:\n" + configAsXML);
        System.out.println("HashCode of the config XML string: " +
                configAsXML.hashCode());

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

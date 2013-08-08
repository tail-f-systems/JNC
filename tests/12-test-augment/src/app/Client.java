package app;

import java.io.IOException;

import ietfSystem.System;
import ietfSystem.system.Ntp;
import ietfSystem.system.ntp.NtpList;

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
     * Gets the first configuration element in configs with name "system".
     *
     * @param configs Set of device configuration data.
     * @return First system configuration, or null if none present.
     */
    public static System getSystemConfig(NodeSet configs) {
        Element systemConfig = configs.first();
        if (!systemConfig.name.equals("system")) {
            systemConfig = null;
            for (Element config : configs) {
                if (config.name.equals("system")) {
                    systemConfig = config;
                }
            }
        }
        return (System)systemConfig;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Client client = new Client();
        Union.enable();
        client.init();
        NodeSet configs = client.getConfig();

        // Get (first) config with name "system"
        System systemConfig = getSystemConfig(configs);

        // Clone a backup configuration for rollback purposes
        YangElement backup = systemConfig.clone();

        String configAsXML = systemConfig.toXMLString();
        System.out.println("Initial config:\n" + configAsXML);
    }

}

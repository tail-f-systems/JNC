package app;

import gen.ietfInetTypes.IpAddress;
import gen.ietfSystem.system.ntp.NtpServer;

import java.io.IOException;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.YangElement;

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
    public static gen.ietfSystem.System getSystemConfig(NodeSet configs) {
        Element systemConfig = configs.first();
        if (!systemConfig.name.equals("system")) {
            systemConfig = null;
            for (Element config : configs) {
                if (config.name.equals("system")) {
                    systemConfig = config;
                }
            }
        }
        return (gen.ietfSystem.System)systemConfig;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Client client = new Client();
        client.init();
        gen.ietfSystem.Sys.enable();
        NodeSet configs = client.getConfig();

        // Get (first) config with name "system"
        gen.ietfSystem.System systemConfig = getSystemConfig(configs);
        
        // Add new server
        NtpServer server = new NtpServer(new IpAddress("4.4.4.4"));
        systemConfig.getChild("ntp").addChild(server);

        String configAsXML = systemConfig.toXMLString();
        System.out.println("Initial config:\n" + configAsXML);
    }

}

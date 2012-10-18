package app;

import gen.ietfInterfaces.Interfaces;
import gen.ietfInterfaces.JIf;
import gen.ietfIp.Ip;

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
     * Gets the first configuration element in configs with specified name.
     * 
     * @param configs Set of device configuration data.
     * @param name The identifier of the configuration to select
     * @return First configuration with matching name, or null if none present.
     */
    public static Element getConfig(NodeSet configs, String name) {
        Element config = configs.first();
        if (!config.name.equals(name)) {
            config = null;
            for (Element elem : configs) {
                if (elem.name.equals(name)) {
                    config = elem;
                }
            }
        }
        return config;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Client client = new Client();
        JIf.enable();
        client.init();
        NodeSet configs = client.getConfig();
        
        // Get (first) config with name "interfaces"
        Interfaces config = (Interfaces)getConfig(configs, "interfaces");
        
        System.out.println(config.toXMLString());

        // Cleanup
        client.dev.close();
    }

}

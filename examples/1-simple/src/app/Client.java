package app;

import java.io.IOException;

import com.tailf.jnc.ConfDSession;
import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;

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

    public int run() {
        try {
            Simple.enable();
        } catch (JNCException e) {
            System.err.println("Schema file not found.");
            return -1;
        }
        return 0;
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
        System.out.println("Initial config:");
//        System.out.println(configs.first().toXMLString());
        
        for (Element config : configs) {
            System.out.println(config.toXMLString());
        }

        // gen.L k = (gen.L) l.clone();
        // System.out.println(l.getKValue());
        // l.setRefLeafValue("test2");
        // System.out.println(l.getKValue());
        //
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

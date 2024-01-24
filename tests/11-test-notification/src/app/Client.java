package app;

import java.io.IOException;

import notif.Notif;
import notif.Interfaces;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;

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

    public NodeSet getStreams() throws IOException, JNCException {
        return dev.getSession("cfg").getStreams();
    }

    public void subscribe() throws IOException, JNCException {
        dev.getSession("cfg").createSubscription("interface");
    }

    public Element waitForNotification() throws IOException, JNCException {
        return dev.getSession("cfg").receiveNotification();
    }

    /**
     * Gets the first configuration element in configs with name "c".
     *
     * @param configs Set of device configuration data.
     * @return First c configuration, or null if none present.
     */
    public static Interfaces getInterfacesConfig(NodeSet configs) {
        Element interfacesConfig = configs.first();
        if (!"interfaces".equals(interfacesConfig.name)) {
            interfacesConfig = null;
            for (Element config : configs) {
                if ("interfaces".equals(config.name)) {
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

        NodeSet reply = client.getStreams();
        System.out.println("got streams:" + reply.toXMLString());

        NodeSet configs = client.getConfig();
        if (configs.isEmpty()) {
            System.out.println("Received empty configuration");
            return;
        }

        // Confirm that there are no interfaces configs
        Interfaces interfacesConfig = getInterfacesConfig(configs);
        if (interfacesConfig == null) {
            System.out.println("Remote configuration contains no interfaces");
        } else {
            System.err.println("Remote configuration contains interfaces");
        }

        // Subscribe to "interface" notifications
        client.subscribe();

        // Wait for a notification and print it as XML (blocking)
        System.out.println("Waiting for \"interface\" notification...");
        System.out.println(client.waitForNotification().toXMLString());

        // Loop for 10000 iterations, print every 1000 received notification
        System.out.println("Waiting for more \"interface\" notifications...");
        System.out.println("Only every 1000th notification is printed.");
        for (int i=0; i<10000; i++) {
            Element notification = client.waitForNotification();
            if ((i+1) % 1000 == 0) {
                System.out.println(notification.toXMLString());
            }
        }

        // Cleanup
        client.dev.close();
    }

}

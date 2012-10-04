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
    
    public void subscribe() throws IOException, JNCException {
        dev.getSession("cfg").createSubscription();
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
        if (configs.isEmpty()) {
            System.out.println("Received empty configuration");
            return;
        }

        // Buffered reader used to pause execution
        java.io.InputStreamReader isr =
                new java.io.InputStreamReader(System.in);
        java.io.BufferedReader br = new java.io.BufferedReader(isr);
        
        // Confirm that there are no interfaces configs
        Interfaces interfacesConfig = getInterfacesConfig(configs);
        if (interfacesConfig == null) {
            System.out.println("Remote configuration contains no interfaces");
        } else {
            System.err.println("Remote configuration contains interfaces");
        }
        
        client.subscribe();
        System.out.println(client.waitForNotification().toXMLString());
        
//        interfacesConfig = new Interfaces();
//        JInterface interface0 = new JInterface();
//        JInterface interface1 = new JInterface();
//        interfacesConfig.addJInterface(interface0);
//        interfacesConfig.addJInterface(interface1);
//        System.out.println(interfacesConfig.toXMLString());

        // Cleanup
        client.dev.close();
    }

}

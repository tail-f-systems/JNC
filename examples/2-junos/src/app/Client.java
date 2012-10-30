package app;

import java.io.IOException;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.SSHConnection;
import com.tailf.jnc.SSHSession;

import gen.junosSystem.Junos;
import gen.junosSystem.Configuration;

public class Client {

    private Device dev;
    private DeviceUser duser;

    public Client() {
        this.init();
    }

    // Hardcoded fields
    private String emsUserName = "bobby";
    private String junosUserName = "admin";
    private String pass = "Admin99";
    private String junosHost = "olive1.lab";

    private void init() {
        duser = new DeviceUser(emsUserName, junosUserName, pass);
        dev = new Device("mydev", duser, junosHost, 22);

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

    public NodeSet getConfig() throws IOException, JNCException {
        NetconfSession session = dev.getSession("cfg");
        NodeSet reply = session.getConfig(NetconfSession.RUNNING);
        return reply;
    }
    
    /**
     * Gets the first configuration element in configs with name "hosts".
     * 
     * @param configs Set of device configuration data.
     * @return First hosts configuration, or null if none present.
     */
    public static Configuration getJunosConfiguration(NodeSet configs) {
        Element config = configs.first();
        if (!config.name.equals("hosts")) {
            config = null;
            for (Element elem : configs) {
                if (elem.name.equals("configuration")) {
                    config = elem;
                }
            }
        }
        return (Configuration)config;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Client client = new Client();
        Junos.enable();

        // Start (NETCONF) sessions towards devices
        SSHConnection c1 = new SSHConnection(client.junosHost, 22);
        c1.authenticateWithPassword(client.junosUserName, client.pass);
        SSHSession ssh1 = new SSHSession(c1);
        NetconfSession dev1 = new NetconfSession(ssh1);

        // take locks on CANDIDATE datastore so that we are not interrupted
        dev1.lock(NetconfSession.CANDIDATE);

        // reset candidates so that CANDIDATE is an exact copy of running
        dev1.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);

        // Get system configuration from dev1
        NodeSet configs = client.getConfig();
        Element sys1 = getJunosConfiguration(configs);

        // Manipulate element trees locally
        // TODO

        // Write back the updated element tree to the device
        dev1.editConfig(NetconfSession.CANDIDATE, sys1);

        // candidates are now updated
        dev1.confirmedCommit(60);

        // now commit them, unlock the candidate config and close connection
        // Note: in case of error, the device will rollback within 1 min
        dev1.commit();
        dev1.unlock(NetconfSession.CANDIDATE);
        client.dev.close();
        
        System.out.println("Committed:\n" + sys1.toXMLString());
    }

}

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
import com.tailf.jnc.YangString;

import gen.junosSystem.Junos;
import gen.junosSystem.Configuration;
import gen.junosSystem.configuration.routingOptions.AutonomousSystem;

/**
 * Client code to get the running configuration from a juniper router, copy it
 * to the candidate configuration and then commit any changes made to it.
 * 
 * NetconfSession is used directly, no "Device" is used.
 */
public class Client {

    // Hard coded host name, user name and password
    private String emsUserName = "bobby";
    private String junosUserName = "admin";
    private String pass = "Admin99";
    private String junosHost = "olive1.lab";
    
    // The device and device user used by the client
    private Device dev;
    private DeviceUser duser;

    /**
     * Constructor, creates a connected client object.
     * 
     * @throws IOException
     * @throws JNCException
     */
    public Client() {
        this.init();
    }

    /**
     * Connects to host using hard coded values
     * 
     * @throws IOException
     * @throws JNCException
     */
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
     * Gets the first element in configs with name "configuration".
     *
     * @param configs Set of device configuration data.
     * @return First hosts configuration, or null if none present.
     */
    public static Configuration getJunosConfiguration(NodeSet configs) {
        Element config = null;
        for (Element elem : configs) {
            if (elem.name.equals("configuration")) {
                config = elem;
                break;
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
        SSHConnection sshConn = new SSHConnection(client.junosHost, 22);
        sshConn.authenticateWithPassword(client.junosUserName, client.pass);
        SSHSession sshSession = new SSHSession(sshConn);
        NetconfSession session = new NetconfSession(sshSession);

        // take locks on CANDIDATE datastore so that we are not interrupted
        session.lock(NetconfSession.CANDIDATE);

        // reset candidates so that CANDIDATE is an exact copy of running
        session.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);

        // Get system configuration from the hpst
        NodeSet configs = client.getConfig();
        Configuration config = getJunosConfiguration(configs);

        // ========== Manipulate element tree locally
        
        // Change the as-number leaf value of 
        // /configuration/routing-options/autonomous-system
        // to 13.37
        AutonomousSystem aSystem = config.routingOptions.autonomousSystem;
        YangString asNumber = aSystem.getAsNumberValue();
        asNumber.setValue("13.37");
        
        // ========== End of local element tree manipulation

        // Write back the updated element tree to the device
        session.editConfig(NetconfSession.CANDIDATE, config);

        // candidates are now updated
        session.confirmedCommit(60);

        // Get the new configuration from the host
        NodeSet newConfigs = client.getConfig();
        Configuration newConfig = getJunosConfiguration(newConfigs);
        System.out.println("Committed:\n" + newConfig.toXMLString());

        // now commit them, unlock the candidate config and close connection
        // Note: in case of error, the device will rollback within 1 min
        session.commit();
        session.unlock(NetconfSession.CANDIDATE);
        client.dev.close();
    }

}

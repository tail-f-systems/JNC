package app;

import java.io.IOException;

import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.SSHConnection;
import com.tailf.jnc.SSHSession;

import gen.junosSystem.Junos;
import gen.junosSystem.Configuration;

/**
 * Client code to get the running configuration from a juniper router, copy it
 * to the candidate configuration and then commit any changes made to it.
 * 
 * NetconfSession is used directly, no "Device" is used.
 */
public class Client {

    // Hard coded host name, user name and password
    private String junosHost = "olive1.lab";
    private String junosUserName = "admin";
    private String pass = "Admin99";
    private NetconfSession session = null;

    /**
     * Constructor, creates a connected client object.
     * 
     * @throws IOException
     * @throws JNCException
     */
    public Client() throws IOException, JNCException {
        this.init();
    }

    /**
     * Connects to host using hard coded values
     * 
     * @throws IOException
     * @throws JNCException
     */
    private void init() throws IOException, JNCException {
        SSHConnection conn = new SSHConnection(junosHost, 22);
        conn.authenticateWithPassword(junosUserName, pass);
        SSHSession sshSession = new SSHSession(conn);
        session = new NetconfSession(sshSession);
    }
    
    /**
     * Returns the netconf session initiated by the client
     * 
     * @return an active netconf session
     */
    public NetconfSession getSession() {
        return session;
    }

    /**
     * Gets the first configuration element in configs with name "configuration".
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
        return (Configuration) config;
    }

    /**
     * @param args Ignored
     * @throws JNCException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JNCException {
        Junos.enable();
        Client client = new Client();
        NetconfSession session = client.getSession();

        // lock CANDIDATE datastore so that we are not interrupted
        session.lock(NetconfSession.CANDIDATE);

        // reset candidates so that CANDIDATE is an exact copy of running
        session.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);

        // Get system configuration from dev1
        NodeSet configs = session.getConfig(NetconfSession.RUNNING);
        Element sys1 = getJunosConfiguration(configs);
        System.out.println("Received:\n" + sys1.toXMLString());
        System.out.println("\n-----------------\n\n");

        // Manipulate element trees locally
        // TODO

        // Write back the updated element tree to the device
        session.editConfig(NetconfSession.CANDIDATE, sys1);

        // candidates are now updated
        session.confirmedCommit(60);

        // now commit them
        session.commit();
        session.unlock(NetconfSession.CANDIDATE);

        System.out.println("Committed:\n" + sys1.toXMLString());

        // Devices will rollback within 1 min
    }

}

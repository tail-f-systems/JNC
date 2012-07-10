package util;

import java.io.IOException;

import com.tailf.confm.Device;
import com.tailf.confm.DeviceUser;
import com.tailf.inm.ConfDSession;
import com.tailf.inm.Element;
import com.tailf.inm.INMException;
import com.tailf.inm.NetconfSession;
import com.tailf.inm.NodeSet;

public abstract class Client {

    /**
     * Creates a Device and a Device user and connects to localhost:2022
     * 
     * The ems user name defaults to "myusername", the device user name and
     * password to "admin" and the device name to "mydev".
     * 
     * @param dev A Device array of length 1 to save the connected Device in
     * @param duser A DeviceUser array of length 1 to save the Device User in
     */
    public static void init(Device[] dev, DeviceUser[] duser) {
        String emsUserName = "myusername";
        assert dev != null && dev.length == 1: 
            "dev must be Device array of length 1";
        assert duser != null && duser.length == 1: 
            "duser must be DeviceUser array of length 1";
        
        Device devcpy = dev[0];
        DeviceUser dusercpy = duser[0];

        try {
            duser[0] = new DeviceUser(emsUserName, "admin", "admin");
            dev[0] = new Device("mydev", duser[0], "localhost", 2022);
        } catch (NullPointerException npe) {
            dev[0] = devcpy;
            duser[0] = dusercpy;
        }

        try {
            dev[0].connect(emsUserName);
            dev[0].newSession("cfg");
        } catch (IOException e0) {
            System.err.println("Can't connect");
            System.exit(1);
        } catch (INMException e1) {
            System.err.println("Can't authenticate" + e1);
            System.exit(1);
        }
    }
    
    /**
     * Gets the running configuration of d
     * @param d A connected confm device
     * @return the current running configuration of d
     * @throws IOException If an I/O error occurs
     * @throws INMException if d is not connected
     */
    public static NodeSet getConfig(Device d) throws IOException, INMException {
        ConfDSession session = d.getSession("cfg");
        NodeSet reply = session.getConfig(NetconfSession.RUNNING);
        return reply;
    }

    /**
     * Updates the running configuration of d
     * 
     * @param d A connected confm device
     * @param config A configuration tree to update the running configuration with
     * @return The new running configuration
     * @throws IOException If an I/O error occurs
     * @throws INMException If configuration contains invalid data, the device
     *           lacks a connection, etc.
     */
    public static NodeSet editConfig(Device d, Element config)
            throws IOException, INMException {
        d.getSession("cfg").editConfig(config);
        // Inspect the updated RUNNING configuration
        return getConfig(d);
    }

}

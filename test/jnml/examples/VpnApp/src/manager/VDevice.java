/*    -*- Java -*- 
 * 
 *  Copyright 2007 Tail-F Systems AB. All rights reserved. 
 *
 *  This software is the confidential and proprietary 
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */
package manager;

import com.tailf.confm.*;
import java.util.ArrayList;
import java.io.*;


/**
 * 
 */
public class VDevice extends Device 
    implements Serializable {

    public transient NetconfLog console = null;
    private transient Subscriber sub;
    public String endpointIp;
    public String webuiUrl;


    /**
     *
     */
    VDevice(String name, DeviceUser user,
            String mgmt_ip,int mgmt_port,
            String endpointIp, int webuiPort) {
        
        super(name , user, mgmt_ip, mgmt_port);
	this.endpointIp = endpointIp;
	this.webuiUrl = "http://" + mgmt_ip + ":" + webuiPort;
    }
    

    public void setConsole(NetconfLog c) {
	console = c;
    }

    public boolean reconnect(NetworkCanvas nc) {
	close();
	return createSession(nc);
    }
    

    public boolean createSession(NetworkCanvas nc) {
	if (hasSession("cfg")) {
            // session already exists
            return true;
	}
        Subscriber sub = new Subscriber(console, name);
        try {
	    VDevice d;
	    // create the configTree even if we later fail to
	    // connect, we need that to accumulate the config changes
	    // also to down devices - this is to be able to backlog
            newSessionConfigTree("cfg");
            connect("admin");
            newSession( sub, "cfg");
            nc.enableRouter(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

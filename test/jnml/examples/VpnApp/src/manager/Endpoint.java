


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
import com.tailf.inm.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.InetAddress;
import java.io.*;

// An endpoint belongs to a Vpn
public class Endpoint implements Serializable {

    public String ip;          // tunnel endpoint IP
    public String deviceName;  // Name of VDevice where Endpoint lives
    public ArrayList networks; // list of networks contained at his end

    Endpoint(String ip, String deviceName) {
	this.deviceName = deviceName;
	this.ip = ip;
	this.networks = new ArrayList();
    }
    
    public String toString() {
	String s = 
	    "  EpIp: " + ip + "Name: " + deviceName + "\n" +
	    "  Networks ...\n" ;
	for(int i = 0; i<networks.size(); i++) {
	    Network n = (Network)networks.get(i);
	    s += "     " + n.toString() + "\n";
	}
	return s;
    }


    public Network findNetwork(String network, int masksz)
	throws Exception {

	for(int i=0; i<networks.size(); i++) {
	    Network n = (Network)networks.get(i);
	    if ((n.net.compareTo(network) == 0) &&
		n.masksize == masksz) {
		return n;
	    }
	}
	throw new Exception("network " + network +
			    " doesn't exists");
    }

    public void addNetwork(String network, int masksz)
	throws Exception {

	for(int i=0; i<networks.size(); i++) {
	    Network n = (Network)networks.get(i);
	    if ((n.net.compareTo(network) == 0) &&
		n.masksize == masksz) {
	    throw new Exception("network " + network +
				" already exists");
	    }
	}
	Network n = new Network(network, masksz);
	networks.add(n);
    }

    public void delNetwork(String network, int masksz)
	throws Exception {
	
	for(int i=0; i<networks.size(); i++) {
	    Network n = (Network)networks.get(i);
	    if (n.net.equals(network) &&
		n.masksize == masksz) {
		networks.remove(i);
		return;
	    }
	}
	return;
    }

}

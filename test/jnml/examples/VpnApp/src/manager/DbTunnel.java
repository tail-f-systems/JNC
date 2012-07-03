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
import java.util.Iterator;
import java.net.InetAddress;
import java.io.*;
import quagga.Tunnel;



public class DbTunnel implements Serializable {
    
    public String deviceName;  //
    public String name;        // name is unique
                               // and name is constructed from Vpn name + incr
    public String vpnName;
    public String localEndpoint;
    public String localNet;
    public String localNetmask;

    public String remoteEndpoint;
    public String remoteNet;
    public String remoteNetmask;
    
    public String sharedSecret;
    public String hashAlgo ="default";
    public String encAlgo="default";
    
    DbTunnel(Vpn v, String deviceName,
	     String localEndpoint, String localNet, String localNetmask,
	     String remoteEndpoint, String remoteNet, String remoteNetmask) {

	
	// Construct a unique name
	this.name = v.name + "-" + Db.tunnelCounter++;
	this.vpnName = v.name;
	this.deviceName = deviceName;
	this.localEndpoint = localEndpoint;
	this.localNet = localNet;
	this.localNetmask = localNetmask;
	
	this.remoteEndpoint = remoteEndpoint;
	this.remoteNet = remoteNet;
	this.remoteNetmask = remoteNetmask;
	
	this.sharedSecret = v.sharedSecret;
	this.hashAlgo = v.hashAlgo;
	this.encAlgo = v.encAlgo;
    }
    
    public String toString() {
	return "DbTunnel: name=" +name+ " device=" +deviceName +  "\n" +
	    "   localEndpoint :" + localEndpoint       + "\n" +
	    "   localNet      :" + localNet            + "\n" +
	    "   localNetmask  :" + localNetmask        + "\n" +
	    "   remoteEndpoint:" + remoteEndpoint      + "\n" +
	    "   remoteNet     :" + remoteNet           + "\n" +
	    "   remoteNetmask :" + remoteNetmask       + "\n" +
	    "   sharedSecret  :" + sharedSecret        + "\n" ;
    }

    // Create a Tunnel object suitable for sending over
    // NETCONF
    Tunnel makeTunnel() throws Exception {
	Tunnel t = new Tunnel(name);
	t.setLocalEndpointValue(localEndpoint);
	t.setLocalNetValue(localNet);
	t.setLocalNetMaskValue(localNetmask);
	t.setRemoteEndpointValue(remoteEndpoint);
	t.setRemoteNetValue(remoteNet);
	t.setRemoteNetMaskValue(remoteNetmask);
	t.setPreSharedKeyValue(sharedSecret);
	t.setHashAlgoValue(hashAlgo);
	t.setEncryptionAlgoValue(encAlgo);
	return t;
    }

}


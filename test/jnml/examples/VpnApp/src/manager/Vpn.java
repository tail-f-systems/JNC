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
import quagga.Defaults;

public class Vpn implements Serializable {

    public String name;         // all vpns must have a name
    public String sharedSecret; //
    public String encAlgo;
    public String hashAlgo;
    public ArrayList endpoints;

    Vpn(String name) {
	this.name = name;
	this.sharedSecret = new String("makemedasgoat");
	encAlgo = new String ("des");
	hashAlgo = new String("md5");
	endpoints = new ArrayList();
    }

    public String toString() {
	int i;
	String s = 
	    "VPN name        : " + name         + "\n" +
	    "sharedSecret    : " + sharedSecret + "\n" +
	    "encAlgo         : " + encAlgo      + "\n" +
	    "hashAlgo        : " + hashAlgo     + "\n" +
	    "trunnelNames    ";
	    
	s+= "\n";
	s+= "endpoints \n" ;

	for (i=0; i<endpoints.size(); i++) {
	    Endpoint ep = (Endpoint)endpoints.get(i);
	    s += ep.toString();
	}
	return s + "\n";
    }

    public Endpoint findEndpoint(String epip) {
	for (int i = 0; i<endpoints.size(); i++) {
	    Endpoint ep = (Endpoint)endpoints.get(i);
	    if (ep.ip.compareTo(epip) == 0)
		return ep;
	}
	return null;
    }


    public static InetAddress string2ip(String ip)  
	throws Exception {
	byte bs[] = new byte[4];
	String s[] = ip.split("\\.");
	bs[0] = (byte)Integer.parseInt(s[0]);
	bs[1] = (byte)Integer.parseInt(s[1]);
	bs[2] = (byte)Integer.parseInt(s[2]);
	bs[3] = (byte)Integer.parseInt(s[3]);
	return InetAddress.getByAddress(null, bs);
    }

    
    /* If endpoint already exist, ignore */
    public void addEndpoint(String epip, String deviceName) {
	
	for (int i=0; i<endpoints.size(); i++) {
	    Endpoint ep = (Endpoint)endpoints.get(i);
	    if (ep.ip.compareTo(epip) == 0)
		return;
	}
	Endpoint ep = new Endpoint(epip, deviceName);
	endpoints.add(ep);
    }

    public void addNetworkToEndpoint(String endpointIp, 
				     String network, int masksz) 
	throws Exception {
	Endpoint ep = findEndpoint(endpointIp);
	if (ep == null) 
	    throw new Exception("No such network found " + endpointIp);
	ep.addNetwork(network, masksz);
    }
    
    public void delNetworkFromEndpoint(String endpointIp, 
				       String network, int masksz)
	throws Exception {
	Endpoint ep = findEndpoint(endpointIp);
	if (ep == null) 
	    throw new Exception("No such network found " + endpointIp);
	ep.delNetwork(network, masksz);
    }
}

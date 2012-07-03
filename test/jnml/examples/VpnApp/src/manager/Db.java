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
import java.io.*;
import quagga.Defaults;
import java.util.Properties;

public class Db {
    public ArrayList devices;
    public ArrayList vpns;
    public ArrayList tunnels;
    public DbDefaults defaults;
    public static int tunnelCounter = 0;
    private String transFile = "./SERIAL.trans";
    private String savedFile = "./SERIAL.saved";
    private boolean transIsActive = false;
    ApplicationLog log;

    Db(ApplicationLog log)  {
	this.log = log;
	try {
	    new File(transFile).delete();
	    devices = new ArrayList();
	    vpns = new ArrayList();
	    tunnels = new ArrayList();
	    defaults = new DbDefaults();
	    new File(transFile).delete();
	    transIsActive = false;
	    try {
		restore(savedFile, true);
		for (int i =0; i<devices.size(); i++) {
		    Device d = (Device)devices.get(i);
		    d.initTransients();
		}
		log.showOnlyMessage("Database initialized from file");
	    } 
	    catch (IOException e)  {
		new File(transFile).delete();
		new File(savedFile).delete();
                demoConfig();
		log.showOnlyMessage("DB is empty ... initalized to default");
	    }
	    catch (Exception e2) {
		System.out.println("DB corrupt ");
		//e2.printStackTrace();
		System.exit(2);
	    }
	    print();
	}
	catch (Exception e) {
	    System.out.println("Failed to create db, remove /tmp/SERIAL.*\n");
	    e.printStackTrace();
	    System.exit(1);
	}
    }
    
    public boolean isActive() { return transIsActive; }

    public void startTrans()  {
	if (new File(transFile).exists())
	    return;
	try {
	    save(transFile);
	    transIsActive = true;
	}
	catch (Exception e) {
	}
    }


    public void abort() throws Exception {
	if (!transIsActive) 
	    throw new Exception("No transaction");
	transIsActive = false;
	restore(transFile, false);
	new File(transFile).delete();
    }
    
    public void commit() throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	transIsActive = false;
	save(savedFile);
	new File(transFile).delete();
	clearConfigTrees();
    }

    public void save() throws Exception {
	save(savedFile);
    }

    public void save(String file)  throws Exception {
	FileOutputStream fos = new FileOutputStream( file );
	ObjectOutputStream outStream = new ObjectOutputStream( fos );
	
	//  Save each object.
	outStream.writeObject( devices );
	outStream.writeObject( vpns );
	outStream.writeObject( tunnels );
	outStream.writeObject( defaults);
	outStream.writeObject( Db.tunnelCounter);
	outStream.flush();
	outStream.close();
    }


    public void restore(String file, boolean firstTime) throws Exception {

	int counter;
	String filePath = new String( file );
	FileInputStream fis = new FileInputStream( filePath );
	ObjectInputStream inStream = new ObjectInputStream( fis );
	ArrayList tmp;

	// Retrieve the Serializable object.
	tmp = ( ArrayList )inStream.readObject();
	if (firstTime) devices = tmp;
	vpns = (ArrayList)inStream.readObject();
	tunnels = (ArrayList)inStream.readObject();
	defaults = (DbDefaults)inStream.readObject();
	counter = (Integer)inStream.readObject();
	if (firstTime)
	    Db.tunnelCounter = counter;
	inStream.close();

   }


    public void print() {
	for (int i =0; i<devices.size(); i++) 
	    System.out.println(((VDevice)devices.get(i)));
	for (int i =0; i<vpns.size(); i++) 
	    System.out.println(((Vpn)vpns.get(i)));
 	for (int i =0; i<tunnels.size(); i++) 
	    System.out.println(((DbTunnel)tunnels.get(i)));
    }


    public boolean demoConfig() {
        try {
            DeviceUser u = new DeviceUser("admin", "admin", "admin");
            Properties configFile = new Properties();
            configFile.load(this.getClass().getClassLoader().
                            getResourceAsStream("manager/VpnApp.properties"));

            
            addDevice(
                "Router-West", u,
                configFile.getProperty("RouterWestManagementIp"),
                Integer.parseInt(configFile.getProperty("RouterWestNetconfPort")),
                configFile.getProperty("RouterWestIp"),
                Integer.parseInt(configFile.getProperty("RouterWestHttpPort")));

            addDevice("Router-North", u,
                      configFile.getProperty("RouterNorthManagementIp"),
                      Integer.parseInt(configFile.getProperty("RouterNorthNetconfPort")),
                      configFile.getProperty("RouterNorthIp"),
                      Integer.parseInt(configFile.getProperty("RouterNorthHttpPort")));

            addDevice("Router-East", u,
                      configFile.getProperty("RouterEastManagementIp"),
                      Integer.parseInt(configFile.getProperty("RouterEastNetconfPort")),
                      configFile.getProperty("RouterEastIp"),
                      Integer.parseInt(configFile.getProperty("RouterEastHttpPort")));

            save(savedFile);
            return true;
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return false;

    }

    VDevice addDevice(String name,  DeviceUser u,
		     String mgmt_ip,int port,
		     String endpointIp, int wwwport) {


	VDevice d = new VDevice(name, u, mgmt_ip, port, endpointIp, wwwport);
	devices.add(d);
	System.out.println("Added " + d);
	return d;
    }
    
    public void clearConfigTrees() {
	for (int i=0; i<devices.size(); i++) {
	    VDevice d = (VDevice)devices.get(i);
	    d.clearConfig("cfg");
	}
    }

    public VDevice getVDevice(String name)  {
	for (int i=0; i<devices.size(); i++) {
	    VDevice d = (VDevice)devices.get(i);
	    if (d.name.compareTo(name) == 0)
		return d;
	}
	return null;
    }

    public Vpn getVpn(String name) throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<vpns.size(); i++) {
	    Vpn d = (Vpn)vpns.get(i);
	    if (d.name.compareTo(name) == 0)
		return d;
	}
	throw new Exception("No such vpn: " + name);
    }

    public Vpn delVpn(String name) throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<vpns.size(); i++) {
	    Vpn d = (Vpn)vpns.get(i);
	    if (d.name.compareTo(name) == 0) {
		vpns.remove(i);
		return d;
	    }
	}
	throw new Exception("No such vpn: " + name);
    }



    public Vpn getVpnFromTunnelName(String tunnelName) throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<tunnels.size(); i++) {
	    DbTunnel t = (DbTunnel)tunnels.get(i);
	    if (t.name.compareTo(tunnelName) == 0)
		return getVpn(t.vpnName);
	}
	throw new Exception ("No such vpn with tunnel " + tunnelName);
    }

    public Vpn addVpn(Vpn v) throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<vpns.size(); i++) {
	    if (((Vpn)vpns.get(i)).name.compareTo(v.name) == 0)
		throw new Exception("VPN " + v.name + " already exists");
	}
	vpns.add(v);
	return v;
    }

    public void updateVpn(Vpn v) throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<vpns.size(); i++) {
	    if (((Vpn)vpns.get(i)).name.compareTo(v.name) == 0) {
		vpns.remove(i);
		vpns.add(v);	    
	    }
	}
    }

    public DbTunnel addTunnel(DbTunnel t) throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<tunnels.size(); i++) {
	    DbTunnel t2 = (DbTunnel)tunnels.get(i);
	    if ((t2.name.compareTo(t.name) == 0) &&
		(t2.deviceName.compareTo(t.deviceName) == 0)) 
		throw new Exception(" already exists " +
				    t2.name + "=" + t.name + " " +
				    t2.deviceName + "=" + t.deviceName);
	}
	tunnels.add(t);
	return t;
    }


    public DbTunnel deleteTunnel(String tunnelName) 
	throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<tunnels.size(); i++) {
	    DbTunnel t = (DbTunnel)tunnels.get(i);
	    if (t.name.compareTo(tunnelName) == 0) {
		tunnels.remove(i);
		return t;
	    }
	}
	throw new Exception(" Nu such tunnel to delete " + tunnelName);
    }

    
    public DbTunnel getTunnel(String tunnelName) 
            throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<tunnels.size(); i++) {
	    DbTunnel t = (DbTunnel)tunnels.get(i);
	    if (t.name.compareTo(tunnelName) == 0)
		return t;
	}
	return null;
    }
    
    public DbTunnel updateTunnel(DbTunnel newTunnel) throws Exception {
	if (!transIsActive ) 
	    throw new Exception("No transaction");
	for (int i=0; i<tunnels.size(); i++) {
	    DbTunnel t = (DbTunnel)tunnels.get(i);
	    if (t.name.compareTo(newTunnel.name) == 0) {
		tunnels.remove(i);
		tunnels.add(newTunnel);
		return t;
	    }
	}
	return null;
    }
    

    // ret/set db default entries
    public void setDefaultEnc(String s) {
	defaults.encAlgo = s;
    }
    public void setDefaultHash(String s) {
	defaults.hashAlgo = s;
    }
    public String getDefaultEnc() {
	return defaults.encAlgo;
    }
    public String getDefaultHash() {
	return defaults.hashAlgo;
    }



    // Generate an /system/vpn/ipsec/defaults object suitable for NETCONF
    Defaults makeDefaults()  {
	try {
	    Defaults d = new Defaults();
	    d.setEncryptionAlgoValue(defaults.encAlgo);
	    d.setHashAlgoValue(defaults.hashAlgo);
	    return d;
	}
	catch (Exception e) {
	    return null;
	}
    }


    public static class DbDefaults implements Serializable {
	public String encAlgo;
	public String hashAlgo;
	
	DbDefaults() {
	    encAlgo = new String("des");
	    hashAlgo = new String("md5");
	}
    }

}

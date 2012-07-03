/*    -*- Jaa -*- 
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
import java.io.*;

import quagga.Tunnel;
import quagga.Ipsec;
import quagga.Defaults;

// Our "Vertical" application
public class Vapp {

    private Db db;
    SockMonitor sockMon = null;
    static ApplicationLog log;
    public VpnSetup vpnSetup;
    public VpnFrame vpnFrame;
    private boolean hasOutstandingCommit = false;
    private ConfirmCommitCountDownThread commitThread = null;

    public static final int  SECRET = 1;
    public static final int  ENC    = 2;
    public static final int  HASH   = 3;

    Vapp(Db db, ApplicationLog log) {
	this.db = db;
	Vapp.log = log;
	sockMon = new SockMonitor(this, db);
	sockMon.start();
    }
    
    public static void log(String s) { 
	log.showOnlyMessage(s); 
    }
    public static void log(String s, boolean showDialog) { 
	log.showMessage(s, showDialog); 
    }
    public static void log(VDevice d, String s) { 
	log.showOnlyMessage("["+d.name+"] " + s); 
    }
    public static void log(VDevice d, String s, boolean showDialog) { 
	log.showMessage("["+d.name+"] " + s, showDialog); 
    }

    public void startTrans() {
	db.startTrans();
    }
    
    public void abort()  {
	for (int i= 0; i<db.devices.size(); i++) {  // Force unlock
	    VDevice d = (VDevice)db.devices.get(i);
	    d.reconnect(vpnFrame.getNetworkCanvas());
	    d.clearConfig("cfg");
	}
	hasOutstandingCommit = false;
	try {
            db.abort();
	    startTrans();
	    vpnSetup.rePaint();
        }
        catch (Exception e) {
            System.out.println("Failed to abort");
            e.printStackTrace();
        }
    }

      
    public void validate()  {
        boolean ok = true;
        for (int i= 0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
	    if (! d.hasSession("cfg")) {
		Vapp.log(d, "Not connected");
		ok = false;
	    }
	    else  {
		try {
		    d.getSession("cfg").validate(NetconfSession.CANDIDATE);
		} catch (Exception e) {
		    Vapp.log(d, ": Failed " + e); 
		    e.printStackTrace();
		    ok=false;
		}
	    }
	}
        if (ok)
            Vapp.log("All Candidates validate ok", true);
	else
            Vapp.log("All Candidates NOT ok", true);
    }


    private String makemask (int n) {
	
	n = 32 - n;
	int iVal = ((1 << n) - 1);

	int[] arr = new int[4];
	arr[0] = ((iVal >> 24) & 0xff);
	arr[1] = ((iVal >> 16) & 0xff);
	arr[2] = ((iVal >> 8)  & 0xff);
	arr[3] = ((iVal >> 0)  & 0xff);

	for (int j=0; j<4; j++)
	    arr[j] = (~(arr[j])) & 0xff;

	String ret = new String();
	for (int k=0; k<4; k++) {
	    ret += arr[k];
	    if ( k!= 3)
		ret += ".";
	}
	return ret;
    }

  
    synchronized public void addNetworkToVpn(
	String vpnName, String deviceName,
	String endpointIp, String network, int masksz)
	throws Exception {

	Vpn v = db.getVpn(vpnName);
	VDevice targetDev = db.getVDevice(deviceName);
	quagga.System targetSys = initVDevice(targetDev);
	
	if (v.findEndpoint(endpointIp) == null) 
	    v.addEndpoint(endpointIp, deviceName);
	v.addNetworkToEndpoint(endpointIp, network, masksz);
	Endpoint targetEp = v.findEndpoint(targetDev.endpointIp);

	// Now we need to figure out which new tunnels 
	// this amounts to 

	for (int i = 0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
	    quagga.System s2 = initVDevice(d);
	    if (d.name.compareTo(targetDev.name) == 0) {
		continue;
	    }
	    // So which tunnels should we have from 
	    // vpn@targetVDevice to vpn@d            */
	    // First find the endpoint at d which is used by our vpn 

	    Endpoint ep = v.findEndpoint(d.endpointIp);
	    if (ep == null)
		continue;

	    for (int j=0; j < ep.networks.size(); j++) {
		Network n = (Network)ep.networks.get(j);
		quagga.System s = initVDevice(d);

		// There'll be two new Tunnel objects from this 
		// Start with the tunnel at d to targetVDevice                 
		DbTunnel dbt1 = new DbTunnel(
		    v, d.name,
		    ep.ip, n.net, makemask(n.masksize),
		    targetEp.ip, network, makemask(masksz));

		db.addTunnel(dbt1);
		s.vpn.ipsec.addTunnel(dbt1.makeTunnel());

		// Now the equivalent at the the targetVDevice
		DbTunnel dbt2 = new DbTunnel(
		    v, targetDev.name,
		    targetEp.ip, network, makemask(masksz),
		    ep.ip, n.net, makemask(n.masksize));

		db.addTunnel(dbt2);
		targetSys.vpn.ipsec.addTunnel(dbt2.makeTunnel());

	    }
	}
    }

    private void p(String s) { System.out.println(s); }

    synchronized public void setDefaultEncryption(String encAlg) {
	try {
	    db.setDefaultEnc(encAlg);
	    for (int j = 0; j<db.devices.size(); j++) {
		Defaults def = db.makeDefaults();
		VDevice d = (VDevice)db.devices.get(j);
		quagga.System s = initVDevice(d);
		s.vpn.ipsec.addDefaults(def);
	    }
	}
	catch (Exception e) {
	    System.out.println("ERR " + e);
	}
    }

    synchronized public void setDefaultHash(String hashAlg) {
	try {
	    db.setDefaultHash(hashAlg);
	    for (int j = 0; j<db.devices.size(); j++) {
		Defaults def = db.makeDefaults();
		VDevice d = (VDevice)db.devices.get(j);
		quagga.System s = initVDevice(d);
		s.vpn.ipsec.addDefaults(def);
	    }
	}
	catch (Exception e) {
	    System.out.println("ERR " + e);
	}
    }



    private boolean hasDefaults(VDevice d) {
	try {
	    quagga.System s = (quagga.System)d.getConfig("cfg");
	    if (s.vpn.ipsec.defaults != null)
		return true;
	    return false;
	}
	catch (Exception e) {
	    return false;
	}
    }

    private boolean hasTunnelsCfg(VDevice d) {
	try {
	    quagga.System s = (quagga.System)d.getConfig("cfg");
	    ElementChildrenIterator it = s.vpn.ipsec.tunnelIterator();
	    return it.hasNext();
	}
	catch (Exception e) {
	    return false;
	}
    }

    synchronized public void changeNetworkInVpn(String vpnName, 
						String deviceName,
						String endpointIp, 
						String newNetwork, 
						int newMasksz,
						String prevNetwork, 
						int prevMasksz)
	
	throws Exception {
	quagga.System s;
	Tunnel t;
	Vpn v = db.getVpn(vpnName);
	Endpoint targetEp = v.findEndpoint(endpointIp);
	VDevice targetDev = db.getVDevice(targetEp.deviceName);
	ArrayList tmp = new ArrayList();
	// Deal with the targetDev first
	s = initVDevice(targetDev);
	for (int i=0; i<db.tunnels.size(); i++) {
	    DbTunnel dbt = (DbTunnel)db.tunnels.get(i);
	    if ((dbt.deviceName.compareTo(targetDev.name) == 0) &&
		(dbt.localNet.compareTo(prevNetwork) == 0) &&
		(dbt.localNetmask.compareTo(makemask(prevMasksz)) == 0)) {
		// WE have a tunnel going out from the dev
		dbt.localNet = newNetwork;
		dbt.localNetmask = makemask(newMasksz);
		tmp.add(dbt);
		t = dbt.makeTunnel();
		s.vpn.ipsec.addTunnel(t);
	    }
	}

	// Now do all the other devs
	for (int j = 0; j<db.devices.size(); j++) {
	    VDevice d = (VDevice)db.devices.get(j);
	    quagga.System s2 = initVDevice(d);
	    if (d.name.compareTo(targetDev.name) == 0) {
		continue;
	    }
	    for (int k=0; k<db.tunnels.size(); k++) {
		DbTunnel dbt2 = (DbTunnel)db.tunnels.get(k);
		if ((dbt2.deviceName.compareTo(d.name) == 0) &&
		    (dbt2.remoteNet.compareTo(prevNetwork) == 0) &&
		    (dbt2.remoteNetmask.compareTo(makemask(prevMasksz)) == 0)) {
		    // WE have a tunnel going out from the dev
		    s = initVDevice(d);
		    dbt2.remoteNet = newNetwork;
		    dbt2.remoteNetmask = makemask(newMasksz);
		    tmp.add(dbt2);
		    t = dbt2.makeTunnel();
		    s.vpn.ipsec.addTunnel(t);
		}
	    }
	}
	// also modify the database accordingly
	for(int k=0; k<tmp.size(); k++) {
	    db.updateTunnel((DbTunnel)tmp.get(k));
	}
	Network targetNet = targetEp.findNetwork(prevNetwork, prevMasksz);
	targetNet.net = newNetwork; 
	targetNet.masksize = newMasksz;
	db.updateVpn(v);
    }

    
    synchronized public void delNetworkFromVpn(String vpnName, 
					       String endpointIp, 
					       String network,int masksz)
	throws Exception {
	
	quagga.System s;
	Tunnel t;
	Vpn v = db.getVpn(vpnName);
	Endpoint targetEp = v.findEndpoint(endpointIp);
	VDevice targetDev = db.getVDevice(targetEp.deviceName);
	
	// Deal with the targetDev first
	s = initVDevice(targetDev);
	for (int i=0; i<db.tunnels.size(); i++) {
	    DbTunnel dbt = (DbTunnel)db.tunnels.get(i);
	    if ((dbt.deviceName.compareTo(targetDev.name) == 0) &&
		(dbt.localNet.compareTo(network) == 0) &&
		(dbt.localNetmask.compareTo(makemask(masksz)) == 0)) {
		// WE have a tunnel going out from the dev
		t = new Tunnel(dbt.name);
		t.markDelete();
		s.vpn.ipsec.addTunnel(t);

		db.tunnels.remove(i); // Can I do this here
	    }
	}

	// Now do all the other devs
	for (int j = 0; j<db.devices.size(); j++) {
	    VDevice d = (VDevice)db.devices.get(j);
	    quagga.System s2 = initVDevice(d);
	    if (d.name.compareTo(targetDev.name) == 0) {
		continue;
	    }
	    for (int k=0; k<db.tunnels.size(); k++) {
		DbTunnel dbt2 = (DbTunnel)db.tunnels.get(k);
		if ((dbt2.deviceName.compareTo(d.name) == 0) &&
		    (dbt2.remoteNet.compareTo(network) == 0) &&
		    (dbt2.remoteNetmask.compareTo(makemask(masksz)) == 0)) {
		    // WE have a tunnel going out from the dev
		    s = initVDevice(d);
		    t = new Tunnel(dbt2.name);
		    t.markDelete();
		    s.vpn.ipsec.addTunnel(t);

		    db.tunnels.remove(k); // Can I do this here
		}
	    }
	}
	v.delNetworkFromEndpoint(endpointIp, network, masksz);
	db.updateVpn(v);
    }


    // Create new empty VPN, with a default shared secret
    synchronized public void addVpn(String vpnName, String secret,
				    String encAlgo, String hashAlgo) 
	throws Exception {
	Vpn v = new Vpn(vpnName);
	v.sharedSecret = new String(secret);
	v.encAlgo = new String(encAlgo);
	v.hashAlgo = new String(hashAlgo);

	for (int i = 0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
	    v.addEndpoint(d.endpointIp, d.name);
	}
	db.addVpn(v);
    }


    synchronized public void delVpn(String vpnName) throws Exception {
	Vpn v = db.getVpn(vpnName);
	quagga.System s;
	int i;
	ArrayList del = new ArrayList();

	for (i = 0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
	    s = initVDevice(d);
	    for (int j=0; j<db.tunnels.size(); j++) {
		DbTunnel dbt = (DbTunnel)db.tunnels.get(j);
		// Is this tunnel part of the Vpn ?
		if ((dbt.vpnName.compareTo(v.name) == 0) &&
		    (dbt.deviceName.compareTo(d.name) == 0)) {
		    Tunnel t = new Tunnel(dbt.name);
		    t.markDelete();
		    s.vpn.ipsec.addTunnel(t);
		    del.add(dbt.name);
		}
	    }
	}
	for (i =0; i<del.size(); i++) {
	    db.deleteTunnel((String)del.get(i));
	}
	// and also remove it from the Db
	for (i = 0; i<db.vpns.size(); i++) {
	    v = (Vpn)db.vpns.get(i);
	    if (v.name.compareTo(vpnName) == 0) {
		db.vpns.remove(i);
		return;
	    }
	}
    }

 
    
    // We always need an initial /system/vpn/ipsec tree
    // to hang our configs in

    public quagga.System initVDevice(VDevice d)  {
	try {
	    quagga.System s;
	    if (!d.hasConfig("cfg")) {
		s =  new quagga.System();
		s.addVpn().addIpsec();
                d.setConfig("cfg", s );
	    }
	    else {
		s = (quagga.System)d.getConfig("cfg");
	    }
	    return s;
	}
	catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }


    private void setVpnGlobal(String vpnName, String str, int type) {
	try {
	    Vpn v = db.getVpn(vpnName);
	    quagga.System s;
	    ArrayList tmp = new ArrayList();

	    // Now figure out which tunnels are configured
	    // at this device for the Vpn
		
	    for (int j=0; j<db.tunnels.size(); j++) {
		DbTunnel dbt  = (DbTunnel)db.tunnels.get(j);
		if (dbt.vpnName.compareTo(v.name) == 0) {
		    Tunnel t = null;
		    VDevice d = db.getVDevice(dbt.deviceName);
		    s = initVDevice(d);
		    switch (type) {
		    case Vapp.SECRET:
			dbt.sharedSecret = str;
			tmp.add(dbt); // accumulate change
			t = new Tunnel(dbt.name);
			t.setPreSharedKeyValue(str);
			break;
		    case Vapp.ENC:
			dbt.encAlgo = str;
			tmp.add(dbt); // accumulate change
			t = new Tunnel(dbt.name);
			t.setEncryptionAlgoValue(str);
			break;
		    case Vapp.HASH:
			dbt.hashAlgo = str;
			tmp.add(dbt); // accumulate change
			t = new Tunnel(dbt.name);
			t.setHashAlgoValue(str);
			break;
		    }
		    s.vpn.ipsec.addTunnel(t);
		}
	    }
	    
	    for (int k=0; k<tmp.size(); k++) {
		db.updateTunnel((DbTunnel)tmp.get(k));
	    }
	    switch (type) {
	    case Vapp.SECRET:
		v.sharedSecret = str;
		break;
	    case Vapp.ENC:
		v.encAlgo = str;
		break;
	    case Vapp.HASH:
		v.hashAlgo = str;
	    }
	    db.updateVpn(v);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    

    synchronized public void setSharedSecret(String vpnName, String secret) {
	setVpnGlobal(vpnName, secret, Vapp.SECRET);

    }
    synchronized public void setEncAlgo(String vpnName, String algo) {
	setVpnGlobal(vpnName, algo, Vapp.ENC);
    }
    synchronized public void setHashAlgo(String vpnName, String algo) {
	setVpnGlobal(vpnName, algo, Vapp.HASH);
    }


    public boolean compareTunnels(Tunnel t1, Tunnel t2) throws Exception {
	
	return t1.compare(t2) == 0;
    }
    
    // Extract the complete ipsec configuration for a device
    // I.e the list of DbTunnel objects for a device
    // kept in the local database

    public quagga.System dbVDeviceConf(VDevice d) 
	throws Exception {

	quagga.System s = new quagga.System();
	s.addVpn().addIpsec();
	s.vpn.ipsec.addDefaults(db.makeDefaults());
	for (int i=0; i<db.tunnels.size(); i++) {
	    DbTunnel dbt = (DbTunnel)db.tunnels.get(i);
	    if (dbt.deviceName.compareTo(d.name) == 0) {
		s.vpn.ipsec.addTunnel(dbt.makeTunnel());
	    }
	}
	return s;
    }


    public static Element ncVDeviceConf(VDevice d) 
	throws Exception {

        quagga.Quagga.enable();
        quagga.Quagga.registerSchema();

	Element filter =
	    Element.create("http://tail-f.com/ns/example/quagga/1.0",
			   "/system/vpn/ipsec");
	NodeSet reply = d.getSession("cfg").getConfig(NetconfSession.RUNNING, 
					    filter);
	Element el = reply.first();
	return el;
    }


    public void ncPrint() throws Exception {
	for (int i=0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
	    Element e = ncVDeviceConf(d);
	    System.out.println("VDevice: "+d.name);
	    if (e!=null)
		System.out.println(e.toXMLString());
	}
    }



    /**
     * CheckSync
     *
     */    
    public String checkSync(VDevice d) throws Exception {
	
	if (d.hasConfig("cfg")) {
	    return new String (
		d.name + 
		": Cannot check sync when we have outstanding config changes" +
		" Abort pending changes first \n");
	}
	if (!d.hasSession("cfg")) {
	    return new String(d.name+ ": Not connected");
	}

	// Get the VPN config for the device
	quagga.Quagga.enable();
        quagga.Quagga.registerSchema();

	Element filter =
	    Element.create("http://tail-f.com/ns/example/quagga/1.0",
			   "/system/vpn/ipsec");
	NodeSet reply = d.getSession("cfg").getConfig(NetconfSession.RUNNING, filter);
        if (reply.first() == null ) {
            return new String(d.name + ": No configuration returned");
        }                                         
	quagga.System s = (quagga.System)reply.first();
	quagga.System s2 = dbVDeviceConf(d);

	// checkSync
	if (s.checkSync( s2 )) {
	    // OK
	    return new String();
	}
	
	// They are different. 
	Container sync = s.sync( s2 );
	String ret = new String(d.name + ": "+ sync.toXMLString());
	return ret;
    }



    /**
     * Sync
     *
     */
    public boolean makeSync(VDevice d) throws Exception {
	// Get the VPN config for the device
	if (d.hasConfig("cfg")) {
            vpnSetup.allIsCommited();
	    Vapp.log(
		d, 
		"Cannot make sync when we have outstanding config changes. " +
		"Abort pending changes first!");
	    return false;
	}
	if (!d.hasSession("cfg")) {
            vpnSetup.allIsCommited();
	    Vapp.log(d, "No connection");
	    return false;
	}

	quagga.Quagga.enable();
        quagga.Quagga.registerSchema();

	Element filter =
	    Element.create("http://tail-f.com/ns/example/quagga/1.0",
			   "/system/vpn/ipsec");
	NodeSet reply = d.getSession("cfg").getConfig(NetconfSession.RUNNING, filter);
	quagga.System s = (quagga.System)reply.first();
	quagga.System s2 = dbVDeviceConf(d);
	
	// sync
	Container sync = s.sync( s2 );
	d.setConfig( "cfg", sync );

        vpnSetup.allIsCommited();
	
        // Now we need to push these changes to the device
	return true;
    }
    
   

    synchronized public void validateButton() {
	validate();
    }
    
    // check sync
    synchronized public void inspectButton() {
	String total = new String();
	boolean ok = true;
	for (int i=0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
	    try {
		String s = checkSync(d);
		total += s;
	    } catch (Exception e) {
		e.printStackTrace();
		log.showOnlyMessage("ERR for  device " + d.name +
                                " " + e.toString());
		ok = false;
	    }
	}
	if (total.length() == 0) {
	    log.showMessage("All devices are synchronized", true);
	}
	else {
	    log.showMessage(total, false);
	    log.showMessage("All devices are NOT synchronized \n", true);
	}
    }
    
    private void unlockSome(int j) {
	int i; VDevice d;
	for (i=0; i<j; i++) {
	    d = (VDevice)db.devices.get(i);
	    if (!d.hasConfig("cfg"))
		continue;
	    if (!d.hasSession("cfg"))  // already released
		continue;
	    try {
		d.getSession("cfg").unlock(NetconfSession.CANDIDATE);
		d.getSession("cfg").unlock(NetconfSession.RUNNING);
	    } catch (Exception e) {}
	}
    }

    private boolean lockResetAll() {
	int i; VDevice d;
	for (i=0; i<db.devices.size(); i++) {
	    d = (VDevice)db.devices.get(i);
	    if (d.getConfig("cfg") == null)
		continue;
	    if (!d.hasSession("cfg")) {
		if (!d.createSession(vpnFrame.getNetworkCanvas())) {
		    if (!hasTunnelsCfg(d) && hasDefaults(d)) {
			// special case this data can be 
			// backlogged, we do that here
			// FIXME I need to get this to play with
			// confirmed commit ... how?
			d.addBackLog(d.getConfig("cfg"));
			d.clearConfig("cfg");
			continue;
		    }
		    Vapp.log(d, "Cannot create SSH session");
		    unlockSome(i);
		    return false;
		}
	    }
	    try {
		d.getSession("cfg").discardChanges();
		d.getSession("cfg").lock(NetconfSession.CANDIDATE);
		d.getSession("cfg").lock(NetconfSession.RUNNING);
		d.getSession("cfg").copyConfig(NetconfSession.RUNNING, 
				     NetconfSession.CANDIDATE);

	    } 
	    catch (IOException ioe) {
		// Maybe old socket was dead - try to reconnect
		try {
		    if (!d.createSession(vpnFrame.getNetworkCanvas())) {
			Vapp.log(d, "Cannot re-create SSH session");
			unlockSome(i);
			return false;
		    }
		    d.getSession("cfg").discardChanges();
		    d.getSession("cfg").lock(NetconfSession.CANDIDATE);
		    d.getSession("cfg").lock(NetconfSession.RUNNING);
		    d.getSession("cfg").copyConfig(NetconfSession.RUNNING, 
					 NetconfSession.CANDIDATE);
		} 
		catch (Exception ee) {  
		    unlockSome(i);
		    Vapp.log(d, "\n" + ee.toString());
		    return false;
		}
	    }
	    catch (INMException nme) {
		Vapp.log(d, nme.toString());
		unlockSome(i);
		return false;
	    }
	    catch (Exception e2) {
		e2.printStackTrace();
		unlockSome(i);
		return false;
	    }
	}
	return true;
    }
		


    synchronized public boolean commitButton(int timeout, ConfirmCommitCountDownThread th) {
	
	int i; VDevice d;
	quagga.System s;

	if (prune() == 0) {
	    Vapp.log("Nothing to do", true);
	    try {
		db.commit();  //
		startTrans();
	    }
	    catch (Exception eee) {}
	    if (th != null) 
		th.closedSocketAborted = true; // hack
	    return true;
	}

	if (!lockResetAll()) {
	    Vapp.log("Failed to lock and reset all devices - try later", true);
	    return false;
	}
	// Start by trying to copy everything to all candidates
	// regardless of timeout
	

	for (i=0; i<db.devices.size(); i++) {
	    d = (VDevice)db.devices.get(i);
	    if ((s = (quagga.System)d.getConfig("cfg")) == null) 
		continue;
	    try {
		d.getSession("cfg").editConfig(NetconfSession.CANDIDATE, s);
		d.getSession("cfg").validate(NetconfSession.CANDIDATE);
	    }
	    catch (IOException ioe) {
		Vapp.log(d, "Cannot SSH: " + ioe);
		vpnSetup.rePaint();
		unlockSome(db.devices.size());
		return false;
	    }
	    catch (INMException nme) {
		Vapp.log(d, "rejects change: " + nme);
		unlockSome(db.devices.size());
		return false;
	    }
            catch (Exception eee) {
                System.out.println("CRASH" + eee);
                eee.printStackTrace();
                return false;
            }
	}

	if (timeout == 0) {  // We commit everything to RUNNING now
	    for (i=0; i<db.devices.size(); i++) {
		d = (VDevice)db.devices.get(i);
		if ((s = (quagga.System)d.getConfig("cfg")) == null) 
		    continue;
		try {
		    d.getSession("cfg").commit();
		    d.getSession("cfg").unlock(NetconfSession.CANDIDATE);
		    d.getSession("cfg").unlock(NetconfSession.RUNNING);
		    d.clearConfig("cfg");
		} catch (Exception e) {
		    Vapp.log(
			d, "Failed to commit, system might be out of sync "+e, 
			true);
		    abort();
		    return false;
		}
	    }
	    try {
                db.commit();
            }
            catch (Exception e2) {
                System.out.println("Failed to DB commit" + e2);
                e2.printStackTrace();
		return false;
            }
	    startTrans();
            vpnSetup.allIsCommited();
	    Vapp.log("Committed to all live devices, backlogged for unavailable devices", true);
	    return true;
	}
	else {  // commit with a timeout
	    for (i=0; i<db.devices.size(); i++) {
		d = (VDevice)db.devices.get(i);
		if ((s = (quagga.System)d.getConfig("cfg")) == null) 
		    continue;
		try {
		    d.getSession("cfg").confirmedCommit(timeout);
		} catch (Exception e) {
		    Vapp.log(d, "Rejected the change: " + e, false);
		    Vapp.log(d, "Rejected the change: ", true);
		    abort();
		    return false;
		}
	    }
	    this.commitThread = th;
	    hasOutstandingCommit = true;
            vpnSetup.allIsCommited();
	    return true;
	}
    }

    
    // called by SockMonitor from different thread
    public void closedSocket(VDevice d) {
	System.out.println("CLOSE " + d.name);
	d.close();
	if (hasOutstandingCommit) {
	    abort();
	    Vapp.log(d, "Socket closed - aborting ", true);
	    commitThread.closedSocketAborted = true;
	    commitThread = null;
	    vpnSetup.networkCanvas.disableRouter(d.name);
	}
	else {
	    Vapp.log(d, "Socket closed");
	    vpnSetup.networkCanvas.disableRouter(d.name);
	}
    }

    
    public void openedSocket(VDevice d) {
	System.out.println("OPEN " + d.name);
	Vapp.log(d, "Socket reopened");
	vpnSetup.networkCanvas.enableRouter(d.name);
    }


    public int prune()  {
	int retVal = 0;
	for (int i=0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
            retVal +=  prune(d);
	}
	return retVal;
    }
    

    public int prune(VDevice d) { 
	quagga.System s = (quagga.System)d.getConfig("cfg");
	if (s == null)
	    return 0;
	if (hasDefaults(d)) {
	    return 1;
	}
	if (hasTunnelsCfg(d)) {
	    return 1;
	}
	d.clearConfig("cfg");
	return 0;
    }

    synchronized void confirmCommitButton(int timeout) {
	int i; VDevice d;
        quagga.System s;
	String activeDevs = new String();
	hasOutstandingCommit = false;
	if (prune() == 0) {
	    Vapp.log("Nothing to do", true);
	    return;
	}
	// we start off by releasing all our CANDIDATE locks
	// just to make sure all sockets are still upp
	// If they are not - we want to abort
	for (i=0; i<db.devices.size(); i++) {
	    d = (VDevice)db.devices.get(i);
	    if ((s = (quagga.System)d.getConfig("cfg")) == null) 
		continue;
	    try {
		d.getSession("cfg").unlock(NetconfSession.CANDIDATE);
		activeDevs += " " + d.name + " ";
	    }
	    catch (Exception  e) {
		System.out.println("Failed to unlock " + d.name + e);
		// socket died (or was reconnected)  during the timeout
		// wee need to abort everything - d has already aborted
		abort();
		Vapp.log(d, "aborted the transaction", true);
		return;
	    }
	}
	// So far so good - all devices are still up - lets commit them all
	for (i=0; i<db.devices.size(); i++) {
	    d = (VDevice)db.devices.get(i);
	    if ((s = (quagga.System)d.getConfig("cfg")) == null) 
		continue;
	    try {
		d.getSession("cfg").commit();
		d.getSession("cfg").unlock(NetconfSession.RUNNING);
		d.clearConfig("cfg");
	    }
	    catch (Exception ee) {
		// This is a most unfortunate case - we now have
		// inconsistencies. This is how 2pc works - tough life.
		// abort the remainder and our own db
		ee.printStackTrace();
		Vapp.log(d, "failed the commit - " + 
			 "we have inconsistency");
		abort();
	    }
	}
	// Brilliant - now commit local db 
	try {
            db.commit();
        }
        catch (Exception e3) {
            System.out.println("ERRR FAILED To commit" + e3);
            e3.printStackTrace();
        }
	startTrans();
	vpnSetup.rePaint();
	Vapp.log("Commited to all live devices, backlogged for unavailable devices", true);
    }


    synchronized void rejectCommitButton(int timeout) {
	abort();
        vpnSetup.allIsCommited();
	Vapp.log("Aborted all outstanding transactions", true);
    }   

    synchronized void timedOutCommit() {
	abort();
        vpnSetup.allIsCommited();
	Vapp.log("Commit timed out - transaction is aborted", true);
    }   

    public void probeButton() {
	try {
	    VDevice d = (VDevice)db.getVDevice("Router-North");
	    Element e = ncVDeviceConf(d);
	    System.out.println("CONF NORT" + e.toXMLString());
	    db.print();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    synchronized public void abortButton() {
	if (hasOutstandingCommit) {
            vpnSetup.allIsCommited();
	    Vapp.log("Cannot abort with outstanding commit - use reject",true);
	    return;
	}

	abort();
	Vapp.log("Aborted all outstanding transactions", true);
    }

    
    synchronized public void statsButton() {
	db.print();
	for (int i=0; i<db.devices.size(); i++) {
	    VDevice d = (VDevice)db.devices.get(i);
	    try {
		d.getSession("cfg").lock(NetconfSession.RUNNING);
		d.getSession("cfg").unlock(NetconfSession.RUNNING);
	    }
	    catch (Exception e) {
		System.out.println("Failed to lock/unlock running" + d.name);
	    }

	    try {
		d.getSession("cfg").lock(NetconfSession.CANDIDATE);
		d.getSession("cfg").unlock(NetconfSession.CANDIDATE);
	    }
	    catch (Exception e2) {
		System.out.println("Failed to lock/unlock cand" + d.name);
	    }
	}
    }
    
    synchronized public void syncButton()  {
	int i; VDevice d;
	quagga.System s;
	if (hasOutstandingCommit) {
            vpnSetup.allIsCommited();
	    Vapp.log("Cannot sync with outstanding commit", true);
	    return;
	}
	for (i=0; i<db.devices.size(); i++) {
	    d = (VDevice)db.devices.get(i);
	    try {
		if (!makeSync(d)) {
		    abort();
		    return;
		}
	    }
	    catch (IOException ioe) {
		Vapp.log(d, "Cannot SSH to device " + ioe);
		abort();
		return;
	    }
	    catch (Exception e) {
		e.printStackTrace();
		Vapp.log("ERR for  device " + d.name +
			 " " + e.toString());
		abort();
		return;
	    }
	}
	if (prune() == 0) {
            vpnSetup.allIsCommited();
	    Vapp.log("Nothing to do", true);
	    return;
	}
	for (i=0; i<db.devices.size(); i++) {
	    d = (VDevice)db.devices.get(i);           
	    if ((s = (quagga.System)d.getConfig("cfg")) == null) 
		continue;
	    try {
		d.getSession("cfg").copyConfig(NetconfSession.RUNNING, 
				     NetconfSession.CANDIDATE);
		d.getSession("cfg").editConfig(NetconfSession.CANDIDATE, s);
		d.getSession("cfg").validate(NetconfSession.CANDIDATE);
	    } catch (Exception e2) {
		abort();
		Vapp.log(d, "failed to edit config " + e2);
		return;
	    }
	}
	// so far so good, let's commit everything
	for (i=0; i<db.devices.size(); i++) {
	    d = (VDevice)db.devices.get(i);
	    if ((s = (quagga.System)d.getConfig("cfg")) == null) 
		continue;
	    try {
		d.getSession("cfg").commit();
	    } catch (Exception e3) {
		Vapp.log(d, "Failed to commit, system might be out of sync", 
			 true);
		abort();
		return;
	    }
	}
	// All is ok at the device, now we comit db
    	try {
            db.commit();
        }
        catch (Exception e3) {
            System.out.println("Failed to commit");
            e3.printStackTrace();
	    abort();
	    return;
        }
	startTrans();
	vpnSetup.rePaint();
	log.showMessage("All devices are  synchronized");
    }

    public void showBacklog(VDevice d) {
	if (! d.hasBacklog()) {
	    Vapp.log(d, "No backlog", true);
	    return;
	}
        Element[] backlog = d.getBacklog();
	for (int i=0; i<backlog.length;i++) {
            Vapp.log(backlog[i].toXMLString());
	}
    }
}

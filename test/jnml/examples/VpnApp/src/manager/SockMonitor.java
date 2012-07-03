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
import java.io.*;
import java.lang.Thread;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.ChannelCondition;

import quagga.Tunnel;
import quagga.Ipsec;
import quagga.Defaults;

public class SockMonitor extends Thread {
    Vapp vapp;
    Db db;

    SockMonitor(Vapp vapp, Db db) {
	this.vapp = vapp; this.db = db;
    }

    public void run() {
	quagga.System s;
	try {
	    while(true) {
		Thread.sleep(3000);
		synchronized (vapp) {
		    for (int i=0; i<db.devices.size(); i++) {
			VDevice d = (VDevice)db.devices.get(i);
			if (d.getSession("cfg") == null || 
                            d.getSession("cfg").getTransport() == null) {
			    
			    if (d.createSession(vapp.vpnFrame.getNetworkCanvas())) {
				vapp.openedSocket(d);
			    }
			}
			else if (d.getSession("cfg").getTransport() != null) {
			    Session session = ((SSHSession)d.getSession("cfg").getTransport()).getSession();
			    int conditionSet = 
                                ChannelCondition.TIMEOUT &
				ChannelCondition.CLOSED &
				ChannelCondition.EOF;
			    conditionSet = session.waitForCondition(conditionSet, 1);
			    if (conditionSet != ChannelCondition.TIMEOUT) {
				vapp.closedSocket(d);
			    }
			}
		    }
		}
	    }
	}
	catch (Exception e) {
	    System.out.println("SockMonitor died " + e); 
	    e.printStackTrace();
	    vapp.sockMon = null;
	    return;
	}
    }
}









    

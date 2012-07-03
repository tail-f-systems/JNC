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

import com.tailf.conf.*;
import com.tailf.dp.*;

import java.util.ArrayList;
import java.io.*;
import java.net.*;

public class Notifier implements DpNotifReplayCallback {

    /**
     * Logs start time.     
     */
    public ConfDatetime logStartTime = new ConfDatetime();

    /**
     * The stream to send to
     */
    DpNotifStream stream;


    /**
     * Callback from DpNotifReplayCallback interface.
     */
    public ConfDatetime getLogStartTime(DpNotifStream stream) 
	throws DpCallbackException {
	return logStartTime;
	
    }

    /**
     * Callback from DpNotifReplayCallback interface.
     */
    public ConfDatetime getLogAgedTime(DpNotifStream stream) 
	throws DpCallbackException {
	return null;
    }

    
    /**
     * Inner class for a replay
     *
     */
    class Replay {
	ConfDatetime time;
	ConfXMLTag event;
	Replay(ConfDatetime time, ConfXMLTag event) {
	    this.event = event;
	    this.time = time;
	}
    }


    /**
     * The list of replays. A replay buffer.
     * This *is* the event log.
     * Data is put in from cmdReader thread.
     * Data is read from the DpNotifReplayCallback thread.
     *
     */
    ArrayList<Replay> replayBuffer = new ArrayList<Replay>();
    

    /**
     * Start replay .
     * Simple version, just send all events between
     * the specified times.
     */
    public void replay(DpNotifStream stream, ConfDatetime start, ConfDatetime stop) 
	throws DpCallbackException {
	try {
	    if (stop==null) stop = new ConfDatetime();
	    // trace("start= "+start);
	    // trace("stop= "+stop);
	    for (int i=0;i<replayBuffer.size();i++) {
		Replay r = (Replay) replayBuffer.get(i);
		if (r.time.after(start) && r.time.before(stop)) {
		    stream.send( r.time, r.event );
		} 
	    }  
	} catch (Exception e) {
	    throw new DpCallbackException("failed to send notification");
	}
    }

        
    /**
     *  Send a notification to ConfD
     */ 
    public void send_notification(ConfXMLTag vals)
	throws ConfException, IOException {
	ConfDatetime now = new ConfDatetime();
	replayBuffer.add( new Replay(now,vals) );
	stream.send( now, vals );	
    }
    


    /** ------------------------------------------------------------
     *   main
     *
     */
    static public void main(String args[]) {

		
	try {
	    /* create new control socket */
	    Socket ctrl_socket= new Socket("127.0.0.1",Conf.PORT);
	
	    /* init and connect the control socket */
	    Dp dp = new Dp("server_daemon",ctrl_socket);
	   
	    /* create DpNotifStream 
	     * the callback is this handled by the 'Notifier' class 
	     */	    
	    Notifier notif= new Notifier();
	    DpNotifStream stream = dp.createNotifStream("interface", notif );
	    notif.stream = stream;
	    dp.registerDone();

	    /*
	     * Read commands from stdin, and send events to our notifier
	     * In a separate thread.
	     */
	    new CmdReader(notif,args).start();
	    
	    /*
	     * In this thread continue to read commands from ConfD.
	     * (this may be a normal data provider as well)
	     */
	    while (true) dp.read();
	    
	} catch (Exception e) {
	    System.err.println("(closing) "+e.getMessage());
	}
    }

    static public void trace(String str) {
	System.err.println("*Notifier: "+str);
    }
    
}

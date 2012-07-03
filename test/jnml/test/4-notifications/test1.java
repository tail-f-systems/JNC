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

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import com.tailf.inm.*;

public class test1 {
    
    static public void main(String args[]) {
	System.err.println("---------------------------------------");
	System.err.println("Java NETCONF Notification test 1 ");
	System.err.println("---------------------------------------");
	
	try {
	    /* TCP */	
	    Transport tr = new TCPSession("127.0.0.1", 2023, "admin", "501",
					    "501", "", "/Users/ola", "");
	    NetconfSession sess= new NetconfSession( tr );
	    // test get all
	    // NodeSet got = sess.get();
	    // System.out.println("get: "+got.toXMLString());
	    NodeSet reply = sess.getStreams();	    
	    System.out.println("got streams:"+ reply.toXMLString());	
	    // get interface stream
	    Element ifstream= reply.first().get("self::netconf/streams/stream[name='interface']").first();
	    System.out.println("The interface stream: "+ifstream.toXMLString());
	    
	    // create subscription
	    System.out.println("Creating subscription");
	    sess.createSubscription("interface");
	    System.out.println("Receiving notification");
	    // receive linkUp
	    Element notif =  sess.receiveNotification();
	    System.out.println("Got: "+notif.toXMLString());	    	    
	    // receive linkDown
	    notif= sess.receiveNotification();
	    System.out.println("Got: "+notif.toXMLString());	    	    
	    // receive linkUp2
	    notif= sess.receiveNotification();
	    System.out.println("Got: "+notif.toXMLString());	    	    
	    // receive linkUp3
	    notif= sess.receiveNotification();
	    System.out.println("Got: "+notif.toXMLString());	    	    
	    sess.closeSession();     
	} catch (Exception e) {
		e.printStackTrace();
	    }
    }
}

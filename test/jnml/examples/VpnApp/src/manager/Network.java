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

public class Network implements Serializable {
    public String net;
    public int masksize;

    Network(String net, int masksize) {
	this.net = net;
	this.masksize = masksize;
    }
    
    
    public String toString() {
	return net + "/" + masksize;
    }
    
};


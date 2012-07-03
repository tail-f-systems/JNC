package manager;

import java.lang.Exception;
import java.io.*;
import com.tailf.confm.*;
import com.tailf.inm.*;

class Subscriber extends IOSubscriber {
    NetconfLog console;
    String devName;
    public static boolean rawMode = false;

    Subscriber(NetconfLog c, String n) {
	super(rawMode);
        console = c;
        devName = n;
    }

    public void input(String s) {
	console.receivedXML(s, devName, !Subscriber.rawMode);
    }

    public void output(String s) {
	console.sentXML(s, devName, !Subscriber.rawMode);
    }
}

    

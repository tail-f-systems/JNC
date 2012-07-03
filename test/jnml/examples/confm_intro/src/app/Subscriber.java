package simple;

import java.io.*;
import com.tailf.inm.*;
import com.tailf.confm.*;

class Subscriber extends IOSubscriber {
    String devName;
    public static boolean rawMode = false;

    Subscriber(String devName) {
	super(rawMode);
        this.devName = devName;
    }

    public void input(String s) {
        System.out.println("RECV " + devName);
        System.out.println(s);
    }

    public void output(String s) {
        System.out.println("SEND " + devName);
        System.out.println(s);
    }
}

    

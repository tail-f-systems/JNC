package app;

import java.io.IOException;
import java.util.ListIterator;

import com.tailf.confm.*;
import com.tailf.inm.*;

import gen.Mini;

public class Main {

    private Device dev;
    private DeviceUser duser;
    
    private void init() {
        String emsUserName = "bobby";
        duser = new DeviceUser(emsUserName, "admin", "admin");
        dev = new Device("mydev", duser, "localhost", 2022);

        try {
            dev.connect(emsUserName);
            dev.newSession("cfg");
        } catch (IOException e0) {
            System.err.println("Can't connect");
            System.exit(1);
        } catch (INMException e1) {
            System.err.println("Can't authenticate" + e1);
            System.exit(1);
        }
    }
    
    private static void printNodeSet(NodeSet nset) {
        ListIterator<Element> iter = nset.listIterator();
        while (iter.hasNext()) {
            Element e = iter.next();
            System.out.println(e);
            System.out.println(e.getValue());
            if (e.hasChildren()) {
                printNodeSet(e.getChildren());
            }
        }
    }
    
    private Element getConfig(Device d) throws IOException, INMException{
        Mini.enable();
        ConfDSession session = d.getSession("cfg");
        NodeSet reply = session.getConfig(NetconfSession.RUNNING);
        // printNodeSet(reply);
        Element h = (Element)reply.first();
        return h;
    }
    
    public void getConfig() throws IOException,INMException{
        Mini.enable();
        System.out.println(getConfig(dev).toXMLString());
    }
	
	public int run() {
		try {
			Mini.enable();
		} catch (INMException e) {
			System.err.println("Schema file not found.");
			return -1;
		}
		return 0;
	}

	/**
	 * @param args Ignored
	 * @throws INMException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, INMException {
		Main main = new Main();
		System.out.println(main.run());
		main.init();
		main.getConfig();
	}

}

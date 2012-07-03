import com.tailf.nml.*;
import com.tailf.aware.*;
import hosts.*;

public class SimpleGet {
    static public void main(String args[]) {
        try {
            Transport ssh =
                new SSHTransport("127.0.0.1", 2022, "admin", "admin");
	    Hosts.enable();
	    quagga.System.enable();
	    AwareXMLParser parser = new AwareXMLParser();
            NetconfSession session = new NetconfSession(ssh, parser);
            Element filter;
            NodeSet reply;
	    filter =
		Element.create("http://acme.example.com/simple/1.0", "/hosts");
	    reply = session.getConfig(NetconfSession.RUNNING, filter);
            System.out.println(reply.toXMLString());
	    filter =
		Element.create("http://tail-f.com/ns/example/quagga/1.0",
			       "/system");
	    reply = session.getConfig(NetconfSession.RUNNING, filter);
            System.out.println(reply.toXMLString());
            session.closeSession();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

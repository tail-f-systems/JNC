import com.tailf.inm.*;

public class GetConfig {
    static public void main(String args[]) {
        try {
            SSHConnection c = new SSHConnection("localhost", 2022);
            c.authenticateWithPassword("admin", "admin");
            SSHSession s = new SSHSession(c);
	    //s.addSubscriber(new DefaultIOSubscriber("get"));
            NetconfSession session = new NetconfSession(s);
            Element subtreeFilter =
                Element.create("http://acme.com/ns/simple/1.0", "/hosts");
            NodeSet reply =
		session.getConfig(NetconfSession.RUNNING, subtreeFilter);
            System.out.println(reply.toXMLString());
            session.closeSession();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

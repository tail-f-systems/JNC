import com.tailf.inm.*;
import foo_cae.Ss7;
import foo_cae.Foo_cae;

public class GetConfig {
    static public void main(String args[]) {
        try {
            
            SSHConnection c = new SSHConnection("localhost", 2022);
            c.authenticateWithPassword("admin", "admin");
            SSHSession s = new SSHSession(c);
	    //s.addSubscriber(new DefaultIOSubscriber("get"));
            NetconfSession session =
                new NetconfSession(s, new com.tailf.confm.XMLParser());
            Foo_cae.enable();
            Element subtreeFilter =
                Element.create("http://foo.net/ns/foo-cae", "/ss7");
            NodeSet reply =
                session.getConfig(NetconfSession.RUNNING, subtreeFilter);
            System.out.println(reply.toXMLString());
            Ss7 ss7 = (Ss7)reply.first();
            session.closeSession();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

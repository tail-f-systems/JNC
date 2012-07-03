import com.tailf.inm.*;

public class RevertConfig {
    static public void main(String args[]) {
        try {
            SSHConnection c = new SSHConnection("localhost", 2022);
            c.authenticateWithPassword("admin", "admin");
            SSHSession s = new SSHSession(c);
	    //s.addSubscriber(new DefaultIOSubscriber("update"));
            NetconfSession session = new NetconfSession(s);
            // Create subtree filter 
            Element hosts =
                Element.create("http://acme.com/ns/simple/1.0", "/hosts");
            // Reinsert a new host 'joe' instance. If this host already
            // exists replace it. If it doesn't exist create it.
            Element joe =
                hosts.createPath("host[name='joe', numberOfServers='5']");
            joe.markReplace();
            System.out.println("Replacement config:\n"+ hosts.toXMLString());
            // Send the change back to the device
            session.editConfig(hosts);
            // Inspect the updated RUNNING configuration
            Element subtreeFilter =
                Element.create("http://acme.com/ns/simple/1.0", "/hosts");
            NodeSet reply =
                session.getConfig(NetconfSession.RUNNING, subtreeFilter);
            System.out.println("Resulting config:\n"+reply.toXMLString());
            session.closeSession();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

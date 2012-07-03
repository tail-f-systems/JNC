import com.tailf.inm.*;

public class UpdateConfig {
    static public void main(String args[]) {
        try {
            SSHConnection c = new SSHConnection("localhost", 2022);
            c.authenticateWithPassword("admin", "admin");
            SSHSession s = new SSHSession(c);
	    //s.addSubscriber(new DefaultIOSubscriber("update"));
            NetconfSession session = new NetconfSession(s);
            // Create subtree filter 
            Element subtreeFilter =
                Element.create("http://acme.com/ns/simple/1.0",
                                "/hosts/host[numberOfServers='5']");
            // Extract configuration from the RUNNING datastore
            NodeSet reply =
                session.getConfig(NetconfSession.RUNNING, subtreeFilter);
            if (reply.size() == 0) {
                System.out.println("/hosts/host[name='joe'] already deleted!");
                System.exit(255);
            }
            System.out.println("Current config:\n"+reply.toXMLString());
            Element hosts = reply.first();
            // Mark the host 'joe' for deletion
            hosts.markDelete("host[name='joe']");
            // Send the change back to the device
            session.editConfig(hosts);
            // Inspect the updated RUNNING configuration
            subtreeFilter =
                Element.create("http://acme.com/ns/simple/1.0", "/hosts");
            reply = session.getConfig(NetconfSession.RUNNING, subtreeFilter);
            System.out.println("Resulting config:\n"+reply.toXMLString());
            session.closeSession();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

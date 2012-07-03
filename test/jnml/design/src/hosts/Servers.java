package hosts;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class Servers extends Container {
    public Servers() {
	super(Hosts.NAMESPACE, "servers");
    }
    
    public Servers clone() {
	return (Servers)clone(new Servers());
    }
    
    /* List-container: "server" */
    
    public Server getServer(IpType ip, PortType port) throws NMLException {	
	String path = "server[ip='"+ip+"'][port='"+port+"']";
	return (Server)getListContainer(path);
    }

    public ElementChildrenIterator serverIterator() {	
	return new ElementChildrenIterator(children, "server");
    }
    
    public void addServer(Server server) throws NMLException {
	insertChild(server, (ArrayList)Hosts.schema.get("servers"));
    }
    
    public Server addServer(IpType ip, PortType port) throws NMLException {
	Server server = new Server(ip, port);
	insertChild(server, (ArrayList)Hosts.schema.get("servers"));
	return server;
    }

    public Server addServer() throws NMLException {
	Server server = new Server();
	insertChild(server, (ArrayList)Hosts.schema.get("servers"));
	return server;
    }
    
    public void deleteServer(IpType ip, PortType port) throws NMLException {
	String path = "server[ip='"+ip+"'][port='"+port+"']";
	delete(path);
    }
}

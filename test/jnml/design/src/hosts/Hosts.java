package hosts;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class Hosts extends Container {
    public static final String NAMESPACE = "http://acme.example.com/simple/1.0";
    public static final String PREFIX = "hosts";
    public static Schema schema = new Schema(new String[][] {
	    {"hosts", "host"},
	    {"host", "name", "enabled", "servers"},
	    {"servers", "server"},
	    {"server", "ip", "port", "id", "qos"}
	});
    
    public Hosts() {
	super(NAMESPACE, "hosts");
	setPrefix(new Prefix(PREFIX, NAMESPACE));
    }
    
    public Hosts clone() {
	return (Hosts)clone(new Hosts());
    }

    public static void enable() {
	// The Namespace will use the java package PREFIX
	Container.setPackage(NAMESPACE, PREFIX);
    }
    
    /* List-container: "host" */
    
    public Host getHost(String name) throws NMLException {	
	String path = "host[name='"+name+"']";
	return (Host)getListContainer(path);
    }
    
    public ElementChildrenIterator hostIterator() {	
	return new ElementChildrenIterator(children, "host");
    }
    
    public void addHost(Host host) throws NMLException {
	insertChild(host, (ArrayList)Hosts.schema.get("hosts"));
    }
    
    public Host addHost(String name) throws NMLException {
	Host host = new Host(name); 
	insertChild(host, (ArrayList)Hosts.schema.get("hosts"));
	return host;
    }
    
    public Host addHost() throws NMLException {
	Host host = new Host(); 
	insertChild(host, (ArrayList)Hosts.schema.get("hosts"));
	return host;
    }
    
    public void deleteHost(String name) throws NMLException {
	String path = "host[name='"+name+"']";
	delete(path);
    }
}

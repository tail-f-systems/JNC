package quagga;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class Ipsec extends Container {
    public Ipsec() {
	super(System.NAMESPACE, "ipsec");
    }
    
    public Ipsec clone() {
	return (Ipsec)clone(new Ipsec());
    }
    
    /* Container: "ipsec" */
    
    public Defaults defaults = null;
    
    public void addDefaults(Defaults defaults) throws NMLException {
	this.defaults = defaults;
	insertChild(defaults, (ArrayList)System.schema.get("ipsec"));
    }
    
    public Defaults addDefaults() throws NMLException {
	Defaults defaults = new Defaults(); 
	this.defaults = defaults;
	insertChild(defaults, (ArrayList)System.schema.get("ipsec"));
	return defaults;
    }
    
    public void deleteDefaults() throws NMLException {
	this.defaults = null;
	String path = "defaults";
	delete(path);
    }
        
    /* List-container: "tunnel" */
    
    public Tunnel getTunnel(String name) throws NMLException {	
	String path = "tunnel[name='"+name+"']";
	return (Tunnel)getListContainer(path);
    }

    public ElementChildrenIterator tunnelIterator() {	
	return new ElementChildrenIterator(children, "tunnel");
    }
    
    public void addTunnel(Tunnel tunnel) throws NMLException {
	insertChild(tunnel, (ArrayList)System.schema.get("ipsec"));
    }
    
    public Tunnel addTunnel(String name) throws NMLException {
	Tunnel tunnel = new Tunnel(name); 
	insertChild(tunnel, (ArrayList)System.schema.get("ipsec"));
	return tunnel;
    }
    
    public Tunnel addTunnel() throws NMLException {
	Tunnel tunnel = new Tunnel(); 
	insertChild(tunnel, (ArrayList)System.schema.get("ipsec"));
	return tunnel;
    }
    
    public void deleteTunnel(String name) throws NMLException {
	String path = "tunnel[name='"+name+"']";
	delete(path);
    }
}

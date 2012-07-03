package hosts;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class Host extends Container {
    public Host() {
	super(Hosts.NAMESPACE, "host");
    }
    
    public Host(String nameValue) throws NMLException {
	super(Hosts.NAMESPACE, "host");
	// Add key element
	Leaf name = new Leaf(Hosts.NAMESPACE, "name");
	name.setValue(nameValue);
	insertChild(name, (ArrayList)Hosts.schema.get("host"));
    }
    
    public Host clone() {
	try {
	    return (Host)clone(new Host(getNameValue()));
	} catch (NMLException e) {
	    return null;
	}
    }
    
    /* Container: "servers" */
    
    public Servers servers = null;
    
    public void addServers(Servers servers) throws NMLException {
	this.servers = servers;
	insertChild(servers, (ArrayList)Hosts.schema.get("host"));
    }
    
    public Servers addServers() throws NMLException {
	Servers servers = new Servers(); 
	this.servers = servers;
	insertChild(servers, (ArrayList)Hosts.schema.get("host"));
	return servers;
    }
    
    public void deleteServers() throws NMLException {
	this.servers = null;
	String path = "servers";
	delete(path);
    }    
    
    /* Leaf: "name" (key) */
    
    public String getNameValue() throws NMLException {
	return (String)getValue("name");
    }
    
    public void setNameValue(String nameValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "name", nameValue,
		     (ArrayList)Hosts.schema.get("host"));
    }
    
    /* Leaf: "enabled" [mandatory] */
    
    public Boolean getEnabledValue() throws NMLException {
	return (Boolean)getValue("enabled");
    }

    public void setEnabledValue(Boolean enabledValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "enabled", enabledValue,
		     (ArrayList)Hosts.schema.get("host"));
    }
    
    public void setEnabledValue(String enabledValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "enabled", new Boolean(enabledValue),
		     		     (ArrayList)Hosts.schema.get("host"));
    }
    
    public void unsetEnabledValue() throws NMLException {
	delete("enabled");
    }
    
    public void markEnabledReplace() throws NMLException {
	markLeafReplace("enabled");
    }
    
    public void markEnabledMerge() throws NMLException {
	markLeafMerge("enabled");
    }
    
    public void markEnabledCreate() throws NMLException {
	markLeafCreate("enabled");
    }
}

package hosts;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class Server extends Container {
    public Server() throws NMLException {
	super(Hosts.NAMESPACE, "server");
    }
    
    public Server(IpType ipValue, PortType portValue) throws NMLException {
	super(Hosts.NAMESPACE, "server");
	// Add key element
	Leaf ip = new Leaf(Hosts.NAMESPACE, "ip");
	ip.setValue(ipValue);
	insertChild(ip, (ArrayList)Hosts.schema.get("server"));
	// Add key element
	Leaf port = new Leaf(Hosts.NAMESPACE, "port");
	port.setValue(portValue);
	insertChild(port, (ArrayList)Hosts.schema.get("server"));
    }
    
    public Server clone() {
	try {
	    return (Server)clone(new Server(getIpValue(), getPortValue()));
	} catch (NMLException e) {
	    return null;
	}
    }
    
    /* Leaf: "ip" [key] */
    
    public IpType getIpValue() throws NMLException {
	return (IpType)getValue("ip");
    }
    
    public void setIpValue(IpType ipValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "ip", ipValue,
		     (ArrayList)Hosts.schema.get("server"));
    }
    
    public void setIpValue(String ipValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "ip", new IpType(ipValue),
		     (ArrayList)Hosts.schema.get("server"));
    }
    
    /* Leaf: "port" [key] */
    
    public PortType getPortValue() throws NMLException {
	return (PortType)getValue("port");
    }

    public void setPortValue(PortType portValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "port", portValue,
		     (ArrayList)Hosts.schema.get("server"));
    }

    public void setPortValue(String portValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "port", new PortType(portValue),
		     (ArrayList)Hosts.schema.get("server"));
    }
    
    /* Leaf: "id" [mandatory] */
    
    public String getIdValue() throws NMLException {
	return (String)getValue("id");
    }
    
    public void setIdValue(String idValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "id", idValue,
		     (ArrayList)Hosts.schema.get("server"));
    }
    
    public void unsetIdValue() throws NMLException {
	delete("id");
    }
    
    public void markIdReplace() throws NMLException {
	markLeafReplace("id");
    }
    
    public void markIdMerge() throws NMLException {
	markLeafMerge("id");
    }
    
    public void markIdCreate() throws NMLException {
	markLeafCreate("id");
    }
    
    /* Leaf: "qos" [optional (has default value)] */
    
    public Uint16 getQosValue() throws NMLException {
	Uint16 value = (Uint16)getValue("qos");
	return value == null ? new Uint16(15192) : value;
    }
    
    public boolean isQosDefault() throws NMLException {
	return isLeafDefault("qos");
    }

    public void setQosValue(Uint16 qosValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "qos", qosValue,
		     (ArrayList)Hosts.schema.get("server"));
    }
    
    public void setQosValue(String qosValue) throws NMLException {
	setLeafValue(Hosts.NAMESPACE, "qos", new Uint16(qosValue),
		     (ArrayList)Hosts.schema.get("server"));
    }
    
    public void unsetQosValue() throws NMLException {
	delete("qos");
    }
    
    public void markQosReplace() throws NMLException {
	markLeafReplace("qos");
    }
    
    public void markQosMerge() throws NMLException {
	markLeafMerge("qos");
    }
    
    public void markQosCreate() throws NMLException {
	markLeafCreate("qos");
    }
}

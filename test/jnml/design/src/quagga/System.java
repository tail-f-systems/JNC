package quagga;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class System extends Container {
    public static final String NAMESPACE =
	"http://tail-f.com/ns/example/quagga/1.0";
    public static final String PREFIX = "quagga";
    
    public static Schema schema = new Schema(new String[][] {
	    {"system", "saEstablished", "saExpired", "noPolicy", "vpn"},
	    {"saEstablished", "tunnelName", "spi"},
	    {"saExpired", "tunnelName", "spi"},
	    {"noPolicy", "local-net", "local-net-mask", "remote-endpoint",
	     "remote-net", "remote-net-mask", "direction"},
	    {"vpn", "ipsec"},
	    {"ipsec", "defaults", "tunnel"},
	    {"defaults", "encryption-algo", "hash-algo"},
	    {"tunnel", "name", "local-endpoint", "local-net", "local-net-mask",
	     "remote-endpoint", "remote-net", "pre-shared-key",
	     "encryption-algo", "hash-algo"}
	});
    
    public System() {
	super(NAMESPACE, "system");
	setPrefix(new Prefix(PREFIX, NAMESPACE));
    }
    
    public System clone() {
	return (System)clone(new System());
    }
    
    public static void enable() {
	Container.setPackage(NAMESPACE, PREFIX);
    }

    /* Container: "vpn" */
    
    public Vpn vpn = null;
    
    public void addVpn(Vpn vpn) throws NMLException {
	this.vpn = vpn;
	insertChild(vpn, (ArrayList)System.schema.get("system"));
    }
    
    public Vpn addVpn() throws NMLException {
	Vpn vpn = new Vpn(); 
	this.vpn = vpn;
	insertChild(vpn, (ArrayList)System.schema.get("system"));
	return vpn;
    }
    
    public void deleteVpn() throws NMLException {
	this.vpn = null;
	String path = "vpn";
	delete(path);
    }        
}

package quagga;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.inm.*;

public class Vpn extends Container {
    public Vpn() {
	super(System.NAMESPACE, "vpn");
    }
    
    public Vpn clone() {
	return (Vpn)clone(new Vpn());
    }
    
    /* Container: "ipsec" */
    
    public Ipsec ipsec = null;
    
    public void addIpsec(Ipsec ipsec) throws NMLException {
	this.ipsec = ipsec;
	insertChild(ipsec, (ArrayList)System.schema.get("vpn"));
    }
    
    public Ipsec addIpsec() throws NMLException {
	Ipsec ipsec = new Ipsec(); 
	this.ipsec = ipsec;
	insertChild(ipsec, (ArrayList)System.schema.get("vpn"));
	return ipsec;
    }
    
    public void deleteIpsec() throws NMLException {
	this.ipsec = null;
	String path = "ipsec";
	delete(path);
    }        
}

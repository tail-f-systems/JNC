import quagga.*;
import java.util.*;
import com.tailf.nml.*;
import com.tailf.aware.*;

public class QuaggaTest {
    static public void main(String args[]) {
        try {
	    // There is a conflict between java.lang.System and quagga.System
            quagga.System system = new quagga.System();
            Vpn vpn = system.addVpn();
            Ipsec ipsec = vpn.addIpsec();

            Defaults defaults = ipsec.addDefaults();
	    assert defaults.isHashAlgoDefault() == true;
	    assert defaults.getHashAlgoValue().toString().equals("md5");

	    Tunnel tunnel1 = ipsec.addTunnel("xev1");
	    tunnel1.setLocalNetValue("1.2.3.4");
	    tunnel1.markLocalNetReplace();

	    Tunnel tunnel2 = ipsec.addTunnel("xev2");

	    Iterator iterator = ipsec.tunnelIterator();
	    while (iterator.hasNext())
		java.lang.System.out.println(
	            "Tunnel: "+((Tunnel)iterator.next()).getNameValue());

	    java.lang.System.out.println(system.toXMLString());
        } catch (Exception e) {
            java.lang.System.out.println(e);
        }
    }
}

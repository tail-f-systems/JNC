import java.util.*;
import hosts.*;
import com.tailf.nml.*;
import com.tailf.aware.*;

public class HostsTest {
    static public void main(String args[]) {
        try {
            Hosts hosts = new Hosts();
            Host host = hosts.addHost("xev");
            assert hosts.getHost("xev").getNameValue() == "xev";

	    host.addServers();
	    
            Server server1 =
                host.servers.addServer(new IpType("1.2.3.1"),
                                       new PortType(1031));
            server1.markReplace();

            Server server2 =
                host.servers.addServer(new IpType("1.2.3.2"),
                                       new PortType(1032));
            server2.markCreate();

            Server server3 =
                host.servers.addServer(new IpType("1.2.3.3"),
                                       new PortType(1033));
            server3.markMerge();

            Server server =
                host.servers.getServer(new IpType("1.2.3.3"),
                                       new PortType(1033));
            assert server.getPortValue().intValue() == 1033;

	    Iterator iterator = host.servers.serverIterator();
	    while (iterator.hasNext()) {
		Server aServer = (Server)iterator.next();
		System.out.println("Server: "+aServer.getIpValue()+":"+
				   aServer.getPortValue());
	    }

            assert server3.getQosValue().intValue() == 15192;
            assert server3.isQosDefault() == true;
            
            server3.setQosValue(new Uint16(15192));
            assert server3.getQosValue().intValue() == 15192;
            assert server3.isQosDefault() == false;
            
            server3.setQosValue(new Uint16(43));
            assert server3.getQosValue().intValue() == 43;
            assert server3.isQosDefault() == false;
            
	    server3.unsetQosValue();
	    assert server3.getQosValue().intValue() == 15192;
            assert server3.isQosDefault() == true;
            	    
            host.setEnabledValue(true);
            assert host.getEnabledValue() == true;
            host.markEnabledMerge();

            NodeSet nodes =
                hosts.get("host[name='xev']/servers/server[port='1033']");
            assert nodes.size() == 1;
            System.out.println(nodes.first().toXMLString());

            System.out.println(hosts.toXMLString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

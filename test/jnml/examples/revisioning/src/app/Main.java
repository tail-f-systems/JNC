package simple;

import java.io.*;
import com.tailf.confm.*;
import com.tailf.inm.*;
import simple.*;

public class Main {
    final static int NETCONF_SSH_V1_PORT = 2022;
    final static int NETCONF_SSH_V2_PORT = 2023;
    final static int NETCONF_SSH_V3_PORT = 2024;

    static public void main(String args[]) {
        try {
            Simple.enable();
            Simple.registerSchema();

            /*
             * Setup sessions with the v1, v2 and v3 ConfD daemons
             */

//M3_BEGIN connect
            SSHConnection v1Connection =
                new SSHConnection("localhost", NETCONF_SSH_V1_PORT);
            v1Connection.authenticateWithPassword("admin", "secret");
            SSHSession v1Transport = new SSHSession(v1Connection);		    
            NetconfSession v1Session =
                new NetconfSession(v1Transport,
                                   new com.tailf.confm.XMLParser());
//M3_END connect            

            SSHConnection v2Connection =
                new SSHConnection("localhost", NETCONF_SSH_V2_PORT);
            v2Connection.authenticateWithPassword("admin", "secret");
            SSHSession v2Transport = new SSHSession(v2Connection);		    
            NetconfSession v2Session =
                new NetconfSession(v2Transport,
                                   new com.tailf.confm.XMLParser());

            SSHConnection v3Connection =
                new SSHConnection("localhost", NETCONF_SSH_V3_PORT);
            v3Connection.authenticateWithPassword("admin", "secret");
            SSHSession v3Transport = new SSHSession(v3Connection);		    
            NetconfSession v3Session =
                new NetconfSession(v3Transport,
                                   new com.tailf.confm.XMLParser());
            
            /*
             * Inspect the v1 and v2 ConfD daemon configurations
             */

//M3_BEGIN inspect
            Element configSubtreeFilter = new Config();
            NodeSet v1Config = v1Session.getConfig(configSubtreeFilter);
            NodeSet v2Config = v2Session.getConfig(configSubtreeFilter);
            System.out.println("** v1 configuration:\n"+
                               v1Config.toXMLString());
            System.out.println("** v2 configuration:\n"+
                               v2Config.toXMLString());
//M3_END inspect
            
            /*
             * Set the 'mode' leaf value to an enumeration value which
             * is valid for both v1 and v2, i.e. 'off'. 
             */

//M3_BEGIN set_mode1
            Config config = new Config();
            config.setModeValue("off");
            v1Session.editConfig(config);
            v2Session.editConfig(config);
            v1Config = v1Session.getConfig(configSubtreeFilter);
            v2Config = v2Session.getConfig(configSubtreeFilter);
            ModeType v1ModeValue =
                ((Config)v1Config.first()).getModeValue();
            assert v1ModeValue.equals("off");
            ModeType v2ModeValue =
                ((Config)v2Config.first()).getModeValue();
            assert v2ModeValue.equals("off");
//M3_END set_mode1
            
            /*
             * Try to set the 'mode' leaf value to an enumeration
             * value which is unique for v2, i.e. 'speed-stepping'.
             * This will obviously work for v2, but will fail
             * according to v1.
             */

//M3_BEGIN set_mode2
            config = new Config();
            config.setModeValue("speed-stepping");
            v2Session.editConfig(config);
            try {
                v1Session.editConfig(config);
                assert false;
            } catch (INMException e) {
                assert e.errorCode == INMException.RPC_REPLY_ERROR; 
            }
//M3_END set_mode2          

            /*
             * Set the 'host-metrics' leaf value to an unsigned
             * integer value which is valid for both v1 and v2,
             * i.e. 32.
             */
            
            config = new Config();
            config.setHostMetricsValue(32);
            v1Session.editConfig(config);
            v2Session.editConfig(config);
            v1Config = v1Session.getConfig(configSubtreeFilter);
            v2Config = v2Session.getConfig(configSubtreeFilter);
            com.tailf.confm.xs.UnsignedInt v1HostMetricsValue =
                ((Config)v1Config.first()).getHostMetricsValue();
            assert v1HostMetricsValue.equals(32);
            com.tailf.confm.xs.UnsignedInt v2HostMetricsValue =
                ((Config)v2Config.first()).getHostMetricsValue();
            assert v2HostMetricsValue.equals(32);
            
            /*
             * Try to set the 'host-metrics' leaf value to an unsigned
             * integer which onlyis valid for v2, i.e. 111. This will
             * obviously work for v2, but will fail for according
             * v1.
             */

            config = new Config();
            config.setHostMetricsValue(111);
            v2Session.editConfig(config);
            try {
                v1Session.editConfig(config);
                assert false;
            } catch (INMException e) {
                assert e.errorCode == INMException.RPC_REPLY_ERROR; 
            }
            
            /*
             * Try to set the /config/login/message leaf value which
             * is unique for v2. This will obviously work for v2, but
             * will fail according to v1. See more on how to avoid
             * this below.
             */
            
//M3_BEGIN set_message1
            config = new Config();
            config.addLogin().setMessageValue("Make me rich and famous");
            v2Session.editConfig(config);
            try {
                v1Session.editConfig(config);
                assert false;
            } catch (INMException e) {
                assert e.errorCode == INMException.RPC_REPLY_ERROR; 
            }
//M3_END set_message1
            
            /*
             * Enable support for older revisions and try to set
             * /config/login/message in v1 again. With this support
             * enabled no exceptions are thrown if any unknown
             * containers or leafs are accessed, i.e. ConfM just
             * silently ignores the operation.
             */
            
//M3_BEGIN set_message2
            RevisionInfo.enableOlderRevisionSupport();            
            config = new Config();
            config.addLogin().setMessageValue("Make me rich and famous");
            v1Session.editConfig(config);
//M3_END set_message2

            /*
             * Enable support for newer revisions and try to get the
             * v3 configuration even though the Java code base does
             * not know anything about newly added leaf
             * /config/login/monthly-message in v3. With this support
             * enabled unknown containers and leafs are created as
             * generic INM Element objects instead of data model aware 
             * ConfM objects (which do not exist for v3 which is an
             * unknown revision).
             */

//M3_BEGIN set_monthly
            RevisionInfo.enableNewerRevisionSupport();
            NodeSet v3Config = v3Session.getConfig(configSubtreeFilter);
            Config currentConfig = (Config)v3Config.first();
            Element monthlyMessage =
                currentConfig.login.getChild("monthly-message");
            assert monthlyMessage.getClass().getName().equals("Element");
            System.out.println("** v3 configuration:\n"+
                               v3Config.toXMLString());
//M3_END set_monthly
            
            /*
             * Close sessions to v1, v2 and v3 ConfD daemons
             */
            v1Session.closeSession();
            v2Session.closeSession();
            v3Session.closeSession();

            System.out.println("ALLTESTOK");

        } catch (Exception e) {
            System.out.println("Unexpected: "+e);
            e.printStackTrace();
        }
    }
}

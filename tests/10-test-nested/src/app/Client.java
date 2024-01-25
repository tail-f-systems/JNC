package app;

import java.io.IOException;

import nesting.Nesting;
import nesting.Nested;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;

public class Client {

    private Device dev;

    public Client() {
        this.init();
    }

    private void init() {
        String emsUserName = "bobby";
        String host = "localhost";
        DeviceUser duser = new DeviceUser(emsUserName, "admin", "admin");
        dev = new Device("mydev", duser, host, 2022);

        try {
            dev.connect(emsUserName, 2000, false);
            dev.newSession("cfg");
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        } catch (JNCException e) {
            System.err.println("Can't authenticate " + e);
            System.exit(1);
        }
    }

    public void editConfig(final NodeSet config) throws IOException, JNCException {
        editConfig(dev, config);
    }

    private void editConfig(final Device dev, final NodeSet config) throws IOException,
            JNCException {
        dev.getSession("cfg").editConfig(config);
    }

    public NodeSet getConfig(final String xpath) throws IOException, JNCException {
        NetconfSession session = dev.getSession("cfg");
        return session.getConfig(NetconfSession.RUNNING, xpath);
    }

    public static void main(final String[] args) throws IOException, JNCException {
        Client client = new Client();
        Nesting.enable();
        NodeSet configs = client.getConfig("/nested");
        assert configs.size() == 0;

        Nested inst1 = new Nested("k1"),
            inst2 = new Nested("k2");
        inst1.addNested().setLValue("l1");
        inst2.addNested().setLValue("l2");
        configs.add(inst1);
        configs.add(inst2);
        client.editConfig(configs);

        NodeSet configs2 = client.getConfig("/nested");
        assert configs2.size() == 2;
        assert configs2.get(0) instanceof Nested;
        assert configs2.get(1) instanceof Nested;
        assert ((Nested)configs2.get(0)).nested.getLValue().equals("l1");
        assert ((Nested)configs2.get(1)).nested.getLValue().equals("l2");
        configs2.get(0).markDelete();
        configs2.get(1).markDelete();
        client.editConfig(configs);
        client.dev.close();
    }
}

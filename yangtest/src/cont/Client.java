package cont;

import java.io.IOException;

import com.tailf.confm.*;
import com.tailf.inm.*;

public class Client {

    private Device dev;
    private Device devRef[] = {null};
    private DeviceUser duserRef[] = {null};
    
    public Client() {
        util.Client.init(devRef, duserRef);
        dev = devRef[0];
    }

    public NodeSet getConfig() throws IOException, INMException {
        return util.Client.getConfig(dev);
    }

    public NodeSet editConfig(Element config) throws IOException, INMException {
        return util.Client.editConfig(dev, config);
    }
    
    public void enableCont() throws INMException {
        Cont.enable();
    }

    public static void main(String[] args) throws IOException, INMException {
        Client client = new Client();
        assert (client.dev != null): "Client assertion failed: dev != null";
    }
    
}

package cont;

import java.io.IOException;

import com.tailf.confm.*;
import com.tailf.inm.*;

public class Client {
    
    private Device devRef[] = {null};
    private Device dev;
    private DeviceUser duserRef[] = {null};
    private DeviceUser duser;
    
    public Client() {
        this.init();
    }
    
    private void init() {
        util.Client.init(devRef, duserRef);
        dev = devRef[0];
        duser = duserRef[0];
    }
    
    public NodeSet getConfig() throws IOException,INMException{
        return util.Client.getConfig(dev);
    }
    
    public NodeSet editConfig(Element config) throws IOException,INMException {
        return util.Client.editConfig(dev, config);
    }
    
    public static void main(String[] args) throws IOException, INMException {
        Client client = new Client();
        assert (client.dev != null): "Client: dev = null";
    }
    
}

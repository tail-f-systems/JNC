package cont;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tailf.inm.INMException;
import com.tailf.inm.NodeSet;

public class ClientTest {
    
    private Client client;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        util.Environment env = new util.Environment();
        boolean success = util.ConfD.startConfD("src/cont", env);
        assertTrue("Failed to launch ConfD.\nDid you set the CONFD_DIR env " +
        		"variable correctly?", success);
    }
    
    @Before
    public void setUp() {
        client = new Client();
    }

    @Test
    public void testMain() throws IOException, INMException {
        Client.main(null);
    }

    @Test
    public void testGetConfig() throws IOException, INMException {
        NodeSet config = client.getConfig();
        String xml = config.toXMLString();
        assertFalse("XML empty", xml.isEmpty());
        assertTrue("More than one configuration", config.size() > 1);
    }

}

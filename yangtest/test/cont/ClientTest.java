package cont;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tailf.inm.Element;
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

    @Test
    public void testEditConfig() throws IOException, INMException {
        NodeSet config = client.getConfig();
        Element c = null;
        Iterator iter = config.iterator();
        while(iter.hasNext()) {
            c = (Element)iter.next();
            if (c.name == "c") {
                break;
            }
        }
        Element child = new Element(c.namespace, "s");
        child.setValue("test");
        c.insertChild(child);
        NodeSet newConfig = client.editConfig(c);
        Iterator iter1 = newConfig.iterator(), iter2 = config.iterator();
        while(iter1.hasNext()) {
            Element elem1 = (Element)iter1.next();
            Element elem2 = (Element)iter2.next();
            assertTrue("Roughly equal", elem1.compare(elem2) >= 0);
            assertTrue("Exactly equal", elem1.compare(elem2) == 0);
            if (elem1.name.compareTo("c") == 0) {
                Element c1 = elem1.getChild("s");
                String value = (String) c1.getValue();
                boolean isTest = value.compareTo("test") == 0;
                assertTrue("Value of s in newConfig is 'test'", isTest);
            }
            if (elem2.name.compareTo("c") == 0) {
                // TODO: [DRY] write function that does this check
                Element c2 = elem2.getChild("s");
                String value = (String) c2.getValue();
                boolean isTest = value.compareTo("test") == 0;
                assertFalse("Value of s in newConfig is not 'test'", isTest);
            }
        }
    }

}

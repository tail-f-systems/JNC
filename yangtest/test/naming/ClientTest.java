package naming;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tailf.inm.Element;
import com.tailf.inm.INMException;
import com.tailf.inm.NodeSet;
import com.tailf.inm.Path;

public class ClientTest {

    private Client client;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        util.TestUtils.startConfD("src/cont");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        util.TestUtils.makeStopClean("src/cont");
    }

    @Before
    public void setUp() {
        client = new Client();
    }

    @Test
    public void testEnableCont() throws IOException, INMException {
        client.enableCont();
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
        NodeSet oldConfig = client.getConfig();
        String assertMsg = "Initially, oldConfig has a single value of s";
        assertTrue(assertMsg, oldConfig.get("child::s").size() == 1);
        
        Element c = (Element) oldConfig.getFirst("self::c").clone();
        Element child = new Element(c.namespace, "s");
        assertTrue("Value of child is null", child.value == null);
        child.setValue("test");
        String chValue = (String) child.value;
        assertTrue("Value of child is 'test'", chValue.compareTo("test") == 0);
        
        Set<String> valueSetBefore = c.getValuesAsSet("child::s");
        assertFalse("Child not inserted", valueSetBefore.contains("test"));
        
        c.insertChild(child);
        
        Set<String> valueSetAfter = c.getValuesAsSet("child::s");
        assertTrue("Child inserted", valueSetAfter.contains("test"));
        assertTrue("Number of values increased by one",
                valueSetBefore.size() + 1 == valueSetAfter.size());
        
        NodeSet newConfig = client.editConfig(c);
        Iterator iter1 = newConfig.iterator(), iter2 = oldConfig.iterator();
        boolean sTest1 = false, sTest2 = false, sSeven1 = false, sSeven2 = false;
        while (iter1.hasNext()) {
            Element elem1 = (Element) iter1.next();
            Element elem2 = (Element) iter2.next();
            assertTrue("Same name, ns and value", elem1.compare(elem2) == 0);
            Set<String> set1 = elem1.getValuesAsSet("child::s");
            Set<String> set2 = elem2.getValuesAsSet("child::s");
            sTest1 |= set1.contains("test");
            sTest2 |= set2.contains("test");
            sSeven1 |= set1.contains("Seven");
            sSeven2 |= set2.contains("Seven");
            boolean sameState = iter1.hasNext() == iter2.hasNext();
            assertTrue("Iterators finish at same time", sameState);
        }
        assertTrue("Value of s in newConfig is 'test'", sTest1);
        assertFalse("Value of s in newConfig is not 'Seven'", sSeven1);
        assertFalse("Value of s in oldConfig is not 'test'", sTest2);
        assertTrue("Value of s in oldConfig is 'Seven'", sSeven2);
        assertMsg = "At termination, oldConfig also has a single value of s";
        assertTrue(assertMsg, oldConfig.get("child::s").size() == 1);
        assertMsg = "At termination, newConfig also has a single value of s";
        assertTrue(assertMsg, newConfig.get("child::s").size() == 1);
    }

}

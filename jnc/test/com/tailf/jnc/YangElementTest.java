package com.tailf.jnc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class YangElementTest {

    YangElement a1;
    YangElement a2;
    YangElement b1;
    YangElement b2;
    Leaf leaf1;
    Leaf leaf2;
    NodeSet uniqueA;
    NodeSet uniqueB;
    NodeSet changedA;
    NodeSet changedB;
    private final String ns = "http://test.com/ns/containertest/1.0";
    private final String prefix = "conttest";
    
    private void clearNodeSets() {
        uniqueA = new NodeSet();
        uniqueB = new NodeSet();
        changedA = new NodeSet();
        changedB = new NodeSet();
    }
    
    @Before
    public void setUp() {
        leaf1 = new Leaf(ns, "leaf");
        leaf2 = new Leaf(ns, "leaf");
        a1 = new DummyElement(ns, "a");
        b1 = new DummyElement(ns, "b");
        a2 = a1.cloneContent(new DummyElement(ns, "a"));
        b2 = b1.cloneContent(new DummyElement(ns, "b"));
        clearNodeSets();
        leaf1.value = "leaf";
        leaf2.value = "leaf";
        a1.addChild(leaf1);
        a2.addChild(leaf2);
        b2.addChild(a2);
        b1.addChild(a1);
        a1.setPrefix(prefix);
        b1.setPrefix(prefix);
        Attribute[] attrs = {new Attribute(ns, "presence", "Always present"),
                new Attribute(ns, "description", "For testing"),
                new Attribute(ns, "reference", "YangElementTest"),
        };
        for (int i=0; i<attrs.length; i++) {
            a1.addAttr(attrs[i]);
            b1.addAttr(attrs[i]);
        }
    }
    
    private boolean nodeSetsAreEmpty() {
        boolean noUniques = uniqueA.isEmpty() && uniqueB.isEmpty();
        boolean noChanges = changedA.isEmpty() && changedB.isEmpty();
        return noUniques && noChanges;
    }

    @Test
    public void testGetDiff() throws JNCException {
        // Sanity checks
        assertTrue("Initially, node sets are empty", nodeSetsAreEmpty());
        YangElement.getDiff(a1, a1, uniqueA, uniqueB, changedA, changedB);
        assertTrue("No diff between a and itself", nodeSetsAreEmpty());
        YangElement.getDiff(a1, a2, uniqueA, uniqueB, changedA, changedB);
        assertTrue("attributes do not matter for diff", nodeSetsAreEmpty());
        YangElement.getDiff(a1, b1, uniqueA, uniqueB, changedA, changedB);
        assertFalse("Different trees yield no diff", nodeSetsAreEmpty());
        clearNodeSets();
        assertTrue("The clearNodeSets method works", nodeSetsAreEmpty());
        
        // Remove child 'a' from b2 and confirm that it is then unique to b1
        b2.delete("child::a");
        YangElement.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertFalse("b1 has no unique children", uniqueA.isEmpty());
        assertEquals("a is a child of b1 only", a1, uniqueA.first());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        assertTrue("No changed leaves in A", changedA.isEmpty());
        assertTrue("No changed leaves in B", changedB.isEmpty());
        clearNodeSets();
        
        // Add it and confirm that the trees are now equal again
        b2.addChild(a2);
        YangElement.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());
        
        // Change value of leaf in b2, confirm "changed" status in b1 and b2
        b2.setValue("child::a/leaf", "");
        YangElement.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue("b1 has no unique children", uniqueA.isEmpty());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        assertFalse("No changed leaves in A", changedA.isEmpty());
        assertFalse("No changed leaves in B", changedB.isEmpty());
        assertEquals("leaf changed in A", leaf1, changedA.first());
        assertEquals("leaf changed in B", leaf2, changedB.first());
        clearNodeSets();
        
        // Restore and confirm that the trees are now equal again
        b2.setValue("child::a/leaf", "leaf");
        YangElement.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());

        // Add reference to same leaf and change both at once, confirm change
        b1.addChild(leaf2);
        b1.setValue("child::leaf", "leaf2");
        assertEquals("Value changed", "leaf2", b2.getValue("child::a/leaf"));
        
        // Confirm that leaf2 is 'unique' to b1 (because of structure diff)
        YangElement.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertFalse("b1 has no unique children", uniqueA.isEmpty());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        clearNodeSets();
        
        // Add same leaf to b2 and confirm that leaf1 and leaf2 are 'changed'
        b2.addChild(leaf2);
        YangElement.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue("b1 has no unique children", uniqueA.isEmpty());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        assertFalse("No changed leaves in A", changedA.isEmpty());
        assertFalse("No changed leaves in B", changedB.isEmpty());
        assertEquals("leaf changed in A", leaf1, changedA.first());
        assertEquals("leaf changed in B", leaf2, changedB.first());
        clearNodeSets();
        
        // Change value of leaf2 and confirm that trees are identical
        b2.setValue("child::leaf", "leaf");
        YangElement.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());
    }
    
    @Test
    public void shouldProduceValidXmlRegardlessOfLeafNamespace() throws JNCException {
    	
    	final Leaf leafWithNamespace = new Leaf("http://testNamespace", "leafWithNamespace");
    	leafWithNamespace.setValue("leafValue");
    	a1.addChild(leafWithNamespace);
    	
    	final YangXMLParser parser = new YangXMLParser();
    	 	
    	String expectedXml = new StringBuilder()
    		.append("<a xmlns=\"http://test.com/ns/containertest/1.0\" xmlns:conttest=\"http://test.com/ns/containertest/1.0\" presence=\"Always present\" description=\"For testing\" reference=\"YangElementTest\">\n")
    		.append(" <leaf>leaf</leaf>\n")
    		.append(" <leafWithNamespace xmlns=\"http://testNamespace\">leafValue</leafWithNamespace>\n")
    		.append("</a>")
    		.toString();

    	// Used to verify expectedXML is parseable
    	final Element expectedElement = parser.parse(expectedXml);
    	final Element expectedLeafElement = expectedElement.getChild("leafWithNamespace");
    	assertEquals(expectedLeafElement.namespace, leafWithNamespace.namespace);
    	
    	/*
    	 * Throws SAX exception
    	 * 
    	 * [Fatal Error] The prefix "unknown" for element "unknown:leafWithNamespace" is not bound...
    	 * 
    	 */
    	final Element parsedElement = parser.parse(a1.toXMLString());   
    	assertEquals(expectedXml, parsedElement.toXMLString());  	
    	
    }
    

}


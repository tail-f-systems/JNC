package com.tailf.confm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tailf.inm.Attribute;
import com.tailf.inm.INMException;
import com.tailf.inm.NodeSet;

public class ContainerTest {

    Container a1;
    Container a2;
    Container b1;
    Container b2;
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
        a1 = new DummyContainer(ns, "a");
        b1 = new DummyContainer(ns, "b");
        a2 = a1.cloneContent(new DummyContainer(ns, "a"));
        b2 = b1.cloneContent(new DummyContainer(ns, "b"));
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
                new Attribute(ns, "reference", "ContainerTest"),
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
    public void testGetDiff() throws INMException {
        // Sanity checks
        assertTrue("Initially, node sets are empty", nodeSetsAreEmpty());
        Container.getDiff(a1, a1, uniqueA, uniqueB, changedA, changedB);
        assertTrue("No diff between a and itself", nodeSetsAreEmpty());
        Container.getDiff(a1, a2, uniqueA, uniqueB, changedA, changedB);
        assertTrue("attributes do not matter for diff", nodeSetsAreEmpty());
        Container.getDiff(a1, b1, uniqueA, uniqueB, changedA, changedB);
        assertFalse("Different trees yield no diff", nodeSetsAreEmpty());
        clearNodeSets();
        assertTrue("The clearNodeSets method works", nodeSetsAreEmpty());
        
        // Remove child 'a' from b2 and confirm that it is then unique to b1
        b2.delete("child::a");
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertFalse("b1 has no unique children", uniqueA.isEmpty());
        assertEquals("a is a child of b1 only", a1, uniqueA.first());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        assertTrue("No changed leaves in A", changedA.isEmpty());
        assertTrue("No changed leaves in B", changedB.isEmpty());
        clearNodeSets();
        
        // Add it and confirm that the trees are now equal again
        b2.addChild(a2);
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());
        
        // Change value of leaf in b2, confirm "changed" status in b1 and b2
        b2.setValue("child::a/leaf", "");
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue("b1 has no unique children", uniqueA.isEmpty());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        assertFalse("No changed leaves in A", changedA.isEmpty());
        assertFalse("No changed leaves in B", changedB.isEmpty());
        assertEquals("leaf changed in A", leaf1, changedA.first());
        assertEquals("leaf changed in B", leaf2, changedB.first());
        clearNodeSets();
        
        // Restore and confirm that the trees are now equal again
        b2.setValue("child::a/leaf", "leaf");
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());

        // Add reference to same leaf and change both at once, confirm change
        b1.addChild(leaf2);
        b1.setValue("child::leaf", "leaf2");
        assertEquals("Value changed", "leaf2", b2.getValue("child::a/leaf"));
        
        // Confirm that leaf2 is 'unique' to b1 (because of structure diff)
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertFalse("b1 has no unique children", uniqueA.isEmpty());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        clearNodeSets();
        
        // Add same leaf to b2 and confirm that leaf1 and leaf2 are 'changed'
        b2.addChild(leaf2);
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue("b1 has no unique children", uniqueA.isEmpty());
        assertTrue("b2 has no unique children", uniqueB.isEmpty());
        assertFalse("No changed leaves in A", changedA.isEmpty());
        assertFalse("No changed leaves in B", changedB.isEmpty());
        assertEquals("leaf changed in A", leaf1, changedA.first());
        assertEquals("leaf changed in B", leaf2, changedB.first());
        clearNodeSets();
        
        // Change value of leaf2 and confirm that trees are identical
        b2.setValue("child::leaf", "leaf");
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());
    }

}
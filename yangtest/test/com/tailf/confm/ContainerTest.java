package com.tailf.confm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tailf.inm.Attribute;
import com.tailf.inm.INMException;
import com.tailf.inm.NodeSet;

public class ContainerTest {

    Container a;
    Container b1;
    Container b2;
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
        Leaf leaf = new Leaf(ns, "leaf");
        a = new DummyContainer(ns, "a");
        a.addChild(leaf);
        b1 = new DummyContainer(ns, "b");
        b1.addChild(a);
        b2 = b1.cloneContent(new DummyContainer(ns, "b"));
        clearNodeSets();
        a.setPrefix(prefix);
        b1.setPrefix(prefix);
        Attribute[] attrs = {new Attribute(ns, "presence", "Always present"),
                new Attribute(ns, "description", "For testing"),
                new Attribute(ns, "reference", "ContainerTest"),
        };
        for (int i=0; i<attrs.length; i++) {
            a.addAttr(attrs[i]);
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
        assertTrue(nodeSetsAreEmpty());
        Container.getDiff(a, a, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertTrue(nodeSetsAreEmpty());
        Container.getDiff(a, b1, uniqueA, uniqueB, changedA, changedB);
        assertFalse(nodeSetsAreEmpty());
        clearNodeSets();
        assertTrue(nodeSetsAreEmpty());
        b2.delete("child::a");
        Container.getDiff(b1, b2, uniqueA, uniqueB, changedA, changedB);
        assertFalse(uniqueA.isEmpty());
        assertTrue(uniqueB.isEmpty());
        assertTrue(changedA.isEmpty());
        assertTrue(changedB.isEmpty());
    }

}

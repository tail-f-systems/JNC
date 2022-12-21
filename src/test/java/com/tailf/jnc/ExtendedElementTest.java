package com.tailf.jnc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ExtendedElementTest {

    YangElement parentElement;

    private final String ns = "http://test.com/ns/extendedtest";
    private final String parentName = "test-container";
    private final String childName = "test-child";

    @Before
    public void setUp() {
        parentElement = new TestContainer();
    }

    @Test
    public void test_add_child_by_name() throws Exception {
        assertFalse("parent element has no child", parentElement.hasChildren());
        String[] childrenNames = parentElement.childrenNames();
        assertTrue("parent element has one child name", childrenNames.length == 1);
        YangElement child = (YangElement) parentElement.addChild(childrenNames[0]);
        assertTrue("parent element has children", parentElement.hasChildren());
        YangElement copy = child.cloneContent(new TestChild());
        assertTrue("child copy is a TestChild", copy instanceof TestChild);
        parentElement.addChild(copy);
        assertTrue("parent element has 2 children", parentElement.getChildren().size() == 2);
    }

    public class TestContainer extends YangElement {

        private static final long serialVersionUID = 1L;

        public TestContainer() {
            super(ns, parentName);
        }

        @Override
        public String[] childrenNames() {
            return new String[] {
                    childName,
                };
        }

        @Override
        public String[] keyNames() {
            return null;
        }

        @Override
        public TestContainer cloneShallow() {
            return (TestContainer)cloneShallowContent(new TestContainer());
        }

        public TestChild addTestChild() throws JNCException {
            TestChild testChild = new TestChild();
            insertChild(testChild, childrenNames());
            return testChild;
        }

    }

    public class TestChild extends YangElement {
        private static final long serialVersionUID = 1L;

        public TestChild() {
            super(ns, childName);
        }

        @Override
        public String[] childrenNames() {
            return new String[] {
                    "id",
                    "value",
                };
        }

        @Override
        public String[] keyNames() {
            return new String[] {
                    "id",
                };
        }

        @Override
        public TestChild cloneShallow() {
            return (TestChild)cloneShallowContent(new TestChild());
        }

    }


}

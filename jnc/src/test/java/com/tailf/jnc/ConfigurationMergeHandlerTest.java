
package com.tailf.jnc;

import com.tailf.jnc.Attribute;
import com.tailf.jnc.Element;
import com.tailf.jnc.NodeSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationMergeHandlerTest {
    protected static final String NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-system";

    private final ConfigurationMergeHandler handler = new ConfigurationMergeHandler();

    public ConfigurationMergeHandlerTest() {
    }

    @Test
    public void shouldAddElementAtRoot() throws Exception {

        final NodeSet emptySet = newNodeSet().build();

        final NodeSet newRootSet = newNodeSet()
                .withElement(newElement("parent").build())
                .build();

        final NodeSet result = performMerge("shouldAddElementAtRoot", emptySet, newRootSet, "/parent");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
    }

    @Test
    public void shouldCreateElementsInEmptyTree() throws Exception {

        final NodeSet emptySet = newNodeSet().build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA").build())
                        .build())
                .build();

        final NodeSet result = performMerge("shouldCreateElementsInEmptyTree", emptySet, updateSet, "/parent/childA");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(1, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childA"));
    }

    @Test
    public void shouldAddChildToExistingElement() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA").build())
                        .build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childB").build())
                        .build())
                .build();

        final NodeSet result = performMerge("shouldAddChildToExistingElement", existingSet, updateSet, "/parent/childB");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(2, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childA"));
        assertNotNull(result.get(0).getChild("childB"));
    }

    @Test
    public void shouldAddMissingElements() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent").build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA")
                                .withChild(newElement("grandChildA").build())
                                .build())
                        .build())
                .build();

        final NodeSet result = performMerge("shouldAddMissingElements", existingSet, updateSet, "/parent/childA/grandChildA");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(1, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childA"));
        assertEquals(1, result.get(0).getChild("childA").getChildren().size());
        assertNotNull(result.get(0).getChild("childA").getChild("grandChildA"));
    }

    @Test
    public void shouldDeleteElementFromTree() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA").build())
                        .withChild(newElement("childB").build())
                        .build())
                .build();

        final NodeSet emptySet = newNodeSet().build();

        final NodeSet result = performMerge("shouldDeleteElementFromTree", existingSet, emptySet, "/parent/childB");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(1, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childA"));
    }

    @Test
    public void shouldDeleteByLeaf() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA").withLeaf("leaf", "value1").build())
                        .withChild(newElement("childA").withLeaf("leaf", "value2").build())
                        .build())
                .build();

        final NodeSet emptySet = newNodeSet().build();

        final NodeSet result = performMerge("shouldDeleteByLeaf", existingSet, emptySet, "/parent/childA[leaf=\"value2\"]");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(1, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childA"));
        assertEquals("value1", result.get(0).getChild("childA").getValue("leaf"));
    }

    @Test
    public void shouldDeleteEmptyParentElements() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA")
                                .withChild(newElement("grandChildA").build())
                                .build())
                        .withChild(newElement("childB").build())
                        .build())
                .build();

        final NodeSet emptySet = newNodeSet().build();

        final NodeSet result = performMerge("shouldDeleteEmptyParentElements", existingSet, emptySet, "/parent/childA/grandChildA");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(1, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childB"));
    }

    @Test
    public void shouldDeleteCompleteTree() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA")
                                .withChild(newElement("grandChildA").build())
                                .build())
                        .build())
                .build();

        final NodeSet emptySet = newNodeSet().build();

        final NodeSet result = performMerge("shouldDeleteCompleteTree", existingSet, emptySet, "/parent/childA/grandChildA");

        assertEquals(0, result.size());
    }

    @Test
    public void shouldReplaceExistingChildren() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA")
                                .withChild(newElement("grandChildA1").build())
                                .withChild(newElement("grandChildA2").build())
                                .build())
                        .withChild(newElement("childB")
                                .withChild(newElement("grandChildB").build())
                                .build())
                        .build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA")
                                .withChild(newElement("grandChildA1").build())
                                .withChild(newElement("grandChildA3").build())
                                .build())
                        .build())
                .build();

        final NodeSet result = performMerge("shouldReplaceExistingChildren", existingSet, updateSet, "/parent/childA");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(2, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childB"));
        assertEquals(2, result.get(0).getChild("childA").getChildren().size());
        assertNotNull(result.get(0).getChild("childA").getChild("grandChildA1"));
        assertNull(result.get(0).getChild("childA").getChild("grandChildA2"));
        assertNotNull(result.get(0).getChild("childA").getChild("grandChildA3"));
    }


    @Test
    public void shouldUpdateExistingElement() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA").withAttribute("Attr", "Old Value").build())
                        .build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA").withAttribute("Attr", "New Value").build())
                        .build())
                .build();

        final NodeSet result = performMerge("shouldUpdateExistingElement", existingSet, updateSet, "/parent/childA");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals("New Value", result.get(0).getChild("childA").getAttrValue("Attr"));
    }

    @Test
    public void shouldUpdateTopLevelElement() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childA").build())
                        .build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent")
                        .withChild(newElement("childB").build())
                        .build())
                .build();

        final NodeSet result = performMerge("shouldUpdateTopLevelElement", existingSet, updateSet, "/parent");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
        assertEquals(1, result.get(0).getChildren().size());
        assertNotNull(result.get(0).getChild("childB"));
    }

    @Test
    public void shouldReplaceMasterWithFragmentIfXPathIsEmpty() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent1").build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent2").build())
                .build();

        final NodeSet result = performMerge("shouldReplaceMasterWithFragmentIfXPathIsEmpty", existingSet, updateSet, "");

        assertEquals(1, result.size());
        assertEquals("parent2", result.get(0).name);
    }

    @Test
    public void shouldReplaceMasterWithFragmentIfXPathIsRootOnly() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent1").build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("parent2").build())
                .build();

        final NodeSet result = performMerge("shouldReplaceMasterWithFragmentIfXPathIsRootOnly", existingSet, updateSet, "/");

        assertEquals(1, result.size());
        assertEquals("parent2", result.get(0).name);
    }

    @Test
    public void shouldDoNothingIfFragmentsDoNotMatchPath() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent").build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("garbage").build())
                .build();

        final NodeSet result = performMerge("shouldDoNothingIfFragmentsDoNotMatchPath", existingSet, updateSet, "/parent/childX");

        assertEquals(1, result.size());
        assertEquals("parent", result.get(0).name);
    }

    @Test
    public void shouldReturnUpdateNodeSetIfXPathIsNull() throws Exception {

        final NodeSet existingSet = newNodeSet()
                .withElement(newElement("parent").build())
                .build();

        final NodeSet updateSet = newNodeSet()
                .withElement(newElement("newstuff").build())
                .build();

        final NodeSet result = performMerge("shouldReturnUpdateNodeSetIfXPathIsNull", existingSet, updateSet, null);

        assertEquals(1, result.size());
        assertEquals("newstuff", result.get(0).name);
    }



    private NodeSet performMerge(final String testId, final NodeSet before, final NodeSet update, final String xPath) {
        dumpNodeSet(before, testId + "\nBefore");
        dumpNodeSet(update, "Path " + xPath + " with Update");
        final NodeSet result = handler.updateConfiguration(before, update, xPath);
        dumpNodeSet(result, "Result");
        return result;
    }

    private NodeSetBuilder newNodeSet() {
        return new NodeSetBuilder();
    }

    private ElementBuilder newElement(final String name) {
        return new ElementBuilder(name);
    }

    private static class NodeSetBuilder {
        final NodeSet nodeSet = new NodeSet();

        private NodeSet build() {
            return nodeSet;
        }

        private NodeSetBuilder withElement(final Element element) {
            nodeSet.add(element);
            return this;
        }

    }

    private static class ElementBuilder {

        Element element;

        private ElementBuilder(final String name) {
            element = new Element(NAMESPACE, name);
        }

        private ElementBuilder withChild(final Element element) {
            this.element.addChild(element);
            return this;
        }

        private ElementBuilder withLeaf(final String name, final String value) {
            final Element leafElement = new Element(NAMESPACE, name);
            leafElement.setValue(value);
            this.element.addChild(leafElement);
            return this;
        }

        private ElementBuilder withAttribute(final String name, final String value) {
            this.element.addAttr(new Attribute(name, value));
            return this;
        }

        private Element build() {
            return element;
        }

    }

    private void dumpNodeSet(final NodeSet nodeSet, final String id) {
        final boolean verboseMode = false;
        if (verboseMode) {
            System.out.println(id + ":\n" + nodeSet.toXMLString());
        }
    }
}
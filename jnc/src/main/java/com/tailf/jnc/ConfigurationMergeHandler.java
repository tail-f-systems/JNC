package com.tailf.jnc;

import java.util.ArrayList;
import java.util.List;

/**
 *  Handles merging of netconf configurations.
 */

public class ConfigurationMergeHandler {

    /**
     * Updates a master configuration by merging a configuration fragment into it.
     * @param masterConfiguration   the configuration to be updated
     * @param configurationFragment contains the elements to be used to update the configuration
     * @param xPath contains an XPath definition of the element(s) to be updated
     * @return  the <code>masterConfiguration</code> NodeSet with the elements defined by <code>xPath</code>
     *          updated from corresponding elements in <code>configurationFragment</code>
     */
    public NodeSet updateConfiguration(final NodeSet masterConfiguration,
                                       final NodeSet configurationFragment,
                                       final String xPath) {

        final List<PathComponent> pathComponents = splitPathIntoComponents(xPath);

        if (pathComponents.isEmpty() || pathComponents.get(0).name.isEmpty()) {
            return configurationFragment;
        }

        // Identify the deepest element in the original node set that matches the xpath.
        final Element deepestMasterElement = findDeepestElement(masterConfiguration, pathComponents);

        // If there were no matches, we can just add the update node set to the original.
        if (null == deepestMasterElement) {
            copyAllElementsToNodeSet(masterConfiguration, configurationFragment);
            return masterConfiguration;
        }

        // Determine the depth of the deepest element in the original node set.
        // If this is the same as the number of xpath components, we know that the
        // complete xpath defines exactly that element, meaning it has to be removed and, possibly replaced.
        // If the depth is less than the number of xpath elements, we know that element will be updated by
        // adding an element from the update fragment.
        // For example, given the trivial node set:
        //     A0 -> B0 -> C0
        //        -> B1
        // And different XPaths:
        //     A0/B0/C0
        //         the match is complete and we know that C0 is going to be removed and,
        //         depending on the update fragment, replaced.
        //     A0/B0/C0/D0
        //         the match is incomplete and depending on the update fragment,
        //         a D0 element may need to be added to the existing C0.
        //     A0/B2
        //         the match is incomplete again and, depdending on the update fragment, a B2 element
        //         may need to be added to the exising A0.

        final int masterElementDepth = determineElementDepth(deepestMasterElement);
        final boolean matchingMasterElementNeedsToBeRemoved = (masterElementDepth == pathComponents.size());

        final Element fragmentElementToBeAdded = identifySubFragmentToBeAdded(configurationFragment,
                pathComponents, masterElementDepth);

        // If there is no matching element in the update node set, we know that any matching element in the
        // original node set will need to be deleted.
        final boolean updateFragmentNeedsToBeAdded = (null != fragmentElementToBeAdded);

        // We can now update the original node set.
        // What form this takes depends on:
        // A) did the original node set contain an element matching the complete xpath?
        // B) did the update fragment contain an element matching the xpath.
        //
        // If A is true and B is true, need to remove the element from the original node set
        // and replace it with the one from the update fragment
        //
        // If A is true and B is false, need to remove the element from the original node set.
        //
        // If A is false and B is true, need to add the required elements from the update fragment
        // to the original node set.
        //
        // If A is false and B is false, nothing needs to be done.

        if (matchingMasterElementNeedsToBeRemoved) {

            // Remove element from original node set.
            final Element parentElement = removeElementAndReturnItsParent(masterConfiguration, deepestMasterElement);

            if (updateFragmentNeedsToBeAdded) {
                addElement(masterConfiguration, parentElement, fragmentElementToBeAdded);
            } else {
                removeEmptyParents(masterConfiguration, parentElement);
            }
        } else {
            if (updateFragmentNeedsToBeAdded) {
                // Add elements from update node set to original
                addElement(masterConfiguration, deepestMasterElement, fragmentElementToBeAdded);
            }
        }
        return masterConfiguration;

    }

    // Remove an element and any ancestors that are consequently childless.
    private void removeEmptyParents(final NodeSet nodeSet, final Element elementToRemove) {

        Element nextElementToRemove = elementToRemove;
        while (nextElementToRemove != null && nextElementToRemove.getChildren().size() == 0) {
            nextElementToRemove = removeElementAndReturnItsParent(nodeSet,  nextElementToRemove);
        }
    }

    // Identifies the element at the top of the fragment that will need to be merged into the
    // master configuration. If there is no such fragment, null will be returned.
    private Element identifySubFragmentToBeAdded(final NodeSet fragment,
                                                 final List<PathComponent> pathComponents,
                                                 final int masterElementDepth) {

        // Determine the deepest element in the fragment that matches the xpath
        Element deepestElement = findDeepestElement(fragment, pathComponents);

        if (null != deepestElement) {

            // Adjust the element to allow for missing elements in the original node set.
            // For example, given:
            //     Master nodeset - A0 -> B0 -> C0
            //     Update nodeset - A0 -> B1 -> C1 -> C2
            //     Xpath          - A0/B1/C1
            // The deepest matching element in the fragment will be C1, but we need to add B1 to
            // A0 in the master nodeset.
            // So, based on the depth of the matching element in the master nodeset (1 in this example),
            // go back up the fragment tree to get the required element.

            final int updateElementDepth = determineElementDepth(deepestElement);
            int backupCount = updateElementDepth - masterElementDepth;
            while (backupCount-- > 1) {
                deepestElement = deepestElement.getParent();
            }
        }
        return deepestElement;
    }

    private void copyAllElementsToNodeSet(final NodeSet masterConfiguration, final NodeSet configurationFragment) {
        for (final Element element : configurationFragment) {
            masterConfiguration.add(element);
        }
    }

    // Add an element to a parent element or, if the parent is null, to a nodeset
    private void addElement(final NodeSet nodeSet, final Element parentElement, final Element elementToBeAdded) {
        if (null != parentElement) {
            parentElement.addChild(elementToBeAdded);
        } else {
            nodeSet.add(elementToBeAdded);
        }
    }

    // Remove an element from its parent element, or from a nodeset.
    // Returns the parent element of the removed element
    private Element removeElementAndReturnItsParent(final NodeSet nodeSet, final Element elementToRemove) {
        final Element parentElement = elementToRemove.getParent();
        if (null != parentElement) {
            parentElement.deleteChild(elementToRemove);
        } else {
            nodeSet.removeMember(elementToRemove);
        }
        return parentElement;
    }

    // Determines the depth of an element in its node set.
    // Returns +ve value, with 1 being the top level (i.e. if it has no parent).
    private int determineElementDepth(final Element element) {

        int elementDepth = 0;
        Element nextElementToCheck = element;
        while (nextElementToCheck != null) {
            elementDepth++;
            nextElementToCheck = nextElementToCheck.getParent();
        }
        return elementDepth;
    }

    // Works through the supplied node set to find the 'deepest' element that matches the xpath
    // components.
    // Null is returned if there are no matches.
    //
    // For example, given the trivial node set:
    //     A0 -> B0 -> C0
    //        -> B1
    // the responses for different xpaths would be:
    //     XPATH        ELEMENT
    //     /A0          A0
    //     /A0/B0/C0    C0
    //     /A0/B0/C1    B0
    //     /A0/B1       B1
    //     /A0/B2       A0
    //     /A1          null
    private Element findDeepestElement(final NodeSet nodeSet, final List<PathComponent> pathComponents) {

        Element deepestMatchingElement = null;
        NodeSet nextNodesToCheck = nodeSet;

        for (final PathComponent component : pathComponents) {
            final NodeSet candidates = nextNodesToCheck;
            nextNodesToCheck = null;

            for (final Element candidateElement : candidates) {
                if (isMatchingElement(candidateElement, component)) {
//                    LOG.debug(">>>Matching Elements:{}--{}", candidateElement.toXMLString(), component);
                    deepestMatchingElement = candidateElement;
                    nextNodesToCheck = candidateElement.getChildren();
                    break;
                }
            }
            if (nextNodesToCheck == null) {
                break;
            }
        }
        return deepestMatchingElement;
    }

    // Splits a raw XPath string into discrete elements.
    private List<PathComponent> splitPathIntoComponents(final String rawPath) {
        final List<PathComponent> pcs = new ArrayList<PathComponent>();
        if (rawPath != null) {
            final byte[] buf = rawPath.getBytes();
            int i = 0;

            while (i < rawPath.length()) {
                if (buf[i] == '/') {
                    i++;
                    final int j = scanName(buf, i);
                    final PathComponent pc = new PathComponent(new String(buf, i, j - i));
                    pcs.add(pc);
                    i = scanAndAddKeys(buf, j, pc);
                } else {
                    break;
                }
            }
        }
        return pcs;
    }

    // Determines the index of the last character of a key name.
    private int scanName(final byte[] buf, final int startPos) {
        int i = startPos;
        while ((i < buf.length)
                && (buf[i] != '/') && (buf[i] != '[')) {
            i++;
        }
        return i;
    }

    private int scanAndAddKeys(final byte[] buf, final int startPos, final PathComponent pc) {
        int i = startPos;

        while (i < buf.length) {
            if (buf[i] == '[') {
                i++;
                String keyName = "";
                byte quoteChar = 0;
                int j = i;
                int quoted = 0;
                while ((j < buf.length) && ((buf[j] != '=') && (buf[j] != ']'))) {
                    j++;
                }
                if (buf[j] == '=') {
                    keyName = new String(buf, i, j - i);
                    i = j + 1;
                    j = i;
                }
                while ((j < buf.length) && ((quoteChar != 0) || (buf[j] != ']'))) {
                    if ((buf[j] == '\"') || (buf[j] == '\'')) {
                        if (quoteChar == 0) {
                            quoteChar = buf[j];
                        } else {
                            quoteChar = 0;
                            quoted = 1;
                        }
                    }
                    j++;
                }
                final String keyVal = new String(buf, i + quoted, j - i - 2 * quoted);
                pc.addKey(keyName, keyVal);
                i = j + 1;
            } else {
                return i;
            }
        }
        return i;
    }

    // Determine if element matches an xpath fragment
    private boolean isMatchingElement(final Element elem, final PathComponent pc) {
        return elem != null && pc != null &&
                elem.name.equals(pc.name) &&
                areMatchingKeys(elem.getChildren(), pc.keys);
    }

    // Determines whether a set of key elements match a full set of XPath keys
    private boolean areMatchingKeys(final NodeSet keyElements, final List<Key> keys) {

        if ((keys == null) || (keys.isEmpty())) {
            return true;
        }

        if ((keyElements == null) || (keyElements.size() < keys.size())) {
            return false;
        }

        for (int i = 0; i < keys.size(); i++) {
            if (!keyElements.getElement(i).name.equals(keys.get(i).name)
                    || !keyElements.getElement(i).getValue().toString().equals(keys.get(i).value)) {
                return false;
            }
        }
        return true;
    }

    // Encapsulates the name and value of a 'key' from an xpath predicate
    private static class Key {

        private final String name;
        private final String value;

        public Key(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
    }

    // Encapsulates the name of an xpath component and its associated predicate keys.
    private class PathComponent {

        private final String name;
        private final List<Key> keys;

        public PathComponent(final String name) {
            this.keys = new ArrayList<Key>();
            this.name = name;
        }

        public void addKey(final String name, final String value) {
            this.keys.add(new Key(name, value));
        }

        @Override
        public String toString() {
            String ret = name;
            for (final Key key : keys) {
                ret += "[" + key.name + "='" + key.value + "']";
            }
            return ret;
        }
    }
}

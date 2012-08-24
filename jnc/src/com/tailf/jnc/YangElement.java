/*    -*- Java -*-
 *
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.jnc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * The YangElement is a configuration sub-tree like the
 * {@link com.tailf.jnc.Element Element}. It is an extension of the Element
 * class to make the configuration sub-tree data model aware. Classes generated
 * from the ConfM compiler are either Containers, Leafs, or derived data types.
 * <p>
 * Thus the YangElement which is an abstract class is never used directly.
 * <p>
 * Fundamental methods for comparing, syncing, and inspecting sets of
 * configuration data, is provided by this class. The following two methods
 * highlights the fundmental purpose of this library:
 * <ul>
 * <li>{@link #checkSync checkSync} - checks if two configurations are are
 * equal, or if a sync is needed. This is a common typical operation a manager
 * would like to do when one configuration is stored on the device and the
 * other is stored in a database.
 * <li>{@link #sync sync} - will calculate the difference between two
 * configuration sub trees and build up a resulting subtree which is basically
 * a tree with NETCONF operations needed to turn the source tree into the
 * target tree. The purpose of this is to find a minimal set of operations to
 * sync two configurations so that they become equal.
 * </ul>
 * 
 */
// @SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class YangElement extends Element {

    private static final long serialVersionUID = 1L;

    private static final String reservedWords[] = { "abstract", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return",
            "short", "static", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile",
            "while" };

    /**
     * Structure information. An array of the children names.
     */
    abstract protected String[] childrenNames();

    /**
     * Structure information. An array of the names of the key children.
     */
    abstract protected String[] keyNames();

    /**
     * Constructor for the container
     */
    public YangElement(String ns, String name) {
        super(ns, name);
    }

    /**
     * 
     * @param elem
     * @return Dot-separated path to top-level container, or empty string.
     */
    private static String getPackage(Element elem) {
        if (elem instanceof YangElement) {
            final YangElement c = (YangElement) elem;
            if (c.parent instanceof YangElement) {
                String pkg = getPackage(c.parent);
                if (pkg != "") {
                    pkg += ".";
                }
                return pkg + camelize(c.name);
            }
            // don't add name of top-level container to package
        }
        return "";
    }

    /**
     * Creates an instance of
     * 
     * @param parent of YANG statement counterpart, null if none
     * @param name (non-normalized) name of class to be instantiated
     * @param pkg The base package of the generated classes
     * @return An instance of class name, as a child of parent
     * @throws ClassNotFoundException If normalize(name) does not yield a valid
     *             class name
     * @throws InstantiationException if the referenced class represents an
     *             abstract class, an interface, an array class, a primitive
     *             type, or void; or if the class has no nullary constructor;
     *             or if the instantiation fails for some other reason.
     * @throws IllegalAccessException if the class or its nullary constructor
     *             is not accessible.
     */
    private static Element instantiate(Element parent, String name, String pkg)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        final String className = pkg + "." + getPackage(parent)
                + normalize(name);
        final Class<?> rootClass = Class.forName(className);
        return (Element) rootClass.newInstance();
    }

    /**
     * Data model aware method, creates a container instance child.
     * 
     * @return The created element or null if no container was created.
     */
    protected static Element createInstance(ElementHandler parser,
            Element parent, String ns, String name) throws YangException {
        final String pkg = getPackage(ns);
        if (pkg == null) {
            final Element elem = new Element(ns, name);
            if (parent != null) {
                parent.addChild(elem);
            }
            return elem; // not aware
        }
        try {
            if (parent == null) {
                return instantiate(null, name, pkg); // Root
            } else if (parent instanceof YangElement) {
                // YangElement child, aware
                try {
                    final String methodName = "add" + normalize(name);
                    final Class<?> parentClass = parent.getClass();
                    final Method addContainer = parentClass.getMethod(
                            methodName, new Class[] {});
                    return (Element) addContainer.invoke(parent,
                            new Object[] {});
                } catch (final NoSuchMethodException e) {
                    if (((YangElement) parent).isChild(name)) {
                        // known existing leaf will be handled by endElement
                        // code
                        return null;
                    }
                    // It's an unknown container or child
                    // FIXME - check capabilities
                    if (!RevisionInfo.newerRevisionSupportEnabled) {
                        throw new YangException(
                                YangException.ELEMENT_MISSING,
                                parent.getPath(name) + ": Unexpected element");
                    }
                    parser.unknownLevel = 1;
                    return null;
                }
            } else { // YangElement is aware but parent is not
                     // This is the case where we stop parsing
                     // the NETCONF rpc data and start to create
                     // ConfM objects instead
                final Element child = instantiate(parent, name, pkg);
                parent.addChild(child);
                return child;
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            throw new YangException(YangException.ELEMENT_MISSING,
                    parent.getPath(name) + ": Unexpected element");
        } catch (final InstantiationException e) {
            e.printStackTrace();
            throw new YangException(YangException.ELEMENT_MISSING,
                    parent.getPath(name) + ": Unexpected element");
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
            throw new YangException(YangException.ELEMENT_MISSING,
                    parent.getPath(name) + ": Unexpected element");
        } catch (final InvocationTargetException e) {
            throw new YangException(YangException.ELEMENT_MISSING,
                    parent.getPath(name) + ": Unexpected element");
        }
    }

    /**
     * setLeafValue is a data model aware method to set the leaf value of the
     * specified leaf of this container.
     * 
     */
    public void setLeafValue(String ns, String name, String value)
            throws YangException, JNCException {

        // Aware
        final String methodName = "set" + normalize(name) + "Value";
        final Class<?>[] types = new Class[] { String.class };
        try {
            final Method setLeafValue = getClass().getMethod(methodName,
                    types);
            final Object[] args = { value };
            setLeafValue.invoke(this, args);
        } catch (final NoSuchMethodException e) {
            if (!RevisionInfo.newerRevisionSupportEnabled) {
                // e.printStackTrace();
                throw new YangException(YangException.ELEMENT_MISSING,
                        getPath(name) + ": Unexpected element");
            }
            final NodeSet nodes = get(name);
            if (nodes.isEmpty()) {
                final Element leaf = new Element(ns, name);
                leaf.setValue(value);
                insertLast(leaf);
            } else {
                final Element leaf = nodes.first();
                leaf.setValue(value);
            }
        } catch (final java.lang.reflect.InvocationTargetException cm) {
            // case with added enumerations,
            if (!RevisionInfo.newerRevisionSupportEnabled) {
                throw new YangException(YangException.BAD_VALUE,
                        getPath(name) + ": " + cm.getCause().toString());
            }

            final NodeSet nodes = get(name);
            if (nodes.isEmpty()) {
                final Element leaf = new Element(ns, name);
                leaf.setValue(value);
                insertLast(leaf);
            } else {
                final Element leaf = nodes.first();
                leaf.setValue(value);
            }
        }

        catch (final Exception invErr) {
            // type error
            throw new YangException(YangException.BAD_VALUE, getPath(name)
                    + ": " + invErr.getCause().toString());
        }
    }

    static class Package {
        String pkg;
        String ns;

        Package(String ns, String pkg) {
            this.ns = ns;
            this.pkg = pkg;
        }
    }

    /**
     * Static list of packages.
     * 
     */
    static ArrayList<Package> packages = new ArrayList<Package>();

    /**
     * Locate package from Namespace.
     * 
     * @return Package name, if namespace is data model aware
     */
    public static String getPackage(String ns) {
        if (packages == null) {
            return null;
        }
        for (final Package p : packages) {
            if (p.ns.equals(ns)) {
                return p.pkg;
            }
        }
        return null;
    }

    /**
     * Assiciate a JAVA package with a namespace.
     */
    public static void setPackage(String ns, String pkg) {
        if (packages == null) {
            packages = new ArrayList<Package>();
        }
        removePackage(ns);
        packages.add(new Package(ns, pkg));
    }

    /**
     * Remove a package from the list of Packages
     */
    public static void removePackage(String ns) {
        if (packages == null) {
            return;
        }
        for (int i = 0; i < packages.size(); i++) {
            final Package p = packages.get(i);
            if (p.ns.equals(ns)) {
                packages.remove(i);
                return;
            }
        }
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static boolean isReserved(String s) {
        for (final String reservedWord : reservedWords) {
            if (s.equals(reservedWord)) {
                return true;
            }
        }
        return false;
    }

    private static String camelize(String s) {
        int pos;
        while ((pos = s.indexOf('-')) != -1) {
            s = s.substring(0, pos) + capitalize(s.substring(pos + 1));
        }
        if (isReserved(s)) {
            s += "_";
        } else if (s.matches("[0-9]")) {
            s = "_" + s;
        }
        return s;
    }

    private static String normalize(String s) {
        final String res = camelize(s);
        int start = 0, end = res.length();

        if (res.startsWith("_")) {
            start++;
        }
        if (res.endsWith("_")) {
            end--;
        }

        if (end - start < 0) {
            return "";
        } else if (start != 0 || end != res.length()) {
            return "J" + capitalize(res.substring(start, end));
        } else {
            return capitalize(res);
        }
    }

    protected void setLeafValue(String ns, String path, Object value,
            String[] childrenNames) throws JNCException {
        final NodeSet nodes = get(path);

        if (nodes.isEmpty()) {
            final Leaf leaf = new Leaf(ns, path);
            leaf.setValue(value);
            insertChild(leaf, childrenNames);
        } else {
            final Leaf leaf = (Leaf) nodes.first();
            leaf.setValue(value);
        }
    }

    protected void setLeafListValue(String ns, String path, Object value,
            String[] childrenNames) throws JNCException {
        final Leaf leaf = new Leaf(ns, path);
        leaf.setValue(value);
        insertChild(leaf, childrenNames);
    }

    protected boolean isLeafDefault(String path) throws JNCException {
        final NodeSet nodes = get(path);
        return (nodes.isEmpty());
    }

    protected void markLeafReplace(String path) throws JNCException {
        final NodeSet nodes = get(path);

        if (nodes.isEmpty()) {
            throw new YangException(YangException.ELEMENT_MISSING,
                    getPath(path));
        } else {
            nodes.first().markReplace();
        }
    }

    protected void markLeafMerge(String path) throws JNCException {
        final NodeSet nodes = get(path);
        if (nodes.isEmpty()) {
            throw new YangException(YangException.ELEMENT_MISSING,
                    getPath(path));
        } else {
            nodes.first().markMerge();
        }
    }

    protected void markLeafCreate(String path) throws JNCException {
        final NodeSet nodes = get(path);

        if (nodes.isEmpty()) {
            throw new YangException(YangException.ELEMENT_MISSING,
                    getPath(path));
        } else {
            nodes.first().markCreate();
        }
    }

    protected void markLeafDelete(String path) throws JNCException {
        final NodeSet nodes = get(path);

        if (nodes.isEmpty()) {
            throw new YangException(YangException.ELEMENT_MISSING,
                    getPath(path));
        } else {
            nodes.first().markDelete();
        }
    }

    /**
     *
     */
    protected YangElement searchOne(String path) throws JNCException {
        final NodeSet nodes = get(path);
        if (nodes.isEmpty()) {
            throw new YangException(YangException.ELEMENT_MISSING,
                    getPath(path));
        } else {
            return (YangElement) nodes.first();
        }
    }

    /**
     * Given two (YANG) list entries - compare the keys and return true if both
     * YANG objects are the same
     */

    public boolean keyCompare(YangElement b) {

        // check that namespace and name and value are the same.
        if (!equals(b)) {
            return false;
        }
        final String[] keys = keyNames();
        if (keys == null) {
            return false; // not a list entry
        }
        for (int i = 0; i < keys.length; i++) {
            final Element x = getChild(keys[i]);
            final Element bx = b.getChild(keys[i]);
            if (!x.equals(bx)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare the contents of this container toward another container.
     * Compares children values. Returns:
     * <ul>
     * <li>0 - if the two containers children are equal.
     * <li>-1 - if the two containers keys are not equal, which means that they
     * are completely different.
     * <li>1 - the two containers are the same except the non-key children
     * differs.
     * </ul>
     * 
     * @param b YangElement to compare against.
     */
    public int compare(YangElement b) {
        // check that namespace and name and value are the same.
        if (!equals(b)) {
            return -1;
        }

        final String[] keys = keyNames();
        int i = 0;
        if (keys != null) {
            for (; i < keys.length; i++) {
                final Element x = getChild(keys[i]);
                final Element bx = b.getChild(keys[i]);
                if (!x.equals(bx)) {
                    return -1;
                }
            }
        }
        final String[] names = childrenNames();

        // Continue from 'i', assuming keys are first in childrenNames
        for (; i < names.length; i++) {
            final NodeSet nsA = getChildren(names[i]);
            final NodeSet nsB = b.getChildren(names[i]);

            int hits = 0;
            for (int j = 0; j < nsA.size(); j++) {
                final Element cA = nsA.get(j);
                // Now does this elem exist in nsB
                for (int k = 0; k < nsB.size(); k++) {
                    final Element cB = nsB.get(k);
                    if (cA.equals(cB)) {
                        hits++;
                        break;
                    }
                }
            }
            if (nsA.size() != nsB.size() || nsA.size() != hits) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Compare the contents of this container toward another container.
     * Compares children values. Returns:
     * <ul>
     * <li>0 - if the two containers children are equal.
     * <li>-1 - if the two containers keys are not equal, which means that they
     * are completely different.
     * <li>1 - the two containers are the same except the non-key children
     * differs.
     * </ul>
     * 
     * @param b YangElement to compare against.
     */
    @Override
    public int compare(Element b) {
        if (b instanceof YangElement) {
            return compare((YangElement) b);
        }
        return super.compare(b);
    }

    /**
     * Produces the 'diff' between two trees. Nodes that differ in the compare
     * method are added to one of the four provided node sets.
     * <ul>
     * <li>
     * Elements unique to node tree A are put in uniqueA.
     * <li>
     * Elements unique to node tree B are put in uniqueB.
     * <li>
     * Dynamic elements that differ, but have the same keys, are put in the
     * respective node sets changedA and changedB.
     * </ul>
     * If the two trees are identical the three returned nodesets will be
     * empty.
     * <p>
     * Attributes are not included in the inspection. Only container structures
     * and leaf-values are checked.
     * <p>
     * Note that both subtrees must have a common starting point YangElement in
     * order to compare them.
     * 
     * @param a Subtree A (YangElement)
     * @param b Subtree B (YangElement)
     * @param uniqueA Place for elements that are unique to A.
     * @param uniqueB Place for elements that are unique to B.
     * @param changedA Place for elements changed in A.
     * @param changedB Place for elements changed in B.
     */
    public static void getDiff(YangElement a, YangElement b, NodeSet uniqueA,
            NodeSet uniqueB, NodeSet changedA, NodeSet changedB) {
        if (a.compare(b) >= 0) {
            // Containers are equal, go through the children.
            final NodeSet bList = new NodeSet();
            if (a.children == null || b.children == null) {
                if (b.children != null) {
                    uniqueB.addAll(b.children);
                } else if (a.children != null) {
                    uniqueA.addAll(a.children);
                }
                return;
            }
            bList.addAll(b.children);

            // For each child in a, compare with children in b.
            for (int i = 0; i < a.children.size(); i++) {
                final Element aChild = a.children.getElement(i);
                Element bChild = null;
                int bRes = -1;
                int j;
                for (j = 0; j < bList.size(); j++) {
                    bChild = bList.getElement(j);
                    bRes = aChild.compare(bChild);
                    if (bRes >= 0) {
                        break;
                    }
                }
                if (bRes >= 0) {
                    // child at position j in bList compares equal, remove it.
                    bList.remove(j);
                    if (bRes == 1) { // different content
                        changedA.add(aChild);
                        changedB.add(bChild);
                    } else if (aChild instanceof YangElement) {
                        // bRes == 0 so they are equal, but their children
                        // might not be, so we recurse
                        YangElement.getDiff((YangElement) aChild,
                                (YangElement) bChild, uniqueA, uniqueB,
                                changedA, changedB);
                    }
                    // Skip if equal and not YangElement
                } else { // not found
                    uniqueA.add(aChild);
                }
            }
            // Add any remaining nodes in bList to uniqueB
            uniqueB.addAll(bList);
        } else {
            // a.compare(b) == -1: A and B are completely different
            uniqueA.add(a);
            uniqueB.add(b);
        }
    }

    /**
     * Checks if two configurations are equal, or if a sync is needed.
     * 
     * @return 'true' if both trees are equal. 'false' otherwise.
     */
    public boolean checkSync(YangElement b) {
        return checkSync(this, b);
    }

    /**
     * Checks if two configurations are equal, or if a sync is needed.
     * 
     * @return 'true' if both trees are equal. 'false' otherwise.
     */
    public static boolean checkSync(NodeSet a, NodeSet b) {
        final DummyElement aDummy = new DummyElement("DUMMY", "dummy");
        final DummyElement bDummy = new DummyElement("DUMMY", "dummy");
        int i;
        for (i = 0; i < a.size(); i++) {
            aDummy.addChild(a.get(i));
        }
        for (i = 0; i < b.size(); i++) {
            bDummy.addChild(b.get(i));
        }

        return YangElement.checkSync(aDummy, bDummy);
    }

    /**
     * Checks if two configurations are equal, or if a sync is needed.
     * 
     * @return 'true' if both trees are equal. 'false' otherwise.
     */
    public static boolean checkSync(YangElement a, YangElement b) {
        final NodeSet uniqueA = new NodeSet(), uniqueB = new NodeSet();
        final NodeSet changedA = new NodeSet(), changedB = new NodeSet();
        YangElement.getDiff(a, b, uniqueA, uniqueB, changedA, changedB);
        final boolean noUniques = uniqueA.isEmpty() && uniqueB.isEmpty();
        final boolean noChanges = changedA.isEmpty() && changedB.isEmpty();
        return noUniques && noChanges;
    }

    /**
     * Will return a subtree for syncing a subtree A with all the necessary
     * operations to make it look like the target tree B.
     * 
     * @return Return subtree with operations to transmute subtree A into
     *         subtree B.
     */
    public YangElement sync(YangElement b) throws JNCException {
        return YangElement.sync(this, b);
    }

    /**
     * Will return a subtree for syncing a subtree A with all the necessary
     * operations to make it look like the target tree B. This verion of sync
     * will produce a NETCONF tree with NETCONF replace operations where all
     * list entries that differ are replaced with a new list entry using
     * "nc:operation="replace". An alternative (and better) method is "merge".
     * The method {@link #syncMerge} produces a NETCONF tree with the merge
     * operation instead.
     * 
     * @return Return subtree with operations to transmute subtree A into
     *         subtree B.
     */

    public static YangElement sync(YangElement a, YangElement b)
            throws JNCException {

        final NodeSet uniqueA = new NodeSet();
        final NodeSet uniqueB = new NodeSet();
        final NodeSet changedA = new NodeSet();
        final NodeSet changedB = new NodeSet();

        YangElement.getDiff(a, b, uniqueA, uniqueB, changedA, changedB);

        Element result = null;
        for (int i = 0; i < uniqueA.size(); i++) {
            final Element x = uniqueA.getElement(i);
            result = x.merge(result, OP_DELETE);
        }

        for (int i = 0; i < uniqueB.size(); i++) {
            final Element x = uniqueB.getElement(i);
            result = x.merge(result, OP_CREATE);
        }

        for (int i = 0; i < changedB.size(); i++) {
            final Element x = changedB.getElement(i);
            result = x.merge(result, OP_REPLACE);
        }

        return (YangElement) result;
    }

    /**
     * Returns a list of subtrees for syncing a subtree A with all the
     * necessary operations to make it look like the target tree B. This
     * variant uses the NETCONF merge operation.
     * 
     * @return Subtrees with operations to transmute subtree A into subtree B.
     */

    public static NodeSet syncMerge(NodeSet a, NodeSet b) throws JNCException {
        final DummyElement aDummy = new DummyElement("DUMMY", "dummy");
        final DummyElement bDummy = new DummyElement("DUMMY", "dummy");
        int i;
        for (i = 0; i < a.size(); i++) {
            aDummy.addChild(a.get(i));
        }
        for (i = 0; i < b.size(); i++) {
            bDummy.addChild(b.get(i));
        }

        final YangElement result = YangElement.syncMerge(aDummy, bDummy);
        return result.getChildren();
    }

    /**
     * Will return a subtree for syncing a subtree A with all the necessary
     * operations to make it look like the target tree B. This version of sync
     * will produce a NETCONF tree with NETCONF merge operations.
     * 
     * @return Subtree with operations to transmute subtree A into subtree B.
     */

    public static YangElement syncMerge(YangElement a, YangElement b) {
        final YangElement copy = (YangElement) b.clone();
        final NodeSet toDel = new NodeSet();
        YangElement.csync2((YangElement) a.clone(), copy, toDel);
        for (int i = 0; i < toDel.size(); i++) {
            final Element e = toDel.get(i);
            e.getParent().deleteChild(e);
        }
        return copy;
    }

    /**
     * Which NETCONF do we need to produce in order to go from a to b?
     * 
     * @param a Subtree to sync
     * @param b Copy of subtree to mimic
     * @param toDel A list with leaves that should be removed from 'b'
     * @return Number of diffs
     */
    private static int csync2(YangElement a, YangElement b, NodeSet toDel) {
        int diffs = 0;
        for (int i = 0; b.children != null && i < b.children.size(); i++) {
            final Element bChild = b.children.get(i);
            if (a.keyNames() != null && bChild instanceof Leaf) {
                // inside list entries we ignore keys
                if (((Leaf) bChild).isKey()) {
                    continue;
                }
            }

            Element aChild = null;
            if (a.children != null) {
                aChild = YangElement.findDeleteChild(bChild, a.children);
            }
            if (aChild == null) {
                // It's a new child that needs to be merged
                diffs++;
                continue;
            }

            // It was found - and it was also deleted from 'a'
            if (aChild instanceof YangElement) {
                final int d = YangElement.csync2((YangElement) aChild,
                        (YangElement) bChild, toDel);
                diffs += d;
                if (d == 0) {
                    // both children are identical - remove from b as well
                    toDel.add(bChild);
                }
                continue;
            } else if (aChild instanceof Leaf) {
                if (aChild.equals(bChild)) {
                    // remove identical leaves from b - no need to send them
                    toDel.add(bChild);
                } else {
                    diffs++;
                }
            }
        }

        // Mark remaining elements in 'a' for deletion and move them to 'b'
        for (int i = 0; a.children != null && i < a.children.size(); i++) {
            final Element x = a.children.get(i);
            if (x instanceof Leaf) {
                final Leaf leaf = (Leaf) x;
                if (leaf.isKey()) {
                    // ignore key leafs - they're handled elsewhere
                    continue;
                }
                diffs++;
                b.addChild(x);
                x.markDelete();
            }

            // Remove all children except keys (if any)
            else if (x instanceof YangElement) {
                diffs++;
                final YangElement c = (YangElement) x;
                final YangElement n = (YangElement) c.cloneShallow();
                b.addChild(n);
                n.markDelete();
            }
        }
        return diffs;
    }

    private static Element findDeleteChild(Element e, NodeSet s) {
        if (s == null) {
            return null;
        }
        if (e instanceof Leaf) {
            return findDeleteChildLeaf((Leaf) e, s);
        } else if (e instanceof YangElement) {
            return deleteChild((YangElement) e, s);
        } else {
            // It's the case where we receive Elements
            // instead of YangElement/Leaf because the device
            // has a newer revision than us. This code can
            // never be made to work perfect.
            return findDeleteChildElement(e, s);
        }
    }

    private static Element findDeleteChildElement(Element e, NodeSet s) {
        for (int i = 0; i < s.size(); i++) {
            final Element x = s.get(i);
            if (x.compare(e) >= 0) {
                s.remove(i);
                return x;
            }
        }
        return null;
    }

    private static Element deleteChild(YangElement e, NodeSet s) {
        final String[] keys = e.keyNames();
        for (int i = 0; i < s.size(); i++) {
            final Element x = s.get(i);
            if (x instanceof Leaf) {
                continue;
            }
            final YangElement c = (YangElement) x; // XXX Assuming YangElement
                                                   // if not
            // Leaf
            if (keys == null) {
                // plain container
                if (e.equals(x)) {
                    s.remove(i);
                    return c;
                }
            } else if (e.keyCompare(c)) {
                s.remove(i);
                return c;
            }
        }
        return null;
    }

    private static Element findDeleteChildLeaf(Leaf e, NodeSet s) {
        for (int i = 0; i < s.size(); i++) {
            final Element x = s.get(i);
            if (x instanceof Leaf) {
                if (x.compare(e) >= 0) {
                    s.remove(i);
                    return x;
                }
            }
        }
        return null;
    }

    /**
     * Will return a list of subtrees for syncing a subtree A with all the
     * necessary operations to make it look like the target tree B.
     * 
     * @return Return subtrees with operations to transmute subtree A into
     *         subtree B.
     */

    public static NodeSet sync(NodeSet a, NodeSet b) throws JNCException {
        final DummyElement aDummy = new DummyElement("DUMMY", "dummy");
        final DummyElement bDummy = new DummyElement("DUMMY", "dummy");
        int i;
        for (i = 0; i < a.size(); i++) {
            aDummy.addChild(a.get(i));
        }
        for (i = 0; i < b.size(); i++) {
            bDummy.addChild(b.get(i));
        }

        final YangElement result = YangElement.sync(aDummy, bDummy);
        return result.getChildren();
    }

    /**
     * Clones a container. Only key children are cloned, the other children are
     * skipped. Attributes and values are cloned.
     * 
     */
    @Override
    protected abstract Element cloneShallow();

    /**
     * Clones the contents of this container into a target copy. All content is
     * copied except children (shallow).
     * <p>
     * Note: Used by the generated ConfM classes The key children are already
     * cloned.
     * 
     * @param copy The target copy to clone the contents to
     */
    protected YangElement cloneShallowContent(YangElement copy) {
        cloneAttrs(copy);
        return copy;
    }

    /**
     * Clones the content of this container into a target copy. Key children
     * are not copied.
     * <p>
     * Note: Used by the generated ConfM classes The key children are already
     * cloned.
     * 
     * @param copy The target copy to clone the contents into
     */
    protected YangElement cloneContent(YangElement copy) {
        // copy children, except keys which are already copied
        if (children != null) {
            final String[] keyNames = keyNames();
            int i = 0;
            if (keyNames != null) {
                // Skip the keys by starting the loop from here
                i = keyNames.length;
            }
            for (; i < children.size(); i++) {
                final Element child = children.getElement(i);
                final Element child_copy = (Element) child.clone();
                copy.addChild(child_copy);
            }
        }
        cloneAttrs(copy);
        cloneValue(copy);
        return copy;
    }

    /**
     * Read file with XML text and parse it into a configuration tree.
     * 
     * @param filename File name.
     * @see #writeFile(String)
     */
    public static Element readFile(String filename) throws JNCException {
        final YangXMLParser p = new com.tailf.jnc.YangXMLParser();
        return p.readFile(filename);
    }

    // cache the Tagpath and the SchemaNode
    private Tagpath tp = null;
    private SchemaNode n = null;

    @Override
    protected void encode(Transport out, boolean newline_at_end,
            Capabilities capas) throws JNCException {
        if (RevisionInfo.olderRevisionSupportEnabled && capas != null) {
            if (tp == null) {
                tp = tagpath();
            }
            final String actualNamespace = getRootElement().namespace;
            if (n == null) {
                n = SchemaTree.lookup(actualNamespace, tp);
            }
            final String rev = capas.getRevision(actualNamespace);
            if (n.revInfo != null) {
                for (int i = 0; i < n.revInfo.length; i++) {
                    final RevisionInfo r = n.revInfo[i];
                    if (r.introduced.compareTo(rev) > 0) {
                        // This node was somehow modified
                        // System.out.println("REVINFO " + r.type);
                        switch (r.type) {
                        case RevisionInfo.R_MAX_ELEM_RAISED:
                            final int max = r.idata;
                            if (getChildren().size() > max) {
                                throw new JNCException(
                                        JNCException.REVISION_ERROR,
                                        tp
                                                + "too many children for old node "
                                                + "with rev( " + rev + ")");
                            }
                            break;
                        case RevisionInfo.R_MIN_ELEM_LOWERED:
                            // Do nothing
                            break;
                        case RevisionInfo.R_NODE_ADDED:
                            // System.out.println("Dropping "+this.toXMLString());
                            return;
                        default:
                            break;
                        }
                    }
                }
            }
        }
        super.encode(out, newline_at_end, capas);
    }

    public boolean isChild(String childName) {
        final String[] children = childrenNames();
        for (int i = 0; i < children.length; i++) {
            if (childName.equals(children[i])) {
                return true;
            }
        }
        return false;
    }

}

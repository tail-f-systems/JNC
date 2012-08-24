/**
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.jnc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.lang.StringBuffer;
import java.io.*;

/**
 * A configuration element sub-tree. Makes it possible to create and/or
 * manipulate an element tree. The element tree is then to be used to issue
 * NETCONF operations using the {@link NetconfSession} class.
 * <p>
 * Example:
 * 
 * <pre>
 * 
 * // start (Netconf) sessions towards our device
 * SSHConnection connection = new SSHConnection(&quot;127.0.0.1&quot;);
 * connection.authenticateWithPassword(&quot;admin&quot;, &quot;pass&quot;);
 * SSHSession ssh = new SSHSession(connection);
 * NetconfSession session = new NetconfSession(ssh);
 * 
 * // get system configuration from session
 * Element sysConfig = session.getConfig(&quot;/system&quot;).first();
 * 
 * // manipulate the element tree
 * sysConfig.setValue(&quot;dns&quot;, &quot;83.100.1.1&quot;);
 * sysConfig.setValue(&quot;gateway&quot;, &quot;10.0.0.1&quot;);
 * 
 * // Write back the updated element tree
 * session.editConfig(sysConfig);
 * </pre>
 */
public class Element implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The NETCONF namespace. "urn:ietf:params:xml:ns:netconf:base:1.0".
     */
    public static final String NETCONF_NAMESPACE = "urn:ietf:params:xml:ns:netconf:base:1.0";

    /**
     * The namespace this element name belongs to.
     */
    public String namespace;

    /**
     * The name of the node.
     */
    public String name;

    /**
     * The value of the element.
     */
    public Object value;

    /**
     * Attributes on the node. ArrayList of Attribute.
     */
    ArrayList<Attribute> attrs;

    /**
     * Prefix map are really xmlns attributes. For example:
     * <p>
     * xmlns="http:tail-f.com/aaa" or xmlns:aaa="http:tail-f.com/aaa"
     */
    public PrefixMap prefixes;

    /**
     * Static prefix map that is always a default and will be resolved at root
     * level. For example the NETCONF prefix mapping "nc" is added here as a
     * default.
     */
    public static PrefixMap defaultPrefixes = new PrefixMap(new Prefix[] {
            new Prefix("nc", NETCONF_NAMESPACE),
            new Prefix("pl", Capabilities.NS_PARTIAL_LOCK),
            new Prefix("ncn", Capabilities.NS_NOTIFICATION) });

    /**
     * Children to the node, if container element.
     */
    protected NodeSet children = null;

    /**
     * The parent to this node.
     */
    protected Element parent = null;

    /**
     * Constructor that creates a new element tree. An element consists of a
     * name that belongs to a namespace.
     * 
     * @param ns Namespace
     * @param name Name of the element
     */
    public Element(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    public Element getRootElement() {
        Element top = this;
        while (top.parent != null)
            top = top.parent;
        return top;
    }

    /**
     * Static method that creates a new configuration element tree given a path.
     * A prefix mapping for the namespace will be added to the top element of
     * the created sub-tree. A prefix map is used for resolving prefix to
     * namespace mappings for a given path.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param namespace Namespace
     * @param pathStr A "path create" string
     * @return A new configuration element tree
     */
    public static Element create(String namespace, String pathStr)
            throws JNCException {
        PrefixMap prefixMap = new PrefixMap();
        prefixMap.add(new Prefix("", namespace));
        return Element.create(prefixMap, pathStr);
    }

    /**
     * Static method that creates a new configuration element tree, given a
     * path. A prefix mapping will be added to the top element of the created
     * sub-tree. A prefix map is used for resolving prefix to namespace mappings
     * for a given path.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param prefix A prefix mapping.
     * @param pathStr A "path create" string
     * @return A new configuration element tree
     */
    public static Element create(Prefix prefix, String pathStr)
            throws JNCException {
        PrefixMap prefixMap = new PrefixMap();
        prefixMap.add(prefix);
        return Element.create(prefixMap, pathStr);
    }

    /**
     * Static method that creates a new configuration element tree, given a
     * path. A prefix map will be added to the top element in the created
     * sub-tree. A prefix map is used for resolving prefix to namespace mappings
     * for a given path.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param prefixMap Prefix mappings to be added
     * @param pathStr A "path create" string
     * @return A new configuration element tree
     */
    public static Element create(PrefixMap prefixMap, String pathStr)
            throws JNCException {
        trace("create: \"" + pathStr + "\"");
        PathCreate path = new PathCreate(pathStr);
        Element t = path.eval(prefixMap);
        t.setPrefix(prefixMap);
        return t;
    }

    /**
     * Creates a new path. This is a value for mode in
     * {@link #createPath(int, String)}.
     */
    public static final int CREATE_NEW = 1;

    /**
     * Creates a new path and merges with the existing nodes when possible. This
     * is a value for mode in {@link #createPath(int, String)}.
     */
    public static final int CREATE_MERGE = 2;

    /**
     * Creates a new path and merges with the existing nodes when possible.
     * Several nodes may me matching the path, and a sub-tree will be created
     * for all of them. This is a value for mode in
     * {@link #createPath(int, String)}.
     */
    public static final int CREATE_MERGE_MULTI = 3;

    /**
     * Creates a child element to the context node.
     * 
     * @param name The name of the child element
     */
    public Element createChild(String name) {
        Element elem = new Element(this.namespace, name);
        addChild(elem);
        return elem;
    }

    /**
     * Creates a child element with specified value.
     * 
     * @param name The name of the child element
     * @param value The value of the element
     */
    public Element createChild(String name, Object value) {
        Element elem = new Element(this.namespace, name);
        elem.setValue(value);
        addChild(elem);
        return elem;
    }

    /**
     * Creates a child element with specified value.
     * 
     * @param namespace The namespace that the name belongs to
     * @param name The name of the child element
     * @param value The value of the element
     */
    public Element createChild(String namespace, String name, Object value) {
        Element elem = new Element(namespace, name);
        elem.setValue(value);
        addChild(elem);
        return elem;
    }

    /**
     * Returns the path for this element including an appended sub-path.
     * 
     * @param subPath A sub-path to be appended to the current path
     */
    public String getPath(String subPath) {
        return getPath() + "/" + subPath;
    }

    /**
     * Creates an element tree as a child to the context node from a create path
     * expression.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param pathStr A "path create" string.
     * @return A new configuration element sub-tree that is a child of the
     *         context node
     */
    public Element createPath(String pathStr) throws JNCException {
        return createPath(CREATE_MERGE, null, pathStr);
    }

    /**
     * Creates an element tree as a child to the context node from a create path
     * expression. The mode parameter is one of: {@link #CREATE_NEW},
     * {@link #CREATE_MERGE}, {@link #CREATE_MERGE_MULTI}
     * 
     * @param mode The creation mode.
     * @param pathStr A "path create" string.
     * @return A new configuration element sub-tree that is a child of the
     *         context node
     */
    public Element createPath(int mode, String pathStr) throws JNCException {
        return createPath(mode, null, pathStr);
    }

    /**
     * Creates an element tree as a child to the context node from a create path
     * expression. A prefix map containing the provided namespace will be added
     * to the context nodes prefix mapping. A prefix map is used for resolving
     * prefix to namespace mappings for a given path.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param namespace Namespace.
     * @param pathStr A "path create" string.
     * @return A new configuration element sub-tree that is a child of the
     *         context node
     */
    public Element createPath(String namespace, String pathStr)
            throws JNCException {
        PrefixMap p = new PrefixMap();
        p.add(new Prefix("", namespace));
        return createPath(CREATE_MERGE, p, pathStr);
    }

    /**
     * Creates an element tree as a child to the context node from a create path
     * expression. A prefix containing the provided prefix mapping will be added
     * to the context nodes prefix mappings. A prefix map is used for resolving
     * prefix to namespace mappings for a given path.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param prefix A prefix mapping
     * @param pathStr A "path create" string.
     * @return A new configuration element sub-tree that is a child of the
     *         context node
     */
    public Element createPath(Prefix prefix, String pathStr)
            throws JNCException {
        PrefixMap p = new PrefixMap();
        p.add(prefix);
        return createPath(CREATE_MERGE, p, pathStr);
    }

    /**
     * Creates an element tree as a child to the context node from a create path
     * expression. A prefix map containing prefix mappings will be added to the
     * context nodes prefix mappings. A prefix map is used for resolving prefix
     * to namespace mappings for a given path.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param addPrefixes Prefix mappings
     * @param pathStr A "path create" string.
     * @return A new configuration element sub-tree that is a child of the
     *         context node
     */
    public Element createPath(PrefixMap addPrefixes, String pathStr)
            throws JNCException {
        return createPath(CREATE_MERGE, addPrefixes, pathStr);
    }

    /**
     * Creates an element tree as a child to the context node. A prefix map
     * containing prefix mappings will be added to the context nodes prefix
     * mappings. A prefix map is used for resolving prefix to namespace mappings
     * for a given path.
     * <p>
     * The mode parameter is one of: {@link #CREATE_NEW}, {@link #CREATE_MERGE},
     * {@link #CREATE_MERGE_MULTI}
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param mode The creation mode.
     * @param addPrefixes Prefix mappings
     * @param pathStr A "path create" string.
     * @return A new configuration element sub-tree that is a child of the
     *         context node
     */
    public Element createPath(int mode, PrefixMap addPrefixes, String pathStr)
            throws JNCException {
        trace("createPath: \"" + pathStr + "\"");
        PathCreate path = new PathCreate(pathStr);
        if (addPrefixes != null)
            setPrefix(addPrefixes);
        if (mode == CREATE_MERGE || mode == CREATE_MERGE_MULTI) {
            /* Step down the locationsteps until we cannot go further */
            NodeSet nodeSet = new NodeSet(), deepest;
            Element first_found = null;
            nodeSet.add(this);
            deepest = nodeSet;
            int step = 0;
            int steps = path.steps();
            while (nodeSet.size() > 0 && step < steps) {
                nodeSet = ((Path) path).evalStep(nodeSet, step);
                if (nodeSet.size() > 0) {
                    deepest = nodeSet;
                    if (first_found == null)
                        first_found = nodeSet.getElement(0);
                }
                step++;
            }
            if (step == steps && nodeSet.size() > 0)
                return first_found; /* path already exist */
            if (mode == CREATE_MERGE && deepest.size() > 1)
                throw new JNCException(JNCException.PATH_CREATE_ERROR,
                        "multiple nodes found by path: \"" + pathStr + "\"");
            step--; // need to do last step again
            for (Element parent : deepest) {
                PrefixMap prefixMap = parent.getContextPrefixMap();
                for (int i = step; i < steps; i++) {
                    Element elem = path.evalStep(prefixMap, i, parent);
                    parent.addChild(elem);
                    parent = elem;
                    if (first_found == null)
                        first_found = elem;
                }
            }
            return first_found;
        } else { /* mode== CREATE_NEW */
            PrefixMap prefixMap = getContextPrefixMap();
            Element elem = path.eval(prefixMap);
            addChild(elem);
            return elem;
        }
    }

    /**
     * Sets the default prefix mapping on this node. xmlns= 'NAMESPACE' A prefix
     * mapping is used for resolving prefix to namespace mappings for a given
     * path.
     */
    public void setDefaultPrefix() {
        setPrefix(new Prefix("", namespace));
    }

    /**
     * Removes the default prefix mapping on this node (if any). xmlns=
     * 'NAMESPACE' A prefix mapping is used for resolving prefix to namespace
     * mappings for a given path.
     */
    public void removeDefaultPrefix() {
        removePrefix("");
    }

    /**
     * Sets a prefix mapping to the context node. A prefix map is used for
     * resolving prefix to namespace mappings for a given path.
     * 
     * @param prefix String prefix to be used for the namespace.
     */
    public void setPrefix(String prefix) {
        setPrefix(new Prefix(prefix, namespace));
    }

    /**
     * Sets a prefix map to the context node. A prefix map is used for resolving
     * prefix to namespace mappings for a given path.
     * 
     * @param prefix A prefix mapping
     */
    public void setPrefix(Prefix prefix) {
        setPrefix(new PrefixMap(prefix));
    }

    /**
     * Sets prefix mappings to the context node. A prefix map is used for
     * resolving prefix to namespace mappings for a given path.
     * 
     * @param prefixMap Prefix mappings
     */
    public void setPrefix(PrefixMap prefixMap) {
        if (prefixes == null)
            prefixes = new PrefixMap();
        prefixes.set(prefixMap);
    }

    /**
     * Removes a prefix map from the context node.
     */
    public void removePrefix(String prefix) {
        if (prefixes == null)
            return;
        prefixes.remove(prefix);
    }

    /* Parent and Children */

    /**
     * Returns the parent node of this node. Or <code>null</code> if none.
     * 
     * @return Parent configuration element node or <code>null</code>
     */
    public Element getParent() {
        return parent;
    }

    /**
     * Adds child to children and makes this element the parent of child.
     * 
     * @param child Child element to be added
     */
    public void addChild(Element child) {
        if (children == null)
            children = new NodeSet();

        children.add(child);
        child.parent = this;
    }

    /**
     * Inserts a child element and returns the position of (the first
     * occurrence of) the inserted child in the list of children.
     * <p>
     * Checks that child is not already in use.
     * 
     * @param child Child element to be inserted
     * @throws JNCException If child is already a child of another element or
     *                      if child equals this element
     */
    public int insertChild(Element child) throws JNCException {
        if (child.parent != null || child == this)
            throw new JNCException(JNCException.ELEMENT_ALREADY_IN_USE, this);
        addChild(child);
        return children.indexOf(child);
    }

    /**
     * Inserts a child element at a specific index in the list of children and
     * returns that index upon success.
     * 
     * @param child Child element to be inserted
     * @param index Position in child list to insert child to. 0 is the first.
     * @throws JNCException If child is already a child of another element.
     */
    public int insertChild(Element child, int index) throws JNCException {
        if (child.parent != null)
            throw new JNCException(JNCException.ELEMENT_ALREADY_IN_USE, this);

        if (children == null)
            children = new NodeSet();

        child.parent = this;
        children.add(index, child);
        return children.indexOf(child);
    }

    /**
     * Inserts a child element at the correct position by providing structure
     * information (the names of all the children, in order).
     * 
     * @param child Child element to be inserted
     * @param childrenNames The names of all children in order.
     * @throws JNCException If child is already a child of another element.
     */
    public int insertChild(Element child, String[] childrenNames)
            throws JNCException {
        if (child.parent != null)
            throw new JNCException(JNCException.ELEMENT_ALREADY_IN_USE, this);

        if (children == null)
            children = new NodeSet();

        child.parent = this;

        int pos = 0;
        int i = 0;

        while (true)
            if (pos == children.size())
                break;
            else if (children.getElement(pos).name.compareTo(childrenNames[i]) == 0)
                ++pos;
            else if (child.name.compareTo(childrenNames[i]) == 0)
                break;
            else
                ++i;

        children.add(pos, child);
        return pos;
    }

    /**
     * Inserts a child element first in the list of children. Always returns 0.
     * 
     * @param child Child element to be inserted
     * @throws JNCException If child is already a child of another element.
     */
    public int insertFirst(Element child) throws JNCException {
        return insertChild(child, 0);
    }

    /**
     * Inserts a child element last in the list of children. Returns the
     * position of the inserted child.
     * 
     * @param child Child element to be inserted
     * @throws JNCException If child is already a child of another element or
     *                      if child equals this element
     */
    public int insertLast(Element child) throws JNCException {
        return insertChild(child);
    }

    /**
     * Returns the position of this element in the the parent child list. '0' is
     * the first position.
     */
    public int position() {
        return (parent == null) ? -1 : parent.children.indexOf(this);
    }

    /**
     * Deletes child node(s). All children matching the path string will be
     * deleted. An array of the deleted children is returned.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string for children that will be deleted
     * @return An array of the deleted element nodes.
     */
    public NodeSet delete(String pathStr) throws JNCException {
        NodeSet nodes = get(pathStr);

        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                nodes.getElement(i).delete();
            }
        }
        return nodes;
    }

    /**
     * Deletes this node. Means that the parent will no longer have reference to
     * us. This node will be removed from our parents child list. This method
     * will not do anything if this node is the root node of a tree.
     */
    public void delete() {
        if (parent != null)
            parent.deleteChild(this);
    }

    /**
     * Deletes a child node, provided it is present in the children list.
     * 
     * @param child Child to delete
     */
    public void deleteChild(Element child) {
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            if (child == children.getElement(i)) {
                children.remove(i);
                child.parent = null;
                break;
            }
        }
    }

    /**
     * Returns <code>true</code> if this node has any children,
     * <code>false</code> otherwise.
     * 
     * @return <code>true</code> or <code>false</code>
     */
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    /* Attibutes */

    /**
     * Adds an attribute for this element.
     * 
     */
    public void addAttr(Attribute attr) {
        if (attrs == null) {
            attrs = new ArrayList<Attribute>();
        }
        attrs.add(attr);
    }

    /**
     * Gets all attributes for this element.
     * 
     * @return An array of configuration attributes or <code>null</code>
     */
    public Attribute[] getAttrs() {
        if (attrs != null) {
            return (Attribute[]) attrs.toArray(new Attribute[attrs.size()]);
        }
        return null;
    }

    /**
     * Returns the string value of the named attribute. Returns
     * <code>null</code> if no such attribute is found or "" if no value is
     * given to the attribute.
     * 
     * @param name The name of the attribute
     * @return String value of the attribute.
     */
    public String getAttrValue(String name) {
        return getAttr(name).getValue();
    }

    /**
     * Gets an Attribute
     * 
     * @param name Lookup using the name of attribute
     */
    public Attribute getAttr(String name) {
        if (attrs != null) {
            for (Attribute attr : attrs) {
                if (attr.name.equals(name)) {
                    return attr;
                }
            }
        }
        return null; // not found
    }

    /**
     * Sets an attribute on this element, treating xmlns attributes as prefix
     * maps. If the attribute already exists, its value is changed, otherwise
     * the attribute is added.
     * 
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @return The configuration attribute.
     */
    public Attribute setAttr(String name, String value) {
        trace("setAttr: " + name + "=\"" + value + "\"");
        if (name.equals("xmlns")) {
            // it's an xmlns attribute - treat this as a prefix map
            Prefix p = new Prefix("", value);
            setPrefix(p);
            return p;
        } else if (name.startsWith("xmlns:")) {
            String prefix = name.substring(6);
            Prefix p = new Prefix(prefix, value);
            setPrefix(p);
            return p;
        } else if (attrs == null) {
            attrs = new ArrayList<Attribute>();
        } else {
            for (Attribute attr : attrs) {
                if (attr.name.equals(name)) {
                    attr.setValue(value);
                    return attr;
                }
            }
        }
        Attribute attr = new Attribute(namespace, name, value);
        addAttr(attr);
        return attr;
    }

    /**
     * Sets an attribute on this element, aware of its namespace. If an
     * attribute with this name and ns already exists, its value is changed.
     * Otherwise such an attribute is added.
     * <p>
     * If name starts with xmlns and ns starts with the xmlns namespace
     * (http://www.w3.org/2000/xmlns/), the value is set as a prefix map.
     * 
     * @param ns The namespace that the attribute name belongs to
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @return The configuration attribute.
     */
    public Attribute setAttr(String ns, String name, String value) {
        trace("setAttr: (" + ns + ") " + name + "=\"" + value + "\"");
        if (name.startsWith("xmlns") && ns.startsWith(Prefix.XMLNS_NAMESPACE))
            return setAttr(name, value);

        if (attrs == null) {
            attrs = new ArrayList<Attribute>();
        } else {
            for (Attribute attr : attrs) {
                if (attr.ns.equals(ns) && attr.name.equals(name)) {
                    // Change existing attribute
                    attr.setValue(value);
                    return attr;
                }
            }
        }

        // Add new one
        Attribute attr = new Attribute(ns, name, value);
        addAttr(attr);
        return attr;
    }

    /**
     * Removes an attribute with specified name. This method does not consider
     * namespace so note that it will remove the first attribute which matches
     * the name (and no other).
     * 
     * @param name The name of the attribute to be removed.
     */
    public void removeAttr(String name) {
        if (attrs != null) {
            for (int i = 0; i < attrs.size(); i++) {
                Attribute attr = (Attribute) attrs.get(i);
                if (attr.name.equals(name)) {
                    trace("removeAttr: " + name);
                    attrs.remove(i);
                    return;
                }
            }
        }
    }

    /**
     * Removes an attribute with specified namespace and name from this
     * element's attribute list.
     * 
     * @param namespace the namespace the name belongs to.
     * @param name The name of the attribute to be removed.
     */
    public void removeAttr(String namespace, String name) {
        if (attrs != null) {
            for (int i = 0; i < attrs.size(); i++) {
                Attribute attr = (Attribute) attrs.get(i);
                if (attr.name.equals(name) && attr.ns.equals(namespace)) {
                    trace("removeAttr: (" + namespace + ") " + name);
                    attrs.remove(i);
                }
            }
        }
    }

    /* Values */

    /**
     * Finds the value of child with specified name, if it exists.
     * @param childName Name of child
     * @return Value of child, or null if none
     */
    public Object getValueOfChild(String childName) {
        for (Element child : children) {
            if (child.name.equals(childName)) {
                return child.getValue();
            }
        }
        return null;
    }

    /**
     * Returns the value of this element.
     * 
     * @return The value of the element.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Check if any nodes in this element matches a given path string.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr The path to match
     * @return <code>true</code> if any node matches pathStr;
     *         <code>false</code> otherwise.
     */
    public boolean exists(String pathStr) throws JNCException {
        NodeSet nodes = get(pathStr);
        return (nodes != null && !nodes.isEmpty());
    }

    /**
     * Returns the value of a the first subnode matching a given path string,
     * or null if there are no matches.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find node
     * @return The value of the (first) found element or <code>null</code>
     */
    public Object getValue(String pathStr) throws JNCException {
        NodeSet nodes = get(pathStr);
        // Don't call exists, to avoid computing matches twice
        return (nodes != null && !nodes.isEmpty()) ?
                nodes.getElement(0).getValue() : null;
    }

    /**
     * Returns the value(s) of nodes in a given path expression.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     * @return An array of the values of the element nodes found by the
     *         expression (or <code>null</code>)
     */
    public Object[] getValues(String pathStr) throws JNCException {
        NodeSet nodes = get(pathStr);
        Object[] values = null;
        if (nodes != null) {
            values = new String[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                values[i] = nodes.getElement(i).getValue();
            }
        }
        return values;
    }

    /**
     * Returns the values of nodes in a given path expression.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     * @return A set with the values of the element nodes found by the
     *         expression (or <code>null</code>)
     */
    public Set<String> getValuesAsSet(String pathStr) throws JNCException {
        String[] valuesBefore = (String[]) getValues(pathStr);
        return new HashSet<String>(Arrays.asList(valuesBefore));
    }

    /**
     * Sets a new value for this node element.
     * 
     * @param value Value to be set
     */
    public void setValue(Object value) {
        trace("setValue: " + name + "=\"" + value + "\"");
        this.value = value;
    }

    /**
     * Sets value for all node elements matching specified path string.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     * @param value Value to be set
     */
    public void setValue(String pathStr, Object value) throws JNCException {
        for (Element elem : get(pathStr)) {
            elem.setValue(value);
        }
    }

    /**
     * Deletes value of node(s)
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     */
    public void deleteValue(String pathStr) throws JNCException {
        for (Element elem : get(pathStr)) {
            elem.deleteValue();
        }
    }

    /**
     * Deletes the value for this node.
     */
    public void deleteValue() {
        value = null;
    }

    /* Get */

    /**
     * Returns first node that matches the path expression, or
     * <code>null</code> if no such node was found.
     * <p>
     * Example:
     * 
     * <pre>
     *      Element full = NetconfSession:getConfig();
     * 
     *      Element first_host = full.getFirst("/hosts/host");
     *      Element last_host = full.getLast("/hosts/host");
     * </pre>
     * 
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     * @return The first element node found by the expression.
     */
    public Element getFirst(String pathStr) throws JNCException {
        NodeSet nodeSet = get(pathStr);
        if (nodeSet == null || nodeSet.isEmpty()) {
            return null;
        }
        return nodeSet.getElement(0);
    }

    /**
     * Returns the last node that matches the path expression, or
     * <code>null</code> if no such node was found.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     * @return The last element node found by the expression.
     */
    public Element getLast(String pathStr) throws JNCException {
        NodeSet nodeSet = get(pathStr);
        if (nodeSet == null || nodeSet.isEmpty())
            return null;
        return (Element) nodeSet.get(nodeSet.size() - 1);
    }

    /**
     * Gets all nodes matching a given path expression.
     * <p>
     * Example:
     * 
     * <pre>
     *      Element full_config = session.get();
     *      NodeSet calle_nodes = full_config.get("host[www='Calle']");
     * </pre>
     * 
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     * @return An array of the element nodes found by the expression.
     */
    public NodeSet get(String pathStr) throws JNCException {
        Path path = new Path(pathStr);
        return path.eval(this);
    }

    /**
     * Returns the children of this node.
     * 
     * @return The children node set of this node or <code>null</code>
     */
    public NodeSet getChildren() {
        return children;
    }

    /**
     * Get the children with specified name, from children list
     * 
     * @param name Name of child
     * @return a NodeSet with all chldren that has the name
     */
    public NodeSet getChildren(String name) {
        NodeSet n = new NodeSet();
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                Element elem = children.getElement(i);
                if (elem.name.equals(name))
                    n.add(elem);
            }
        }
        return n;
    }

    /**
     * Get the (first) child with specified name, from children list
     * 
     * @param name Name of child
     * @return The found element or <code>null</code>
     */
    public Element getChild(String name) {
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                Element elem = children.getElement(i);
                if (elem.name.equals(name))
                    return elem;
            }
        }
        return null;
    }

    /**
     * Clones the tree, making an exact copy. The entire tree is treated as if
     * it was created.
     * 
     * @return A copy of the element sub-tree.
     */
    @Override
    public Object clone() {
        Element copy = new Element(namespace, name);
        // copy all children
        if (children != null) {
            if (copy.children == null)
                copy.children = new NodeSet();

            for (int i = 0; i < children.size(); i++) {
                Element child = children.getElement(i);
                Element child_copy = (Element) child.clone();
                copy.addChild(child_copy);
            }
        }

        cloneAttrs(copy);
        cloneValue(copy);
        return copy;
    }

    /**
     * Tries to find an element with the same namespace and name as x.
     * 
     * @param child Element to compare against
     * @return the matching element if it exists; <code>null</code> otherwise.
     */
    protected Element getChild(Element child) {
        if (children != null) {
            for (Element other : children) {
                if (child.compare(other) >= 0)
                    return other;
            }
        }
        return null;
    }

    /**
     * Clones the tree, making an exact copy. Does only clone this level. not
     * the children.
     * 
     * @return A copy of the shallow element sub-tree.
     */
    protected Element cloneShallow() {
        Element copy = new Element(namespace, name);
        cloneAttrs(copy);
        cloneValue(copy);
        return copy;
    }

    /**
     * Operation flag to be used with {@link #merge(Element,int)}.
     */
    public final static int OP_CREATE = 1;
    /**
     * Operation flag to be used with {@link #merge(Element,int)}.
     */
    public final static int OP_DELETE = 2;
    /**
     * Operation flag to be used with {@link #merge(Element,int)}.
     */
    public final static int OP_REPLACE = 3;

    /**
     * Operation flag to be used with {@link #merge(Element,int)}.
     */
    public final static int OP_MERGE = 4;

    /**
     * Merges a subtree into a resulting target subtree. The 'op' parameter
     * controls how the nodes that are added in the target subtree should be
     * marked. Either OP_CREATE, OP_DELETE, OP_MERGE or OP_REPLACE.
     * 
     * @param root Target subtree. Must start from root node.
     * @param op One of {@link #OP_CREATE}, {@link #OP_DELETE},
     *            {@link #OP_MERGE} or {@link #OP_REPLACE}
     * @return Resulting target subtrees in NodeSet
     * 
     */
    public Element merge(Element root, int op) throws JNCException {

        // make list of nodes down to this node from root
        NodeSet list = new NodeSet();
        Element a = this;
        while (a != null) {
            list.add(0, a);
            a = a.parent;
        }

        // pop first element and assert that it's the same as root
        Element x = list.getElement(0);
        list.remove(0);

        if (root == null) {
            if (list.size() >= 1) {
                root = x.cloneShallow();
            } else {
                // special case. only one path in root. Differ already
                if (op == OP_CREATE) {
                    Element x1 = (Element) x.clone();
                    x1.markCreate();
                    return x1;
                } else if (op == OP_DELETE) {
                    Element x1 = x.cloneShallow();
                    x1.markDelete();
                    return x1;
                } else if (op == OP_REPLACE) {
                    Element x1 = (Element) x.clone();
                    x1.markReplace();
                    return x1;
                } else if (op == OP_MERGE) {
                    Element x1 = (Element) x.clone();
                    x1.markMerge();
                    return x1;
                }
            }
        }

        if (x.compare(root) == -1) {
            System.err.println(" x= " + x);
            System.err.println(" root= " + root);
            System.err.println(" compare: " + x.compare(root));

            throw new JNCException(JNCException.ELEMENT_MISSING, x.getPath()
                    + ", " + root.getPath());
        }
        // same now, go down
        Element parent = root;
        while (list.size() > 1) {
            // pop first element from list
            x = list.getElement(0);
            list.remove(0);

            Element child = parent.getChild(x);
            if (child == null) {
                // need to create it
                child = x.cloneShallow();
                parent.addChild(child);
            }
            // go down
            parent = child;
        }
        // last one
        if (list.size() > 0) {
            x = list.getElement(0);
            // list.remove(0); // no need
            if (op == OP_CREATE) {
                // we know it's unique
                Element x1 = (Element) x.clone();
                x1.markCreate();
                parent.addChild(x1);
            } else if (op == OP_DELETE) {
                // we know it's unique, cut all children except keys
                Element x1 = x.cloneShallow();
                x1.markDelete();
                parent.addChild(x1);
            } else if (op == OP_MERGE) {
                Element x1 = (Element) x.clone();
                x1.markMerge();
                parent.addChild(x1);

            } else { // OP_REPLACE
                Element x1 = (Element) x.clone();

                x1.markReplace();
                parent.addChild(x1);
            }
        }
        // done
        return root;
    }

    /**
     * clones the attributes to the target copy. Note: help method to Containers
     * clone and clone above.
     * 
     * @param copy The target copy to clone the attributes to
     */
    protected Element cloneAttrs(Element copy) {
        // copy attrs
        if (attrs != null) {
            copy.attrs = new ArrayList<Attribute>();
            for (int i = 0; i < attrs.size(); i++) {
                Attribute attr = (Attribute) attrs.get(i);
                Attribute copy_attr = (Attribute) attr.clone();
                copy.attrs.add(copy_attr);
            }
        }
        // copy xmlns attrs
        if (prefixes != null)
            copy.prefixes = (PrefixMap) prefixes.clone();
        return copy;
    }

    /**
     * clones the value to the target copy. Note: help method to Containers
     * clone and clone above.
     * 
     * @param copy The target copy to clone the value field to
     */
    protected Element cloneValue(Element copy) {
        if (value instanceof YangBaseType<?>) {
            copy.value = ((YangBaseType<?>) value).clone();
        } else if (value != null) {
            copy.value = value;
        }
        return copy;
    }

    /* Mark operations. Uses the nc:operations attribute */

    /**
     * Removes the operation attribute from a node. This is eqvivalent to:
     * <code>removeAttr(Element.NETCONF_NAMESPACE,"operation");</code> see @removeAttr
     */
    public void removeMark() {
        removeAttr(NETCONF_NAMESPACE, "operation");
    }

    /**
     * Removes all operation attributes from a sub-tree.
     * 
     */
    public void removeMarks() {
        removeMark();
        if (children != null)
            for (int i = 0; i < children.size(); i++)
                children.getElement(i).removeMarks();
    }

    /**
     * Marks a node with operation delete.
     */
    public void markDelete() {
        setAttr(NETCONF_NAMESPACE, "operation", "delete");
    }

    /**
     * Marks node(s) with operation delete.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     */
    public void markDelete(String pathStr) throws JNCException {
        Path path = new Path(pathStr);
        NodeSet nodeSet = path.eval(this);
        if (nodeSet != null)
            for (int i = 0; i < nodeSet.size(); i++)
                nodeSet.getElement(i).markDelete();
    }

    /**
     * Marks a node with operation replace.
     */
    public void markReplace() {
        setAttr(NETCONF_NAMESPACE, "operation", "replace");
    }

    /**
     * Marks node(s) with operation replace.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     */
    public void markReplace(String pathStr) throws JNCException {
        Path path = new Path(pathStr);
        NodeSet nodeSet = path.eval(this);
        if (nodeSet != null)
            for (int i = 0; i < nodeSet.size(); i++)
                nodeSet.getElement(i).markReplace();
    }

    /**
     * Marks a node with operation merge.
     */
    public void markMerge() {
        setAttr(NETCONF_NAMESPACE, "operation", "merge");
    }

    /**
     * Marks node(s) with operation merge.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     */
    public void markMerge(String pathStr) throws JNCException {
        Path path = new Path(pathStr);
        NodeSet nodeSet = path.eval(this);
        if (nodeSet != null)
            for (int i = 0; i < nodeSet.size(); i++)
                nodeSet.getElement(i).markMerge();
    }

    /**
     * Marks a node with operation create
     */
    public void markCreate() {
        setAttr(NETCONF_NAMESPACE, "operation", "create");
    }

    /**
     * Marks node(s) with operation create.
     * <p>
     * See {@link Path} for more information about path expressions.
     * 
     * @param pathStr Path string to find nodes
     */
    public void markCreate(String pathStr) throws JNCException {
        Path path = new Path(pathStr);
        NodeSet nodeSet = path.eval(this);
        if (nodeSet != null)
            for (int i = 0; i < nodeSet.size(); i++)
                nodeSet.getElement(i).markCreate();
    }

    /* Info methods */

    /**
     * A qualified name is a prefixed name. This method will find the prefix of
     * this elements namespace and build a name in the format: "prefix:name". If
     * no prefix is found the prefix "unknown" will be used.
     * 
     * @return The qualified name of the element.
     */
    public String qualifiedName() {
        String qName;
        String prefix = prefix();

        if (prefix == null)
            prefix = "unknown";

        if (prefix.equals(""))
            qName = name;
        else
            qName = prefix + ":" + name;

        return qName;
    }

    /**
     * Returns the prefix name of this element.
     * 
     * @return The prefix name that the namespace of this element is bound to
     */
    public String prefix() {
        return nsToPrefix(namespace);
    }

    /**
     * Returns a prefix map, as it is in the current context. The prefix map is
     * built up by traversing the parents.
     * 
     * @return The prefix mappings available at this context node
     */
    public PrefixMap getContextPrefixMap() {
        PrefixMap p = new PrefixMap();
        Element node = this;
        while (node != null) {
            if (node.prefixes != null)
                p.merge(node.prefixes);
            node = node.parent;
        }
        // merge in the default prefixes as well
        if (defaultPrefixes != null)
            p.merge(defaultPrefixes);
        return p;
    }

    /**
     * Lookups a prefix and returns the associated namespace, traverses up the
     * parent links until the prefix is found. Returns <code>null</code> if the
     * prefix is not found.
     * 
     * @param prefix Prefix string to lookup.
     * @return The namespace of the specified prefix in the context of this
     *         node.
     */
    public String lookupContextPrefix(String prefix) {
        Element node = this;
        while (node != null) {
            if (node.prefixes != null) {
                String ns = node.prefixes.prefixToNs(prefix);
                if (ns != null)
                    return ns;
            }
            node = node.parent;
        }
        // as a last resort use the default prefix map.
        if (defaultPrefixes != null)
            return defaultPrefixes.prefixToNs(prefix);
        return null;
    }

    /**
     * This method will find the prefix of a specified namespace, from the given
     * context node. If no prefix is found <code>null</code> will be returned.
     * 
     * @param ns Namespace string to lookup
     * @return The prefix string of the given namespace at the context node.
     */
    public String nsToPrefix(String ns) {
        Element top = this;
        String prefix = null;

        while (prefix == null && top != null) {
            if (top.prefixes != null)
                prefix = top.prefixes.nsToPrefix(ns);

            top = top.parent;
        }

        if (prefix != null)
            return prefix;
        // as a last resort use the default prefix map
        if (defaultPrefixes != null)
            return defaultPrefixes.nsToPrefix(ns);
        return null;
    }

    /**
     * Returns the path as a string
     * 
     * @return The path of element
     */
    public String getPath() {
        // trace("getPath()");
        Element top = this;
        String s = null;
        while (top != null) {
            if (top.namespace == null)
                s = strConcat(new String(top.name), s);
            else { // top.namespace!=null
                if (top.parent != null && top.parent.namespace != null
                        && top.namespace.equals(top.parent.namespace)) {
                    s = strConcat(new String(top.name), s);
                } else
                    s = strConcat(top.qualifiedName(), s);
            }
            top = top.parent;
        }
        return "/" + s;
    }

    private String strConcat(String s1, String s2) {
        if (s2 != null)
            return s1 + "/" + s2;
        return s1;
    }

    /**
     * Compare if equal to another Object. This method does not compare
     * attributes and children. It will only compare:
     * <ul>
     * <li>name
     * <li>namespace
     * <li>value
     * </ul>
     * 
     * @param other Object to compare this element against.
     * @return <code>true</code> if other is an Element with same name,
     *         namespace and value; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Element) {
            Element b = (Element) other;
            boolean sameName = this.name.equals(b.name);
            boolean sameNamespace = this.namespace.equals(b.namespace);
            if (sameName && sameNamespace) {
                if (this.value != null) {
                    return this.value.equals(b.value);
                } else {
                    return b.value == null;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns a hash code value for this object.
     * 
     * @return The sum of the hash codes of the name, namespace and value of
     *         this element subtree node.
     */
    @Override
    public int hashCode() {
        return (this.name.hashCode() +
                this.namespace.hashCode() +
                this.value.hashCode());
    }

    /**
     * Compare two elements. Compares the name, namespace, and value. Returns:
     * 
     * <ul>
     * <li>-1 - if the two containers keys are not equal, which means that they
     * are completely different.
     * <li>0 - if the two elements are equal in name, namespace, and value.
     * <li>1 - the two containers are the same except the value.
     * </ul>
     * 
     * @param b Element to compare this element against.
     */
    public int compare(Element b) {
        boolean sameName = this.name.equals(b.name);
        boolean sameNamespace = this.namespace.equals(b.namespace);
        if (sameName && sameNamespace) {
            return this.equals(b) ? 1 : 0;
        }
        return -1;
    }

    /**
     * Builds a string with the name, value, namespace, prefixes, other
     * attributes, children and the path of this element subtree.
     * 
     * @return String representation of this element
     */
    @Override
    public String toString() {
        StringBuffer s_attrs = new StringBuffer();
        StringBuffer s_children = new StringBuffer();
        StringBuffer res = new StringBuffer();

        res.append("Element{name=" + name);
        if (value != null) {
            res.append(", value=" + value);
        }
        res.append(", ns=" + namespace);
        
        // Attributes
        if (prefixes != null) {
            for (Prefix p : prefixes) {
                s_attrs.append(p.toXMLString());
                s_attrs.append(", ");
            }
        }
        if (attrs != null) {
            for (Attribute a : attrs) {
                s_attrs.append(a.toXMLString(this));
                s_attrs.append(", ");
            }
        }
        if (s_attrs.length() >= ", ".length()) {
            s_attrs.delete(s_attrs.length()-3, s_attrs.length());
            res.append(", attrs=[" + s_attrs);
        }
        
        // Children
        if (children != null) {
            for (Element child : children) {
                s_children.append(child.name);
                s_children.append(", ");
            }
        }
        if (s_children.length() >= ", ".length()) {
            s_children.delete(s_children.length()-3, s_children.length());
            res.append("], children=[" + s_children);
        }
        
        res.append("], path=" + getPath() + "}");
        return res.toString();
    }

    /**
     * This will format the tree as an XML string, which can be printed. The
     * XML code is nicely indented.
     * 
     * @return This element sub-tree represented as an XML string
     */
    public String toXMLString() {
        StringBuffer s = new StringBuffer();
        toXMLString(0, s);
        return s.toString();
    }

    private void toXMLString(int indent, StringBuffer s) {
        boolean flag = hasChildren();
        String qName = qualifiedName();
        s.append(String.format("%" + (indent * 2) + "s", "") + "<" + qName);
        // add xmlns attributes (prefixes)
        if (prefixes != null) {
            for (Prefix p : prefixes) {
                s.append(" " + p.toXMLString());
            }
        }
        // add attributes
        if (attrs != null) {
            for (Attribute attr : attrs) {
                s.append(" " + attr.toXMLString(this));
            }
        }
        s.append(">" + (flag ? "\n" : ""));
        indent++;
        // add children elements if any
        if (flag) {
            for (Element child : children) {
                child.toXMLString(indent, s);
            }
        } else { // add value if any
            if (value != null) {
                String stringValue = value.toString().replaceAll("&", "&amp;");
                s.append(getIndentationSpacing(flag, indent));
                s.append(stringValue + (flag ? "\n" : ""));
            }
        }
        indent--;
        s.append(getIndentationSpacing(flag, indent) + "</" + qName + ">\n");
    }

    /**
     * Gets indentation spacing for any given indent level.
     * 
     * @param shouldIndent Whether or not there should be any indentation.
     * @param indent The indentation level.
     * @return A string with indent * 2 number of spaces if shouldIndent is
     *         <code>true</code>; otherwise an empty string.
     */
    private String getIndentationSpacing(boolean shouldIndent, int indent) {
        return shouldIndent ? String.format("%" + (indent * 2) + "s", "") : "";
    }

    /**
     * Encode to XML and send it to the provided stream. Similar to the
     * toXMLString(), but without the pretty printing.
     * <p>
     * Equivalent to calling encode(out, <code>true</code>, <code>null</code>);
     * 
     * @param out Stream to send the encoded version of this element to.
     * @throws JNCException If a YangElement encode implementation fails
     */
    protected void encode(Transport out) throws JNCException {
        encode(out, true, null);
    }

    /**
     * Encode to XML and send it to the provided stream. Similar to the
     * toXMLString(), but without the pretty printing.
     * <p>
     * Equivalent to calling encode(out, <code>true</code>, c);
     * 
     * @param out Stream to send the encoded version of this element to.
     * @param c Capabilities, used by YangElement instances.
     * @throws JNCException If a YangElement encode implementation fails.
     */
    protected void encode(Transport out, Capabilities c) throws JNCException {
        encode(out, true, c);
    }

    /**
     * Encode to XML and send it to the provided stream. Similar to the
     * toXMLString(), but without the pretty printing.
     * <p>
     * The newline_at_end argument controls whether a newline char is permitted
     * at the end or not.
     * <p>
     * Equivalent to calling encode(out, newline_at_end, <code>null</code>);
     * 
     * @param out Stream to send the encoded version of this element to.
     * @param newline_at_end If 'true' a newline is printed at the end.
     * @throws JNCException If a YangElement encode implementation fails.
     */
    protected void encode(Transport out, boolean newline_at_end)
            throws JNCException {
        encode(out, newline_at_end, null);
    }

    /**
     * Encode to XML and send it to the provided stream. Similar to the
     * toXMLString(), but without the pretty printing.
     * <p>
     * The newline_at_end argument controls whether a newline char is permitted
     * at the end or not.
     * 
     * @param out Stream to send the encoded version of this element to.
     * @param newline_at_end If 'true' a newline is printed at the end.
     * @param c Capabilities, used by YangElement instances.
     * @throws JNCException If a YangElement encode implementation fails.
     */
    protected void encode(Transport out, boolean newline_at_end,
            Capabilities capas) throws JNCException {
        String qName = qualifiedName();
        out.print("<" + qName);
        // add xmlns attributes (prefixes)
        if (prefixes != null) {
            for (Prefix p : prefixes) {
                out.print(" ");
                p.encode(out);
            }
        }
        // add attributes
        if (attrs != null) {
            for (Attribute attr : attrs) {
                out.print(" ");
                attr.encode(out, this);
            }
        }
        out.print(">");
        if (hasChildren()) {
            // add children elements if any
            out.println("");
            for (Element child : children) {
                child.encode(out, true, capas);
            }
        } else if (value != null) {
            // otherwise, add value (if any)
            out.print(value.toString());
        }
        out.print("</" + qName + ">" + (newline_at_end ? "\n" : ""));
    }

    /**
     * Return the full tagpath for this Element
     * @return Absolute tagpath to this node
     */
    public Tagpath tagpath() {
        Element e = this;
        int ix = 0;
        while (e != null) {
            e = e.parent;
            ix++;
        }
        Tagpath tp = new Tagpath(ix);
        e = this;
        while (e != null) {
            tp.p[--ix] = e.name;
            e = e.parent;
        }
        return tp;
    }

    /**
     * Return an iterator for the children of this node.
     * @return A new iterator over this element's children
     */
    public ElementChildrenIterator iterator() {
        return new ElementChildrenIterator(children);
    }

    /**
     * Return an iterator for the children of a specified name of this node.
     * <p>
     * Example usage:
     * 
     * <pre>
     * ElementChildrenIterator hostIter = config.iterator(&quot;host&quot;);
     * while (hostIter.hasNext()) {
     *     Element host = hostIter.next();
     *     System.out.println(&quot;Host: &quot; + host);
     * }
     * </pre>
     * 
     * @param name A filter name, return only children with this name.
     * @return A new iterator over this element's children with specified name
     */
    public ElementChildrenIterator iterator(String name) {
        return new ElementChildrenIterator(children, name);
    }

    /**
     * Write the configuration tree to a file. The configuration tree is written
     * as XML text.
     * 
     * @param filename File name.
     * @see #readFile(String)
     */
    public void writeFile(String filename) throws IOException {
        FileOutputStream fos;
        DataOutputStream dos;
        File file = new File(filename);
        fos = new FileOutputStream(file);
        dos = new DataOutputStream(fos);
        dos.writeBytes(toXMLString());
        fos.close();
    }

    /**
     * Read file with XML text and parse it into a configuration tree.
     * 
     * @param filename File name.
     * @see #writeFile(String)
     */
    public static Element readFile(String filename) throws JNCException {

        XMLParser p = new XMLParser();
        return p.readFile(filename);
    }

    /* help functions */

    /**
     * Sets the debug level. 0 - no debug 1 - Element level: Element, Attribute
     * 2 - Session level: 3 - Parser level: Path, PathCreate, LocationStep, Expr
     * 4 - Other: Prefix, PrefixMap
     */
    static final int DEBUG_LEVEL_ELEMENT = 1;
    static final int DEBUG_LEVEL_ATTRIBUTE = 1;
    static final int DEBUG_LEVEL_SESSION = 2;
    static final int DEBUG_LEVEL_TRANSPORT = 2;
    static final int DEBUG_LEVEL_PATH = 3;
    static final int DEBUG_LEVEL_PATHCREATE = 3;
    static final int DEBUG_LEVEL_EXPR = 3;
    static final int DEBUG_LEVEL_LOCATIONSTEP = 3;
    static final int DEBUG_LEVEL_PARSER = 3;
    static final int DEBUG_LEVEL_PREFIX = 4;
    static final int DEBUG_LEVEL_PREFIXMAP = 4;

    public static void setDebugLevel(int level) {
        debugLevel = level;
    }

    static int debugLevel = 0;

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (debugLevel >= DEBUG_LEVEL_ELEMENT)
            System.err.println("*Element: " + s);
    }
}

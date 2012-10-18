package com.tailf.jnc;

import java.util.HashMap;
import java.util.Set;

/**
 * The SchemaTree class is used to represent the schemas of all namespaces
 */
public class SchemaTree {

    private static HashMap<String, HashMap<Tagpath, SchemaNode>> namespaces = new HashMap<String, HashMap<Tagpath, SchemaNode>>();

    /**
     * If no hashmap exists for namespace, it is created. Used by generated
     * code to populate new hashmaps for YANG modules.
     * 
     * @param namespace The namespace of the module as a String.
     * @return The HashMap associated with namespace.
     */
    public static HashMap<Tagpath, SchemaNode> create(String namespace) {
        if (namespaces.containsKey(namespace)) {
            return namespaces.get(namespace);
        }
        final HashMap<Tagpath, SchemaNode> h = new HashMap<Tagpath, SchemaNode>();
        namespaces.put(namespace, h);
        return h;
    }

    /**
     * @param namespace A YANG module namespace as a String
     * @return The HashMap associated with namespace, or null.
     */
    public static HashMap<Tagpath, SchemaNode> getHashMap(String namespace) {
        return namespaces.get(namespace);
    }

    /**
     * @return The set of all namespaces for which there currently is a HashMap
     *         of TagPath/SchemaNode key/value pairs.
     */
    public static Set<String> getLoadedNamespaces() {
        return namespaces.keySet();
    }

    /**
     * Searches for a SchemaNode given a namespace and a Tagpath.
     * 
     * @param namespace The namespace of the module.
     * @param tp The TagPath of the node to search for.
     * @return The SchemaNode with Tagpath tp in module with specified
     *         namespace, or null if not found.
     */
    public static SchemaNode lookup(String namespace, Tagpath tp) {
        final HashMap<Tagpath, SchemaNode> t = getHashMap(namespace);
        return t == null ? null : t.get(tp);
    }

}
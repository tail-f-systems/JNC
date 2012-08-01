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

package com.tailf.netconfmanager.yang;

import java.util.*;

import com.tailf.netconfmanager.Tagpath;

/**
 * The SchemaTree class is used to represent the schemas of all namespaces
 */
public class SchemaTree {

    private static HashMap<String, HashMap<Tagpath, SchemaNode>> namespaces =
               new HashMap<String, HashMap<Tagpath, SchemaNode>>();

    /*
     * This static method is used by the generated code to populate a new
     * hashmap for a module
     */
    public static HashMap<Tagpath, SchemaNode> create(String namespace) {
        HashMap<Tagpath, SchemaNode> h;
        if ((h = getHashMap(namespace)) != null) {
            return h;
        }
        h = new HashMap<Tagpath, SchemaNode>();
        namespaces.put(namespace, h);
        return h;
    }

    /*
     * Return a hashmap for a given namespace
     */
    public static HashMap<Tagpath, SchemaNode> getHashMap(String namespace) {
        return namespaces.get(namespace);
    }

    /*
     * Return a set of all loaded namespaces
     */
    public static Set<String> getLoadedNamespaces() {
        return namespaces.keySet();
    }

    /*
     * Find the SchemaNode for e specific schema entry returns null if not found
     */
    public static SchemaNode lookup(String namespace, Tagpath tp) {
        HashMap<Tagpath, SchemaNode> t = getHashMap(namespace);
        return t == null ? null : t.get(tp);
    }

}
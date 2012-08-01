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

    private static ArrayList<Hashtable<Tagpath, SchemaNode>> namespaces =
            new ArrayList<Hashtable<Tagpath, SchemaNode>>();
    static {
        for(int i=0; i<32; i++) {
            namespaces.add(new Hashtable<Tagpath, SchemaNode>());
        }
    }
    private static String nsnames[] = new String[32];
    private static int size = 0;

    /*
     * This static method is used by the generated code to populate a new
     * hashtable for a module
     */
    public static Hashtable<Tagpath, SchemaNode> create(String namespace) {
        Hashtable<Tagpath, SchemaNode> h;
        if ((h = getHashtable(namespace)) != null)
            return h;
        ;
        h = new Hashtable<Tagpath, SchemaNode>();
        namespaces.set(size, h);
        nsnames[size++] = namespace;
        return h;
    }

    /*
     * Return a hash table for a given namespace
     */
    public static Hashtable<Tagpath, SchemaNode> getHashtable(String namespace) {
        for (int i = 0; i < size; i++) {
            if (nsnames[i].compareTo(namespace) == 0)
                return namespaces.get(i);
        }
        return null;
    }

    /*
     * Return an array of all loaded namespaces
     */
    public static String[] getLoadedNamespaces() {
        return nsnames;
    }

    /*
     * Find the SchemaNode for e specific schema entry returns null if not found
     */
    public static SchemaNode lookup(String namespace, Tagpath tp) {
        Hashtable<Tagpath, SchemaNode> t = getHashtable(namespace);
        if (t == null) {
            return null;
        }
        return t.get(tp);
    }

}
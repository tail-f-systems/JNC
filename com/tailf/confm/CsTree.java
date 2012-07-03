/*    -*- Java -*-
 *
 *  Copyright 2007 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm;
import java.util.*;
import com.tailf.inm.Tagpath;

/**
 * The CsTree class is used to represent the schemas of all namespaces
 */

public class CsTree {

    private static Hashtable namespaces[] = new Hashtable[32];
    private static String    nsnames[] = new String[32];
    private static int size = 0;

    /*
     * This static methid is used by the
     * generated code to populate a new hashtable for a
     * module
     */

    public static Hashtable create(String namespace) {
        Hashtable h;
        if ((h = getHashtable(namespace)) != null) return h;;
        namespaces[size] = h = new Hashtable();
        nsnames[size++] = namespace;
        return h;
    }

    /*
     * Return a hash table for a given namespace
     */

    public static Hashtable getHashtable(String namespace) {
        for (int i=0; i<size; i++) {
            if (nsnames[i].compareTo(namespace) == 0)
                return namespaces[i];
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
     * Find the CsNode for  e specific schema entry
     * returns null if not found
     */

    public static CsNode lookup(String namespace, Tagpath tp) {
        Hashtable t = getHashtable(namespace);
        if (t == null) {
            return null;
        }
        return (CsNode)t.get(tp);
    }
}

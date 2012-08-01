/**
 *  Copyright 2007 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.netconfmanager;

import java.util.ArrayList;

/**
 * This class implements a list of prefix mappings, which provides mappings from
 * namespaces to prefixes.
 * 
 **/
public class PrefixMap extends ArrayList {

    /**
     * Creates an empty prefix map object.
     */
    public PrefixMap() {
    }

    /**
     * Inserts a prefix initially in the new prefix map.
     */
    public PrefixMap(Prefix p) {
        add(p);
    }

    /**
     * Inserts an array of prefixes initially in the new prefix map.
     */
    public PrefixMap(Prefix[] p) {
        for (int i = 0; i < p.length; i++)
            add(p[i]);
    }

    /**
     * Merges two prefix maps. Prefix names that already exists will be
     * unchanged.
     * 
     */
    public void merge(PrefixMap prefixes) {
        // trace("merge()");
        for (int i = 0; i < prefixes.size(); i++)
            merge(prefixes.getPrefix(i));
    }

    /**
     * Merges in a prefix in the prefix map. Will not replace already existing
     * ones.
     */
    public void merge(Prefix prefix) {
        int index = indexOfName(prefix.name);
        if (index == -1)
            add(prefix);
    }

    /**
     * Stores the prefixes in the prefix map. Replaces those that already
     * exists, and add the new ones that doesn't exist.
     * 
     * @param prefixes
     *            Prefix mappings
     */
    public void set(PrefixMap prefixes) {
        trace("set: " + prefixes);
        for (int i = 0; i < prefixes.size(); i++)
            set(prefixes.getPrefix(i));
    }

    /**
     * Stores a prefix in the prefix map. Replace any old occurance.
     * 
     * @param prefix
     *            Prefix mapping to be set
     */
    public void set(Prefix prefix) {
        int index = indexOfName(prefix.name);
        if (index == -1)
            // if default prefix, add it first in list.
            if (prefix.name.equals(""))
                add(0, prefix); // add first
            else
                add(prefix);
        else
            set(index, prefix);
    }

    /**
     * Removes a prefix mapping.
     * 
     * @param prefix
     *            Name of prefix mapping to be removed.
     */
    public void remove(String prefix) {
        int index = indexOfName(prefix);
        if (index >= 0)
            remove(index);
    }

    /**
     * Gets the prefix at specified index.
     * 
     * @param i
     *            Index of prefix mapping to get.
     * @return Prefix mapping
     */
    public Prefix getPrefix(int i) {
        return (Prefix) super.get(i);
    }

    /**
     * Returns the index of the prefix name in the prefix map.
     * 
     * @param name
     *            The prefix name
     * @return Index of prefix mapping with specified prefix name
     */
    public int indexOfName(String name) {
        // trace("indexOfName("+name+")");
        for (int i = 0; i < size(); i++) {
            if (name.equals(getPrefix(i).name))
                return i;
        }
        return -1;
    }

    /**
     * Lookups prefix name in the prefix map. Return the Prefix object.
     * 
     * @param name
     *            The prefix name
     * @return Prefix mapping
     */
    public Prefix lookup(String name) {
        // trace("lookup("+name+")");
        for (int i = 0; i < size(); i++) {
            Prefix p = getPrefix(i);
            if (name.equals(p.name))
                return p;
        }
        return null;
    }

    /**
     * Lookups namespace and returns prefix for it.
     * 
     * @param ns
     *            The namespace to lookup in this prefix
     */
    public String nsToPrefix(String ns) {
        // trace("nsToPrefix(\""+ns+"\")");
        for (int i = 0; i < size(); i++) {
            Prefix p = getPrefix(i);
            if (p.ns != null && ns.equals(p.value))
                return p.name;
        }
        return null;
    }

    /**
     * Lookups prefix and returns the associated namespace
     */
    public String prefixToNs(String name) {
        // trace("prefixToNs("+name+")");
        for (int i = 0; i < size(); i++) {
            Prefix p = getPrefix(i);
            if (name.equals(p.name))
                return p.value;
        }
        return null;
    }

    /**
     * ------------------------------------------------------------ help
     * functions
     */

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_PREFIXMAP)
            System.err.println("*PrefixMap: " + s);
    }

}

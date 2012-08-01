/**
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.netconfmanager;

import java.util.*;

/**
 * This is an iterator class that is used for iterating over all the children
 * with a specified name in a NodeSet. An object of this iterator class is
 * obtained from the {@link Element#iterator} method.
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
 */
public class ElementChildrenIterator implements Iterator<Element> {

    private Iterator<Element> childrenIterator;
    private Element nextChild;
    private boolean hasNextChild = false;
    private String name;

    /**
     * Constructor to create new children iterator for all children.
     */
    public ElementChildrenIterator(NodeSet children) {
        if (children != null)
            childrenIterator = children.iterator();
        else
            childrenIterator = null;
        name = null;
    }

    /**
     * Constructor to create a new children iterator for children of a specific
     * name.
     */
    public ElementChildrenIterator(NodeSet children, String name) {
        if (children != null)
            childrenIterator = children.iterator();
        else
            childrenIterator = null;
        this.name = name;
    }

    /**
     * Return true if there are more children, false otherwise.
     * 
     */
    public boolean hasNext() {
        if (hasNextChild)
            return true;
        if (childrenIterator == null)
            return false;
        while (childrenIterator.hasNext()) {
            if (name == null)
                return true;
            Element child = (Element) childrenIterator.next();
            if (child.name.equals(name)) {
                hasNextChild = true;
                nextChild = child;
                return true;
            }
        }
        hasNextChild = false;
        return false;
    }

    /**
     * Return next child or null.
     * 
     */
    public Element nextElement() {
        if (hasNextChild) {
            hasNextChild = false;
            return nextChild;
        }
        hasNextChild = false;
        while (childrenIterator.hasNext()) {
            Element child = (Element) childrenIterator.next();
            if (name == null)
                return child;
            else if (child.name.equals(name))
                return child;
        }
        return null;
    }

    /**
     * Return next child or null.
     */
    public Element next() {
        return nextElement();
    }

    /**
     * Remove is not supported.
     * 
     */
    public void remove() {
    }
}

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

class DummyElement extends YangElement {

    private static final long serialVersionUID = 1L;

    /**
     * Structure information. An array of the children names.
     */
    protected String[] childrenNames() {
        return new String[0];
    }

    /**
     * Structure information. An array of the names of the key children.
     */
    protected String[] keyNames() {
        return new String[0];
    }

    /**
     * Clones this object, returning an exact copy.
     * 
     * @return A clone of the object.
     */
    public Object clone() {
        try {
            return (DummyElement) cloneContent(new DummyElement(namespace,
                    name));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clones this object, returning a shallow copy.
     * 
     * @return A clone of the object. Children are not included.
     */
    protected Element cloneShallow() {
        try {
            return cloneShallowContent(new DummyElement(namespace, name));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Constructor for the container
     */
    public DummyElement(String ns, String name) {
        super(ns, name);
        setDefaultPrefix();
    }
}

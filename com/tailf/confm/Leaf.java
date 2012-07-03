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
import com.tailf.inm.Element;
import com.tailf.inm.Capabilities;
import com.tailf.inm.Transport;
import com.tailf.inm.Tagpath;
import com.tailf.inm.INMException;

/**
 * The Leaf is the leaf of a sub-tree.
 * <p>
 * It is an extension of the Element class to make
 * the configuration sub-tree data model aware.
 * Classes generated from the ConfM compiler are
 * either Containers, Leafs, or derived data types.
 *
 * @see Container
 */
public class Leaf extends Element {
    public Leaf(String ns, String name) {
        super(ns, name);
    }

    // cache the Tagpath and the CsNode
    private Tagpath tp  = null;
    private CsNode n = null;
    protected void encode(Transport out, boolean newline_at_end,
                          Capabilities capas) throws INMException  {
        if (RevisionInfo.olderRevisionSupportEnabled && capas != null) {
            if (tp == null)
                tp = tagpath();
            if (n == null)
                n = CsTree.lookup(namespace, tp);
            String rev = capas.getRevision(namespace);
            if (n != null && n.revInfo != null) {
                for (int i = 0; i<n.revInfo.length; i++) {
                    RevisionInfo r = n.revInfo[i];
                    if (r.introduced.compareTo(rev) > 0) {
                        // This node was somehow modified
                        switch(r.type) {
                        case RevisionInfo.R_NODE_ADDED:
                            System.out.println("NODE_ADDED Skipping " + this);
                            return;
                        case RevisionInfo.R_ENUM_ADDED:

                            // Need to check if the added enum is the one we
                            // we wish to send
                            if (r.data.equals(getValue().toString())) {

                                throw new INMException(
                                    INMException.REVISION_ERROR,
                                    tp + " bad enum value for rev (" + rev +
                                    ") "  + r.data);
                            }
                            break;
                        case RevisionInfo.R_BITS_ADDED:

                            // Same thing - check for too new bit strings
                            if (r.data.equals(getValue().toString())) {
                                throw new INMException(
                                    INMException.REVISION_ERROR,
                                    tp + " bad bits value for rev (" +
                                    rev +") " + r.data);
                            }
                            break;
                        case RevisionInfo.R_MANDATORY_TRUE_TO_FALSE:
                            // nothing to do
                            break;
                        default:
                            ;
                        }

                    }
                }
            }
        }
        super.encode(out, newline_at_end, capas);
    }

    public Leaf  clone() {
        Leaf copy= new Leaf(namespace, name);
        cloneAttrs(copy);
        cloneValue(copy);
        return copy;
    }

    /*
     * returns true if this Leaf  is a key leaf
     */

    public boolean isKey() {
        Container p = (Container)getParent();
        String[] keys = p.keyNames();
        if (keys == null)
            return false;
        for (int i=0; i<keys.length; i++) {
            if (this.name.equals(keys[i]))
                return true;
        }
        return false;
    }

}

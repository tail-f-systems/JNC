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

/**
 * A "path create" expression.
 * <p>
 * The result of evaluating a "path create" expression is a new {@link Element}
 * node tree. A configuration tree can easily be created with this class.
 * <p>
 * Example:
 * 
 * <pre>
 * PathCreate path = new PathCreate(&quot;/hosts/host[name='john', age=42]&quot;);
 * Element element_tree = path.eval(PrefixMap);
 * </pre>
 * 
 **/

public class PathCreate extends Path {

    /**
     * Constructor for a PathCreate (parse tree) from a "path create" string.
     * This PathCreate can be used for creating a new {@link Element} tree.
     * <p>
     * See {@link PathCreate} for more information about path create
     * expressions.
     * 
     * @param pathStr
     *            A "path create" string
     */
    public PathCreate(String pathStr) throws NetconfException {
        create = true;
        locationSteps = parse(tokenize(pathStr));
    }

    /**
     * Evaluates the path expression and build a new Element subtree.
     * 
     * @param prefixMap
     *            Prefix mappings
     * @return A new element tree
     */
    public Element eval(PrefixMap prefixMap) throws NetconfException {
        trace("eval(): " + this);
        Element top = null, parent = null;
        for (int i = 0; i < locationSteps.size(); i++) {
            LocationStep step = (LocationStep) locationSteps.get(i);
            Element child = step.createElem(prefixMap, parent);
            if (top == null)
                top = child;
            if (parent != null)
                parent.addChild(child);
            parent = child;
        }
        return top;
    }

    /**
     * Evaluates one step in the path create expression an build a new Element
     * node.
     */
    Element evalStep(PrefixMap prefixMap, int step, Element parent)
            throws NetconfException {
        if (step < 0 || step >= locationSteps.size())
            throw new NetconfException(NetconfException.PATH_CREATE_ERROR,
                    "cannot eval location step: " + step + " in create path");
        LocationStep locStep = (LocationStep) locationSteps.get(step);
        trace("evalStep(): step=" + step + ", " + locStep);
        Element child = locStep.createElem(prefixMap, parent);
        return child;
    }

    /**
     * ------------------------------------------------------------
     * 
     */

    /**
     * Returns a string representation of this PathCreate. It's a parse tree.
     */
    public String toString() {
        String s = "PathCreate[";
        boolean comma = false;
        for (int i = 0; i < locationSteps.size(); i++) {
            if (comma)
                s = s + ",";
            s = s + locationSteps.get(i);
        }
        s = s + "]";
        return s;
    }

    /**
     * ------------------------------------------------------------ help
     * functions
     */

    /**
     *
     */
    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_PATHCREATE)
            System.err.println("*PathCreate: " + s);
    }

}

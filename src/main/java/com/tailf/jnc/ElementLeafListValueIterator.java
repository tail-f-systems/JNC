package com.tailf.jnc;

import java.util.Iterator;

/**
 * This is an iterator class that is used for iterating over all leaf-list
 * children with a specified name in a NodeSet. An object of this iterator
 * class is obtained from the {@link Element#iterator} method.
 * <p>
 * Example usage:
 * 
 * <pre>
 * ElementLeafListValueIterator domainIter = config.iterator(&quot;domain&quot;);
 * while (domainIter.hasNext()) {
 *     String domain = (String) domainIter.next();
 *     System.out.println(&quot;Domain: &quot; + host);
 * }
 * </pre>
 * 
 */
public class ElementLeafListValueIterator implements Iterator<Object> {
    private Iterator<Element> childrenIterator;
    private Element nextChild;
    private boolean hasNextChild = false;
    private final String name;

    /**
     * Constructor to create a new children iterator for leaf-list children of
     * a specific name.
     */
    public ElementLeafListValueIterator(NodeSet children, String name) {
        childrenIterator = (children == null) ? null : children.iterator();
        this.name = name;
    }

    /**
     * @return <code>true</code> if there are more children;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasNext() {
        if (hasNextChild) {
            return true;
        }
        if (childrenIterator == null) {
            return false;
        }
        while (childrenIterator.hasNext()) {
            if (name == null) {
                return true;
            }
            final Element child = childrenIterator.next();
            if (child.name.equals(name)) {
                hasNextChild = true;
                nextChild = child;
                return true;
            }
        }
        return hasNextChild = false;
    }

    /**
     * @return next child or <code>null</code>.
     */
    public Object nextElement() {
        if (hasNextChild) {
            hasNextChild = false;
            return nextChild.value;
        }
        hasNextChild = false;
        while (childrenIterator.hasNext()) {
            final Element child = childrenIterator.next();
            if (name == null) {
                return child.value;
            } else if (child.name.equals(name)) {
                return child.value;
            }
        }
        return null;
    }

    /**
     * @return next child or <code>null</code>.
     */
    @Override
    public Object next() {
        return nextElement();
    }

    /**
     * Remove is not supported.
     */
    @Override
    public void remove() {
    }
}

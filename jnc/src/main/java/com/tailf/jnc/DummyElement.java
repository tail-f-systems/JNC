package com.tailf.jnc;

/**
 * Used to instantiate YangElement in sync-methods and in tests
 */
class DummyElement extends YangElement {

    private static final long serialVersionUID = 1L;

    /**
     * Structure information. An array of the children names.
     */
    @Override
    protected String[] childrenNames() {
        return new String[0];
    }

    /**
     * Structure information. An array of the names of the key children.
     */
    @Override
    protected String[] keyNames() {
        return new String[0];
    }

    /**
     * Clones this object, returning an exact copy.
     * 
     * @return A clone of the object.
     */
    @Override
    public Object clone() {
        try {
            return cloneContent(new DummyElement(namespace, name));
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Clones this object, returning a shallow copy.
     * 
     * @return A clone of the object. Children are not included.
     */
    @Override
    protected Element cloneShallow() {
        try {
            return cloneShallowContent(new DummyElement(namespace, name));
        } catch (final Exception e) {
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

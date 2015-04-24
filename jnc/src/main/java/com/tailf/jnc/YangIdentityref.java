package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "identityref".
 * 
 * FIXME add toString
 * 
 * @author emil@tail-f.com
 */
public class YangIdentityref extends YangBaseType<Element> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangIdentityref object from a String, formatted as described
     * in {@link YangIdentityref#fromString(String)}.
     * 
     * @param s The string.
     * @throws YangException If s is improperly formatted.
     */
    public YangIdentityref(String s) throws YangException {
        super(s);
    }

    /**
     * Creates a YangIdentityref object from an Element.
     * 
     * @param identity The initial value of the new YangIdentityref object.
     * @throws YangException If an invariant was broken during initialization.
     */
    public YangIdentityref(Element identity) throws YangException {
        super(identity);
    }

    /**
     * Creates a YangIdentityref object from three strings: identity namespace,
     * prefix and argument/identifier.
     * 
     * @param ns identity module namespace
     * @param prefix identity module prefix
     * @param id identity argument/identifier
     * @throws YangException If an invariant was broken during initialization.
     */
    public YangIdentityref(String ns, String prefix, String id)
            throws YangException {
        super(ns + " " + prefix + " " + id);
    }

    /**
     * Returns an identity element from a String.
     * 
     * @param s The string.
     * @return An Element representing the referenced identity, parsed from s.
     */
    @Override
    protected Element fromString(String s) {
        return new Element("", s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangIdentityref;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangIdentityref cloneShallow() throws YangException {
        return new YangIdentityref(toString());
    }

}

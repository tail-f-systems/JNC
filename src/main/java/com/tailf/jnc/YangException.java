package com.tailf.jnc;

/**
 * This is the class of exceptions that are thrown from the classes generated
 * by the JNC pyang plugin, and by the yang type classes.
 */
public class YangException extends JNCException {

    private static final long serialVersionUID = 1L;

    public YangException(int errorCode, Object opaqueData) {
        super(errorCode, opaqueData);
    }

    public static final int ELEMENT_MISSING = -1025;
    public static final int BAD_VALUE = -1026;
    public static final int BAD_SESSION_NAME = -1032;

    @Override
    public String toString() {
        switch (errorCode) {
        case ELEMENT_MISSING:
            return "Element does not exist: " + opaqueData;
        case BAD_VALUE:
            return "Bad value: " + opaqueData;
        case BAD_SESSION_NAME:
            return "Session name doesn't exist (or already exists)";
        default:
            return super.toString();
        }
    }

    /**
     * Asserts that fail is false, throws BAD_VALUE with o otherwise.
     * 
     * @param fail Determines if a Bad Value YangException is thrown
     * @param o Object which toString-method will be appended to exception
     * @throws YangException if and only if fail is true
     */
    public static void throwException(boolean fail, Object o)
            throws YangException {
        if (fail) {
            throw new YangException(BAD_VALUE, o);
        }
    }

}

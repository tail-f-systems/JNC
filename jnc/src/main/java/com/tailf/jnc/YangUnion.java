package com.tailf.jnc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Implements the built-in YANG data type "union". Represents a union of
 * different sub-types.
 * <p>
 * From RFC 6020: When a string representing a union data type is validated,
 * the string is validated against each member type, in the order they are
 * specified in the "type" statement, until a match is found. Any default value
 * or "units" property defined in the member types is not inherited by the
 * union type.
 * 
 * @author emil@tail-f.com
 */
public class YangUnion extends YangBaseType<YangType<?>> {

    private static final long serialVersionUID = 1L;
    
    /**
     * An array of the allowed types, ordered as in the YANG module.
     */
    private String[] memberTypes;
    
    /**
     * Get the types allowed for this union.
     *
     * @return A string array with the member types of this union
     */
    protected String[] memberTypes() {
        return memberTypes;
    };

    /**
     * Creates a YangUnion object from a java.lang.String representing a value
     * of one of the member types.
     * 
     * @param value The Java String.
     * @param memberTypes A string array with the types of the union
     * @throws YangException If an invariant was broken during assignment.
     */
    public YangUnion(String value, String[] memberTypes) throws YangException {
        this.memberTypes = memberTypes;
        setValue(value);
    }

    /**
     * Creates a YangUnion object from a YangType, which should be an instance
     * of one of the member types.
     * 
     * @param value The Object to use as value.
     * @param memberTypes A string array with the types of the union
     * @throws YangException If an invariant was broken during assignment, for
     *                       example if the value is of an incorrect type.
     */
    public YangUnion(YangType<?> value, String[] memberTypes) throws YangException {
        this.memberTypes = memberTypes;
        setValue(value);
    }

    /**
     * Sets the value of this object using a java.lang.String.
     * 
     * @param value The Java String.
     * @throws YangException If an invariant was broken during assignment.
     */
    @Override
    public void setValue(String value) throws YangException {
        YangException.throwException(value == null,
                new NullPointerException());
        this.value = fromString(value);
        check();
    }

    /**
     * Sets the value of this object using an Object.
     * 
     * @param value The Object to set as the new value.
     * @throws YangException If an invariant was broken during assignment.
     */
    @Override
    public void setValue(YangType<?> value) throws YangException {
        YangException.throwException(value == null,
                new NullPointerException());
        this.value = value;
        check();
    }

    /**
     * Parses a value of a member type
     * 
     * @param s String representation of member type value
     * @return first valid value, or null if none
     */
    @Override
    protected YangType<?> fromString(String s) {
        String[] mtypes = memberTypes();
        for (String memberType : mtypes) {
            try {
                Class<?> cl = Class.forName(memberType);
                Constructor<?> c;
                c = cl.getConstructor(new Class[] { String.class });
                Object o = c.newInstance(new Object[] { s });
                if (o instanceof YangType<?>) {
                    return (YangType<?>) o;
                }
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
            } catch (ClassNotFoundException e) {
            }
            // Unable to instantiate a value of this memberType - try next
        }
        return null;
    }
    
    /**
     * Checks that the value of this object is not null and is instance of a
     * member type. Called in constructors and value setters.
     * 
     * @throws YangException If the value of this object is invalid.
     */
    @Override
    public void check() throws YangException {
        super.check();
        // TODO: Check that value is instance of a member type
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangUnion; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangUnion;
    }
    
    /**
     * Compares this union with another object for equality.
     * 
     * @param obj The object to compare with.
     * @return true if obj is a a union with equal value and member types;
     *         false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return (canEqual(obj)
                && java.util.Arrays.equals(memberTypes,
                        ((YangUnion)obj).memberTypes)
                && super.equals(obj));
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangBaseType#hashCode()
     */
    @Override
    public int hashCode() {
        final int hash = super.hashCode();
        return (memberTypes == null) ? hash : (hash + Arrays.hashCode(memberTypes));
    }

    /*
     * (non-Javadoc)
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangUnion cloneShallow() throws YangException {
        return new YangUnion(value.toString(), memberTypes);
    }

}
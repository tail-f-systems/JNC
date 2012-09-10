package com.tailf.jnc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class YangEnumerationTest {

    private YangEnumeration one;
    private YangEnumeration two;
    private YangEnumeration spacy;
    private String spacystr = "  leading  and trailing  ";

    @Before
    public void setUp() throws Exception {
        one = new YangEnumeration("one", new String[] {"one"});
        two = new YangEnumeration("two", new String[] {"two"});
        spacy = new YangEnumeration("spacy", new String[] {"spacy"});
    }

    @Test
    public void testCanEqual() {
        assertTrue(one.canEqual(one));
        assertTrue(one.canEqual(two));
        assertTrue(one.canEqual(spacy));
        assertTrue(spacy.canEqual(one));
        assertFalse(one.canEqual("one"));
        assertFalse(one.canEqual(null));
    }

    @Test
    public void testYangEnumeration() throws YangException {
        // Values set in setUp
        assertTrue(one.value.equals("one"));
        assertTrue(two.value.equals("two"));
        assertTrue(spacy.value.equals("spacy"));

        // Empty string not allowed
        try {
            spacy = new YangEnumeration("", new String[] {""});
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData.equals("empty string in enum value"));
        }
        assertFalse(spacy.value.equals(""));
        assertTrue(spacy.value.equals("spacy"));
        
        // Leading and trailing spaces not allowed
        try {
            spacy = new YangEnumeration(spacystr, new String[] {spacystr});
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData.equals(spacystr));
        }
        assertFalse(spacy.value.equals(spacystr));
        assertTrue(spacy.value.equals("spacy"));
        
        // No wsCollapse occurs
        spacy = new YangEnumeration("leading  and trailing",
                new String[] {"leading  and trailing"});
        assertFalse(spacy.value.equals("leading and trailing"));
        assertTrue(spacy.value.equals("leading  and trailing"));
        
        // Null not allowed
        try {
            one = new YangEnumeration(null, null);
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData instanceof NullPointerException);
        }
        try {
            one = new YangEnumeration("hej", null);
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData instanceof NullPointerException);
        }
        try {
            one = new YangEnumeration(null, new String[] {"hej"});
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData instanceof NullPointerException);
        }
        one.value.equals("one");
    }

}

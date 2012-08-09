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
        one = new YangEnumeration("one");
        two = new YangEnumeration("two");
        spacy = new YangEnumeration("spacy");
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
            spacy = new YangEnumeration("");
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData.equals("empty string"));
        }
        assertFalse(spacy.value.equals(""));
        assertTrue(spacy.value.equals("spacy"));
        
        // Leading and trailing spaces not allowed
        try {
            spacy = new YangEnumeration(spacystr);
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData.equals(spacystr));
        }
        assertFalse(spacy.value.equals(spacystr));
        assertTrue(spacy.value.equals("spacy"));
        
        // No wsCollapse occurs
        spacy = new YangEnumeration("leading  and trailing");
        assertFalse(spacy.value.equals("leading and trailing"));
        assertTrue(spacy.value.equals("leading  and trailing"));
        
        // Null not allowed
        try {
            one = new YangEnumeration(null);
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData instanceof NullPointerException);
        }
        one.value.equals("one");
    }

    @Test
    public void testEnumeration() {
        assertTrue(one.enumeration(one.value));
        assertFalse(one.enumeration(two.value));
        assertTrue(two.enumeration(two.value));
        assertTrue(spacy.enumeration(spacy.value));
        assertFalse(spacy.enumeration(one.value));
        assertFalse(spacy.enumeration(null));
    }

}

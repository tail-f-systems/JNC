package com.tailf.jnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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
        assertEquals("one", one.value);
        assertEquals("two", two.value);
        assertEquals("spacy", spacy.value);

        // Empty string not allowed
        try {
            spacy = new YangEnumeration("", new String[] {""});
            fail("Expected YangException");
        } catch (YangException e) {
            assertEquals("empty string in enum value", e.opaqueData);
        }
        assertNotEquals("", spacy.value);
        assertEquals("spacy", spacy.value);

        // Leading and trailing spaces not allowed
        try {
            spacy = new YangEnumeration(spacystr, new String[] {spacystr});
            fail("Expected YangException");
        } catch (YangException e) {
            assertEquals(spacystr, e.opaqueData);
        }
        assertNotEquals(spacystr, spacy.value);
        assertEquals("spacy", spacy.value);

        // No wsCollapse occurs
        spacy = new YangEnumeration("leading  and trailing",
                new String[] {"leading  and trailing"});
        assertNotEquals("leading and trailing", spacy.value);
        assertEquals("leading  and trailing", spacy.value);

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
            assertEquals("no enum names provided", e.opaqueData);
        }
        try {
            one = new YangEnumeration(null, new String[] {"hej"});
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData instanceof NullPointerException);
        }
        assertEquals("one", one.value);
    }

}

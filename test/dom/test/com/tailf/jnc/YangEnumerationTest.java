package com.tailf.jnc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class YangEnumerationTest {

    private YangEnumeration one;
    private YangEnumeration two;
    private YangEnumeration spacy;

    @Before
    public void setUp() throws Exception {
        one = new YangEnumeration("one");
        two = new YangEnumeration("two");
        spacy = new YangEnumeration("  leading  and trailing  ");
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
        assertTrue(one.value.equals("one"));
        assertTrue(two.value.equals("two"));
        assertFalse(spacy.value.equals("  leading  and trailing  "));
        assertFalse(spacy.value.equals("leading and trailing"));
        assertTrue(spacy.value.equals("leading  and trailing"));
        try {
            one = new YangEnumeration(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {}
        one.value.equals("one");
    }

    @Test
    public void testEnumeration() {
        assertTrue(one.enumeration(one.value));
        assertTrue(two.enumeration(two.value));
        assertTrue(spacy.enumeration(spacy.value));
    }

}

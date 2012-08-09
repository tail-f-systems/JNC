package com.tailf.jnc;

import static org.junit.Assert.*;

import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;

public class YangBaseStringTest {

	private YangBaseString bs;
    private YangBaseString empty;
    private YangBaseString spacy;
	private YangBaseString nullary;

	@Before
	public void setUp() throws Exception {
		bs = new YangBaseString("baseString");
        empty = new YangBaseString("");
        spacy = new YangBaseString("  A\t  space   ");
		nullary = null;
	}

	@Test
	public void testSetValueString() throws YangException {
		try {
			nullary.setValue("setValue");
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
		}

		assertTrue(bs.value.equals("baseString"));
		bs.setValue("newString");
		assertTrue(bs.value.equals("newString"));

		try {
			bs.setValue(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testCheck() throws YangException {
		bs.check();
		empty.check();

		try {
			nullary.check();
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testCanEqual() {
		assertTrue(bs.canEqual(bs));
		assertTrue(bs.canEqual(empty));
		assertFalse(bs.canEqual(nullary));
		assertFalse(bs.canEqual(new String("baseString")));
	}

	@Test
	public void testBaseString() throws YangException {
		try {
			nullary = new YangBaseString(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
		} catch (YangException e) {}
		assertTrue(nullary == null);

		nullary = new YangBaseString("nullary");
		assertTrue(nullary.value.equals("nullary"));
	}

	@Test
	public void testFromString() {
		bs.fromString("baseString").equals("baseString");
		bs.fromString("fromString").equals("fromString");
		assertTrue(bs.value + " is not baseString", bs.value.equals("baseString"));
	}

	@Test
	public void testPatternString() throws YangException {
		bs.pattern("baseString");
		bs.pattern("base.*");
		bs.pattern(".*String");
		bs.pattern("b...S.*");
		bs.pattern("[a-zS]*");
		bs.pattern(".*");
		bs.pattern("[bzaszeSztrzinzg]+");
		empty.pattern("");
		empty.pattern(".*");

		try {
			bs.pattern("[a-z]*");
			fail("Expected pattern mismatch");
		} catch (YangException e) {}
		try {
			bs.pattern("[a*(\\");
			fail("Expected syntax error");
		} catch (YangException e) {
			assertTrue("Expected syntax error",
					e.opaqueData instanceof PatternSyntaxException);
		}
		
		try {
			nullary.pattern("null");
		} catch (NullPointerException e) {}
		try {
			bs.pattern((String)null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {}
		
		try {
			bs.pattern("");
			fail("Expected pattern mismatch");
		} catch (YangException e) {}
	}

	@Test
	public void testPatternStringArray() throws YangException {
		try {
			nullary.pattern(new String[] {"null"});
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {}
		try {
			bs.pattern((String[])null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {}
		
		bs.pattern(new String[] {});
	}

	@Test
	public void testWsReplace() {
		assertTrue(spacy.value.equals("  A\t  space   "));
		spacy.wsReplace();
        assertTrue(spacy.value.equals("  A   space   "));
	}

	@Test
	public void testWsCollapse() {
        assertTrue(spacy.value.equals("  A\t  space   "));
        spacy.wsCollapse();
        assertTrue(spacy.value.equals("A\t space"));
        spacy.wsReplace();
        assertTrue(spacy.value.equals("A  space"));
        spacy.wsCollapse();
        assertTrue(spacy.value.equals("A space"));
	}

	@Test
	public void testHashCode() {
        assertTrue(bs.hashCode() == "baseString".hashCode());
        assertTrue(empty.hashCode() == 0);
        assertTrue(spacy.hashCode() == "  A\t  space   ".hashCode());
        spacy.wsReplace();
        spacy.wsCollapse();
        assertFalse(spacy.hashCode() == "  A\t  space   ".hashCode());
        assertTrue(spacy.hashCode() == "A space".hashCode());
	}

	@Test
	public void testToString() {
        assertTrue(bs.toString().equals(bs.value));
        assertTrue(empty.toString().equals(""));
        assertTrue(spacy.toString().equals(spacy.value));
        assertFalse(spacy.toString().equals(bs.value));
        assertFalse(spacy.toString().equals(empty.value));
        spacy.value = "";
        assertTrue(spacy.toString().equals(empty.value));
	}

	@Test
	public void testEquals() {
        assertTrue(bs.equals(bs));
        assertFalse(bs.equals(null));
        assertFalse(bs.equals("baseString"));
	}

	@Test
	public void testExact() throws YangException {
        bs.exact("baseString".length());
        try {
            bs.exact(0);
            fail("Expected YangException");
        } catch (YangException e) {}
        try {
            bs.exact(-1);
            fail("Expected YangException");
        } catch (YangException e) {}
        try {
            bs.exact(Integer.MAX_VALUE);
            fail("Expected YangException");
        } catch (YangException e) {}

        empty.exact(0);
        spacy.exact("  A\t  space   ".length());
        spacy.wsReplace();
        spacy.exact("  A\t  space   ".length());
        spacy.wsCollapse();
        spacy.exact("A space".length());
	}

	@Test
	public void testMin() throws YangException {
        bs.min("baseString".length());
        bs.min("baseString".length() - 1);
        bs.min(0);
        bs.min(-1);
        bs.min(Integer.MIN_VALUE);
        
        try {
            bs.min(Integer.MAX_VALUE);
            fail("Expected YangException");
        } catch (YangException e) {}
        try {
            bs.min("baseString".length() + 1);
            fail("Expected YangException");
        } catch (YangException e) {}
        
        bs.value = "base";
        bs.min("base".length());
        try {
            bs.min("baseString".length());
            fail("Expected YangException");
        } catch (YangException e) {}

        empty.min(0);
        spacy.min(0);
	}

	@Test
	public void testMax() throws YangException {
        bs.max("baseString".length());
        bs.max("baseString".length() + 1);
        bs.max(Integer.MAX_VALUE);
        
        try {
            bs.max(Integer.MIN_VALUE);
            fail("Expected YangException");
        } catch (YangException e) {}
        try {
            bs.max("baseString".length() - 1);
            fail("Expected YangException");
        } catch (YangException e) {}
        try {
            bs.max(0);
            fail("Expected YangException");
        } catch (YangException e) {}
        try {
            bs.max(-1);
            fail("Expected YangException");
        } catch (YangException e) {}
        
        bs.value = "base";
        bs.max("base".length());
        bs.max("baseString".length());
        try {
            bs.max("bas".length());
            fail("Expected YangException");
        } catch (YangException e) {}

        empty.max(0);
        try {
            spacy.max(0);
            fail("Expected YangException");
        } catch (YangException e) {}
	}

}

package com.tailf.jnc;

import static org.junit.Assert.*;

import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;

public class BaseStringTest {

	private BaseString bs;
	private BaseString empty;
	private BaseString nullary;

	@Before
	public void setUp() throws Exception {
		bs = new BaseString("baseString");
		empty = new BaseString("");
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
			nullary = new BaseString(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
		} catch (YangException e) {}
		assertTrue(nullary == null);

		nullary = new BaseString("nullary");
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
		fail("Not yet implemented");
	}

	@Test
	public void testWsCollapse() {
		fail("Not yet implemented");
	}

	@Test
	public void testHashCode() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetValueT() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	@Test
	public void testFromStringString1() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testExact() {
		fail("Not yet implemented");
	}

	@Test
	public void testMin() {
		fail("Not yet implemented");
	}

	@Test
	public void testMax() {
		fail("Not yet implemented");
	}

}

package com.tailf.jnc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tailf.jnc.YangException;
import com.tailf.jnc.YangInt16;

public class YangInt16Test {

    private int iv1;
    private int iv2;
    private String iv3;
    private YangInt16 i1;
    private YangInt16 i2;
    private YangInt16 i3;

    @Before
    public void setUp() throws Exception {
        iv1 = 7;
        iv2 = 13;
        iv3 = "13";
        i1 = new YangInt16(iv1);
        i2 = new YangInt16(iv2);
        i3 = new YangInt16(iv3);
    }

    @Test
    public void testCheck() throws YangException {
        i1.check();
        i2.check();
        i3.check();
    }

    @Test
    public void testEquals() throws YangException {
        assertFalse(i1.equals((Object)iv1));
        assertFalse(i1.equals((Object)7));
        assertFalse(i1.equals((Object)iv2));
        assertFalse(i1.equals((Object)"7"));
        assertFalse(i2.equals((Object)iv2));
        assertTrue(i2.equals((Object)i3));
        assertFalse(i3.equals((Object)iv3));
        assertFalse(i3.equals((Object)iv2));

        assertTrue(i1.equals(i1));
        assertFalse(i1.equals(null));
        assertFalse(i1.equals(7));
        assertFalse(i1.equals(Short.valueOf((short)7)));
        assertTrue(i1.equals(new YangInt16((short)7)));
        i1.value = null;
        assertFalse(i1.equals(i1));
        assertFalse(i1.equals(new YangInt16((short)7)));
        i1.value = 7;
        
        assertTrue(i2.equals(i3));
    }

    @Test
    public void testCanEqual() {
        assertTrue(i1.canEqual(i1));
        assertTrue(i1.canEqual(i2));
        assertTrue(i1.canEqual(i3));
        assertFalse(i1.canEqual(null));
        assertFalse(i1.canEqual(Short.MAX_VALUE));
    }

    @Test
    public void testValid() {
        // These should be valid
    	assertTrue(i1.valid(0));
    	assertTrue(i1.valid(1));
    	assertTrue(i1.valid(-1));
    	assertTrue(i1.valid(255));
    	assertTrue(i1.valid(256));
    	assertTrue(i1.valid(Short.MAX_VALUE));
    	assertTrue(i1.valid(Short.MIN_VALUE));
    	assertTrue(i1.valid(0x7fffL));
    	assertTrue(i1.valid(0x0000L));
    	assertTrue(i1.valid(-0x8000L));
    	
    	// These should not be valid
    	assertFalse(i1.valid(0x8000L));
    	assertFalse(i1.valid(-0x8001L));
    	assertFalse(i1.valid(0x10000L));
    	assertFalse(i1.valid(Short.MAX_VALUE+1));
    	assertFalse(i1.valid(Short.MIN_VALUE-1));
    	assertFalse(i1.valid(-Short.MIN_VALUE));
    	assertFalse(i1.valid(Long.MAX_VALUE));
    	assertFalse(i1.valid(Long.MIN_VALUE));
    	assertFalse(i1.valid(0xffffffffL));
    	
    	// null
        try {
            i1.valid(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {}
    }

    @Test
    public void testDecodeString() {
        assertTrue(i1.decode("123") == 123);
        assertTrue(i1.decode("123").equals(Short.valueOf((short)123)));
        assertTrue(i1.decode("-123") == -123);
        assertTrue(i1.decode("-0") == 0);
        assertTrue(i1.decode("010") == 8);  // Octal
        assertTrue(i1.decode("0x10") == 16);  // Hexadecimal
        assertTrue(i1.decode("#10") == 16);  // Hexadecimal
        assertTrue(i1.decode("-0x8000") == -32768);  // Min
        assertTrue(i1.decode("0x7fFf") == 32767);  // Max, mixed case

        try {
            i1.decode("");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {}
        try {
            i1.decode("0x8000"); // Max + 1
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {}
        try {
            i1.decode(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {}
    }

}

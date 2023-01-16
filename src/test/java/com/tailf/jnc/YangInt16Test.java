package com.tailf.jnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

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
        assertNotEquals((Object)iv1, i1);
        assertNotEquals((Object)7, i1);
        assertNotEquals((Object)iv2, i1);
        assertNotEquals((Object)"7", i1);
        assertNotEquals((Object)iv2, i2);
        assertEquals((Object)i3, i2);
        assertNotEquals((Object)iv3, i3);
        assertNotEquals((Object)iv2, i3);

        assertEquals(i1, i1);
        assertNotEquals(null, i1);
        assertNotEquals(7, i1);
        assertNotEquals(Short.valueOf((short)7), i1);
        assertEquals(new YangInt16((short)7), i1);
        i1.value = null;
        assertNotEquals(i1, i1);
        assertNotEquals(new YangInt16((short)7), i1);
        i1.value = 7;

        assertEquals(i3, i2);
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

        assertThrows(NullPointerException.class, () -> {
            i1.valid(null);
        });
    }

    @Test
    public void testDecodeString() {
        assertEquals((Short)(short)123, i1.decode("123"));
        assertEquals(Short.valueOf((short)123), i1.decode("123"));
        assertEquals((Short)(short)-123, i1.decode("-123"));
        assertEquals((Short)(short)0, i1.decode("-0"));
        assertEquals((Short)(short)8, i1.decode("010"));  // Octal
        assertEquals((Short)(short)16, i1.decode("0x10"));  // Hexadecimal
        assertEquals((Short)(short)16, i1.decode("#10"));  // Hexadecimal
        assertEquals((Short)(short)-32768, i1.decode("-0x8000"));  // Min
        assertEquals((Short)(short)32767, i1.decode("0x7fFf"));  // Max, mixed case

        assertThrows(NumberFormatException.class, () -> {
            i1.decode("");
        });
        assertThrows(NumberFormatException.class, () -> {
            i1.decode("0x8000"); // Max + 1
        });
        assertThrows(NullPointerException.class, () -> {
            i1.decode(null);
        });
    }
}

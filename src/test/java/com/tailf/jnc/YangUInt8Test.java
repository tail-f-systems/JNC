package com.tailf.jnc;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class YangUInt8Test {

    private int iv1;
    private int iv2;
    private String iv3;
    private YangUInt8 i1;
    private YangUInt8 i2;
    private YangUInt8 i3;

    @Before
    public void setUp() throws Exception {
        iv1 = 7;
        iv2 = 13;
        iv3 = "13";
        i1 = new YangUInt8(iv1);
        i2 = new YangUInt8(iv2);
        i3 = new YangUInt8(iv3);
    }

    @Test
    public void testCheck() throws YangException {
        i1.check();
        i2.check();
        i3.check();
    }

    @Test
    public void testEqualsObject() {
        assertNotEquals((Object)iv1, i1);
        assertNotEquals((Object)7, i1);
        assertNotEquals((Object)iv2, i1);
        assertNotEquals((Object)"7", i1);
        assertNotEquals((Object)iv2, i2);
        assertNotEquals((Object)iv3, i3);
        assertNotEquals((Object)iv2, i3);
    }

    @Test
    public void testCanEqual() {
        assertTrue(i1.canEqual(i1));
        assertTrue(i1.canEqual(i2));
        assertTrue(i1.canEqual(i3));
    }

    @Test
    public void testValid() {
        // These should be valid
        assertTrue(i1.valid(0));
        assertTrue(i1.valid(0x00));
        assertTrue(i1.valid(1));
        assertTrue(i1.valid(255));
        assertTrue(i1.valid(Byte.MAX_VALUE));
        assertTrue(i1.valid(0x7f));

        // These should not be valid
        assertFalse(i1.valid(-1));
        assertFalse(i1.valid(256));
        assertFalse(i1.valid(Byte.MIN_VALUE));
        assertFalse(i1.valid(Short.MAX_VALUE));
        assertFalse(i1.valid(Short.MIN_VALUE));
        assertFalse(i1.valid(-0x80));
        assertFalse(i1.valid(0x8000L));
        assertFalse(i1.valid(-0x8001L));
        assertFalse(i1.valid(0x10000L));
        assertFalse(i1.valid(Short.MAX_VALUE+1));
        assertFalse(i1.valid(Short.MIN_VALUE-1));
        assertFalse(i1.valid(-Short.MIN_VALUE));
        assertFalse(i1.valid(Long.MAX_VALUE));
        assertFalse(i1.valid(Long.MIN_VALUE));
        assertFalse(i1.valid(0xffffffffL));
    }

}

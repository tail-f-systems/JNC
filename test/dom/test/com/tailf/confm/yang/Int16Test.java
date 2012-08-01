package com.tailf.confm.yang;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tailf.confm.ConfMException;
import com.tailf.confm.yang.Int16;

public class Int16Test {

    private int iv1;
    private int iv2;
    private String iv3;
    private Int16 i1;
    private Int16 i2;
    private Int16 i3;

    @Before
    public void setUp() throws Exception {
        iv1 = 7;
        iv2 = 13;
        iv3 = "13";
        i1 = new Int16(iv1);
        i2 = new Int16(iv2);
        i3 = new Int16(iv3);
    }

    @Test
    public void testCheck() throws ConfMException {
        i1.check();
        i2.check();
        i3.check();
    }

    @Test
    public void testEqualsObject() {
        assertTrue(i1.equals((Object)iv1));
        assertTrue(i1.equals((Object)7));
        assertFalse(i1.equals((Object)iv2));
        assertFalse(i1.equals((Object)"7"));

        assertTrue(i2.equals((Object)iv2));

        assertFalse(i3.equals((Object)iv3));
        assertTrue(i3.equals((Object)iv2));
    }

    @Test
    public void testCanEqual() {
        i1.canEqual(i1);
        i1.canEqual(i2);
        i1.canEqual(i3);
    }

    @Test
    public void testValid() {
        fail("Not yet implemented");
    }

    @Test
    public void testParseString() {
        fail("Not yet implemented");
    }

    @Test
    public void testEqualsShort() {
        fail("Not yet implemented");
    }

}

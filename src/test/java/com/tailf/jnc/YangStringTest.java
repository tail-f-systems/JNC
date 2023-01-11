package com.tailf.jnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class YangStringTest {

    private String str1;
    private String str2;
    private String str3;
    private String str4;
    private int intVal;
    private YangString ys1;
    private YangString ys2;
    private YangString ys3;
    private YangString ys4;
    private YangInt16 yi16;


    @Before
    public void setUp() throws Exception {
        str1 = "A test string";
        str2 = "A test string";
        str3 = "Another \n string";
        str4 = "  Hi   there ";
        intVal = 7;
        ys1 = new YangString(str1);
        ys2 = new YangString(str1);
        ys3 = new YangString(str3);
        ys4 = new YangString(str4);
        yi16 = new YangInt16(intVal);
    }

    @Test
    public void testSetValueString() throws YangException {
        assertEquals(str1, ys1.value);
        assertEquals(str2, ys1.value);
        assertNotEquals(str3, ys1.value);
        ys1.setValue(str3);
        assertEquals(str3, ys1.value);
        assertNotEquals(str1, ys1.value);
    }

    @Test
    public void testToString() {
        assertEquals(str1, ys1.toString());
    }

    @Test
    public void testEquals() {
        assertEquals(ys1, ys1);
        assertEquals(ys2, ys1);
        assertNotEquals(yi16, ys1);
        assertNotEquals(ys1, yi16);

        assertEquals(ys1, str1);
        assertEquals(ys1, str2);
        assertNotEquals(ys1, str3);
    }

    @Test
    public void testCanEqual() {
        assertTrue(ys1.canEqual(ys1));
        assertTrue(ys1.canEqual(ys2));
        assertTrue(ys1.canEqual(str1));
        assertTrue(ys1.canEqual(str2));
        assertFalse(ys1.canEqual(intVal));
        assertFalse(ys1.canEqual(yi16));
    }

    @Test
    public void testFromStringString() {
        assertEquals(str1, ys1.fromString(str1));
        assertEquals(str2, ys1.fromString(str2));
    }

    @Test
    public void testPatternString() throws YangException {
        ys1.pattern(str1);
        ys2.pattern(".*string");
        ys3.pattern(".*\n.*string");
        ys3.pattern("An.*\n.*");
    }

    @Test(expected=YangException.class)
    public void testPatternStringException() throws YangException {
        ys1.pattern(str1+str2);
    }

    @Test
    public void testPatternStringArray() throws YangException {
        ys1.pattern(new String[] {"A .*", ".*string"});
    }

    @Test
    public void testLength() throws YangException {
        ys1.exact(str1.length());
        ys3.exact(str3.length());
    }

    @Test(expected=YangException.class)
    public void testLengthException() throws YangException {
        ys1.exact(str3.length());
    }

    @Test
    public void testMinInt() throws YangException {
        ys1.min(str1.length()-1);
    }

    @Test
    public void testMaxInt() throws YangException {
        ys1.max(str1.length()+1);
    }

    @Test
    public void testWsReplace() {
        assertTrue(ys3.value.contains("\n"));
        ys3.wsReplace();
        assertFalse(ys3.value.contains("\n"));
    }

    @Test
    public void testWsCollapse() {
        assertEquals(str4, ys4.value);
        assertTrue(ys4.value.startsWith(" "));
        assertTrue(ys4.value.endsWith(" "));
        assertTrue(ys4.value.contains("   "));
        ys4.wsCollapse();
        assertNotEquals(str4, ys4.value);
        assertFalse(ys4.value.startsWith(" "));
        assertFalse(ys4.value.endsWith(" "));
        assertFalse(ys4.value.contains("   "));
        assertEquals("Hi there", ys4.value);
    }

}

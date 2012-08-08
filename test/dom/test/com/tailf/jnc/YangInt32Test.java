package com.tailf.jnc;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

public class YangInt32Test {

    private short iv1;
    private long iv2;
    private String iv3;
    private YangInt32 i1;
    private YangInt32 i2;
    private YangInt32 i3;

    private YangInt32 inull;
    private YangDecimal64 d;
    
    private YangInt32 tmp1;
    private YangInt16 tmp2;
    private java.io.Serializable tmp3;
    private YangUInt64 tmp4;
    private Short s1;
    private Long l1;
    private String str1;

    @Before
    public void setUp() throws Exception {
        iv1 = 7;
        iv2 = 0xffff;
        iv3 = "13";
        i1 = new YangInt32(iv1);
        i2 = new YangInt32(iv2);
        i3 = new YangInt32(iv3);
        
        inull = new YangInt32(iv3);
        inull.value = null;
        d = new YangDecimal64(1, 0);
        
        tmp1 = new YangInt32(7);
        tmp2 = new YangInt16(7);
        tmp3 = new YangInt32(7);
        tmp4 = new YangUInt64(7);
        s1 = 7;
        l1 = 7L;
        str1 = "7";
    }

    @Test
    public void testMin() throws YangException {
        i1.min(Short.MIN_VALUE);
        i1.min(Integer.MIN_VALUE);
        
        i2.min(Short.MAX_VALUE);  // 0xffff Smaller than Short.MAX_VALUE
        
        i1.min(-1);
        i1.min(0);
        
        try {
            i1.min(Short.MAX_VALUE);
            fail("7 should compare smaller than Short.MAX_VALUE");
        } catch (YangException e) {}
        i2.min(Short.MAX_VALUE);  // 0xffff Smaller than Short.MAX_VALUE
        try {
            i1.min(Integer.MAX_VALUE);
            fail("7 should compare smaller than Integer.MAX_VALUE");
        } catch (YangException e) {}
        try {
            i2.min(Integer.MAX_VALUE);
            fail("0xffff should compare smaller than Integer.MAX_VALUE");
        } catch (YangException e) {}
        
        try {
            i1.min(0xffff);
            fail("7 should compare smaller than 0xffff");
        } catch (YangException e) {}
        
        i2.min(0xffff);  // 0xffff Smaller than 0xffff
        
        try {
            i2.min(0x10000);
            fail("0x100000 valid and/or 0xffff smaller than 0x10000");
        } catch (YangException e) {}
    }

    @Test
    public void testMax() throws YangException {
        i2.max(Integer.MAX_VALUE);  // Integer.MAX_VALUE should be valid
        i2.max(0x10000);  // 0x100000 should be valid
        try {
            i2.max(-1);
            fail("0xffff should compare larger than -1");
        } catch (YangException e) {}
        try {
            i2.max(-0x8000);
            fail("0xffff should compare larger than -0x8000");
        } catch (YangException e) {}
        try {
            i2.max(Short.MAX_VALUE);
            fail("0xffff should compare larger than Short.MAX_VALUE");
        } catch (YangException e) {}

        try {
            i2.max(0);
            fail("0xffff should be larger than 0");
        } catch (YangException e) {}
        i2.max(0xffff);  // 0xffff should not compare smaller than 0xffff
    }

    @Test
    public void testDecodeString() {
        assertFalse(i1.decode("7").equals((byte)7));
        assertFalse(i1.decode("7").equals((short)7));
        assertTrue(i1.decode("7").equals((int)7));
        assertFalse(i1.decode("7").equals((long)7));
        
        assertTrue(i1.decode("7") == (byte)7);
        assertTrue(i1.decode("7") == (short)7);
        assertTrue(i1.decode("7") == (int)7);
        assertTrue(i1.decode("7") == (long)7);

        assertTrue(i1.decode("-1") == -1);
        
        assertTrue(i1.decode(Integer.valueOf(Integer.MAX_VALUE).toString())
                == Integer.MAX_VALUE);
        try {
            i1.decode(Long.valueOf(Integer.MAX_VALUE + 1L).toString());
            fail("Should not be able to parse such a large number");
        } catch (NumberFormatException e) {}
        
        assertTrue(i1.decode(Integer.valueOf(Integer.MIN_VALUE).toString())
                == Integer.MIN_VALUE);
        try {
            i1.decode(Long.valueOf(Integer.MIN_VALUE - 1L).toString());
            fail("Should not be able to parse such a small number");
        } catch (NumberFormatException e) {}

        try {
            i1.fromString("k");
            fail("Should not accept non numbers");
        } catch (YangException e) {}
        try {
            i1.fromString("1k");
            fail("Should not accept strings ending with characters");
        } catch (YangException e) {}
        try {
            i1.fromString("k1");
            fail("Should not accept strings beginning with characters");
        } catch (YangException e) {}
    }

    @Test
    public void testCheck() throws YangException {
        try {
            inull.check();
            fail("Expected YangException since value is null");
        } catch (YangException e) {}
        i1.check();
        i2.check();
        i3.check();
    }

    @Test
    public void testCanEqual() throws YangException {
        assertTrue("Reflexive", i1.canEqual(i1));
        assertTrue("Transitive", !(i1.canEqual(i2) && i2.canEqual(i3))
                || i1.canEqual(i3));

        YangInt64 l = new YangInt64("89");
        YangInt8 b = new YangInt8(-1);
        assertTrue(i2.canEqual(l));
        assertTrue(i2.canEqual(b));
        assertFalse(i2.canEqual(iv1));

        assertTrue(i2.canEqual(d));
        assertFalse(d.canEqual(i2));
        assertFalse("Symmetry", i1.canEqual(d) == d.canEqual(i1));
    }

    @Test
    public void testExact() throws YangException {
        i1.exact(7);
        i2.exact(0xffff);
        i3.exact(13);
        
        try {
            i1.exact(0);
            fail("i1 is not 0");
        } catch (YangException e) {}
        try {
            i1.exact(0xffff);
            fail("i1 is not 0xffff");
        } catch (YangException e) {}
        
        try {
            i2.exact(-0x8000);
            fail("i2 is not -0x8000");
        } catch (YangException e) {}
        try {
            i2.exact(0x10000);
            fail("i2 is not 0x10000");
        } catch (YangException e) {}
        
        i2.exact((int)0x10000ffffL);
        try {
            i2.exact((int)0x1000fffffL);
            fail("i2 is not 0xfffff");
        } catch (YangException e) {}
    }

    @Test
    public void testValid() {
        assertTrue(i1.valid(0));
        assertTrue(i1.valid(-1));
        assertTrue(i1.valid(Byte.MAX_VALUE));
        assertTrue(i1.valid(Byte.MIN_VALUE));
        assertTrue(i1.valid(Short.MAX_VALUE));
        assertTrue(i1.valid(Short.MIN_VALUE));
        assertTrue(i1.valid(Integer.MAX_VALUE));
        assertTrue(i1.valid(Integer.MIN_VALUE));
        assertTrue(i1.valid(0xffff));
        assertTrue(i1.valid(0x10000));
        assertTrue(i1.valid(-0xffff));
        assertTrue(i1.valid(-0x10000));

        assertTrue(i1.valid(BigInteger.ZERO));
        assertTrue(i1.valid(BigInteger.ONE));
        assertTrue(i1.valid(BigInteger.ONE.negate()));
        assertTrue(i1.valid(new BigInteger("65535")));
        assertTrue(i1.valid(new BigInteger("65536")));
        
        assertFalse(i1.valid(Long.MAX_VALUE));
        assertFalse(i1.valid(Long.MIN_VALUE));
    }

    @Test
    public void testHashCode() {
        assertTrue(i1 + "not 7", i1.hashCode() == 7);
        assertTrue(i2 + "not 65535", i2.hashCode() == 0xffff);
        assertTrue(i3 + "not 13", i3.hashCode() == 13);
    }

    @Test
    public void testSetValueString() throws YangException {
        assertTrue(i1.value == 7);
        i1.setValue("8");
        assertFalse(i1.value == 7);
        assertTrue(i1.value == 8);
        try {
            i1.setValue("xxx");
            fail("Should not be able to set the value to xxx");
        } catch (YangException e) {}
        i1.setValue("-1");  // Should be able to set a negative value
        i1.setValue("65536");  // 65536 should not be too large
        
        i1.setValue("0xffff");
        assertFalse(i1.value == 7);
        assertFalse(i1.value == 8);
        assertTrue(i1.value == 0xffff);

        i1.setValue("-0x1");  // Should be able to set a negative value
        i1.setValue("0xffffff"); // 0xffffff should not be too large
    }

    @Test
    public void testSetValueInteger() throws YangException {
        assertTrue(i1.value == 7);
        i1.setValue(8);
        assertFalse(i1.value == 7);
        assertTrue(i1.value == 8);
        
        i1.setValue(-1);  // Should be able to set a negative value
        i1.setValue(65536);  // 65536 should not be too large
        
        i1.setValue(0xffff);
        assertFalse(i1.value == 7);
        assertFalse(i1.value == 8);
        assertTrue(i1.value == 0xffff);

        i1.setValue(-0x1);  // Should be able to set a negative value
        i1.setValue(0xffffff); // 0xffffff should not be too large
    }

    @Test
    public void testToString() {
        assertTrue(i1 + "not 7", i1.toString().equals("7"));
        assertTrue(i2 + "not 65535", i2.toString().equals("65535"));
        assertTrue(i3 + "not 13", i3.toString().equals("13"));
    }

    @Test
    public void testEqualsObject() throws YangException {

        assertTrue("Reflexive", i1.equals(i1));
        assertTrue("Reflexive", tmp1.equals(tmp1));
        assertTrue("Reflexive", tmp3.equals(tmp3));

        assertTrue("Symmetric", i1.equals(tmp1) == tmp1.equals(i1));
        assertTrue(i1.equals(tmp1));
        assertTrue("Symmetric", i1.equals(tmp2) == tmp2.equals(i1));
        assertTrue(i1.equals(tmp2));
        assertTrue("Symmetric", i1.equals(tmp3) == tmp3.equals(i1));
        assertTrue(i1.equals(tmp3));
        assertTrue("Symmetric", i1.equals(tmp4) == tmp4.equals(i1));
        assertTrue(i1.equals(tmp4));
        assertTrue("Symmetric", tmp2.equals(tmp3) == tmp3.equals(tmp2));
        assertTrue(tmp2.equals(tmp3));

        assertTrue("Symmetric", i1.equals(s1) == s1.equals(i1));
        assertFalse(i1.equals(s1));
        assertTrue("Symmetric", i1.equals(iv1) == ((Short)iv1).equals(i1));
        assertFalse(i1.equals(iv1));
        assertTrue("Symmetric", s1.equals(iv1) == ((Short)iv1).equals(s1));
        assertTrue(s1.equals(iv1));
        
        assertTrue("Symmetric", s1.equals(l1) == l1.equals(s1));
        assertFalse(s1.equals(l1));
        assertTrue("Symmetric", i1.equals(str1) == str1.equals(i1));
        assertFalse(i1.equals(str1));

        // Transitivity: (A r B and B r C) implies (A r C)
        // A1 implies A2: (not A1) or A2
        assertTrue("Transitive", !(i1.equals(tmp1) && tmp1.equals(tmp3))
                || i1.equals(tmp3));
        assertTrue("Transitive", !(i1.equals(s1) && s1.equals(iv1))
                || i1.equals(iv1));
        assertTrue("Transitive", !(i1.equals(tmp4) && tmp4.equals(tmp2))
                || i1.equals(tmp2));
        assertTrue("Transitive", !(i1.equals(s1) && s1.equals(l1))
                || i1.equals(l1));
        assertTrue("Transitive", !(i1.equals(l1) && l1.equals(i1))
                || i1.equals(i1));

        assertFalse(i1.equals(i2));
        assertFalse(i1.equals(i3));
        assertFalse(i2.equals(i3));
    }

}

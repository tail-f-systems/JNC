package com.tailf.jnc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

public class YangUInt16Test {

    private short iv1;
    private YangUInt16 i1;
    private YangUInt16 i2;
    private YangUInt16 i3;

    private YangUInt16 inull;
    private YangDecimal64 d;

    private YangUInt16 tmp1;
    private YangInt16 tmp2;
    private java.io.Serializable tmp3;
    private YangUInt64 tmp4;
    private Short s1;
    private Long l1;
    private String str1;

    @Before
    public void setUp() throws Exception {
        iv1 = 7;
        long iv2 = 0xffff;
        String iv3 = "13";
        i1 = new YangUInt16(iv1);
        i2 = new YangUInt16(iv2);
        i3 = new YangUInt16(iv3);

        inull = new YangUInt16(iv3);
        inull.value = null;
        d = new YangDecimal64(1, 0);

        tmp1 = new YangUInt16(7);
        tmp2 = new YangInt16(7);
        tmp3 = new YangUInt16(7);
        tmp4 = new YangUInt64(7);
        s1 = 7;
        l1 = 7L;
        str1 = "7";
    }

    @Test
    public void testMin() throws YangException {
        assertThrows("Short.MIN_VALUE should be invalid", YangException.class, () -> {
            i1.min(Short.MIN_VALUE);
        });
        assertThrows("-1 should be invalid", YangException.class, () -> {
            i1.min(-1);
        });

        i1.min(0);  // Zero boundary value

        assertThrows("7 not smaller than Short.MAX_VALUE", YangException.class, () -> {
            i1.min(Short.MAX_VALUE);
        });
        i2.min(Short.MAX_VALUE);  // 0xffff Smaller than Short.MAX_VALUE

        assertThrows("7 not smaller than 0xffff", YangException.class, () -> {
            i1.min(0xffff);
        });
        i2.min(0xffff);  // 0xffff Smaller than 0xffff

        assertThrows("0x100000 valid and/or 0xffff smaller than 0x10000", YangException.class, () -> {
            i2.min(0x10000);
        });
    }

    @Test
    public void testMax() throws YangException {
        assertThrows("0x100000 should not be valid", YangException.class, () -> {
            i2.max(0x10000);
        });
        assertThrows("-1 should not be valid", YangException.class, () -> {
            i2.max(-1);
        });
        assertThrows("-0x8000 should not be valid", YangException.class, () -> {
            i2.max(-0x8000);
        });

        assertThrows("0xffff should be larger than 0", YangException.class, () -> {
            i2.max(0);
        });
        i2.max(0xffff);  // 0xffff should not be smaller than 0xffff
    }

    @Test
    public void testDecodeString() {
        assertNotEquals(Byte.valueOf((byte)7), i1.decode("7"));
        assertNotEquals(Short.valueOf((short)7), i1.decode("7"));
        assertEquals(Integer.valueOf((int)7), i1.decode("7"));
        assertNotEquals(Long.valueOf((long)7), i1.decode("7"));

        assertTrue(i1.decode("7") == (byte)7);
        assertTrue(i1.decode("7") == (short)7);
        assertEquals((Integer)(int)7, i1.decode("7"));
        assertTrue(i1.decode("7") == (long)7);

        assertEquals(Integer.valueOf(-1), i1.decode("-1"));

        assertEquals(Integer.valueOf(Integer.MAX_VALUE), i1.decode(Integer.valueOf(Integer.MAX_VALUE).toString()));
        assertThrows("Should not be able to parse such a large number", NumberFormatException.class, () -> {
            i1.decode(Long.valueOf(Integer.MAX_VALUE + 1L).toString());
        });

        assertEquals(Integer.valueOf(Integer.MIN_VALUE), i1.decode(Integer.valueOf(Integer.MIN_VALUE).toString()));
        assertThrows("Should not be able to parse such a small number", NumberFormatException.class, () -> {
            i1.decode(Long.valueOf(Integer.MIN_VALUE - 1L).toString());
        });

        assertThrows("Should not accept non numbers", YangException.class, () -> {
            i1.fromString("k");
        });
        assertThrows("Should not accept strings ending with characters", YangException.class, () -> {
            i1.fromString("1k");
        });
        assertThrows("Should not accept strings beginning with characters", YangException.class, () -> {
            i1.fromString("k1");
        });
    }

    @Test
    public void testCheck() throws YangException {
        assertThrows("Expected YangException since value is null", YangException.class, () -> {
            inull.check();
        });
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
        assertNotEquals("Symmetry", i1.canEqual(d), d.canEqual(i1));
    }

    @Test
    public void testExact() throws YangException {
        i1.exact(7);
        i2.exact(0xffff);
        i3.exact(13);

        assertThrows("i1 is not 0", YangException.class, () -> {
            i1.exact(0);
        });
        assertThrows("i1 is not 0xffff", YangException.class, () -> {
            i1.exact(0xffff);

        });

        assertThrows("i2 is not -0x8000", YangException.class, () -> {
            i2.exact(-0x8000);
        });
        assertThrows("i2 is not 0x10000", YangException.class, () -> {
            i2.exact(0x10000);
        });

        i2.exact((int)0x10000ffffL);
        assertThrows("i2 is not 0xfffff", YangException.class, () -> {
            i2.exact((int)0x1000fffffL);
        });
    }

    @Test
    public void testValid() {
        assertTrue(i1.valid(0));
        assertFalse(i1.valid(-1));

        assertTrue(i1.valid(Byte.MAX_VALUE));
        assertFalse(i1.valid(Byte.MIN_VALUE));
        assertTrue(i1.valid(Short.MAX_VALUE));
        assertFalse(i1.valid(Short.MIN_VALUE));

        assertTrue(i1.valid(0xffff));
        assertFalse(i1.valid(0x10000));
        assertFalse(i1.valid(-0xffff));
        assertFalse(i1.valid(-0x10000));

        assertFalse(i1.valid(Integer.MAX_VALUE));
        assertFalse(i1.valid(Integer.MIN_VALUE));
        assertFalse(i1.valid(Long.MAX_VALUE));
        assertFalse(i1.valid(Long.MIN_VALUE));

        assertTrue(i1.valid(BigInteger.ZERO));
        assertTrue(i1.valid(BigInteger.ONE));
        assertFalse(i1.valid(BigInteger.ONE.negate()));
        assertTrue(i1.valid(new BigInteger("65535")));
        assertFalse(i1.valid(new BigInteger("65536")));
    }

    @Test
    public void testHashCode() {
        assertEquals(i1 + "not 7", 7, i1.hashCode());
        assertEquals(i2 + "not 65535", 0xffff, i2.hashCode());
        assertEquals(i3 + "not 13", 13, i3.hashCode());
    }

    @Test
    public void testSetValueString() throws YangException {
        assertEquals((Integer)7, i1.value);
        i1.setValue("8");
        assertNotEquals((Integer)7, i1.value);
        assertEquals((Integer)8, i1.value);
        assertThrows("Should not be able to set the value to xxx", YangException.class, () -> {
            i1.setValue("xxx");
        });
        assertThrows("Should not be able to set a negative value", YangException.class, () -> {
            i1.setValue("-1");
        });
        assertThrows("Should not be able to set a value that is too large", YangException.class, () -> {
            i1.setValue("65536");
        });

        i1.setValue("0xffff");
        assertNotEquals((Integer)7, i1.value);
        assertNotEquals((Integer) 8, i1.value);
        assertEquals((Integer) 0xffff, i1.value);

        assertThrows("Should not be able to set a negative value", YangException.class, () -> {
            i1.setValue("-0x1");
        });
        assertThrows("Should not be able to set a value that is too large", YangException.class, () -> {
            i1.setValue("0xffffff");
        });
    }

    @Test
    public void testSetValueInteger() throws YangException {
        assertEquals((Integer)7, i1.value);
        i1.setValue(8);
        assertNotEquals((Integer)7, i1.value);
        assertEquals((Integer)8, i1.value);
        assertThrows("Should not be able to set a negative value", YangException.class, () -> {
            i1.setValue(-1);
        });
        assertThrows("Should not be able to set a value that is too large", YangException.class, () -> {
            i1.setValue(65536);
        });

        i1.setValue(0xffff);
        assertNotEquals((Integer)7, i1.value);
        assertNotEquals((Integer)8, i1.value);
        assertEquals((Integer)0xffff, i1.value);

        assertThrows("Should not be able to set a negative value", YangException.class, () -> {
            i1.setValue(-0x1);
        });
        assertThrows("Should not be able to set a value that is too large", YangException.class, () -> {
            i1.setValue(0xffffff);
        });
    }

    @Test
    public void testToString() {
        assertEquals(i1 + "not 7", "7", i1.toString());
        assertEquals(i2 + "not 65535", "65535", i2.toString());
        assertEquals(i3 + "not 13", "13", i3.toString());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEqualsObject() throws YangException {

        assertEquals("Reflexive", i1, i1);
        assertEquals("Reflexive", tmp1, tmp1);
        assertEquals("Reflexive", tmp3, tmp3);

        assertEquals("Symmetric", tmp1.equals(i1), i1.equals(tmp1));
        assertEquals(tmp1, i1);
        assertEquals("Symmetric", tmp2.equals(i1), i1.equals(tmp2));
        assertEquals(tmp2, i1);
        assertEquals("Symmetric", tmp3.equals(i1), i1.equals(tmp3));
        assertEquals(tmp3, i1);
        assertEquals("Symmetric", tmp4.equals(i1), i1.equals(tmp4));
        assertEquals(tmp4, i1);
        assertEquals("Symmetric", tmp3.equals(tmp2), tmp2.equals(tmp3));
        assertEquals(tmp3, tmp2);

        assertEquals("Symmetric", s1.equals(i1), i1.equals(s1));
        assertNotEquals(s1, i1);
        assertEquals("Symmetric", ((Short)iv1).equals(i1), i1.equals(iv1));
        assertNotEquals(iv1, i1);
        assertEquals("Symmetric", ((Short)iv1).equals(s1), s1.equals(iv1));
        assertEquals(s1, Short.valueOf(iv1));

        assertEquals("Symmetric", l1.equals(s1), s1.equals(l1));
        assertNotEquals(l1, s1);
        assertEquals("Symmetric", str1.equals(i1), i1.equals(str1));
        assertNotEquals(str1, i1);

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

        assertNotEquals(i2, i1);
        assertNotEquals(i3, i1);
        assertNotEquals(i3, i2);
    }

}

package com.tailf.jnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

public class YangDecimal64Test {

    private YangDecimal64 d1;
    private YangDecimal64 d2;
    private YangDecimal64 d3;
    YangDecimal64 tmp1;

    @Before
    public void setUp() throws YangException {
        d1 = new YangDecimal64(0, 1);
        d2 = new YangDecimal64("3.14", 2);
        d3 = new YangDecimal64(new BigDecimal("3.14"), 1);
    }

    @Test
    public void testCheck() throws YangException {
        d1.check();
        d2.check();
        d3.check();
    }

    @Test
    public void testToString() {
        assertEquals(d1 + "not 0", "0", d1.toString());
        assertEquals(d2 + "not 3.14", "3.14", d2.toString());
        assertEquals(d3 + "not 3.14", "3.14", d3.toString());
    }

    @Test
    public void testEqualsObject() throws YangException {
        assertEquals(d1, d1);
        assertEquals(new YangDecimal64(0, 1), d1);
        assertNotEquals(d2, d1);
        assertNotEquals(d3, d1);
        assertNotEquals(d3, d2);
    }

    @Test
    public void testYangDecimal64StringInt() throws YangException {
        tmp1 = new YangDecimal64("0", 1);
        assertEquals(0, tmp1.value.intValue());
        assertEquals(1, tmp1.getFractionDigits());

        tmp1 = new YangDecimal64("0.01000", 5);
        assertTrue(Math.abs(tmp1.value.doubleValue() - 0.01) < Utils.EPSILON);
        assertEquals(5, tmp1.getFractionDigits());
    }

    @Test
    public void testYangDecimal64NumberInt() throws YangException {
        tmp1 = new YangDecimal64(0, 1);
        assertEquals(0, tmp1.value.intValue());
        assertEquals(1, tmp1.getFractionDigits());

        tmp1 = new YangDecimal64(0.01, 5);
        assertTrue(Math.abs(tmp1.value.doubleValue() - 0.01) < Utils.EPSILON);
        assertEquals(5, tmp1.getFractionDigits());
    }

    @Test
    public void testSetValueString() throws YangException {
        assertTrue(Math.abs(d1.value.doubleValue()) < Utils.EPSILON);
        assertEquals(1, d1.getFractionDigits());
        d1.setValue("-0.1");
        assertTrue(Math.abs(d1.value.doubleValue()+0.1) < Utils.EPSILON);
    }

    @Test
    public void testSetValueNumber() throws YangException {
        assertTrue(Math.abs(d1.value.doubleValue()) < Utils.EPSILON);
        assertEquals(1, d1.getFractionDigits());
        d1.setValue(-0.1);
        assertTrue(Math.abs(d1.value.doubleValue()+0.1) < Utils.EPSILON);

        d1.setValue(0);
        assertTrue(Math.abs(d1.value.doubleValue()) < Utils.EPSILON);
        d1.setValue(new BigDecimal("-0.1"));
        assertTrue(Math.abs(d1.value.doubleValue()+0.1) < Utils.EPSILON);
    }

    @Test
    public void testDecodeString() {
        assertEquals(7, d1.decode("7").byteValue());
    }

    @Test
    public void testExact() throws YangException {
        d1.exact(0);
        d2.exact(new BigDecimal("3.14"));
        d3.exact(new BigDecimal("3.14"));

        assertThrows("Truncation should not occur", YangException.class, () -> {
            (new YangDecimal64(0.945645, 1)).exact(0);
        } );
        assertThrows("Rounding should not occur", YangException.class, () -> {
            d2.exact(3);
            d3.exact(3);
        } );

        // Invalid fraction digits
        assertThrows("Should not accept that high precision", YangException.class, () -> {
            new YangDecimal64("0.9999999999999999999", 19);
        } );

        BigDecimal bd19 = new BigDecimal("0.9999999999999999999");
        YangDecimal64 d18 = new YangDecimal64(bd19, 18);
        d18.exact(1);

        // Outside precision
        YangDecimal64 d17 = new YangDecimal64("0.99999999999999999", 17);
        assertThrows("Outside precision", YangException.class, () -> {
            d17.exact(1);
       } );
    }

    @Test
    public void testMin() throws YangException {
        d1.min(Integer.MIN_VALUE);
        d2.min(Integer.MIN_VALUE);
        d3.min(Integer.MIN_VALUE);

        assertThrows("No value should be larger than MAX_VALUE", YangException.class, () -> {
            d1.min(Integer.MAX_VALUE);
        });
        assertThrows("No value should be larger than MAX_VALUE", YangException.class, () -> {
            d2.min(Integer.MAX_VALUE);
        });
        assertThrows("No value should be larger than MAX_VALUE", YangException.class, () -> {
            d3.min(Integer.MAX_VALUE);
        });

        // Tight integer lower bound
        d1.min(0);
        d2.min(3);
        d3.min(3);
    }

    @Test
    public void testMax() throws YangException {
        d1.max(Integer.MAX_VALUE);
        d2.max(Integer.MAX_VALUE);
        d3.max(Integer.MAX_VALUE);

        assertThrows("No value should be smaller than MIN_VALUE", YangException.class, () -> {
            d1.max(Integer.MIN_VALUE);
        });
        assertThrows("No value should be smaller than MIN_VALUE", YangException.class, () -> {
            d2.max(Integer.MIN_VALUE);
        });
        assertThrows("No value should be smaller than MIN_VALUE", YangException.class, () -> {
            d3.max(Integer.MIN_VALUE);
        });

        // Tight integer lower bound
        d1.max(0);
        d2.max(4);
        d3.max(4);

        assertThrows("Truncation should not occur", YangException.class, () -> {
            d2.max(3);
        });
        assertThrows("Truncation should not occur", YangException.class, () -> {
            d3.max(3);
        });
    }

    @Test
    public void testValid() {
        // Valid numbers for d1: -922337203685477580.8 to 922337203685477580.7
        assertTrue(d1.valid(0));
        assertTrue(d1.valid(3.14));
        assertTrue(d1.valid(-1));
        assertTrue(d1.valid(Integer.MIN_VALUE));
        assertTrue(d1.valid(Integer.MAX_VALUE));
        assertTrue(d1.valid(new BigDecimal("-325982539.623620948312049241")));
        assertTrue(d1.valid(new BigDecimal("678235.325908235792384372843")));

        assertFalse(d1.valid(Long.MIN_VALUE));
        assertFalse(d1.valid(Long.MAX_VALUE));
        assertFalse(d1.valid(new BigDecimal("-752389597325739823759827.389")));
        assertFalse(d1.valid(new BigDecimal("18721468758671265235134231.42")));

        assertTrue(d1.valid(-922337203685477580L));
        assertFalse(d1.valid(-922337203685477581L));

        assertTrue(d1.valid(922337203685477580L));
        assertFalse(d1.valid(922337203685477581L));

        assertTrue(d1.valid(new BigDecimal("-922337203685477580.8")));
        assertFalse(d1.valid(new BigDecimal("-922337203685477580.9")));

        assertTrue(d1.valid(new BigDecimal("922337203685477580.7")));
        assertFalse(d1.valid(new BigDecimal("922337203685477580.8")));

        // Valid numbers for d2: -92233720368547758.08 to 92233720368547758.07
        assertTrue(d2.valid(new BigDecimal("-92233720368547758.08")));
        assertFalse(d2.valid(new BigDecimal("-92233720368547758.09")));

        assertTrue(d2.valid(new BigDecimal("92233720368547758.07")));
        assertFalse(d2.valid(new BigDecimal("92233720368547758.08")));
    }

    @Test
    public void testHashCode() {
        int expected = BigDecimal.ZERO.hashCode() << 1;  // 0
        assertEquals("was: "+d2.hashCode(), expected, d1.hashCode());
        expected = new BigDecimal("3.14").hashCode() << 2;   // 38944
        assertEquals("was: "+d2.hashCode(), expected, d2.hashCode());
        expected = new BigDecimal("3.14").hashCode() << 1;   // 19472
        assertEquals("was: "+d3.hashCode(), expected, d3.hashCode());
    }

}

package com.tailf.jnc;

import static org.junit.Assert.*;

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
        d3 = new YangDecimal64("3.14", 1);
    }

    @Test
    public void testCheck() throws YangException {
        d1.check();
        d2.check();
        d3.check();
    }

    @Test
    public void testToString() {
        assertTrue(d1 + "not 0", d1.toString().equals("0"));
        assertTrue(d2 + "not 3.14", d2.toString().equals("3.14"));
        assertTrue(d3 + "not 3.14", d3.toString().equals("3.14"));
    }

    @Test
    public void testEqualsObject() throws YangException {
        assertTrue(d1.equals(d1));
        assertTrue(d1.equals(new YangDecimal64(0, 1)));
        assertFalse(d1.equals(d2));
        assertFalse(d1.equals(d3));
        assertFalse(d2.equals(d3));
    }

    @Test
    public void testYangDecimal64StringInt() throws YangException {
        tmp1 = new YangDecimal64("0", 1);
        assertTrue(tmp1.value.intValue() == 0);
        assertTrue(tmp1.getFractionDigits() == 1);

        tmp1 = new YangDecimal64("0.01", 5);
        assertTrue(Math.abs(tmp1.value.doubleValue() - 0.01) < Utils.EPSILON);
        assertTrue(tmp1.getFractionDigits() == 5);
    }

    @Test
    public void testYangDecimal64NumberInt() throws YangException {
        tmp1 = new YangDecimal64(0, 1);
        assertTrue(tmp1.value.intValue() == 0);
        assertTrue(tmp1.getFractionDigits() == 1);

        tmp1 = new YangDecimal64(0.01, 5);
        assertTrue(Math.abs(tmp1.value.doubleValue() - 0.01) < Utils.EPSILON);
        assertTrue(tmp1.getFractionDigits() == 5);
    }

    @Test
    public void testSetValueStringInt() throws YangException {
        assertTrue(Math.abs(d1.value.doubleValue()) < Utils.EPSILON);
        assertTrue(d1.getFractionDigits() == 1);
        d1.setValue("-0.1", 7);
        assertTrue(Math.abs(d1.value.doubleValue()+0.1) < Utils.EPSILON);
        assertTrue(d1.getFractionDigits() == 7);
    }

    @Test
    public void testSetValueNumberInt() throws YangException {
        assertTrue(Math.abs(d1.value.doubleValue()) < Utils.EPSILON);
        assertTrue(d1.getFractionDigits() == 1);
        d1.setValue(-0.1, 7);
        assertTrue(Math.abs(d1.value.doubleValue()+0.1) < Utils.EPSILON);
        assertTrue(d1.getFractionDigits() == 7);
    }

    @Test
    public void testDecodeString() {
        assertTrue(d1.decode("7").byteValue() == 7);
    }

    @Test
    public void testExact() throws YangException {
        d1.exact(0);
        d2.exact(new BigDecimal("3.14"));
        d3.exact(new BigDecimal("3.14"));

        try {
            (new YangDecimal64(0.945645, 1)).exact(0);
            fail("Truncation should not occur");
        } catch (YangException e) {}
        try {
            d2.exact(3);
            d3.exact(3);
            fail("Rounding should not occur");
        } catch (YangException e) {}
        
        // Within precision
        (new YangDecimal64("0.9999999999999999999", 1)).exact(1);
        (new YangDecimal64("0.999999999999999999", 1)).exact(1);
        
        // Outside precision
        try {
            (new YangDecimal64("0.99999999999999999", 1)).exact(1);
            fail("Outside precision");
        } catch (YangException e) {}
    }

    @Test
    public void testMin() throws YangException {
        d1.min(Integer.MIN_VALUE);
        d2.min(Integer.MIN_VALUE);
        d3.min(Integer.MIN_VALUE);

        try {
            d1.min(Integer.MAX_VALUE);
            fail("No value should be larger than MAX_VALUE");
        } catch (YangException e) {}
        try {
            d2.min(Integer.MAX_VALUE);
            fail("No value should be larger than MAX_VALUE");
        } catch (YangException e) {}
        try {
            d3.min(Integer.MAX_VALUE);
            fail("No value should be larger than MAX_VALUE");
        } catch (YangException e) {}
        
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

        try {
            d1.max(Integer.MIN_VALUE);
            fail("No value should be smaller than MIN_VALUE");
        } catch (YangException e) {}
        try {
            d2.max(Integer.MIN_VALUE);
            fail("No value should be smaller than MIN_VALUE");
        } catch (YangException e) {}
        try {
            d3.max(Integer.MIN_VALUE);
            fail("No value should be smaller than MIN_VALUE");
        } catch (YangException e) {}
        
        // Tight integer lower bound
        d1.max(0);
        d2.max(4);
        d3.max(4);

        try {
            d2.max(3);
            fail("Truncation should not occur");
        } catch (YangException e) {}
        try {
            d3.max(3);
            fail("Truncation should not occur");
        } catch (YangException e) {}
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
        int expected = new BigDecimal("0").hashCode() << 1;  // 0
        assertTrue("was: "+d2.hashCode(), d1.hashCode() == expected);
        expected = new BigDecimal("3.14").hashCode() << 2;   // 38944
        assertTrue("was: "+d2.hashCode(), d2.hashCode() == expected);
        expected = new BigDecimal("3.14").hashCode() << 1;   // 19472
        assertTrue("was: "+d3.hashCode(), d3.hashCode() == expected);
    }

    @Test
    public void testSetValueString() throws YangException {
        assertTrue(Math.abs(d1.value.doubleValue()) < Utils.EPSILON);
        d1.setValue("-0.1");
        assertTrue(Math.abs(d1.value.doubleValue()+0.1) < Utils.EPSILON);
    }

    @Test
    public void testSetValueBigDecimal() throws YangException {
        assertTrue(Math.abs(d1.value.doubleValue()) < Utils.EPSILON);
        d1.setValue(new BigDecimal("-0.1"));
        assertTrue(Math.abs(d1.value.doubleValue()+0.1) < Utils.EPSILON);
    }

}

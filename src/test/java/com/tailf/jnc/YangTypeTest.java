package com.tailf.jnc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

public class YangTypeTest {

    private YangType<Object> objType;
    private YangType<Object> strType;
    private YangType<Short> shortType1;
    private YangType<Short> shortType2;
    private YangType<Long> longType;
    private YangInt32 i32;

    private class YangTypeDummy<T> extends YangBaseType<T> {
        private static final long serialVersionUID = 1L;
        public YangTypeDummy() {
            // nothing to do
        }
        public YangTypeDummy(String s) throws YangException { super(s); }
        public YangTypeDummy(T t) throws YangException { super(t); }
        @SuppressWarnings("unchecked") @Override
        protected T fromString(String s) { return (T)s; }
        @Override public void check() throws YangException {
            // no checks
        }
        @Override public boolean canEqual(Object obj) {
            return value != null && obj instanceof YangTypeDummy;
        }
        @Override protected YangTypeDummy<T> cloneShallow()
                throws YangException {
            return new YangTypeDummy<T>(value.toString());
        }
    }

    @Before
    public void setUp() throws Exception {
        objType = new YangTypeDummy<Object>("7");
        strType = new YangTypeDummy<Object>();
        shortType1 = new YangTypeDummy<Short>((short)7);
        shortType2 = new YangInt16("7");
        longType = new YangTypeDummy<Long>(7L);
        i32 = new YangInt32(7);
    }

    @Test
    public void testHashCode() {
        int hcode = objType.hashCode();
        assertEquals(hcode + " not 7", "7".hashCode(), hcode);
        assertEquals(0, strType.hashCode());
        assertEquals(7, shortType1.hashCode());
        assertEquals(7, shortType2.hashCode());
        assertEquals(7, longType.hashCode());
    }

    @Test
    public void testEquals() throws YangException {
        assertEquals(shortType1, shortType1);

        // YangInt16 can't equal non-number types
        assertNotEquals(shortType2, shortType1);

        assertEquals(longType, shortType1);
        assertNotEquals(objType, shortType1);
        assertNotEquals(shortType1, objType);

        assertNotEquals((Object)7, shortType1);
        assertNotEquals((Object)7L, shortType1);
        assertNotEquals((Object)"7", shortType1);
        assertNotEquals(null, shortType1);

        assertNotEquals(objType, strType);
        strType.setValue("7");
        assertEquals(objType, strType);
    }

    @Test
    public void testToString() {
        assertEquals("7", shortType1.toString());
        assertEquals("7", shortType2.toString());
        assertEquals("7", longType.toString());
        assertEquals("7", objType.toString());
    }

    @Test(expected=NullPointerException.class)
    public void testToStringException2() {
        assertEquals("null", strType.toString());
    }

    @Test
    public void testFromString() throws YangException {
        assertNotEquals(Byte.valueOf((byte)7), i32.fromString("7"));
        assertNotEquals(Short.valueOf((short)7), i32.fromString("7"));
        assertEquals(Integer.valueOf((int)7), i32.fromString("7"));
        assertNotEquals(Long.valueOf((long)7), i32.fromString("7"));

        assertTrue(i32.fromString("7") == (byte)7);
        assertTrue(i32.fromString("7") == (short)7);
        assertEquals(Integer.valueOf((int)7), i32.fromString("7"));
        assertTrue(i32.fromString("7") == (long)7);

        assertEquals(Integer.valueOf(-1), i32.fromString("-1"));

        assertEquals(Integer.valueOf(Integer.MAX_VALUE), i32.fromString(Integer.valueOf(Integer.MAX_VALUE).toString()));
        assertThrows("Should not be able to parse such a large number", YangException.class, () -> {
            i32.fromString(Long.valueOf(Integer.MAX_VALUE + 1L).toString());
        });

        assertEquals(Integer.valueOf(Integer.MIN_VALUE), i32.fromString(Integer.valueOf(Integer.MIN_VALUE).toString()));
        assertThrows(YangException.class, () -> {
            i32.fromString(Long.valueOf(Integer.MIN_VALUE - 1L).toString());
        });

        assertThrows(YangException.class, () -> {
            i32.fromString("a");
        });
        assertThrows(YangException.class, () -> {
            i32.fromString("1a");
        });
        assertThrows(YangException.class, () -> {
            i32.fromString("a1");
        });
    }
}

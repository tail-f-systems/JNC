package com.tailf.jnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;

public class YangBaseStringTest {

    private YangBaseString bs;
    private YangBaseString empty;
    private YangBaseString spacy;
    private YangBaseString nullary;

    @Before
    public void setUp() throws Exception {
        bs = new YangBaseString("baseString");
        empty = new YangBaseString("");
        spacy = new YangBaseString("  A\t  space   ");
        nullary = null;
    }

    @Test
    public void testSetValueString() throws YangException {
        assertThrows(NullPointerException.class, () -> {
            nullary.setValue("setValue");
        });

        assertEquals("baseString", bs.value);
        bs.setValue("newString");
        assertEquals("newString", bs.value);

        try {
            bs.setValue(null);
            fail("Expected YangException");
        } catch (YangException e) {
            assertTrue(e.opaqueData instanceof NullPointerException);
        }
        assertNotEquals(null, bs.value);
    }

    @Test
    public void testCheck() throws YangException {
        bs.check();
        empty.check();
        assertThrows(NullPointerException.class, () -> {
            nullary.check();
        });
    }

    @Test
    public void testCanEqual() {
        assertTrue(bs.canEqual(bs));
        assertTrue(bs.canEqual(empty));
        assertFalse(bs.canEqual(nullary));
        assertFalse(bs.canEqual(new String("baseString")));
    }

    @Test
    public void testBaseString() throws YangException {
        assertThrows(YangException.class, () -> {
            nullary = new YangBaseString(null);
            fail("Expected NullPointerException");
        });
        assertEquals(null, nullary);

        nullary = new YangBaseString("nullary");
        assertEquals("nullary", nullary.value);
    }

    @Test
    public void testFromString() {
        assertEquals("baseString", bs.fromString("baseString"));
        assertEquals("fromString", bs.fromString("fromString"));
        assertEquals(bs.value + " is not baseString", "baseString", bs.value);
    }

    @Test
    public void testPatternString() throws YangException {
        bs.pattern("baseString");
        bs.pattern("base.*");
        bs.pattern(".*String");
        bs.pattern("b...S.*");
        bs.pattern("[a-zS]*");
        bs.pattern(".*");
        bs.pattern("[bzaszeSztrzinzg]+");
        empty.pattern("");
        empty.pattern(".*");

        assertThrows(YangException.class, () -> {
            bs.pattern("[a-z]*");
        });
        try {
            bs.pattern("[a*(\\");
            fail("Expected syntax error");
        } catch (YangException e) {
            assertTrue("Expected syntax error",
                    e.opaqueData instanceof PatternSyntaxException);
        }

        assertThrows(NullPointerException.class, () -> {
            nullary.pattern("null");
        });
        assertThrows(NullPointerException.class, () -> {
            bs.pattern((String) null);
        });

        assertThrows(YangException.class, () -> {
            bs.pattern("");
        });
    }

    @Test
    public void testPatternStringArray() throws YangException {
        assertThrows(NullPointerException.class, () -> {
            nullary.pattern(new String[] { "null" });
        });
        assertThrows(NullPointerException.class, () -> {
            bs.pattern((String[]) null);
        });

        bs.pattern(new String[] {});
    }

    @Test
    public void testWsReplace() {
        assertEquals("  A\t  space   ", spacy.value);
        spacy.wsReplace();
        assertEquals("  A   space   ", spacy.value);
    }

    @Test
    public void testWsCollapse() {
        assertEquals("  A\t  space   ", spacy.value);
        spacy.wsCollapse();
        assertEquals("A\t space", spacy.value);
        spacy.wsReplace();
        assertEquals("A  space", spacy.value);
        spacy.wsCollapse();
        assertEquals("A space", spacy.value);
    }

    @Test
    public void testHashCode() {
        assertEquals("baseString".hashCode(), bs.hashCode());
        assertEquals(0, empty.hashCode());
        assertEquals("  A\t  space   ".hashCode(), spacy.hashCode());
        spacy.wsReplace();
        spacy.wsCollapse();
        assertNotEquals("  A\t  space   ".hashCode(), spacy.hashCode());
        assertEquals("A space".hashCode(), spacy.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals(bs.value, bs.toString());
        assertEquals("", empty.toString());
        assertEquals(spacy.value, spacy.toString());
        assertNotEquals(bs.value, spacy.toString());
        assertNotEquals(empty.value, spacy.toString());
        spacy.value = "";
        assertEquals(empty.value, spacy.toString());
    }

    @Test
    public void testEquals() {
        assertEquals(bs, bs);
        assertNotEquals(null, bs);
        assertNotEquals("baseString", bs);
    }

    @Test
    public void testExact() throws YangException {
        bs.exact("baseString".length());
        assertThrows(YangException.class, () -> {
            bs.exact(0);
        });
        assertThrows(YangException.class, () -> {
            bs.exact(-1);
        });
        assertThrows(YangException.class, () -> {
            bs.exact(Integer.MAX_VALUE);
        });

        empty.exact(0);
        spacy.exact("  A\t  space   ".length());
        spacy.wsReplace();
        spacy.exact("  A\t  space   ".length());
        spacy.wsCollapse();
        spacy.exact("A space".length());
    }

    @Test
    public void testMin() throws YangException {
        bs.min("baseString".length());
        bs.min("baseString".length() - 1);
        bs.min(0);
        bs.min(-1);
        bs.min(Integer.MIN_VALUE);

        assertThrows(YangException.class, () -> {
            bs.min(Integer.MAX_VALUE);
        });
        assertThrows(YangException.class, () -> {
            bs.min("baseString".length() + 1);
        });
        bs.value = "base";
        bs.min("base".length());
        assertThrows(YangException.class, () -> {
            bs.min("baseString".length());
        });

        empty.min(0);
        spacy.min(0);
    }

    @Test
    public void testMax() throws YangException {
        bs.max("baseString".length());
        bs.max("baseString".length() + 1);
        bs.max(Integer.MAX_VALUE);

        assertThrows(YangException.class, () -> {
            bs.max(Integer.MIN_VALUE);
        });
        assertThrows(YangException.class, () -> {
            bs.max("baseString".length() - 1);
        });
        assertThrows(YangException.class, () -> {
            bs.max(0);
        });
        assertThrows(YangException.class, () -> {
            bs.max(-1);
        });

        bs.value = "base";
        bs.max("base".length());
        bs.max("baseString".length());
        assertThrows(YangException.class, () -> {
            bs.max("bas".length());
        });

        empty.max(0);
        assertThrows(YangException.class, () -> {
            spacy.max(0);
        });
    }

}

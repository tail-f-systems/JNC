package com.tailf.jnc;

import static org.junit.Assert.*;

import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;

public class UtilsTest {

    private YangString abc;
    private YangString empty;
    private YangString withSpaces;
    private YangString leading;
    private YangString trailing;
    private YangString withTabs;

    @Before
    public void setUp() throws Exception {
        empty = new YangString("");
        abc = new YangString("abc");
        withSpaces = new YangString("  A string   in space ");
        leading = new YangString("    in space");
        trailing = new YangString("Truth   ");
        withTabs = new YangString("\t\tTwo tabs\n\rTwo lines");
    }

    @Test
    public void testBigDecimalValueOf() {
        fail("Not yet implemented");
    }

    @Test
    public void testRestrictObjectNumberOperator() {
        fail("Not yet implemented");
    }

    @Test
    public void testRestrictNumberBigDecimalOperator() {
        fail("Not yet implemented");
    }

    @Test
    public void testWsCollapse() {
        // wsCollapse should not modify strings without whitespaces
        assertTrue(abc.toString(), abc.value.equals("abc"));
        abc.wsCollapse();
        assertTrue(abc.toString(), abc.value.equals("abc"));
        
        // wsCollapse should work on empty strings
        assertTrue(empty.toString(), empty.value.equals(""));
        empty.wsCollapse();
        assertTrue(empty.toString(), empty.value.equals(""));
        
        // wsCollapse should remove leading spaces
        assertTrue(leading.toString(), leading.value.equals("    in space"));
        leading.wsCollapse();
        assertTrue(leading.toString(), leading.value.equals("in space"));
        
        // wsCollapse should remove trailing spaces
        assertTrue(trailing.toString(), trailing.value.equals("Truth   "));
        trailing.wsCollapse();
        assertTrue(trailing.toString(), trailing.value.equals("Truth"));
        
        // More complex strings should pose no problem
        assertTrue(withSpaces.toString(), withSpaces.value.equals("  A string   in space "));
        withSpaces.wsCollapse();
        assertTrue(withSpaces.toString(), withSpaces.value.equals("A string in space"));
        
        assertTrue(withTabs.toString(), withTabs.value.equals("\t\tTwo tabs\n\rTwo lines"));
        withTabs.wsCollapse();
        assertTrue(withTabs.toString(), withTabs.value.equals("\t\tTwo tabs\n\rTwo lines"));
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.value.equals("  Two tabs  Two lines"));
        withTabs.wsCollapse();
        assertTrue(withTabs.toString(), withTabs.value.equals("Two tabs Two lines"));
    }

    @Test
    public void testWsReplace() {
        // wsReplace should not modify strings without whitespaces
        assertTrue(abc.value.equals("abc"));
        abc.wsReplace();
        assertTrue(abc.value.equals("abc"));
        
        // wsReplace should work on empty strings
        assertTrue(empty.value.equals(""));
        empty.wsReplace();
        assertTrue(empty.value.equals(""));
        
        // More complex strings should pose no problem
        assertTrue(withSpaces.toString(), withSpaces.value.equals("  A string   in space "));
        withSpaces.wsReplace();
        assertTrue(withSpaces.toString(), withSpaces.value.equals("  A string   in space "));
        
        assertTrue(withTabs.toString(), withTabs.value.equals("\t\tTwo tabs\n\rTwo lines"));
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.value.equals("  Two tabs  Two lines"));
        withTabs.wsCollapse();
        assertTrue(withTabs.toString(), withTabs.value.equals("Two tabs Two lines"));
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.value.equals("Two tabs Two lines"));
    }

    @Test
    public void testMatches() throws YangException {
        abc.pattern("abc");
        abc.pattern("[ab]*c+d*");
        try {
            abc.pattern("(abc)*c+d*");
            fail("Did not raise ConfMException for '[abc]*c+d*'");
        } catch (YangException e) {
            assertSame(e.opaqueData.toString(), abc.value, e.opaqueData);
        }
        
        withSpaces.pattern("  A string   in space ");
        withSpaces.pattern(" +A string +in space ");
        withSpaces.pattern(" +A string.*");
        
        withTabs.pattern("\t\tTwo tabs\n\rTwo lines");
        withTabs.pattern("\\s+Two\\stabs\\s+Two\\slines");
        
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.value.equals("  Two tabs  Two lines"));
        withTabs.pattern("  Two tabs  Two lines");
        withTabs.pattern("\\s+Two\\stabs\\s+Two\\slines");
        
        withTabs.wsCollapse();
        withTabs.pattern("Two tabs Two lines");
        withTabs.pattern("Two\\stabs\\sTwo\\slines");
        try {
            withTabs.pattern("\\s+Two\\stabs\\s+Two\\slines");
            fail("Did not raise ConfMException for \\s+Two\\stabs\\s+Two\\slines'");
        } catch (YangException e) {
            assertSame(e.opaqueData.toString(), withTabs.value, e.opaqueData);
        }
        
        empty.pattern(new String[0]);
        java.lang.String[] empty_patterns = {"", ".*"};
        empty.pattern(empty_patterns);
        
        java.lang.String[] abcpatterns = {"abc", "[ab]*c+d*"};
        abc.pattern(abcpatterns);
        
        java.lang.String[] abcpatterns2 = {"(abc)*c+d*"};
        try {
            abc.pattern(abcpatterns2);
            fail("Did not raise YangException for '[abc]*c+d*'");
        } catch (YangException e) {
            assertSame(e.opaqueData.toString(), abc.value, e.opaqueData);
        }
        
        try {
            withSpaces.pattern("**");
            fail("Did not raise YangException for invalid pattern");
        } catch (YangException e) {
            assertTrue(e.opaqueData instanceof PatternSyntaxException);
        }
    }

}

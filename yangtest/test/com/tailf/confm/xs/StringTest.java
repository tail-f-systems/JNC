package com.tailf.confm.xs;

import static org.junit.Assert.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.tailf.confm.ConfMException;

public class StringTest {

    private com.tailf.confm.xs.String empty;
    private com.tailf.confm.xs.String abc;
    private com.tailf.confm.xs.String withSpaces;
    private com.tailf.confm.xs.String leading;
    private com.tailf.confm.xs.String trailing;
    private com.tailf.confm.xs.String withTabs;
    
    @Before
    public void setUp() {
        empty = new com.tailf.confm.xs.String("");
        abc = new com.tailf.confm.xs.String("abc");
        withSpaces = new com.tailf.confm.xs.String("  A string   in space ");
        leading = new com.tailf.confm.xs.String("    in space");
        trailing = new com.tailf.confm.xs.String("Truth   ");
        withTabs = new com.tailf.confm.xs.String("\t\tTwo tabs\n\rTwo lines");
    }

    @Test
    public void testEqualsObject() throws ConfMException {
        assertTrue("abc equal self", abc.equals(abc));
        assertTrue("abc equal object", abc.equals((Object)"abc"));
        assertTrue("abc equal string", abc.equals("abc"));
        assertTrue(abc.equals(new com.tailf.confm.xs.String("abc")));
        assertTrue("abc".equals(abc.getValue()));
        assertFalse("abc".equals(abc));
        assertFalse(abc.equals("abcd"));
        assertFalse(abc.equals(new com.tailf.confm.xs.String("")));
        assertFalse(abc.equals(empty));
        assertFalse(abc.equals(withSpaces));
        assertTrue("withSpaces equal self", withSpaces.equals(withSpaces));
        abc.setValue("abcd");
        assertTrue(abc.equals("abcd"));
        assertFalse("abc equal string", abc.equals("abc"));
    }

    @Test
    public void testWsCollapseString() {
        // wsCollapse should not modify strings without whitespaces
        assertTrue(abc.toString(), abc.equals("abc"));
        abc.wsCollapse();
        assertTrue(abc.toString(), abc.equals("abc"));
        
        // wsCollapse should work on empty strings
        assertTrue(empty.toString(), empty.equals(""));
        empty.wsCollapse();
        assertTrue(empty.toString(), empty.equals(""));
        
        // wsCollapse should remove leading spaces
        assertTrue(leading.toString(), leading.equals("    in space"));
        leading.wsCollapse();
        assertTrue(leading.toString(), leading.equals("in space"));
        
        // wsCollapse should remove trailing spaces
        assertTrue(trailing.toString(), trailing.equals("Truth   "));
        trailing.wsCollapse();
        assertTrue(trailing.toString(), trailing.equals("Truth"));
        
        // More complex strings should pose no problem
        assertTrue(withSpaces.toString(), withSpaces.equals("  A string   in space "));
        withSpaces.wsCollapse();
        assertTrue(withSpaces.toString(), withSpaces.equals("A string in space"));
        
        assertTrue(withTabs.toString(), withTabs.equals("\t\tTwo tabs\n\rTwo lines"));
        withTabs.wsCollapse();
        assertTrue(withTabs.toString(), withTabs.equals("\t\tTwo tabs\n\rTwo lines"));
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.equals("  Two tabs  Two lines"));
        withTabs.wsCollapse();
        assertTrue(withTabs.toString(), withTabs.equals("Two tabs Two lines"));
    }

    @Test
    public void testWsReplaceString() {
        // wsReplace should not modify strings without whitespaces
        assertTrue(abc.equals("abc"));
        abc.wsReplace();
        assertTrue(abc.equals("abc"));
        
        // wsReplace should work on empty strings
        assertTrue(empty.equals(""));
        empty.wsReplace();
        assertTrue(empty.equals(""));
        
        // More complex strings should pose no problem
        assertTrue(withSpaces.toString(), withSpaces.equals("  A string   in space "));
        withSpaces.wsReplace();
        assertTrue(withSpaces.toString(), withSpaces.equals("  A string   in space "));
        
        assertTrue(withTabs.toString(), withTabs.equals("\t\tTwo tabs\n\rTwo lines"));
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.equals("  Two tabs  Two lines"));
        withTabs.wsCollapse();
        assertTrue(withTabs.toString(), withTabs.equals("Two tabs Two lines"));
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.equals("Two tabs Two lines"));
    }

    @Test
    public void testPatternString() throws ConfMException {
        abc.pattern("abc");
        abc.pattern("[ab]*c+d*");
        try {
            abc.pattern("(abc)*c+d*");
            fail("Did not raise ConfMException for '[abc]*c+d*'");
        } catch (ConfMException e) {
            assertSame(e.opaqueData.toString(), abc, e.opaqueData);
        }
        
        withSpaces.pattern("  A string   in space ");
        withSpaces.pattern(" +A string +in space ");
        withSpaces.pattern(" +A string.*");
        
        withTabs.pattern("\t\tTwo tabs\n\rTwo lines");
        withTabs.pattern("\\s+Two\\stabs\\s+Two\\slines");
        
        withTabs.wsReplace();
        assertTrue(withTabs.toString(), withTabs.equals("  Two tabs  Two lines"));
        withTabs.pattern("  Two tabs  Two lines");
        withTabs.pattern("\\s+Two\\stabs\\s+Two\\slines");
        
        withTabs.wsCollapse();
        withTabs.pattern("Two tabs Two lines");
        withTabs.pattern("Two\\stabs\\sTwo\\slines");
        try {
            withTabs.pattern("\\s+Two\\stabs\\s+Two\\slines");
            fail("Did not raise ConfMException for \\s+Two\\stabs\\s+Two\\slines'");
        } catch (ConfMException e) {
            assertSame(e.opaqueData.toString(), withTabs, e.opaqueData);
        }
    }

    @Test
    public void testPatternStringArray() throws ConfMException {
        empty.pattern(new java.lang.String[0]);
        java.lang.String[] empty_patterns = {"", ".*"};
        empty.pattern(empty_patterns);
        
        java.lang.String[] abcpatterns = {"abc", "[ab]*c+d*"};
        abc.pattern(abcpatterns);
        
        java.lang.String[] abcpatterns2 = {"(abc)*c+d*"};
        try {
            abc.pattern(abcpatterns2);
            fail("Did not raise ConfMException for '[abc]*c+d*'");
        } catch (ConfMException e) {
            assertSame(e.opaqueData.toString(), abc, e.opaqueData);
        }
        
        try {
            withSpaces.pattern("**");
            fail("Did not raise ConfMException for invalid pattern");
        } catch (ConfMException e) {
            assertTrue(e.opaqueData instanceof PatternSyntaxException);
        }
    }

}

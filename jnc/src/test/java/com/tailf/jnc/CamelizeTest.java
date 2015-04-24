package com.tailf.jnc;

import static org.junit.Assert.*;

import org.junit.Test;

public class CamelizeTest {

    @Test
    public void test_when_upper_case() {
        String result = YangElement.camelize("TESTSTRING");
        String expected = "teststring";
        String msg = "should convert to lower case";
        assertEquals(msg, expected, result);
    }

    @Test
    public void test_when_lower_camelcase() {
        String result = YangElement.camelize("testString");
        String expected = "testString";
        String msg = "should return string unchanged";
        assertEquals(msg, expected, result);
    }

    @Test
    public void test_when_upper_camelcase() {
        String result = YangElement.camelize("TestString");
        String expected = "testString";
        String msg = "should return string decapitalized";
        assertEquals(msg, expected, result);
    }

    @Test
    public void test_when_contains_hyphens() {
        String result1 = YangElement.camelize("test-string");
        String result2 = YangElement.camelize("TEST-STRING");
        String expected = "testString";
        String msg = "should remove hyphens";
        assertEquals(msg, expected, result1);
        assertEquals(msg, expected, result2);
    }

    @Test
    public void test_when_contains_underlines() {
        String result1 = YangElement.camelize("test_string_");
        String result2 = YangElement.camelize("TEST_STRING_");
        String expected = "test_string_";
        String msg = "should not remove underlines";
        assertEquals(msg, expected, result1);
        assertEquals(msg, expected, result2);
    }

    @Test
    public void test_when_empty() {
        String result = YangElement.camelize("");
        String expected = "";
        String msg = "should return empty string";
        assertEquals(msg, expected, result);
    }

    @Test
    public void test_when_null() {
        String result = YangElement.camelize(null);
        String expected = "";
        String msg = "should return empty string";
        assertEquals(msg, expected, result);
    }

    @Test
    public void test_when_single_character() {
        String result = YangElement.camelize("A");
        String expected = "a";
        String msg = "should return lower case version of string";
        assertEquals(msg, expected, result);
    }

    @Test
    public void test_when_trailing_hyphen() {
        String result = YangElement.camelize("test-");
        String expected = "test-"; // "test" might be better
        String msg = "should not remove hyphen";
        assertEquals(msg, expected, result);
    }

    @Test
    public void test_when_many_dots_and_hyphens() {
        String result = YangElement.camelize("test--...STR.ING.");
        String expected = "test-.StrIng."; // "testStrIng" might be better
        String msg = "should remove all except consecutive and trailing";
        assertEquals(msg, expected, result);
    }
}

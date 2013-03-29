'TEST-STRING'"""
Created on 28 mar 2013

@author: emil@tail-f.com

PyUnit is needed to run these tests.

To run, stand in project dir and enter:
$ python -m unittest discover -v
"""
import unittest

import jnc

class Test(unittest.TestCase):

    def test__camelize__when_string_is_all_upper_case(self):
        result = jnc.camelize('TESTSTRING')
        expected = 'teststring'
        message = 'should convert to lower case'
        assert result == expected, message + ' but was ' + result

    def test__camelize__when_string_is_lower_camelcase(self):
        result = jnc.camelize('testString')
        expected = 'testString'
        message = 'should return string unchanged'
        assert result == expected, message + ' but was ' + result

    def test__camelize__when_string_is_upper_camelcase(self):
        result = jnc.camelize('TestString')
        expected = 'testString'
        message = 'should return string decapitalized'
        assert result == expected, message + ' but was ' + result

    def test__camelize__when_string_contains_hyphens(self):
        result1 = jnc.camelize('test-string')
        result2 = jnc.camelize('TEST-STRING')
        expected = 'testString'
        message = 'should remove hyphens'
        assert result1 == expected, message + ' but was ' + result1
        assert result2 == expected, message + ' but was ' + result2

    def test__camelize__when_string_contains_underlines(self):
        result1 = jnc.camelize('test_string_')
        result2 = jnc.camelize('TEST_STRING_')
        expected = 'test_string_'
        message = 'should not remove underlines'
        assert result1 == expected, message + ' but was ' + result1
        assert result2 == expected, message + ' but was ' + result2

    def test__camelize__when_string_is_empty(self):
        result = jnc.camelize('')
        expected = ''
        message = 'should return empty string'
        assert result == expected, message + ' but was ' + result

    def test__camelize__when_string_is_none(self):
        result = jnc.camelize(None)
        expected = ''
        message = 'should return empty string'
        assert result == expected, message + ' but was ' + result

    def test__camelize__when_string_is_single_character(self):
        result = jnc.camelize('A')
        expected = 'a'
        message = 'should return lower case version of string'
        assert result == expected, message + ' but was ' + result

    def test__camelize__when_string_contains_trailing_hyphen(self):
        result = jnc.camelize('test-')
        expected = 'test-'  # 'test' might be better
        message = 'will not remove hyphen'
        assert result == expected, message + ' but was ' + result

    def test__camelize__when_string_contains_many_dots_and_hyphens(self):
        result = jnc.camelize('test--...STR.ING.')
        expected = 'test-.StrIng.'  # 'testStrIng' might be better
        message = 'will remove all except consecutive and trailing'
        assert result == expected, message + ' but was ' + result

if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']  # Only one
    unittest.main()

"""
Created on 20 jul 2012

@author: emil@tail-f.com

PyUnit is needed to run these tests.

To run, stand in project dir and enter:
$ python -m unittest discover -v
"""
import unittest

from pyang.plugins import jpyang  #@UnresolvedImport
from pyang.tests import util  #@UnresolvedImport


class Test(unittest.TestCase):
    """Contains tests for methods in JPyang.JavaMethod"""

    def setUp(self):
        """Runs before each test"""
        # Construct a statement tree rooted at self.m, and method generators
        util.init_context(self)
        util.create_statement_tree(self)
        util.create_method_generators(self)

    def tearDown(self):
        """Runs after each test"""
        pass

    def testSetUp(self):
        """Statement tree and generators are properly constructed"""
        util.test_default_context(self)
        util.test_statement_tree(self)
        util.test_method_generators(self)

    def testInit(self):
        """Values and references correct in Java Methods of different origin"""
        # Create method with default settings
        method1 = jpyang.JavaMethod()
        assert method1.exact is None
        assert method1.javadocs == []
        assert method1.modifiers == []
        assert method1.return_type is None
        assert method1.name is None
        assert method1.parameters == []
        assert method1.exceptions == []
        assert method1.body == []
        assert method1.indent == ' ' * 4
        
        # Create empty constructor method for container statement c
        method2 = self.cgen.empty_constructor()
        assert method2.exact is None
        assert method2.javadocs
        assert 'public' in method2.modifiers
        assert method2.return_type is None
        assert method2.name == self.cgen.n
        assert method2.parameters == []
        assert method2.exceptions == []
        assert method2.body
        assert method2.indent == ' ' * 4
        
        # Check that no references are shared, even for empty lists
        assert method1.javadocs is not method2.javadocs
        assert method1.modifiers is not method2.modifiers
        assert method1.parameters is not method2.parameters
        assert method1.parameters == method2.parameters
        assert method1.exceptions is not method2.exceptions
        assert method1.exceptions == method2.exceptions
        assert method1.body is not method2.body
        assert method1.indent is not method2.indent
        assert method1.indent == method2.indent

    def testClone(self):
        """Clones have equal string representation but different reference"""
        method = self.cgen.empty_constructor()
        clone = method.clone()
        assert method is method, 'Sanity check'
        assert method == method, 'method.__eq__ should return True'
        assert method is not clone, 'Different reference'
        assert method != clone, 'method.__eq__ should (maybe) return False'
        assert method.as_string() == clone.as_string(), 'Same string repr'

    def testClone_to(self):
        """Clones have equal string representation but different reference"""
        method1 = self.cgen.empty_constructor()
        method2 = jpyang.JavaMethod()
        method1.clone_to(method2)
        assert method1 is not method2, 'Different reference'
        assert method1 != method2, 'method.__eq__ should (maybe) return False'
        assert method1.as_string() == method2.as_string(), 'Same string repr'


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']  # Only one
    unittest.main()
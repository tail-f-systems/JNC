"""
Created on 20 jul 2012

@author: emil@tail-f.com

PyUnit is needed to run these tests.

To run, stand in project dir and enter:
$ python -m unittest discover -v
"""
import unittest

# from pyang.plugins import jpyang  #@UnresolvedImport
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
        """Statement tree in this test is properly constructed"""
        util.test_statement_tree(self)
        util.test_method_generators(self)

    def testInit(self):
        """Values correct in Java Methods created in different ways"""
        return NotImplemented

    def testClone(self):
        """Clones have equal string representation but different reference"""
        method = self.cgen.empty_constructor()
        clone = method.clone()
        assert method is method, 'Sanity check'
        assert method == method, 'method.__eq__ should return True'
        assert method is not clone, 'Different reference'
        assert method != clone, 'method.__eq__ should (maybe) return False'
        assert method.as_string() == clone.as_string(), 'Same string repr'


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']  # Only one
    unittest.main()
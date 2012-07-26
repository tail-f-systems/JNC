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
import copy


class Test(unittest.TestCase):
    """Contains tests for methods in JPyang.JavaMethod"""

    def setUp(self):
        """Runs before each test"""
        # Construct a statement tree rooted at self.m, and method generators
        util.init_context(self)
        util.create_statement_tree(self)
        util.create_method_generators(self)
        self.method = jpyang.JavaMethod()
        self.constructor = self.cgen.empty_constructor()

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
        assert self.method.exact is None
        assert self.method.javadocs == []
        assert self.method.modifiers == []
        assert self.method.return_type is None
        assert self.method.name is None
        assert self.method.parameters == []
        assert self.method.exceptions == []
        assert self.method.body == []
        assert self.method.indent == ' ' * 4
        
        # Create empty constructor method for container statement c
        assert self.constructor.exact is None
        assert self.constructor.javadocs
        assert 'public' in self.constructor.modifiers
        assert self.constructor.return_type is None
        assert self.constructor.name == self.cgen.n
        assert self.constructor.parameters == []
        assert self.constructor.exceptions == []
        assert self.constructor.body
        assert self.constructor.indent == ' ' * 4
        
        # Check that no references to mutable instance data are shared
        assert self.method.javadocs is not self.constructor.javadocs
        assert self.method.modifiers is not self.constructor.modifiers
        assert self.method.parameters is not self.constructor.parameters
        assert self.method.parameters == self.constructor.parameters
        assert self.method.exceptions is not self.constructor.exceptions
        assert self.method.exceptions == self.constructor.exceptions
        assert self.method.body is not self.constructor.body
        assert self.method.indent is not self.constructor.indent
        assert self.method.indent == self.constructor.indent
        assert not self.method.shares_mutables_with(self.constructor)

    def testEq(self):
        """Equality checks with == and != works as expected"""
        method2 = self.constructor
        assert self.constructor is method2, 'Sanity check: same reference'
        assert self.constructor == method2, 'Sanity check: equal objects'
        assert not self.constructor != method2, 'Sanity check: equal objects'
        
        # Test equality between objects returned from same function
        clone = self.cgen.empty_constructor()
        assert self.constructor is not clone, 'Different reference'
        assert self.constructor == clone, 'But still equal'
        clone.return_type = 'bogus'
        assert self.constructor != clone, 'return_type matters for equality'
        assert not(self.constructor == clone), 'check both __eq__ and __ne__'
        clone.return_type = None
        assert self.constructor == clone, 'Should be equal again'
        
        # Test that deleted attributes are handled correctly
        del clone.return_type
        assert not(self.constructor == clone), 'Not equal if attribute is missing'
        clone.return_type = None
        assert self.constructor == clone, 'Should be equal again'

    def testShares_mutables_with(self):
        """Sharing of mutable instance data is detected"""
        method = self.constructor
        clone = copy.deepcopy(method)
        shallow = copy.copy(method)
        
        # Test that the copy methods works as expected
        assert method is not clone, 'Different reference'
        assert method is not shallow, 'Different reference'
        assert method == clone, 'But still equal'
        assert method == shallow, 'But still equal'
        assert method.as_list() == clone.as_list(), 'Same string repr'
        assert method.as_list() == shallow.as_list(), 'Same string repr'
        assert not method.shares_mutables_with(clone)
        assert method.shares_mutables_with(shallow)
        
        # Test that deleted attributes are handled correctly
        clone.javadocs = method.javadocs
        assert method.shares_mutables_with(clone), 'javadoc is shared'
        del clone.javadocs
        assert not hasattr(clone, 'javadocs'), 'deleted from clone'
        assert hasattr(method, 'javadocs'), 'not deleted from method'
        assert not method.shares_mutables_with(clone), 'Not sharing anymore'

    def testExact_caching(self):
        """Setting instance data with set and add methods overwrites cache"""
        self.assertRaises(AssertionError, self.method.as_list)  # name=None
        
        # A method initialized with a cache is represented by it
        method = jpyang.JavaMethod(exact='bogus')
        assert method.as_list() == 'bogus'
        
        # If we change the indentation, the cache is discarded
        assert method.indent == '    ', 'default indent should be 4 spaces'
        method.set_indent(2)
        assert method.indent == '  ', 'the indentation level should change'
        assert method.exact is None, 'cache should be discarded'
        self.assertRaises(AssertionError, method.as_list)  # since name=None
        
        # If we set the name, the method can be represented once again
        method.set_name('name')
        res = '\n'.join(method.as_list())
        expected = '\n'.join(['  name() {', '  }'])
        assert res == expected, '\nwas: ' + res + '\nnot: ' + expected
        
        # Shadow the true representation by assigning a value to the cache
        method.exact = 'bogus'
        assert method.as_list() == 'bogus'
        
        # Adding a modifiers invalidates the cache again
        method.add_modifier('public')
        res = '\n'.join(method.as_list())
        expected = '\n'.join(['  public name() {', '  }'])
        assert res == expected, '\nwas: ' + res + '\nnot: ' + expected


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testInit']  # Only one
    unittest.main()
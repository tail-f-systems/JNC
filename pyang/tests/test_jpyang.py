"""
Created on 16 jul 2012

@author: emil@tail-f.com

Contains class Test, for function tests. PyUnit is needed to run these tests.

To run, stand in project dir and enter:
$ python -m unittest discover -v
"""
import unittest

from pyang.plugins import jpyang  #@UnresolvedImport
from pyang.tests import util  #@UnresolvedImport
from pyang.statements import Statement


class Test(unittest.TestCase):
    """Contains all JPyang function tests"""

    def setUp(self):
        """Runs before each test"""
        # Initialize context with directory 'gen'
        util.init_context(self)
        util.test_default_context(self)
        
        # Construct a statement tree: c, c/l, c/leaf, c/l/key and c/l/k
        self.c = Statement(None, None, None, 'container', arg='c')
        self.l = Statement(self.c, self.c, None, 'list', arg='l')
        self.leaf = Statement(self.c, self.c, None, 'leaf', arg='leaf')
        self.key = Statement(self.l, self.l, None, 'key', arg='k')
        self.k = Statement(self.l, self.l, None, 'leaf', arg='k')

    def tearDown(self):
        """Runs after each test"""
        pass

    def testCapitalize_first(self):
        """Simple cases of the capitalize_first function"""
        res = jpyang.capitalize_first('A')
        assert res == 'A', 'was: ' + res
        res = jpyang.capitalize_first('Ab')
        assert res == 'Ab', 'was: ' + res
        res = jpyang.capitalize_first('AB')
        assert res == 'AB', 'was: ' + res
        res = jpyang.capitalize_first('aB')
        assert res == 'AB', 'was: ' + res
        res = jpyang.capitalize_first('ab')
        assert res == 'Ab', 'was: ' + res
        res = jpyang.capitalize_first('teSt')
        assert res == 'TeSt', 'was: ' + res

    def testCamelize(self):
        """Special, "unlikely" cases of the camelize function 
        
        Does not test for removal of any characters other than - and .
        
        """
        res = jpyang.camelize('teSt')
        assert res == 'teSt', 'was: ' + res
        res = jpyang.camelize('a.weird-stringThis')
        assert res == 'aWeirdStringThis', 'was: ' + res
        res = jpyang.camelize('...')
        assert res == '..', 'was: ' + res
        res = jpyang.camelize('.-.')
        assert res == '-.', 'was: ' + res
        res = jpyang.camelize('.-')
        assert res == '-', 'was: ' + res
        res = jpyang.camelize('.-a')
        assert res == '-a', 'was: ' + res
        res = jpyang.camelize('a-.')
        assert res == 'a.', 'was: ' + res
        res = jpyang.camelize('a-')
        assert res == 'a-', 'was: ' + res
        res = jpyang.camelize('-a')
        assert res == 'A', 'was: ' + res
    
    def testGet_package(self):
        """Correct package is retrieved for all nodes in the statement tree
        
        Perform tests on all nodes in the tree. The top level statement and its
        immediate children should have the base package.
        
        """
        directory = self.ctx.opts.directory
        
        res = jpyang.get_package(self.c, self.ctx)
        assert res == directory, 'was: ' + res
        res = jpyang.get_package(self.leaf, self.ctx)
        assert res == directory, 'was: ' + res
        res = jpyang.get_package(self.l, self.ctx)
        assert res == directory, 'was: ' + res
        res = jpyang.get_package(self.key, self.ctx)
        assert res == directory + '.l', 'was: ' + res
        res = jpyang.get_package(self.k, self.ctx)
        assert res == directory + '.l', 'was: ' + res

    def testPairwise(self):
        """The iterator includes the next item also"""
        l = [1, 2, 3]

        # Test that the next() method returns correct values
        res = jpyang.pairwise(l)
        for i in range(len(l)):
            if i != len(l) - 1:
                assert res.next() == (l[i], l[i+1])
            else:
                assert res.next() == (l[i], None)

        # Test that next_item contains correct value during iteration
        res = jpyang.pairwise(l)
        i = 0
        prev = l[0]
        for item, next_item in res:
            assert item != None
            assert item == prev
            prev = next_item
            i += 1
        assert i == len(l), '#iterations (should be ' + len(l) + '): ' + str(i)

    def testMake_valid_identifier(self):
        """Statement arguments converts to valid Java identifiers
        
        the make_valid_identifier function prepends keyword args
        with a J and that the forbidden characters . and - are removed.
        
        Some simple sanity checks are performed as well. There are no tests for
        other forbidden characters than '.' and '-'.
        
        """
        # Call on statement with a lowercase arg
        assert self.c.arg == 'c', 'was: ' + self.c.arg
        res = jpyang.make_valid_identifier(self.c)
        assert self.c.arg == 'c', 'was: ' + self.c.arg
        assert res.arg == 'c', 'was: ' + res.arg

        # Call on statement with arg containing hyphens
        stmt = Statement(None, None, None, 'container', arg='test-hyphen')
        assert stmt.arg == 'test-hyphen', 'was: ' + stmt.arg
        res = jpyang.make_valid_identifier(stmt)
        assert stmt.arg == 'testHyphen', 'was: ' + stmt.arg
        assert res.arg == 'testHyphen', 'was: ' + res.arg

        # Call on statement with no arg
        stmt = Statement(None, None, None, 'container', arg=None)
        assert stmt.arg == None, 'was: ' + stmt.arg
        res = jpyang.make_valid_identifier(stmt)
        assert stmt.arg == None, 'was: ' + stmt.arg
        assert res.arg == None, 'was: ' + res.arg

        # Call on statement with arg = boolean
        stmt = Statement(None, None, None, 'container', arg='boolean')
        assert stmt.arg == 'boolean', 'was: ' + stmt.arg
        res = jpyang.make_valid_identifier(stmt)
        assert stmt.arg == 'Jboolean', 'was: ' + stmt.arg
        assert res.arg == 'Jboolean', 'was: ' + res.arg

        # Check that all reserved words are handled correctly
        assert len(jpyang.java_reserved_words) > 0
        for word in jpyang.java_reserved_words:
            stmt = Statement(None, None, None, 'leaf', arg=word)
            assert stmt.arg == word, 'was: ' + stmt.arg
            ok = 'J' + jpyang.camelize(stmt.arg)
            jpyang.make_valid_identifier(stmt)
            assert stmt.arg == ok, 'was: ' + stmt.arg + ' (not ' + ok + ')'

    def testMake_valid_identifiers(self):
        """Tree is recursed correctly"""
        self.c.arg = 'interface'
        assert self.c.arg == 'interface', 'was: ' + self.c.arg
        self.key.arg = 'long'
        assert self.key.arg == 'long', 'was: ' + self.c.arg
        jpyang.make_valid_identifiers(self.k)
        assert self.c.arg == 'interface', 'was: ' + self.c.arg
        assert self.key.arg == 'long', 'was: ' + self.c.arg
        jpyang.make_valid_identifiers(self.c)
        assert self.c.arg == 'Jinterface', 'was: ' + self.c.arg
        assert self.key.arg == 'long', 'was: ' + self.c.arg

    def testGet_types(self):
        """Type conversions for string, int32, etc."""
        # String
        stmt = Statement(None, None, None, 'type', arg='string')
        confm, primitive = jpyang.get_types(stmt, self.ctx)
        assert confm == 'com.tailf.confm.xs.String', 'was: ' + confm
        assert primitive == 'String'

        # int32 - signed, so xs.Int and int is used
        stmt = Statement(None, None, None, 'type', arg='int32')
        confm, primitive = jpyang.get_types(stmt, self.ctx)
        assert confm == 'com.tailf.confm.xs.Int', 'was: ' + confm
        assert primitive == 'int', 'was: ' + primitive

        # uint32 - unsigned, so xs.UnsignedLong and long are used (same as 64)
        stmt = Statement(None, None, None, 'type', arg='uint32')
        confm, primitive = jpyang.get_types(stmt, self.ctx)
        assert confm == 'com.tailf.confm.xs.UnsignedLong', 'was: ' + confm
        assert primitive == 'long', 'was: ' + primitive
        
        # TODO: Test typedefs, other non-type stmts, None and remaining types

    def testGet_base_type(self):
        """Correct result from get_base_type for different statements"""
        # Statement not containing a type at all should return None
        res = jpyang.get_base_type(self.c)
        assert res == None, 'was: ' + res
        
        # A type statement without any children should also return None
        type_stmt = Statement(None, None, None, 'type', arg='string')
        res = jpyang.get_base_type(type_stmt)
        assert res == None, 'was: ' + res.arg
        
        # Adding the type statement as a child to l should work
        self.l.substmts.append(type_stmt)
        type_stmt.parent = self.l
        res = jpyang.get_base_type(self.l)
        assert res.arg == 'string', 'was: ' + res.arg
        
        # Calling with the container c (parent of l) should still return None
        res = jpyang.get_base_type(self.c)
        assert res == None, 'was: ' + res.arg


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']  # Only one
    unittest.main()
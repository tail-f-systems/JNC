"""
Created on 16 jul 2012

@author: emil@tail-f.com

PyUnit is needed to run these tests.

To run, stand in project dir and enter:
$ python -m unittest discover -v
"""
import unittest

from pyang.plugins import jpyang  #@UnresolvedImport
from pyang.statements import Statement
from pyang import FileRepository
from pyang import Context


class DummyOption(object):
    """Used to initialize Context with option fields in test setUp"""
    
    def __init__(self, directory):
        """Sets the directory field to the supplied value"""
        self.directory = directory


class Test(unittest.TestCase):
    """Contains all JPyang tests"""

    def setUp(self):
        """Runs before each test"""
        # Initialize context with directory 'gen'
        repo = FileRepository()
        self.ctx = Context(repo)
        self.ctx.opts = DummyOption('gen')
        
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
        """Tests simple cases of the capitalize_first function"""
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
        """Tests special, "unlikely" cases of the camelize function. 
        
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
        """Tests the get_package function on all nodes in the statement tree.
        
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
        """Tests that the iterator includes the next item also"""
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
        """Tests conversion of statement arguments to valid Java identifiers.
        
        Tests that the make_valid_identifier function prepends keyword args
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

        # Call with reserved word as argument
        assert len(jpyang.java_reserved_words) > 0
        for word in jpyang.java_reserved_words:
            stmt = Statement(None, None, None, 'leaf', arg=word)
            assert stmt.arg == word, 'was: ' + stmt.arg
            ok = 'J' + jpyang.camelize(stmt.arg)
            jpyang.make_valid_identifier(stmt)
            assert stmt.arg == ok, 'was: ' + stmt.arg + ' (not ' + ok + ')'


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']  # Only one
    unittest.main()
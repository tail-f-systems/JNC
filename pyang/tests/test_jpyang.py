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
        self.c = Statement(None, None, None, 'container', 'c')
        self.l = Statement(self.c, self.c, None, 'list', 'l')
        self.leaf = Statement(self.c, self.c, None, 'leaf', 'leaf')
        self.key = Statement(self.l, self.l, None, 'key', 'k')
        self.k = Statement(self.l, self.l, None, 'leaf', 'k')

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
        """Tests special, "unlikely" cases of the camelize function"""
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
        """Tests that the get_package function works on all nodes in the
        statement tree
        
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
        """Tests that the pairwise function returns an iterator that includes
        the next item also
        
        """
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
        for word in jpyang.java_reserved_words:
            stmt = Statement(None, None, None, 'leaf', word)
            assert stmt.arg == word, 'was: ' + stmt.arg
            ok = 'J' + jpyang.camelize(stmt.arg)
            jpyang.make_valid_identifier(stmt)
            assert stmt.arg == ok, 'was: ' + stmt.arg + ' (not ' + ok + ')'


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']  # Only one
    unittest.main()
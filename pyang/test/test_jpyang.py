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


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']
    unittest.main()
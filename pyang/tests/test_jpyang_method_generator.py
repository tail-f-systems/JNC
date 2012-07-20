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
        
        # Construct a statement tree rooted at m
        self.m = Statement(None, None, None, 'module', arg='m')
        self.p = Statement(self.m, self.m, None, 'prefix', arg='root-m')
        self.c = Statement(self.m, self.m, None, 'container', arg='c')
        self.m.substmts = [self.p, self.c]
        
        self.l = Statement(self.m, self.c, None, 'list', arg='l')
        self.leaf = Statement(self.m, self.c, None, 'leaf', arg='leaf')
        self.c.substmts = [self.l, self.leaf]
        
        self.key = Statement(self.m, self.l, None, 'key', arg='k')
        self.k = Statement(self.m, self.l, None, 'leaf', arg='k')
        self.l.substmts = [self.key, self.k]
        
        # Create new Method Generators for the list and container statements
        self.cgen = jpyang.MethodGenerator(self.c, self.ctx)
        self.lgen = jpyang.MethodGenerator(self.l, self.ctx)

    def tearDown(self):
        """Runs after each test"""
        pass

    def testInit(self):
        """Values correct in newly created Method Generator"""
        assert self.cgen.stmt == self.c, 'was: ' + self.c.arg
        assert self.lgen.stmt == self.l, 'was: ' + self.l.arg
        assert self.cgen.n == 'C', 'was: ' + self.cgen.n
        assert self.lgen.n == 'L', 'was: ' + self.lgen.n
        assert self.cgen.root == 'RootM', 'was: ' + self.cgen.root
        assert self.lgen.root == 'RootM', 'was: ' + self.lgen.root
        assert self.cgen.is_container
        assert not self.lgen.is_container
        assert not self.cgen.is_list
        assert self.lgen.is_list
        assert self.cgen.is_config
        assert self.lgen.is_config
        assert not self.cgen.is_typedef
        assert not self.lgen.is_typedef
        assert self.cgen.stmt_type is None, 'was: ' + self.cgen.stmt_type.arg
        assert self.lgen.stmt_type is None, 'was: ' + self.lgen.stmt_type.arg
        assert not self.cgen.is_string
        assert not self.lgen.is_string
        assert self.cgen.ctx is self.ctx
        assert self.lgen.ctx is self.ctx


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testCapitalize_first']  # Only one
    unittest.main()
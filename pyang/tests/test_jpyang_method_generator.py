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
        
        ###### Construct a statement tree rooted at m
        
        # Module m with prefix p, container c and typedef t children
        self.m = Statement(None, None, None, 'module', arg='m')
        self.ns = Statement(self.m, self.m, None, 'namespace', arg='ns')
        self.p = Statement(self.m, self.m, None, 'prefix', arg='root-m')
        self.c = Statement(self.m, self.m, None, 'container', arg='c')
        self.t = Statement(self.m, self.m, None, 'typedef', arg='t')
        self.m.substmts = [self.p, self.ns, self.c, self.t]
        
        # list and leaf children of c
        self.l = Statement(self.m, self.c, None, 'list', arg='l')
        self.leaf = Statement(self.m, self.c, None, 'leaf', arg='leaf')
        self.c.substmts = [self.l, self.leaf]
        
        # type child of t
        self.ty = Statement(self.m, self.t, None, 'type', arg='int32')
        self.t.substmts = [self.ty]
        
        # key and leaf children of c/l
        self.key = Statement(self.m, self.l, None, 'key', arg='k my')
        self.k = Statement(self.m, self.l, None, 'leaf', arg='k')
        self.my = Statement(self.m, self.l, None, 'leaf', arg='my')
        self.l.substmts = [self.key, self.k, self.my]
        
        # type child of c/leaf
        self.leafty = Statement(self.m, self.leaf, None, 'type', arg='string')
        self.leaf.substmts = [self.leafty]
        
        # type child of c/l/k
        self.kty = Statement(self.m, self.k, None, 'type', arg='string')
        self.k.substmts = [self.kty]
        
        # type child of c/l/my
        self.myty = Statement(self.m, self.my, None, 'type', arg='t')
        self.my.substmts = [self.myty]
        
        ########
        
        # Create new Method Generators for the top level statements
        self.cgen = jpyang.MethodGenerator(self.c, self.ctx)
        self.lgen = jpyang.MethodGenerator(self.l, self.ctx)
        self.tgen = jpyang.MethodGenerator(self.t, self.ctx)

    def tearDown(self):
        """Runs after each test"""
        pass

    def testSetUp(self):
        """Statement tree in this test is properly constructed"""
        res = map(self.m.search, ['prefix', 'container', 'typedef'])
        assert res == [[self.p], [self.c], [self.t]]
        res = map(self.c.search, ['list', 'leaf'])
        assert res == [[self.l], [self.leaf]]
        res = map(self.t.search, ['type'])
        assert res == [[self.ty]]
        res = map(self.l.search, ['key', 'leaf'])
        assert res == [[self.key], [self.k, self.my]]

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
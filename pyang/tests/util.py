"""
Created on 23 jul 2012

Contains code shared by test modules, such as the DummyOption class, used to
initialize contexts with options, and various setUp facilities.

@author: emil@tail-f.com
"""
from pyang import FileRepository, Context
from pyang.statements import Statement
from pyang.plugins import jpyang  #@UnresolvedImport


class DummyOption(object):
    """Used to initialize Context with option fields in create_statement_tree
    and test_jpyang.setUp"""
    
    def __init__(self, directory='gen', debug=False, verbose=False):
        """Sets the directory field to the supplied value"""
        self.directory = directory
        self.debug = debug
        self.verbose = verbose


def init_context(self):
    """Initialize context with directory 'gen' and debug flags turned off"""
    repo = FileRepository()
    self.ctx = Context(repo)
    self.ctx.opts = DummyOption()


def create_statement_tree(self):
    """Constructs a statement tree rooted at self.m"""
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
    self.myty.i_typedef = self.t
    self.my.substmts = [self.myty]


def create_method_generators(self):
    """Constructs method generators for the top level statements in statement
    tree of self. Since this requires the statement and context attributes of
    self to be set, this method should be called after init_context and
    create_statement_tree.
    
    """
    assert hasattr(self, 'c'), 'self should have a statement attribute c'
    assert hasattr(self, 'l'), 'self should have a statement attribute l'
    assert hasattr(self, 't'), 'self should have a statement attribute t'
    assert hasattr(self, 'ctx'), 'self should have a context attribute ctx'
    self.cgen = jpyang.MethodGenerator(self.c, self.ctx)
    self.lgen = jpyang.MethodGenerator(self.l, self.ctx)
    self.tgen = jpyang.MethodGenerator(self.t, self.ctx)


def test_statement_tree(self):
    """Test that the statement tree is structured as intended"""
    mystr = lambda l: str(map(lambda ll: map(lambda s: s.arg, ll), l))
    res = map(self.m.search, ['prefix', 'container', 'typedef'])
    assert res == [[self.p], [self.c], [self.t]], 'was: ' + mystr(res)
    res = map(self.c.search, ['list', 'leaf'])
    assert res == [[self.l], [self.leaf]], 'was: ' + mystr(res)
    res = map(self.t.search, ['type'])
    assert res == [[self.ty]], 'was: ' + mystr(res)
    res = map(self.l.search, ['key', 'leaf'])
    assert res == [[self.key], [self.k, self.my]], 'was: ' + mystr(res)
    self.stmt_type = self.leaf.search_one('type')
    assert self.stmt_type and self.stmt_type.arg == 'string'


def test_method_generators(self):
    assert hasattr(self, 'cgen'), 'self should have a generator attribute cgen'
    assert hasattr(self, 'lgen'), 'self should have a generator attribute lgen'
    assert hasattr(self, 'tgen'), 'self should have a generator attribute tgen'
    assert self.cgen.stmt is self.c, 'statement of cgen should be c'
    assert self.lgen.stmt is self.l, 'statement of lgen should be l'
    assert self.tgen.stmt is self.t, 'statement of tgen should be t'
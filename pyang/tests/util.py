"""
Created on 23 jul 2012

Contains code shared by test modules, such as the DummyOption class, used to
initialize contexts with options, and various setUp facilities.

@author: emil@tail-f.com
"""

from numbers import Number

from pyang import FileRepository, Context
from pyang.statements import Statement
from pyang.plugins import jpyang  # @UnresolvedImport


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
    """Constructs a statement tree rooted at self.m

    The tree corresponds to the following yang module:

    module m {
      namespace ns;
      prefix root-m;
      container m {
        list l {
          key "k my";
          leaf k {
            type string;
          }
          leaf my {
            type t;
          }
        }
        leaf-list ll {
          type decimal64;
        }
        leaf leaf {
          type string;
        }
      }
      typedef t {
        type int32;
      }
    }

    """
    # Module m with prefix p, container c and typedef t children
    self.m = Statement(None, None, None, 'module', arg='m')
    self.ns = Statement(self.m, self.m, None, 'namespace', arg='ns')
    self.p = Statement(self.m, self.m, None, 'prefix', arg='root-m')
    self.c = Statement(self.m, self.m, None, 'container', arg='c')
    self.t = Statement(self.m, self.m, None, 'typedef', arg='t')
    self.m.substmts = [self.p, self.ns, self.c, self.t]

    # list, leaf-list and leaf children of c
    self.l = Statement(self.m, self.c, None, 'list', arg='l')
    self.ll = Statement(self.m, self.c, None, 'leaf-list', arg='ll')
    self.leaf = Statement(self.m, self.c, None, 'leaf', arg='leaf')
    self.c.substmts = [self.l, self.ll, self.leaf]

    # type child of t
    self.ty = Statement(self.m, self.t, None, 'type', arg='int32')
    self.t.substmts = [self.ty]

    # key and leaf children of c/l
    self.key = Statement(self.m, self.l, None, 'key', arg='k my')
    self.k = Statement(self.m, self.l, None, 'leaf', arg='k')
    self.my = Statement(self.m, self.l, None, 'leaf', arg='my')
    self.l.substmts = [self.key, self.k, self.my]

    # type child of c/ll
    self.llty = Statement(self.m, self.ll, None, 'type', arg='decimal64')
    self.ll.substmts = [self.llty]

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


def test_default_context(self):
    """The context of self has the default attribute values"""
    assert self.ctx.opts.directory == 'gen', 'directory should be \'gen\''
    assert not self.ctx.opts.debug, 'debug option should be turned off'
    assert not self.ctx.opts.verbose, 'verbose option should be turned off'


def test_statement_tree(self):
    """Statement tree is structured as intended"""
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
    """Method generator attributes present, with correct values"""
    generators = (('cgen', self.cgen, self.c),
                  ('lgen', self.lgen, self.l),
                  ('tgen', self.tgen, self.t))

    for genattr, gen, stmt in generators:
        # Generator reference to context and subclass instance correct
        assert gen.ctx is self.ctx
        assert (type(gen) is jpyang.MethodGenerator) != (gen.gen is gen)
        assert (type(gen.gen) is jpyang.ContainerMethodGenerator) == gen.is_container
        assert (type(gen.gen) is jpyang.ListMethodGenerator) == gen.is_list
        assert (type(gen.gen) is jpyang.TypedefMethodGenerator) == gen.is_typedef

        # Correct generator statement value, reference, name and root
        assert gen.stmt == stmt, 'was: ' + stmt.arg
        assert gen.stmt is stmt, 'statement of ' + genattr + ' should be ' + stmt.arg
        assert gen.n == jpyang.normalize(gen.stmt.arg), 'was: ' + gen.n
        assert gen.root == 'RootM', 'was: ' + gen.root

        # Correct values of fields
        assert (stmt.keyword == 'container') == gen.is_container
        assert (stmt.keyword == 'list') == gen.is_list
        assert (stmt.keyword == 'typedef') == gen.is_typedef
        assert not hasattr(gen, 'is_config') or gen.is_config
        assert not hasattr(gen, 'stmt_type') or gen.stmt_type == self.ty
        assert not hasattr(gen, 'is_string') or not gen.is_string


def shares_mutables_with(self, other):
        """Returns True iff mutable instance data is shared with other"""
        Immutable = basestring, tuple, Number, frozenset
        for attr, value in vars(self).items():
            if value is None or isinstance(value, Immutable):
                continue
            try:
                if getattr(other, attr) is value:
                    return True
            except AttributeError:
                pass
        return False

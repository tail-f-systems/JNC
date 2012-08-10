"""
Created on 20 jul 2012

@author: emil@tail-f.com

The unittest module is needed to run these tests. If you are using a really old
version of python you might have to download PyUnit to get it.

To run, stand in project dir and enter:
$ python -m unittest discover -v
"""
import unittest

from pyang.plugins import jpyang  #@UnresolvedImport
from pyang.tests import util  #@UnresolvedImport


class Test(unittest.TestCase):
    """Contains tests for methods in JPyang.LeafMethodGenerator"""

    def setUp(self):
        """Runs before each test"""
        # Construct a statement tree rooted at self.m, and method generators
        util.init_context(self)
        util.create_statement_tree(self)
        self.strleafgen = jpyang.LeafMethodGenerator(self.leaf, self.ctx)
        self.int32leafgen = jpyang.LeafMethodGenerator(self.my, self.ctx)
        self.llgen = jpyang.LeafMethodGenerator(self.ll, self.ctx)

    def tearDown(self):
        """Runs after each test"""
        pass

    def testSetUp(self):
        """Statement tree and generators are properly constructed"""
        util.test_default_context(self)
        util.test_statement_tree(self)
        
        assert self.strleafgen.is_string
        assert not self.int32leafgen.is_string
        assert not self.llgen.is_string
        
        assert self.strleafgen.stmt.arg == 'leaf'
        assert self.int32leafgen.stmt.arg == 'my'
        assert self.llgen.stmt.arg == 'll'
        
        assert self.strleafgen.type_str[1] == 'String'
        assert self.int32leafgen.type_str[1] == 'int'
        assert self.llgen.type_str[1] == 'BigDecimal'

    def testMark(self):
        mark = '''    /**
     * Marks the {0} "{1}" with operation "{2}".{3}
     */
    public void mark{4}{5}({6}) throws JNCException {{
        markLeaf{5}("{7}");
    }}'''
        javadoc = ['\n     * @param ',
                   'Value The value to mark',
                   ', given as a String']
        generators = ((self.strleafgen, 'leaf'),
                      (self.int32leafgen, 'my'),
                      (self.llgen, 'll'))
        params = ('YangDecimal64 llValue', 'String llValue')
        for gen, arg in generators:
            for op in ('replace', 'merge', 'create', 'delete'):
                mark_methods = gen.mark(op)
                assert len(mark_methods) == 1 + gen.is_leaflist, 'was ' + str(len(mark_methods))
                for i, method in enumerate(mark_methods):
                    res = '\n'.join(method.as_list())
                    if gen.is_leaf:
                        expected = mark.format('leaf', arg, op, '',
                                               jpyang.capitalize_first(arg),
                                               op.capitalize(), '', arg)
                    else:
                        expected = mark.format('leaf-list', arg, op,
                                               (javadoc[0] + arg +
                                                ''.join(javadoc[1:i+2])),
                                               jpyang.capitalize_first(arg),
                                               op.capitalize(), params[i],
                                               ''.join([arg, '[name=\'" + ',
                                                        arg, 'Value + "\']']))
                    assert res == expected, '\n'.join(['was', res,
                                                      'not', expected])


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testInit']  # Only one
    unittest.main()
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


class Test(unittest.TestCase):
    """Contains tests for methods in JPyang.LeafMethodGenerator"""

    def setUp(self):
        """Runs before each test"""
        # Construct a statement tree rooted at self.m, and method generators
        util.init_context(self)
        util.create_statement_tree(self)
        self.strleafgen = jpyang.LeafMethodGenerator(self.leaf, self.ctx)
        self.int32leafgen = jpyang.LeafMethodGenerator(self.my, self.ctx)

    def tearDown(self):
        """Runs after each test"""
        pass

    def testSetUp(self):
        """Statement tree and generators are properly constructed"""
        util.test_default_context(self)
        util.test_statement_tree(self)
        assert self.strleafgen.is_string
        assert not self.int32leafgen.is_string
        assert self.strleafgen.stmt.arg == 'leaf'
        assert self.int32leafgen.stmt.arg == 'my'
        assert self.strleafgen.type_str[1] == 'String'
        assert self.int32leafgen.type_str[1] == 'int'

    def testMark(self):
        for op in ('replace', 'merge', 'create', 'delete'):
            # String leaf
            methodlist = self.strleafgen.mark(op)
            assert len(methodlist) == 1, 'was ' + str(len(methodlist))
            method = methodlist[0]
            print '\n'.join(method.as_list())
            
            # Int32 leaf
            methodlist = self.int32leafgen.mark(op)
            assert len(methodlist) == 2, 'was ' + str(len(methodlist))
            method1 = methodlist[0]
            method2 = methodlist[1]
            print '\n'.join(method1.as_list())
            print '\n'.join(method2.as_list())


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testInit']  # Only one
    unittest.main()
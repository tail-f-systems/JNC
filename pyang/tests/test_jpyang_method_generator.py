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
    """Contains tests for methods in JPyang.MethodGenerator"""

    def setUp(self):
        """Runs before each test"""
        # Construct a statement tree rooted at self.m, and method generators
        util.init_context(self)
        util.create_statement_tree(self)
        util.create_method_generators(self)

    def tearDown(self):
        """Runs after each test"""
        pass

    def testSetUp(self):
        """Statement tree and generators are properly constructed"""
        util.test_default_context(self)
        util.test_statement_tree(self)
        util.test_method_generators(self)

    def testRoot_namespace(self):
        """Joining root_namespace return value yields (RootM.NAMESPACE, "c")"""
        res = self.cgen.root_namespace(self.cgen.stmt.arg)
        expected = ['(', self.cgen.root, '.NAMESPACE, "']
        expected.extend([self.cgen.stmt.arg, '");'])
        assert ''.join(expected) == '(RootM.NAMESPACE, "c");'
        assert res == expected, 'was: ' + str(res) + '\nnot: ' + str(expected)
        
        # 
        res = self.lgen.root_namespace(self.my.arg)
        expected = ['(', self.lgen.root, '.NAMESPACE, "']
        expected.extend([self.my.arg, '");'])
        assert ''.join(expected) == '(RootM.NAMESPACE, "my");'
        assert res == expected, 'was: ' + str(res) + '\nnot: ' + str(expected)

    def testEmpty_constructor(self):
        """Method fields and as_string representation as expected"""
        res = [self.cgen.empty_constructor(), self.lgen.empty_constructor()]
        assert res[0].modifiers == res[1].modifiers == ['public']
        assert res[0].return_type == res[1].return_type == None
        assert res[0].name == 'C'
        assert res[1].name == 'L'
        assert res[0].parameters == res[1].parameters == []
        assert res[0].exceptions == res[1].exceptions == []
        assert res[0].indent == res[1].indent == '    '
        expected = '''
    /**
     * Constructor for an empty {0} object.
     */
    public {0}() {{
        super(RootM.NAMESPACE, "{1}");{2}
    }}
'''
        set_prefix = '''
        setDefaultPrefix();
        setPrefix(RootM.PREFIX);'''
        assert res[0].as_string() == expected.format('C', 'c', set_prefix), \
            '\nwas:' + res[0].as_string() + \
            '\nnot:' + expected.format('C', 'c', set_prefix)
        assert res[1].as_string() == expected.format('L', 'l', ''), \
            '\nwas:' + res[1].as_string() + \
            '\nnot:' + expected.format('L', 'l', '')

    def testTypedef_constructors(self):
        """Both methods' fields and as_string representations as expected"""
        res = self.tgen.constructors()
        assert res[0].modifiers == res[1].modifiers == ['public']
        assert res[0].return_type == res[1].return_type == None
        assert res[0].name == res[1].name == 'T'
        assert res[0].parameters == ['String value']
        assert res[1].parameters == ['int value']
        assert res[0].exceptions == res[1].exceptions == ['ConfMException']
        assert res[0].indent == res[1].indent == '    '
        expected = '''
    /**
     * Constructor for T object from a {}.
     * @param value Value to construct the T from.
     */
    public T({} value) throws ConfMException {{
        super(value);
        check();
    }}
'''
        assert res[0].as_string() == expected.format('string', 'String'), \
            '\nwas:' + res[0].as_string() + \
            '\nnot:' + expected.format('string', 'String')
        assert res[1].as_string() == expected.format('int', 'int'), \
            '\nwas:' + res[1].as_string() + \
            '\nnot:' + expected.format('int', 'int')

    def testValue_constructors(self):
        """All methods' fields and as_string representations as expected"""
        res = self.lgen.value_constructors()
        assert len(res) == 3, 'There should be three constructors'
        params = [['com.tailf.confm.xs.String kValue', 'gen.T myValue'],
                  ['String kValue', 'String myValue'],
                  ['String kValue', 'int myValue']]
        addition = ['',
                    'with Strings for the keys.',
                    'with primitive Java types.']
        confm_string = 'new com.tailf.confm.xs.String('
        gen_t = 'new gen.T('
        setvalue = [['kValue', 'myValue'],
                    [confm_string + 'kValue)', gen_t + 'myValue)'],
                    [confm_string + 'kValue)', gen_t + 'myValue)']]
        expected = '''
    /**
     * Constructor for an initialized L object,
     * {}
     * @param kValue Key argument of child.
     * @param myValue Key argument of child.
     */
    public L({}) throws INMException {{
        super(RootM.NAMESPACE, "l");
        Leaf k = new Leaf(RootM.NAMESPACE, "k");
        k.setValue({});
        insertChild(k, childrenNames());
        Leaf my = new Leaf(RootM.NAMESPACE, "my");
        my.setValue({});
        insertChild(my, childrenNames());
    }}
'''
        for i, method in enumerate(res):
            assert method.modifiers == ['public']
            assert method.return_type == None
            assert method.name == 'L'
            assert method.parameters == params[i]
            assert method.exceptions == ['INMException']
            assert method.indent == '    '
            assert method.as_string() == expected.format(addition[i],
                                                         ', '.join(params[i]),
                                                         setvalue[i][0],
                                                         setvalue[i][1])

    def testConstructors(self):
        """The correct subroutines are called from constructor"""
        # List constructors
        constructors1 = self.lgen.constructors()
        constructors2 = [self.lgen.empty_constructor()]
        constructors2.extend(self.lgen.value_constructors())
        res = map(jpyang.JavaMethod.as_string, constructors1)
        expected = map(jpyang.JavaMethod.as_string, constructors2)
        msg = ['All 4 LIST constructors should be generated correctly']
        msg.extend(['was:', ''.join(res), 'expected:', ''.join(expected)])
        assert res == expected, '\n'.join(msg)
        
        # Container constructors
        res = self.cgen.constructors()[0].as_string()
        expected = self.cgen.empty_constructor().as_string()
        msg = ['CONTAINER constructor generated should be parameter free']
        msg.extend(['was:', ''.join(res), 'expected:', ''.join(expected)])
        assert res == expected, '\n'.join(msg)
        
        # Typedef constructors
        constructors1 = self.tgen.constructors()
        constructors2 = self.tgen.gen.constructors()
        res = map(jpyang.JavaMethod.as_string, constructors1)
        expected = map(jpyang.JavaMethod.as_string, constructors2)
        msg = ['TYPEDEF constructors should be generated properly']
        msg.extend(['was:', ''.join(res), 'expected:', ''.join(expected)])
        assert res == expected, '\n'.join(msg)

    def testCloners(self):
        """Cloners correctly generated for lists and containers"""
        expected = '''
    /**
     * Clones this object, returning {0} copy.
     * @return A clone of the object.{1}
     */
    public Container clone{2}() {{
        return clone{2}Content(new {3}());
    }}
'''

        # List and Container cloners
        for gen, name in [(self.lgen, 'L'), (self.cgen, 'C')]:
            # Deep clone method
            res = gen.cloners()
            expected0 = expected.format('an exact', '', '', name)
            res0 = res[0].as_string() 
            assert res0 == expected0, '\nwas:' + res0 + '\nnot:' + expected0
            
            # Shallow clone method
            tmp = ' Children are not included.'
            expected1 = expected.format('a shallow', tmp, 'Shallow', name)
            res1 = res[1].as_string() 
            assert res1 == expected1, '\nwas:' + res1 + '\nnot:' + expected1
        
        # Generating cloners for a typedef should raise an AssertionError
        self.assertRaises(AssertionError, self.tgen.cloners)

    def testSetters(self):
        """Setters correctly generated for lists, containers and typedefs"""
        expected = '''
    /**
     * Sets the value using a {}.
     * @param value The value to set.
     */
    public void setValue({} value) throws ConfMException {{
        super.setValue(value);
        check();
    }}
'''
        res = self.tgen.setters()
        expected0 = expected.format('string value', 'String')
        res0 = res[0].as_string() 
        assert res0 == expected0, '\nwas:' + res0 + '\nnot:' + expected0
        expected1 = expected.format('value of type int', 'int')
        res1 = res[1].as_string() 
        assert res1 == expected1, '\nwas:' + res1 + '\nnot:' + expected1


if __name__ == "__main__":
    """Launch all unit tests"""
    #import sys;sys.argv = ['', 'Test.testInit']  # Only one
    unittest.main()
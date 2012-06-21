#!/usr/bin/python
# -*- coding: latin-1 -*-
"""JPyang: java output plugin
 * Copyright (c) 2012 Tail-F Systems AB.
 * Korgmakargränd 2, SE-111 22, Stockholm, Sweden
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Tail-F
 * Systems AB. ('Confidential Information').  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Tail-F Systems AB.
 * 
 * To be released under GNU General Public License. 
 * Please see http://www.opensource.org/licenses/gpl-3.0.html
 * or http://www.gnu.org/licenses/gpl.html
 
Invoke with:
>pyang -f jpyang -d <package.output.dir> <file.yang>

"""

from __future__ import with_statement # Not required from Python 2.6 and
# ... onwards, but kept for the sake of backwards compatibility

import optparse # FIXME Deprecated in python 2.7, should use argparse instead
# ... See http://stackoverflow.com/questions/3217673/why-use-argparse-rather-than-optparse and http://docs.python.org/dev/library/argparse.html#upgrading-optparse-code
import os, errno, sys
import datetime

from pyang import plugin
from pyang import util
from pyang import error
from pyang import statements

def pyang_plugin_init():
    """Registers an instance of the jpyang plugin"""
    plugin.register_plugin(JPyangPlugin())

class JPyangPlugin(plugin.PyangPlugin):
    """The plugin class of JPyang"""
    
    def add_output_format(self, fmts):
        """Adds 'java' and 'jpyang' as valid output formats"""
        self.multiple_modules = True
        fmts['java'] = self
        fmts['jpyang'] = self

    def add_opts(self, optparser):
        """Adds the --jpyang-help option"""
        optlist = [
            optparse.make_option(
                '-d', '--java-package',
                dest='directory',
                help='Generate output to DIRECTORY.'),
            optparse.make_option(
                '--jpyang-help',
                dest='jpyang_help',
                action='store_true',
                help='Print help on java format/JPyang usage and exit'),
            optparse.make_option(
                '--jpyang-debug',
                dest='debug',
                action='store_true',
                help='Print debug messages.'),
            optparse.make_option(
                '--jpyang-javadoc',
                dest='javadoc_directory',
                help='Generate javadoc to JAVADOC_DIRECTORY.'),
            ]
        g = optparser.add_option_group('JPyang output specific options')
        g.add_options(optlist)
        (self.o, args) = optparser.parse_args()

    def setup_ctx(self, ctx):
        """Called after ctx has been set up in main module. Checks if the
        jpyang help option was supplied and if not, that the -d or 
        --java-package was used.
        
        ctx -- Context object as defined in __init__.py
        
        """
        if ctx.opts.jpyang_help:
            print_help()
            sys.exit(0)
        if (ctx.opts.format in ['java', 'jpyang'] and 
            ctx.opts.directory is None):
            print >> sys.stderr, 'ERROR: Option -d (or --java-package) is' \
                ' mandatory when using the JPyang plugin/java output format'
            sys.exit(1) # Makes above message more visible than self.fatal()

    def setup_fmt(self, ctx):
        """Disables implicit errors for the Context"""
        ctx.implicit_errors = False

    def emit(self, ctx, modules, fd):
        """Deletes any previous files in the supplied directory, creates
        directory structure and generates java code to it.
        
        ctx     -- Context used to get output directory
        modules -- A list of pyang Statements, should be nothing else than 
                   module and/or submodule statements.
        fd      -- File descriptor ignored.
        
        """
        directory = ctx.opts.directory
        wd = os.getcwd()
        # Create directory
        d = directory.replace('.', '/')
        try:
            os.makedirs(d, 0777)
        except OSError as exc:
            if exc.errno == errno.EEXIST:
                pass # The directory already exists
            else:
                raise
        try:
            os.chdir(d)
        except OSError as exc:
            if exc.errno == errno.ENOTDIR:
                if ctx.opts.debug:
                    print 'WARNING: Unable to change directory to '+d+ \
                        '. Probably a non-directory file with same name'+ \
                        'as one of the subdirectories already exists.'
            else:
                raise
        if ctx.opts.debug:
            print 'GENERATING FILES TO: '+os.getcwd()
        for module in modules:
            if module.keyword == 'module' or module.keyword == 'submodule':
                # Generate schema
                ns = module.search_one('namespace').arg
                name = module.search_one('prefix').arg.capitalize()
                with open(os.getcwd()+'/'+name+'.schema', 'w+') as f:
                    f.write('<schema>\n'+indent( \
                        ('<node>\n'+ \
                            indent(schema_node(module, '/', ns, ctx))+ \
                            '\n</node>'+ \
                            schema_nodes(module.substmts, '/', ns, ctx) \
                        ).splitlines())+'\n</schema>'
                )
                if ctx.opts.debug:
                    print 'Schema generation COMPLETE.'
                # Generate java classes
                src = 'module "'+module.arg+'", revision: "'+ \
                    util.get_latest_revision(module)+'".'
                generate_classes(module, os.getcwd(), directory, src, ctx)
                if module.keyword == 'submodule':
                    #FIXME add support for submodule
                    print >> sys.stderr, \
                        'Warning: no support for submodule'
                if ctx.opts.debug:
                    print 'Java classes generation COMPLETE.'
            else:
                print >> sys.stderr, \
                    'Error: unrecognized keyword: '+module.keyword+ \
                    'top-level element should be module or submodule'
                self.fatal()
        # Generate javadoc
        is_java_file = lambda s: s.endswith('.java')
        directory_listing = os.listdir(os.getcwd())
        java_files = filter(is_java_file, directory_listing)
        class_hierarchy = generate_javadoc(modules, java_files, ctx)
        gen_package(class_hierarchy, directory, ctx)
        os.chdir(wd)
        javadir = ctx.opts.javadoc_directory
        if javadir:
            os.system('javadoc -d '+javadir+' '+d+'/*.java')
            if ctx.opts.debug:
                print 'Javadoc generation COMPLETE.'

    def fatal(self, exitCode=1):
        """Raise an EmitError"""
        raise error.EmitError(self, exitCode)

def print_help():
    """Prints a description of what JPyang is and how to use it"""
    print '''
The JPyang/java output format can be used to generate a java class hierarchy 
from a single yang data model. Each module, container, list, etc. is 
represented by a .java file which can be used to retrieve and/or edit 
configurations (e.g. by calling methods to add, delete or replace statements).

One way to use the java output format plugin of pyang is to type
$ pyang -f java -d output.package.dir <file.yang>

The two formats java and jpyang produce identical results.

Type '$ pyang --help' for more details on how to use pyang.
'''

def extract_names(stmt_arg):
    """Returns a tuple with a capitalized stmt_arg prepended with .java, and
    stmt_arg capitalized.
    
    stmt_arg -- Any string, really
    
    """
    filename = stmt_arg.capitalize()+'.java'
    return (filename, filename.split('.')[0])

def is_module(stmt):
    """Returns True iff stmt is a module or submodule."""
    return stmt.keyword == 'module' or stmt.keyword == 'submodule'

def is_container(stmt, strict=False):
    """Returns True iff stmt is a list, container or something of the sort."""
    return (stmt.keyword == 'container' or 
        (not strict and stmt.keyword == 'list'))

def in_schema(stmt):
    """Returns True iff stmt is to be included in schema."""
    return (is_container(stmt) or 
        stmt.keyword == 'module' or 
        stmt.keyword == 'leaf')

def indent(lines, level=1):
    """Returns a string consisting of all strings in lines concatenated,
    each string prepended by a level*4 number of spaces and appended with
    a newline, except the last which has no newline.
    
    lines -- list of strings
    level -- indentation level (number of spaces divided by 4)
    
    """
    #TODO implement a more efficient version using replace on strings
    res = ''
    for line in lines:
        res += ' '*level*4 + line + '\n'
    return res[:-1] # Don't include the last newline character

def java_docify(s):
    """Returns the string s, but with each row prepended by ' * '"""
    res = ''
    for row in s.splitlines():
        res += ' * ' + row + '\n'
    return res[:-1] # Don't include the last newline character

def schema_nodes(stmts, tagpath, ns, ctx):
    """Generate XML schema as a list of "node" elements"""
    res = ''
    for stmt in stmts:
        if in_schema(stmt):
            res += '<node>\n'+ \
                indent(schema_node(stmt, tagpath+stmt.arg+'/', ns, ctx))+ \
                '\n</node>'
            res += schema_nodes(stmt.substmts, tagpath+stmt.arg+'/', ns, ctx)
    return res

def schema_node(stmt, tagpath, ns, ctx):
    """Generate "node" element content for an XML schema"""
    res = []
    res.append('<tagpath>'+tagpath+'</tagpath>') # Could use stmt.full_path()
    # ... but it is marked for removal (and it would be less efficient)
    res.append('<namespace>'+ns+'</namespace>')
    res.append('<primitive_type></primitive_type>')
    
    min_occurs = '0'
    max_occurs = '-1'
    mandatory = stmt.search_one('mandatory')
    isMandatory = mandatory is not None and mandatory.arg == 'true'
    unique = stmt.search_one('unique')
    isUnique = unique is not None and unique.arg == 'true'
    key = None
    if stmt.parent is not None:
        key = stmt.parent.search_one('key')
    isKey = key is not None and key.arg == stmt.arg
    childOfContainerOrList = (stmt.parent is not None and \
        is_container(stmt.parent))
    if (is_module(stmt) or isKey or 
        (childOfContainerOrList and is_container(stmt, True))):
        min_occurs = '1'
        max_occurs = '1'
    if isMandatory:
        min_occurs = '1'
    if isUnique or childOfContainerOrList or is_container(stmt, True):
        max_occurs = '1'
    res.append('<min_occurs>'+min_occurs+'</min_occurs>') #TODO verify correct
    res.append('<max_occurs>'+max_occurs+'</max_occurs>') #TODO verify correct
    
    children = ''
    for ch in stmt.substmts:
        children += ch.arg+' '
    res.append('<children>'+children[:-1]+'</children>')
    
    res.append('<flags></flags>')
    res.append('<desc></desc>')
    if ctx.opts.debug:
        print 'Schema node generated: '+tagpath
    return res

def generate_classes(module, directory, package, src, ctx):
    """Generates a java class hierarchy providing an interface to a YANG module
    
    module    -- A data model tree, parsed from a YANG model
    directory -- Path to where files should be written
    package   -- Name of java package
    src       -- The .yang file from which the module was parsed, or the module
                 name and revision if filename is unknown
    ctx       -- Context used to fetch option parameters
    
    """
    ns = module.search_one('namespace')
    prefix = module.search_one('prefix')
    (filename, name) = extract_names(prefix.arg)
    for stmt in module.substmts:
        if (stmt.keyword == 'container' or
            stmt.keyword == 'list'): #FIXME add support for submodule, etc.
            generate_class(stmt, directory, package, src, '', ns.arg, name, 
                top_level=True, ctx=ctx)
    with open(directory+'/'+filename, 'w+') as f:
        f.write(java_class(filename, package, 
            ['com.tailf.confm.*', 'com.tailf.inm.*', 'java.util.Hashtable'], 
            'The root class for namespace '+ns.arg+' (accessible from \n * '+ \
            name+'.NAMESPACE) with prefix "'+prefix.arg+'" ('+name+'.PREFIX).', 
            class_fields(ns.arg, prefix.arg)+
            enable(name)+register_schema(name), 
            source=src
        )
    )
    if ctx.opts.debug:
        print 'Java class generated: '+filename

def generate_class(stmt, directory, package, src, path, ns, prefix_name, ctx,
        top_level=False):
    """Generates a java class hierarchy providing an interface to a YANG module
    
    stmt        -- A data model subtree
    directory   -- Path to where files should be written
    package     -- Name of java package
    src         -- The .yang file from which the module was parsed, or the
                   module name and revision if filename is unknown
    path        -- The XPath of stmt in the original module
    ns          -- The XML namespace of the module
    prefix_name -- The module prefix
    top_level   -- Whether or not this is a top-level statement
    ctx         -- Context used to fetch option parameters
    
    """
    (filename, name) = extract_names(stmt.arg)
    access_methods = ''
    fields = []
    for sub in stmt.substmts: 
    #FIXME Perhaps better to have several loops to get correct order and be 
    #      able to call generate_class outside of if-statements
        if sub.keyword == 'list':
            generate_class(sub, directory, package, src, path+stmt.arg+'/', ns,
                prefix_name, ctx)
            key = sub.search_one('key')
            access_methods += access_methods_comment(sub)+ \
            get_stmt(sub, key.arg)+ \
            get_stmt(sub, key.arg, arg_type='String')+ \
            child_iterator(sub)+ \
            add_stmt(sub, arg_name=sub.arg, arg_type=sub.arg)+ \
            add_stmt(sub, arg_name=key.arg)+ \
            add_stmt(sub, arg_name=key.arg, arg_type='String')+ \
            add_stmt(sub, arg_name=sub.arg, arg_type='')+ \
            delete_stmt(sub, arg_name=key.arg)+ \
            delete_stmt(sub, arg_name=key.arg, arg_type='String')
        if sub.keyword == 'container':
            generate_class(sub, directory, package, src, path+stmt.arg+'/', ns,
                prefix_name, ctx)
            fields.append(sub.arg) #FIXME might have to append more fields
            access_methods += access_methods_comment(sub)+ \
            child_field(sub)+ \
            add_stmt(sub, arg_name=sub.arg, arg_type=sub.arg, field=True)+ \
            add_stmt(sub, arg_name=sub.arg, arg_type='', field=True)+ \
            delete_stmt(sub, arg_name='', arg_type='')
        if sub.keyword == 'leaf':
            key = stmt.search_one('key')
            if key is not None and sub.arg in key.arg.split(' '):
                temp = statements.Statement(None, None, None, 'key',
                    arg=sub.arg) 
                optional = False
                # Pass temp to avoid multiple keys
                access_methods += access_methods_comment(temp, optional)
            else:
                optional = True
                access_methods += access_methods_comment(sub, optional)
                #TODO ensure that the leaf is really optional
            type_stmt = sub.search_one('type')
            if type_stmt.arg == 'uint32':
                type_str = 'com.tailf.confm.xs.UnsignedInt'
                access_methods += get_value(sub, ret_type=type_str)+ \
                    set_value(sub, prefix=prefix_name, arg_type=type_str)+ \
                    set_value(sub, prefix='', arg_type='String', 
                        confm_type=type_str)+ \
                    set_value(sub, prefix='', arg_type='long', 
                        confm_type=type_str)
            elif type_stmt.arg == 'string':
                type_str = 'com.tailf.confm.xs.String'
                access_methods += get_value(sub, ret_type=type_str)+ \
                    set_value(sub, prefix=prefix_name, arg_type=type_str)+ \
                    set_value(sub, prefix='', arg_type='String', 
                        confm_type=type_str)
            else:
                print >> sys.stderr, 'WARNING! No support for type "'+ \
                type_stmt.arg+'", defaulting to string.'
                type_str = 'com.tailf.confm.xs.String'
                access_methods += get_value(sub, ret_type=type_str)+ \
                    set_value(sub, prefix=prefix_name, arg_type=type_str)+ \
                    set_value(sub, prefix='')
            if optional:
                access_methods += unset_value(sub)
            access_methods += add_value(sub, prefix_name)
            if optional:
                access_methods += mark(sub, 'replace')+ \
                mark(sub, 'merge')+ \
                mark(sub, 'create')+ \
                mark(sub, 'delete')
    contructors = ''
    cloners = ''
    support_methods = ''
    if filter(is_container, stmt.substmts): #TODO Verify correctness of cond.
        support_methods = support_add(fields)
    if stmt.keyword == 'container':
        constructors = constructor(stmt, top_level, prefix=prefix_name)
        cloners = clone(name, shallow=False)+clone(name, shallow=True)
    elif stmt.keyword == 'list':
        key = stmt.search_one('key')
        constructors = constructor(stmt, top_level, prefix=prefix_name, 
            throws="\n        throws INMException")+ \
        constructor(stmt, top_level, prefix=prefix_name, 
            arg_type='com.tailf.confm.xs.String', arg_name=key.arg, throws='''
        throws INMException''')+ \
        constructor(stmt, top_level, prefix=prefix_name, 
            arg_type='String', arg_name=key.arg, throws='''
        throws INMException''') #FIXME multiple keys not handled
        cloners = clone(name, key.arg.capitalize(), shallow=False)+ \
            clone(name, key.arg.capitalize(), shallow=True)
    with open(directory+'/'+filename, 'w+') as f:
        f.write(java_class(filename, package, 
            ['com.tailf.confm.*', 'com.tailf.inm.*', 'java.util.Hashtable'], 
            #FIXME Hashtable not used
            'This class represents a "'+path+stmt.arg+
            '" element\n * from the namespace '+ns,
            constructors+cloners+key_names(stmt)+
            children_names(stmt)+access_methods+support_methods,
            #TODO add getters, setters, etc. for children stmts
            source=src,
            modifiers=' extends Container'
        )
    )
    if ctx.opts.debug:
        print 'Java class generated: '+filename

def generate_javadoc(stmts, java_files, ctx):
    """Generates a list of class filenames and lists of their subclasses'
    filenames, positioned immediately after each filename if any.
    
    stmts      -- list of statements representing a YANG module tree node
    java_files -- list of java class filenames that has been generated
    ctx        -- Context, ignored
    
    """
    hierarchy = []
    for stmt in stmts:
        (filename, name) = extract_names(stmt.arg)
        if filename in java_files:
            java_files.remove(filename)
            hierarchy.append(filename)
            children = generate_javadoc(stmt.substmts, java_files, ctx)
            if children:
                hierarchy.append(children)
    return hierarchy

def java_class(filename, package, imports, description, body, version='1.0',
               modifiers='', source='<unknown>.yang'):
    """The java_class function returns a string representing java code for a 
    class.
    
    filename    -- Should preferrably not contain a complete path since it is
                   displayed in a java comment in the beginning of the code. 
    package     -- Should be just the name of the package in which the class 
                   will be included.
    imports     -- Should be a list of names of imported libraries.
    description -- Defines the class semantics.
    body        -- Should contain the actual code of the class
    version     -- Version number, defaults to '1.0'.
    modifiers   -- Can contain java statements
                   such as ' implements Serializable' or ' extends Element'.
    source      -- A string somehow representing the origin of the class
    
    """
    # Fetch current date and set date format
    time = datetime.date.today()
    date = str(time.day)+'/'+str(time.month)+'/'+str(time.year%100)
    #The header_comment is placed in the beginning of the java file
    header_comment = '/* \n * @(#)'+filename+'        '+version+' '+date+'''
 *
 * This file has been auto-generated by JPyang, the java output format plugin 
 * of pyang. Origin: '''+source+'\n */'
    # Change date format for class doc comment
    date = str(time.year)+'-'+str(time.month)+'-'+str(time.day)
    class_doc = '''/**
 * '''+description+'''
 *
 * @version    '''+version+' '+date+'''
 * @author    Auto Generated
 */'''
    # package and import statement goes here
    header = header_comment+'\n\npackage '+package+';\n'
    if len(imports) > 0:
        header += '\n'
    for im in imports:
        header += 'import '+im+';\n'
    header += '\n'+class_doc+'\n'
    # Here is the class declaration, with modifiers. 
    # The class name is the filename without the file extension.
    class_decl = 'public class '+filename.split('.')[0]+modifiers+' {\n'
    return header+class_decl+body+'\n}'

def constructor(stmt, set_prefix=False, prefix='', arg_type='', 
    arg_name='', child='Leaf', throws=''):
    """The constructor function returns a string representing java code for a 
    constructor of a java class corresponding to the stmt parameter.
    
    stmt        -- Typically a module, submodule, container or list Statement
    set_prefix  -- Set to true to add setDefaultPrefix and setPrefix calls
    prefix      -- Path to class containing the XML namespace prefix of the 
                   YANG module. 
    arg_type    -- The argument type. If not set, the method will have no
                   argument.
    arg_name    -- Argument name but without the 'Value* suffix. Typically
                   used as a key in the method. Only used if arg_type is set.
    child       -- Type of the key, if used.
    throws      -- Typically 'throws INMException', prepended with a newline 
                   and spaces for indentation.
    
    """
    name = stmt.arg.capitalize()
    if prefix == '':
        prefix = name
    setters = ''
    if set_prefix:
        setters = '''
        setDefaultPrefix();
        setPrefix('''+prefix+'.PREFIX);'
    docstring = ''
    if arg_type == '':
        setName = ''
        obj_status = 'empty '
    else:
        obj_status = 'initialized '
        if arg_type == 'String':
            value = 'new com.tailf.confm.xs.String('+arg_name+'Value)'
            docstring = '\n     * with Strings for the keys.'
        else:
            value = arg_name+'Value'
        arg_type += ' '
        setName = '''
        // Set key element: '''+arg_name+'''
        '''+child+' '+arg_name+' = new '+ \
        child+'('+prefix+'.NAMESPACE, "'+arg_name+'''");
        '''+arg_name+'.setValue('+value+''');
        insertChild('''+arg_name+', childrenNames());' #FIXME throws
        arg_name = arg_name+'Value'
        docstring += '\n     * @param '+arg_name+' Key argument of child.'
    return '''
    /**
     * Constructor for an '''+obj_status+name+' object.'+docstring+'''
     */
    public '''+name+'('+arg_type+arg_name+')'+throws+''' {
        super('''+prefix+'.NAMESPACE, "'+stmt.arg+'");'+ \
        setName+setters+'''
    }
'''

def clone(class_name, key_name='', shallow='False'):
    """Returns a string representing a java clone method. If key_name is set
    to a non-empty string, the get<key_name>Value() method is called and null
    is returned if an INMException is raised. If shallow is set, the
    cloneShallowContent method is invoked instead of cloneContent.
    
    class_name -- The name of the class to clone instances of
    key_name   -- Key identifier
    shallow    -- Boolean flag for which clone method to use
    
    """ #FIXME add support for multiple keys
    if key_name != '':
        try_stmt = 'try {\n'+' '*12
        catch_stmt = '(get'+key_name+'''Value()));
        } catch (INMException e) { return null; }
    }\n'''
    else:
        try_stmt = ''
        catch_stmt = '());\n    }\n'
    if not shallow:
        copy = 'n exact'
        children = ''
        signature = 'Object clone()'
        cast = '('+class_name+')' #FIXME Maybe this is not always required
        method = 'cloneContent'
    else:
        copy = ' shallow'
        children = ' Children are not included.'
        signature = 'Element cloneShallow()'
        cast = ''
        method = 'cloneShallowContent'
    return '''
    /**
     * Clones this object, returning a'''+copy+''' copy.
     * @return A clone of the object.'''+children+'''
     */
    public '''+signature+''' {
        '''+try_stmt+'return '+cast+method+'(new '+class_name+catch_stmt

def key_names(stmt):
    """Returns a string representing a java method that returns a String[]
    with the identifiers of the keys in stmt. If stmt does not have any keys,
    null is returned.
    
    stmt -- A pyang Statement, typically a list or a container
    
    """
    keys = stmt.search('key')
    if not keys: # if len(keys) == 0:
        res = 'return null'
    else:
        res = 'return new String[] {\n'
        # Add keys to res, one key per line, indented by 12 spaces
        for key_str in keys:
            for key in key_str.arg.split(' '):
                res += ' '*12+'"'+key+'",\n'
        res = res[:-2]+'\n'+' '*8+'}'
    return '''
    /**
     * Structure information which specifies
     * the keys for the list entries.
     */
    public String[] keyNames() {
        '''+res+''';
    }
''' #FIXME Add support for multiple keys

def children_names(stmt):
    """Returns a string representing a java method that returns a String[]
    with the identifiers of the children of stmt, excluding any keys.
    
    stmt -- A pyang Statement, typically a list or a container
    
    """
    children = filter(lambda x: #x.keyword != 'key' and #FIXME add more
        x.keyword != 'key', 
        stmt.substmts)
    names = [ch.arg for ch in children]
    if len(names) > 0:
        names = repr(names)[1:-1].replace("'", '"').replace(', ', ',\n'+' '*12)
    else:
        names = ''
    return '''
    /**
     * Structure information with the names of the children.
     * Makes it possible to order the children.
     */
    public String[] childrenNames() {
        return new String[] {
            '''+names+'''
        };
    }
''' #FIXME What if there are no children?

def class_fields(ns_arg, prefix_arg):
    """Returns a string representing java code for two fields"""
    return '''
    public static final String NAMESPACE = "'''+ns_arg+'''";

    public static final String PREFIX = "'''+prefix_arg+'''";
    '''

def enable(prefix_name):
    """Returns a string representing a java method that calls the
    Container.setPackage method of the confm library, and the registerSchema
    method of the class with prefix == prefix_name (typically = this).
    
    prefix_name -- The name of the class containing the registerSchema method
    
    """
    return '''
    /**
     * Enable the elements in this namespace to be aware 
     * of the data model and use the generated classes.
     */
    public static void enable() throws INMException {
        Container.setPackage(NAMESPACE, PREFIX);
        '''+prefix_name+'''.registerSchema();
    }
'''

def register_schema(prefix_name):
    """Returns a string representing a java method that creates a SchemaParser
    and calls its readFile method with the schema corresponding to the class
    and a hashtable obtained from a call to CsTree.create (a method in the 
    ConfM library).
    
    prefix_name -- The name of the class containing the registerSchema method
    
    """
    return '''
    /**
     * Register the schema for this namespace in the global 
     * schema table (CsTree) making it possible to lookup 
     * CsNode entries for all tagpaths 
     */
    public static void registerSchema() throws INMException {
        StackTraceElement[] sTrace = (new Exception()).getStackTrace();
        ClassLoader loader = sTrace[0].getClass().getClassLoader();
        java.net.URL schemaUrl = loader.getSystemResource("'''+ \
            prefix_name+'''.schema");
        SchemaParser parser = new SchemaParser();
        Hashtable h = CsTree.create(NAMESPACE);
        if (schemaUrl == null)
            parser.readFile("'''+prefix_name+'''.schema", h);
        else
            parser.readFile(schemaUrl, h);
    }
'''

def access_methods_comment(stmt, optional=False):
    """Returns a string representing a java comment for code structure"""
    if optional:
        property = 'optional '
    else:
        property = ''
    return '''
    /**
     * -------------------------------------------------------
     * Access methods for '''+property+stmt.keyword+ \
     ' child: "'+stmt.arg+'''".
     * -------------------------------------------------------
     */
'''

def child_field(stmt):
    """Returns a string representing java code for a field"""
    return '''
    /**
     * Field for child '''+stmt.keyword+' "'+stmt.arg+'''".
     */
    public '''+stmt.arg.capitalize()+' '+stmt.arg+''' = null;
'''

def get_stmt(stmt, arg_name, arg_type='com.tailf.confm.xs.String'):
    """Get method generator. Similar to add_stmt (see below), but does not
    allow parameter-free methods to be generated.
    
    stmt     -- Typically a list statement
    arg_name -- The key identifier
    arg_type -- Java type of argument
    
    """
    name = stmt.arg.capitalize()
    spec = ''
    if arg_type == 'String':
        spec = '\n     * The keys are specified as Strings'
    return '''
    /**
     * Get method for '''+stmt.keyword+' entry: "'+stmt.arg+'''".
     * Return the child with the specified keys '''+ \
     '(if any).'+spec+'''
     * @param '''+arg_name+''' Key argument of child.
     * @return The '''+stmt.keyword+''' entry with the specified keys.
     */
    public '''+name+' get'+name+'('+arg_type+' '+arg_name+''')
        throws INMException {
        String path = "'''+stmt.arg+'['+arg_name+'=\'"+'+arg_name+'''+"']";
        return ('''+name+''')getListContainer(path);
    }
'''

def get_value(stmt, ret_type='com.tailf.confm.xs.String'):
    """get<Identifier>Value method generator. Similar to get_stmt (see below),
    but allows parameter-free methods to be generated.
    
    stmt     -- Typically a leaf statement
    ret_type -- The type of the return value of the generated method
    
    """
    name = stmt.arg.capitalize()
    return '''
    /**
     * Return the value for child '''+stmt.keyword+' "'+stmt.arg+'''".
     * @return The value of the '''+stmt.keyword+'''.
     */
    public '''+ret_type+' get'+name+'''Value()
        throws INMException {
        return ('''+ret_type+')getValue("'+stmt.arg+'''");
    }
'''

def set_value(stmt, prefix='', arg_type='', confm_type=''):
    """set<Identifier>Value method generator.
    
    stmt       -- Typically a leaf statement
    prefix     -- Namespace prefix of module, empty if the setLeafValue method
                  is not to be used in the method
    arg_type   -- Type of method parameter, empty if parameter free
    confm_type -- Type to use internally, empty if the setIdValue method is not
                  to be used in the method
    
    """
    name = stmt.arg.capitalize()
    spec1 = ''
    spec2 = ''
    if arg_type == 'String':
        spec1 = ', using a string value'
        spec2 = 'string representation of the '
    elif arg_type == 'long':
        spec1 = ', using the java primitive value'
    if prefix: # Equivalent to 'if len(prefix) > 0:'
        body = 'setLeafValue('+prefix+'''.NAMESPACE,
            "'''+stmt.arg+'''",
            '''+stmt.arg+'''Value,
            childrenNames());'''
    else:
        body = 'set'+name+'Value(new '+confm_type+'('+stmt.arg+'Value));'
    return '''
    /**
     * Sets the value for child '''+stmt.keyword+' "'+stmt.arg+'"'+spec1+'''.
     * @param '''+stmt.arg+'Value The '+spec2+'''value to set.
     */
    public void set'''+name+'Value('+arg_type+' '+stmt.arg+'''Value)
        throws INMException {
        '''+body+'''
    }
'''

def unset_value(stmt):
    """unset<Identifier> method generator"""
    return '''
    /**
     * Unsets the value for child '''+stmt.keyword+' "'+stmt.arg+'''".
     */
    public void unset'''+stmt.arg.capitalize()+'''Value()
        throws INMException {
        delete("'''+stmt.arg+'''");
    }
'''

def add_value(stmt, prefix):
    """add<Identifier> method generator, designed for leaf statements. Not to
    be confused with the add_stmt function which is similar but does not
    contain a call to the setLeafValue function.
    
    stmt   -- Typically a leaf statement
    prefix -- Namespace prefix of module
    
    """
    name = stmt.arg.capitalize()
    return '''
    /**
     * This method is used for creating a subtree filter.
     * The added "'''+stmt.arg+'" '+stmt.keyword+''' will not have a value.
     */
    public void add'''+name+'''()
        throws INMException {
        setLeafValue('''+prefix+'''.NAMESPACE,
            "'''+stmt.arg+'''",
            null,
            childrenNames());
    }
'''

def mark(stmt, op):
    """Generates a method that enqueues an operation to be performed on an
    element.
    
    stmt -- Typically a leaf statement
    op   -- The operation 'replace', 'merge', 'create' or 'delete'
    
    """
    return '''
    /**
     * Marks the "'''+stmt.arg+'" '+stmt.keyword+' with operation "'+op+'''".
     */
    public void mark'''+stmt.arg.capitalize()+op.capitalize()+'''()
        throws INMException {
        markLeaf'''+op.capitalize()+'("'+stmt.arg+'''");
    }
'''

def child_iterator(substmt):
    """Returns a string representing a java iterator method for the substmt"""
    return '''
    /**
     * Iterator method for the '''+substmt.keyword+' "'+substmt.arg+'''".
     * @return An iterator for the '''+substmt.keyword+'''.
     */
    public ElementChildrenIterator '''+substmt.arg+'''Iterator() {
        return new ElementChildrenIterator(children, "'''+substmt.arg+'''");
    }
'''

def add_stmt(stmt, arg_name, arg_type='com.tailf.confm.xs.String', field=False):
    """Generates add-method for stmt, optionally parametrized by an 
    argument of specified type and with customizable comments.
    
    stmt     -- The YANG statement that needs an adder
    arg_name -- The name of the method's argument, typically a key identifier
    arg_type -- The type of the method's argument, also used to deduce the 
                semantics of the method. Four cases:
                1. The default value produces a method that adds a new stmt 
                   with key of the Tail-f String type.
                2. An empty string produces a method with no argument, which
                   can be used to create subtree filters.
                3. If arg_type is the same as stmt.arg, the produced method
                   will add its argument as a child instead of creating a new
                   one. No cloning occurs.
                4. If 'String', The key is specified with the ordinary String
                   type instead of the Tail-f String type.
    field    -- If set to True, the statement is added to a field, typically
                in a container class.
    
    """
    name = stmt.arg.capitalize()
    if arg_type == stmt.arg:
        spec1 = '.\n     * @param '+arg_name+ \
            ' Child to be added to children'
        spec2 = name+' '+stmt.arg
        spec3 = ''
    else:
        spec3 = '\n'+' '*8+name+' '+stmt.arg+ \
            ' = new '+name+'('
        if arg_type == '':
            spec1 = '.\n     * This method is used for creating subtree filters'
            spec2 = ''
            spec3 += ');'
        else:
            spec1 = ', with given key arguments'
            if arg_type == 'String':
                spec1 += '.\n     * The keys are specified as strings'
            spec1 += '.\n     * @param '+arg_name+' Key argument of child'
            spec2 = arg_type+' '+arg_name
            spec3 += arg_name+');'
    if field:
        spec3 += '\n'+' '*8+'this.'+arg_name+' = '+arg_name+';'
    return '''
    /**
     * Adds '''+stmt.keyword+' entry "'+stmt.arg+'"'+spec1+'''.
     * @return The added child.
     */
    public '''+name+' add'+name+'('+spec2+''')
        throws INMException {'''+spec3+'''
        insertChild('''+stmt.arg+''', childrenNames());
        return '''+stmt.arg+''';
    }
'''

def delete_stmt(stmt, arg_name, arg_type='com.tailf.confm.xs.String'):
    """Delete method generator. Similar to get_stmt (see above).
    
    stmt     -- Typically a list or container statement
    arg_name -- The key identifier, if any (set to empty string otherwise)
    arg_type -- Java type of argument. If no argument, set it to empty string.
    
    """
    name = stmt.arg.capitalize()
    spec1 = '", with specified keys.'
    if arg_type == 'String':
        spec1 += '\n     * The keys are specified as Strings'
    if arg_type == '':
        spec1 = ''
        spec2 = 'this.'+stmt.arg+' = null;\n        '
        spec3 = ''
    else:
        arg_type += ' '
        spec1 += '\n     * @param '+arg_name+' Key argument of child.'
        spec2 = ''
        spec3 = '['+arg_name+'=\'"+'+arg_name+'+"\']'
    return '''
    /**
     * Deletes '''+stmt.keyword+' entry "'+stmt.arg+spec1+'''"
     */
    public void delete'''+name+'('+arg_type+arg_name+''')
        throws INMException {
        '''+spec2+'String path = "'+stmt.arg+spec3+'''";
        delete(path);
    }
'''

def support_add(fields=[]):
    """Generates an addChild method.
    
    fields -- a list of fields in the generated class
    
    """
    assignments = ''
    for i in range(len(fields)-1, -1, -1):
        assignments += 'if ($child instanceof '+fields[i].capitalize()+') '+ \
            fields[i]+' = ('+fields[i].capitalize()+')$child;'
        if i > 0:
            assignments += '\n        else '
    return '''
    /**
     * -------------------------------------------------------
     * Support method for addChild.
     * -------------------------------------------------------
     */

    /**
     * Adds a child to this object.
     */
    public void addChild(Element $child) {
        super.addChild($child);
        '''+assignments+'''
    }
'''#FIXME '$' should be removed unless it is actually needed

def is_not_list(entry):
    """Returns False iff entry is instance of list"""
    return not isinstance(entry, list)

def html_list(body, indent_level, tag='ul'):
    """Returns a string representing javadoc with a <ul> html-element if ul,
    else with a <li> html-element.
    
    """
    body = '<'+tag+'>\n'+body
    if body[-1:] != '\n':
        body += '\n'
    body += '</'+tag+'>'
    return indent(body.split('\n'), indent_level)

def parse_hierarchy(hierarchy):
    """Returns html for a list of javadoc pages corresponding to the .java
    files in the hierarchy list.
    
    hierarchy -- a tree of .java files represented as a list, for example:
        ['Foo.java', ['Bar.java', ['Baz.java'], 'Qux.java']] would represent the
        hierarchy structure:
        Foo
        |   Bar
        |   |   Baz
        |   Qux
        
        That is, Baz is a child of Bar in the data model tree, and Bar and Qux
        are children of the top level element Foo.
    
    """
    res = ''
    for entry in hierarchy:
        if is_not_list(entry):
            body = '    <a href="'+entry[:-5]+'.html">'+entry[:-5]+'</a>'
            res += html_list(body, 1, tag='li')
        else:
            body = parse_hierarchy(entry)
            res += html_list(body, 1)
        if body[-1:] != '\n':
            res += '\n'
    return res

def gen_package(class_hierarchy, package, ctx):
    """Writes a package-info.java file to the package directory with a high
    level description of the package functionality and requirements.
    
    class_hierarchy -- A tree represented as a list as in parse_hierarchy
    package         -- The package directory as a string
    ctx             -- Context used only for debugging purposes
    
    """
    src = ''
    decapitalize = lambda s: s[:1].lower() + s[1:] if s else ''
    top_level_entries = filter(is_not_list, class_hierarchy)
    for entry in top_level_entries:
        module_arg = decapitalize(entry[:-5])
        rev = ctx.revs[module_arg][-1:][:0]
        if not rev:
            rev = 'unknown'
        src += 'module "'+module_arg+'" (rev "'+rev+'"), '
    source = ''
    if len(top_level_entries) > 1:
        source += 's'
    source += '\n'+src[:-2]
    html_hierarchy = html_list(parse_hierarchy(class_hierarchy), 0)
    specification = '''
This class hierarchy was generated from the Yang module'''+source+ \
' by the <a target="_top" href="https://github.com/Emil-Tail-f/JPyang">'+ \
'JPyang</a> plugin of <a target="_top" '+ \
'''href="http://code.google.com/p/pyang/">pyang</a>.
The generated classes may be used to manipulate pieces of configuration data
with NETCONF operations such as edit-config, delete-config and lock. These
operations are typically accessed through the ConfM Java library by
instantiating Device objects and setting up NETCONF sessions with real devices
using a compatible YANG model.

'''
    with open('package-info.java', 'w+') as f:
        f.write('/**'+java_docify(specification+html_hierarchy)+'''
 * 
 * @see <a target="_top" href="https://github.com/Emil-Tail-f/JPyang">JPyang project page</a>
 * @see <a target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc6020.txt">RFC 6020: YANG - A Data Modeling Language for the Network Configuration Protocol (NETCONF)</a>
 * @see <a target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc6241.txt">RFC 6241: Network Configuration Protocol (NETCONF)</a>
 * @see <a target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc6242.txt">RFC 6242: Using the NETCONF Protocol over Secure Shell (SSH)</a>
 * @see <a target="_top" href="http://www.tail-f.com">Tail-f Systems</a>
 */
package '''+package+';')
    if ctx.opts.debug:
        print 'Package description generated: package-info.java'








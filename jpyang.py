#!/usr/bin/python
# -*- coding: latin-1 -*-
"""JPyang: Java output plug-in
 * Copyright (c) 2012 Tail-F Systems AB, Stockholm, Sweden
 * All rights reserved.
 *
 * Licensed under the BSD 3-Clause License
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Tail-f Systems AB nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.

For complete functionality, invoke with:
> pyang \
    --path <yang search path> \
    --format java \
    --jpyang-output <package.name> \
    --jpyang-verbose \
    --jpyang-ignore-errors \
    --jpyang-javadoc <javadoc directory path> \
    <file.yang>

Or, if you like to keep things simple:
> pyang -f jpyang -d <package.name> <file.yang>

"""

import optparse  # TODO: Deprecated in python 2.7, should use argparse instead
# ... See http://stackoverflow.com/questions/3217673/why-use-argparse-rather-than-optparse
# ... and http://docs.python.org/dev/library/argparse.html#upgrading-optparse-code
import os
import errno
import sys
import collections
import re

from datetime import date
from pyang import plugin, util, error


def pyang_plugin_init():
    """Registers an instance of the jpyang plugin"""
    plugin.register_plugin(JPyangPlugin())


class JPyangPlugin(plugin.PyangPlugin):
    """The plug-in class of JPyang.

    The methods of this class are invoked by pyang during initialization. The
    emit method is of particular interest if you are new to writing plugins to
    pyang. It is from there that the parsing of the YANG statement tree
    emanates, producing the generated classes that constitutes the output of
    this plug-in.

    """

    def add_output_format(self, fmts):
        """Adds 'java' and 'jpyang' as valid output formats and sets the format
        to jpyang if the --jpyang-output option is set
        
        """
        self.multiple_modules = False
        fmts['java'] = fmts['jpyang'] = self

        args = sys.argv[1:]
        if not any(x in args for x in ('-f', '--format')):
            if any(x in args for x in ('-d', '--jpyang-output')):
                sys.argv.insert(1, '--format')
                sys.argv.insert(2, 'jpyang')

    def add_opts(self, optparser):
        """Adds options to pyang, displayed in the pyang CLI help message"""
        optlist = [
            optparse.make_option(
                '-d', '--jpyang-output',
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
                help='Print debug messages. Redundant if verbose mode is on.'),
            optparse.make_option(
                '--jpyang-javadoc',
                dest='javadoc_directory',
                help='Generate javadoc to JAVADOC_DIRECTORY.'),
            optparse.make_option(
                '--jpyang-no-schema',
                dest='no_schema',
                action='store_true',
                help='Do not generate schema.'),
            optparse.make_option(
                '--jpyang-verbose',
                dest='verbose',
                action='store_true',
                help='Verbose mode: Print detailed debug messages.'),
            optparse.make_option(
                '--jpyang-ignore-errors',
                dest='ignore',
                action='store_true',
                help='Ignore errors from validation.'),
            ]
        g = optparser.add_option_group('JPyang output specific options')
        g.add_options(optlist)

    def setup_ctx(self, ctx):
        """Called after ctx has been set up in main module. Checks if the
        jpyang help option was supplied and if not, that the -d or
        --java-package was used.

        ctx -- Context object as defined in __init__.py

        """
        if ctx.opts.jpyang_help:
            self.print_help()
            sys.exit(0)
        if ctx.opts.format in ['java', 'jpyang']:
            if not ctx.opts.directory:
                ctx.opts.directory = 'gen'
                print_warning(msg=('Option -d (or --java-package) not set, ' +
                    'defaulting to "gen".'))
            ctx.rootpkg = ctx.opts.directory.rpartition('src')[2][1:]

    def setup_fmt(self, ctx):
        """Disables implicit errors for the Context"""
        ctx.implicit_errors = False

    def emit(self, ctx, modules, fd):
        """Generates Java classes from the YANG module supplied to pyang.

        The generated classes are placed in the directory specified by the '-d'
        or '--java-package' flag, or in "gen" if no such flag was provided,
        using the 'directory' attribute of ctx. If there are existing files
        in the output directory with the same name as the generated classes,
        they are silently overwritten.

        ctx     -- Context used to get output directory, verbosity mode, error
                   handling policy (the ignore attribute) and whether or not a
                   schema file should be generated.
        modules -- A list containing a module statement parsed from the YANG
                   module supplied to pyang.
        fd      -- File descriptor (ignored).

        """
        if not ctx.opts.ignore:
            for (epos, etag, _) in ctx.errors:
                if (error.is_error(error.err_level(etag)) and
                    etag in ('MODULE_NOT_FOUND', 'MODULE_NOT_FOUND_REV')):
                    self.fatal("%s contains errors" % epos.top.arg)
                if (etag in ('TYPE_NOT_FOUND', 'FEATURE_NOT_FOUND',
                    'IDENTITY_NOT_FOUND', 'GROUPING_NOT_FOUND')):
                    print_warning(msg=(etag.lower() + ', generated class ' +
                        'hierarchy might be incomplete.'), key=etag)
                else:
                    print_warning(msg=(etag.lower() + ', aborting.'), key=etag)
                    self.fatal("%s contains errors" % epos.top.arg)
        directory = ctx.opts.directory
        d = directory.replace('.', os.sep)
        for module in modules:
            if module.keyword == 'module':
                # Generate Java classes
                src = ('module "' + module.arg + '", revision: "' +
                    util.get_latest_revision(module) + '".')
                generator = ClassGenerator(module, package=directory, src=src, ctx=ctx)
                generator.generate()
                for aug_module in augmented_modules.values():
                    src = ('module "' + aug_module.arg + '", revision: "' +
                        util.get_latest_revision(aug_module) + '".')
                    generator = ClassGenerator(aug_module, package=directory, src=src, ctx=ctx)
                    generator.generate()
                if ctx.opts.debug or ctx.opts.verbose:
                    print 'Java classes generation COMPLETE.'
                if not ctx.opts.no_schema:

                    # Generate external schema
                    schema_nodes = ['<schema>']
                    stmts = []
                    stmts.extend(module.substmts)
                    module_root = SchemaNode(module, '/')
                    schema_nodes.extend(module_root.as_list())
                    for aug_module in augmented_modules.values():
                        stmts.extend(aug_module.substmts)
                        aug_module_root = SchemaNode(aug_module, '/')
                        schema_nodes.extend(aug_module_root.as_list())
                    schema_generator = SchemaGenerator(stmts, '/', ctx)
                    schema_nodes.extend(schema_generator.schema_nodes())
                    for i in range(1, len(schema_nodes)):
                        # Indent all but the first and last line
                        if schema_nodes[i] in ('<node>', '</node>'):
                            schema_nodes[i] = ' ' * 4 + schema_nodes[i]
                        else:
                            schema_nodes[i] = ' ' * 8 + schema_nodes[i]
                    schema_nodes.append('</schema>')

                    name = capitalize_first(module.search_one('prefix').arg)
                    write_file(d, name + '.schema', '\n'.join(schema_nodes), ctx)
                    if ctx.opts.debug or ctx.opts.verbose:
                        print 'Schema generation COMPLETE.'

                augmented_modules.clear()

            else:
                print_warning(msg=('Ignoring schema tree rooted at "' +
                    module.keyword + ' ' + module.arg + '" - not a module'))

        # Generate javadoc
        for module in modules:
            if module.keyword == 'module':
                package_info_generator = PackageInfoGenerator(d, module, ctx)
                package_info_generator.generate_package_info()
        javadir = ctx.opts.javadoc_directory
        if javadir:
            if ctx.opts.debug or ctx.opts.verbose:
                print 'Generating javadoc...'
            if ctx.opts.verbose:
                os.system('javadoc -d ' + javadir + ' -subpackages ' + d)
            else:
                os.system(('javadoc -d ' + javadir + ' -subpackages ' + d +
                    ' >/dev/null'))
            if ctx.opts.debug or ctx.opts.verbose:
                print 'Javadoc generation COMPLETE.'
        if len(modules) != 1:
            print_warning(msg=('Generating code for several modules has not' +
                ' been tested thoroughly.'), ctx=ctx)

    def fatal(self, exitCode=1):
        """Raise an EmitError"""
        raise error.EmitError(self, exitCode)

    def print_help(self):
        """Prints a description of what JPyang is and how to use it"""
        print '''
The JPyang/Java output format can be used to generate a Java class hierarchy
from a single YANG data model. Together with the JNC (Java NETCONF Client)
library, these generated Java classes may be used as the foundation for a
NETCONF client (AKA manager) written in Java.

The different types of generated files are:

Root class  -- This class has the name of the prefix of the YANG module, and
               contains fields with the prefix and namespace as well as methods
               that enables the JNC library to use the other generated classes
               when interacting with a NETCONF server.

YangElement -- Each YangElement corresponds to a container or a list in the
               YANG model. They represent tree nodes of a configuration and
               provides methods to modify the configuration in accordance with
               the YANG model that they were generated from.

               The top-level containers or lists in the YANG model will have
               their corresponding YangElement classes generated in the output
               directory together with the root class. Their respective
               subcontainers and sublists are generated in subpackages with
               names corresponding to the name of the parent container or list.

YangTypes   -- For each derived type in the YANG model, a class is generated to
               the root of the output directory. The derived type may either
               extend another derived type class, or the JNC type class
               corresponding to a built-in YANG type.

Packageinfo -- For each package in the generated Java class hierarchy, a
               package-info.java file is generated, which can be useful when
               generating javadoc for the hierarchy.

Schema file -- If enabled, an XML file containing structured information about
               the generated Java classes is generated. It contains tagpaths,
               namespace, primitive-type and other useful meta-information.

The typical use case for these classes is as part of a JAVA network management
system (EMS), to enable retrieval and/or storing of configurations on NETCONF
agents/servers with specific capabilities.

One way to use the Java output format plug-in of pyang is
$ pyang -f java --jpyang-output package.dir <file.yang>

The two formats java and jpyang produce identical results.

Type '$ pyang --help' for more details on how to use pyang.
'''


java_reserved_words = {'abstract', 'assert', 'boolean', 'break', 'byte',
    'case', 'catch', 'char', 'class', 'const', 'continue', 'default',
    'double', 'do', 'else', 'enum', 'extends', 'false',
    'final', 'finally', 'float', 'for', 'goto', 'if',
    'implements', 'import', 'instanceof', 'int', 'interface', 'long',
    'native', 'new', 'null', 'package', 'private', 'protected',
    'public', 'return', 'short', 'static', 'strictfp', 'super',
    'switch', 'synchronized', 'this', 'throw', 'throws', 'transient',
    'true', 'try', 'void', 'volatile', 'while'}
"""A set of all identifiers that are reserved in Java"""


java_literals = {'true', 'false', 'null'}
"""The boolean and null literals of Java"""


java_lang = {'Boolean', 'Byte', 'Double', 'Float', 'Integer', 'Long', 'Number',
             'Object', 'Short', 'String', 'StackTraceElement', 'ClassLoader'}
"""A subset of the java.lang classes"""


java_util = {'Collection', 'Enumeration', 'Iterator', 'List', 'ListIterator',
             'Map', 'Queue', 'Set', 'ArrayList', 'Arrays', 'HashMap',
             'HashSet', 'Hashtable', 'LinkedList', 'Properties', 'Random',
             'Scanner', 'Stack', 'StringTokenizer', 'Timer', 'TreeMap',
             'TreeSet', 'UUID', 'Vector'}
"""A subset of the java.util interfaces and classes"""


java_built_in = java_reserved_words | java_literals | java_lang
"""Identifiers that shouldn't be imported in Java"""


package_info = '''/**
 * This class hierarchy was generated from the Yang module{0}
 * by the <a target="_top" href="https://github.com/Emil-Tail-f/JPyang">JPyang</a> plugin of <a target="_top" href="http://code.google.com/p/pyang/">pyang</a>.
 * The generated classes may be used to manipulate pieces of configuration data
 * with NETCONF operations such as edit-config, delete-config and lock. These
 * operations are typically accessed through the JNC Java library by
 * instantiating Device objects and setting up NETCONF sessions with real
 * devices using a compatible YANG model.
 * <p>{1}
 * @see <a target="_top" href="https://github.com/Emil-Tail-f/JPyang">JPyang project page</a>
 * @see <a target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc6020.txt">RFC 6020: YANG - A Data Modeling Language for the Network Configuration Protocol (NETCONF)</a>
 * @see <a target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc6241.txt">RFC 6241: Network Configuration Protocol (NETCONF)</a>
 * @see <a target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc6242.txt">RFC 6242: Using the NETCONF Protocol over Secure Shell (SSH)</a>
 * @see <a target="_top" href="http://www.tail-f.com">Tail-f Systems</a>
 */
 package '''
"""Format string used in package-info files"""


outputted_warnings = []
"""A list of warning message IDs that are used to avoid duplicate warnings"""


augmented_modules = {}
"""A dict of external modules that are augmented by the YANG module"""


def print_warning(msg='', key='', ctx=None):
    """Prints msg to stderr if ctx is None or the debug or verbose flags are
    set in context ctx and key is empty or not in outputted_warnings. If key is
    not empty and not in outputted_warnings, it is added to it. If msg is empty
    'No support for type "' + key + '", defaulting to string.' is printed.

    """
    if ((not key or key not in outputted_warnings) and
        (not ctx or ctx.opts.debug or ctx.opts.verbose)):
        if msg:
            print >> sys.stderr, 'WARNING: ' + msg
            if key:
                outputted_warnings.append(key)
        else:
            print_warning(('No support for type "' + key + '", defaulting ' +
                'to string.'), key, ctx)


def write_file(d, file_name, file_content, ctx):
    """Creates the directory d if it does not yet exist and writes a file to it
    named file_name with file_content in it.

    """
    d = d.replace('.', os.sep)
    wd = os.getcwd()
    try:
        os.makedirs(d, 0777)
    except OSError as exc:
        if exc.errno == errno.EEXIST:
            pass  # The directory already exists
        else:
            raise
    try:
        os.chdir(d)
    except OSError as exc:
        if exc.errno == errno.ENOTDIR:
            print_warning(msg=('Unable to change directory to ' + d +
                '. Probably a non-directory file with same name as one of ' +
                'the subdirectories already exists.'), key=d, ctx=ctx)
        else:
            raise
    finally:
        if ctx.opts.verbose:
            print 'Writing file to: ' + os.getcwd() + os.sep + file_name
        os.chdir(wd)
    with open(d + os.sep + file_name, 'w+') as f:
        if isinstance(file_content, basestring):
            f.write(file_content)
        else:
            for line in file_content:
                print >> f, line


def get_package(stmt, ctx):
    """Returns a string representing the package name of a java class generated
    from stmt, assuming that it has been or will be generated by JPyang.

    """
    sub_packages = collections.deque()
    while stmt.parent is not None:
        stmt = stmt.parent
        if stmt.parent is not None:
            sub_packages.appendleft(camelize(stmt.arg))
    full_package = ctx.rootpkg.split(os.sep)
    full_package.extend(sub_packages)
    return '.'.join(full_package)


def pairwise(iterable):
    """Returns an iterator that includes the next item also"""
    iterator = iter(iterable)
    item = iterator.next()  # throws StopIteration if empty.
    for next_item in iterator:
        yield (item, next_item)
        item = next_item
    yield (item, None)


def capitalize_first(string):
    """Returns string with its first character capitalized (if any)"""
    return string[:1].capitalize() + string[1:]


def camelize(string):
    """Removes hyphens and dots and replaces following character (if any) with
    its upper-case counterpart. Does not remove a trailing hyphen or dot.
    
    If the resulting string is reserved in Java, an underline is appended

    Returns an empty string if string argument is None.

    """
    camelized_str = collections.deque()
    if string is not None:
        iterator = pairwise(string)
        for character, next_character in iterator:
            if next_character and character in '-.':
                camelized_str.append(capitalize_first(next_character))
                iterator.next()
            else:
                camelized_str.append(character)
    res = ''.join(camelized_str)
    if res in java_reserved_words | java_literals:
        camelized_str.append('_')
    if re.match('\d', res):
        camelized_str.appendleft('_')
    return ''.join(camelized_str)


def normalize(string):
    """returns capitalize_first(camelize(string)), except if camelize(string)
    begins with and/or ends with a single underline: then they are/it is
    removed and a 'J' is prepended. Mimics normalize in YangElement of JNC.
    
    """
    res = camelize(string)
    start = 1 if res.startswith('_') else 0
    end = -1 if res.endswith('_') else 0
    if start or end:
        return 'J' + capitalize_first(res[start:end])
    else:
        return capitalize_first(res)


def flatten(l):
    """Returns a flattened version of iterable l

    l must not have an attribute named values unless the return value values()
    is a valid substitution of l. Same applies to all items in l.

    Example: flatten([['12', '34'], ['56', ['7']]]) = ['12', '34', '56', '7']
    """
    res = []
    while hasattr(l, 'values'):
        l = l.values()
    for item in l:
        try:
            assert not isinstance(item, basestring)
            iter(item)
        except (AssertionError, TypeError):
            res.append(item)
        else:
            res.extend(flatten(item))
    return res


def get_types(yang_type, ctx):
    """Returns jnc and primitive counterparts of yang_type, which is a type,
    typedef, leaf or leaf-list statement.

    """
    if yang_type.keyword in ('leaf', 'leaf-list'):
        yang_type = yang_type.search_one('type')
    assert yang_type.keyword in ('type', 'typedef'), 'argument is type, typedef or leaf'
    primitive = normalize(yang_type.arg)
    if primitive == 'JBoolean':
        primitive = 'Boolean'
    jnc = 'com.tailf.jnc.Yang' + primitive
    if yang_type.arg in ('string', 'boolean'):
        pass
    elif yang_type.arg in ('enumeration', 'binary'):
        primitive = 'String'
    elif yang_type.arg in ('bits',):
        primitive = 'BigInteger'
    elif yang_type.arg in ('instance-identifier', 'leafref', 'identityref'):
        primitive = 'Element'
    elif yang_type.arg in ('empty',):
        primitive = 'boolean'
    elif yang_type.arg in ('int8', 'int16', 'int32', 'int64', 'uint8',
            'uint16', 'uint32', 'uint64'):
        integer_type = ['long', 'int', 'short', 'byte']
        if yang_type.arg[:1] == 'u':  # Unsigned
            integer_type.pop()
            integer_type.insert(0, 'long')
            jnc = 'com.tailf.jnc.YangUI' + yang_type.arg[2:]
        if yang_type.arg[-2:] == '64':
            primitive = integer_type[0]
        elif yang_type.arg[-2:] == '32':
            primitive = integer_type[1]
        elif yang_type.arg[-2:] == '16':
            primitive = integer_type[2]
        else:  # 8 bits
            primitive = integer_type[3]
    elif yang_type.arg == 'decimal64':
        primitive = 'BigDecimal'
    else:
        try:
            typedef = yang_type.i_typedef
        except AttributeError:
            type_id = get_package(yang_type, ctx) + yang_type.arg
            print_warning(key=type_id, ctx=ctx)
        else:
            basetype = get_base_type(typedef)
            package = get_package(typedef, ctx)
            typedef_arg = normalize(yang_type.arg)
            return package + '.' + typedef_arg, get_types(basetype, ctx)[1]
    return jnc, primitive


def get_base_type(stmt):
    """Returns the built in type that stmt is derived from"""
    type_stmt = stmt.search_one('type')
    try:
        typedef = type_stmt.i_typedef
    except AttributeError:
        return type_stmt
    else:
        if typedef is not None:
            return get_base_type(typedef)
        else:
            return type_stmt


def is_config(stmt):
    """Returns True if stmt is a configuration data statement"""
    config = None
    while config is None and stmt is not None:
        config = stmt.search_one('config')
        stmt = stmt.parent
    return config is None or config.arg == 'true'


def java_docify(s):
    """Returns the string s, but with each row prepended by ' * '"""
    res = ''
    for row in s.splitlines():
        res += ' * ' + row + '\n'
    return res[:-1]  # Don't include the last newline character


class SchemaNode(object):

    def __init__(self, stmt, tagpath):
        self.stmt = stmt
        self.tagpath = tagpath

    def as_list(self):
        """Returns a string list repr "node" element content for an XML schema"""
        res = ['<node>']
        stmt = self.stmt
        res.append('<tagpath>' + self.tagpath + '</tagpath>')
        if stmt.top is None:
            ns = stmt.search_one('namespace').arg
        else:
            ns = stmt.top.search_one('namespace').arg
        res.append('<namespace>' + ns + '</namespace>')
        res.append('<primitive_type>0</primitive_type>')

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
        childOfContainerOrList = (stmt.parent is not None
            and stmt.parent.keyword in ('container', 'list'))
        if (isKey or stmt.keyword in ('module', 'submodule') or
            (childOfContainerOrList and stmt.keyword in ('container',))):
            min_occurs = '1'
            max_occurs = '1'
        if isMandatory:
            min_occurs = '1'
        if isUnique or childOfContainerOrList or stmt.keyword in ('container',):
            max_occurs = '1'
        res.append('<min_occurs>' + min_occurs + '</min_occurs>')  # TODO: correct?
        res.append('<max_occurs>' + max_occurs + '</max_occurs>')  # TODO: correct?

        children = ''
        for ch in stmt.substmts:
            if ch.keyword in ('container', 'list', 'leaf', 'leaf-list'):
                children += ch.arg + ' '
        res.append('<children>' + children[:-1] + '</children>')

        res.append('<flags>0</flags>')
        res.append('<desc></desc>')
        res.append('</node>')
        return res


class SchemaGenerator(object):
    """Used to generate an external XML schema from a yang module"""

    def __init__(self, stmts, tagpath, ctx):
        self.stmts = stmts
        self.tagpath = tagpath
        self.ctx = ctx

    def schema_nodes(self):
        """Generate XML schema as a list of "node" elements"""
        res = []
        isconfigdata = (lambda stmt: stmt.keyword in
            ('module', 'submodule', 'container', 'list', 'leaf', 'leaf-list'))
        for stmt in filter(isconfigdata, self.stmts):
            node = SchemaNode(stmt, self.tagpath + stmt.arg + '/')
            substmt_generator = SchemaGenerator(stmt.substmts,
                self.tagpath + stmt.arg + '/', self.ctx)
            if self.ctx.opts.verbose:
                print 'Generating schema node "' + self.tagpath + '"...'
            res.extend(node.as_list())
            res.extend(substmt_generator.schema_nodes())
        return res


class YangType(object):
    """Provides an interface to maintain a list of defined yang types"""

    def __init__(self):
        self.defined_types = ['empty', 'int8', 'int16', 'int32', 'int64',
            'uint8', 'uint16', 'uint32', 'uint64', 'binary', 'bits', 'boolean',
            'decimal64', 'enumeration', 'identityref', 'instance-identifier',
            'leafref', 'string', 'union']  # Use set instead!
        """List of types represented by a jnc or generated class"""

    def defined(self, yang_type):
        """Returns true if yang_type is defined, else false"""
        return (yang_type in self.defined_types)

    def add(self, yang_type):
        """Gives yang_type "defined" status in this instance of YangType"""
        self.defined_types.append(yang_type)


class ClassGenerator(object):
    """Used to generate java classes from a yang module"""

    def __init__(self, stmt, package=None, src=None, ctx=None, path='', ns='',
            prefix_name='', top_level=False, yang_types=None, parent=None):
        """Constructor.

        stmt        -- A statement (sub)tree, parsed from a YANG model
        package     -- Name of Java package, also used as path to where files
                       should be written
        src         -- Filename of parsed yang module, or the module name and
                       revision if filename is unknown
        ctx         -- Context used to fetch option parameters
        path        -- The XPath of stmt in the original module
        ns          -- The XML namespace of the module
        prefix_name -- The module prefix
        top_level   -- Whether or not this is a top-level statement
        yang_types  -- An instance of the YangType class
        parent      -- ClassGenerator to copy arguments that were not supplied
                       from (if applicable)

        """
        self.stmt = stmt
        self.package = None if not package else package.replace(os.sep, '.')
        self.src = src
        self.ctx = ctx
        self.path = path
        self.ns = ns
        self.prefix_name = prefix_name
        self.top_level = top_level
        self.yang_types = yang_types
        if yang_types is None:
            self.yang_types = YangType()
        if parent is not None:
            for attr in ('package', 'src', 'ctx', 'path', 'ns', 'prefix_name', 'yang_types'):
                if getattr(self, attr) is None:
                    setattr(self, attr, getattr(parent, attr))

    def generate(self):
        """Generates class(es) for self.stmt"""
        if self.stmt.keyword in ('module', 'submodule'):
            self.generate_classes()
        else:
            self.generate_class()

    def generate_classes(self):
        """Generates a Java class hierarchy from a module or submodule
        statement, allowing for netconf communication using the jnc library.

        """
        if self.stmt.keyword == 'module':
            ns_arg = self.stmt.search_one('namespace').arg
            prefix = self.stmt.search_one('prefix')
        elif self.stmt.keyword == 'submodule':
            parent_module = self.stmt.search_one('belongs-to')
            prefix = parent_module.search_one('prefix')
            ns_arg = '<unknown/prefix: ' + prefix.arg + '>'
        name = normalize(prefix.arg)
        self.filename = name + '.java'

        for stmt in self.stmt.substmts:
            if stmt.keyword in ('container', 'list', 'augment', 'typedef'):
                child_generator = ClassGenerator(stmt, ns=ns_arg,
                    prefix_name=name, top_level=True, parent=self)
                child_generator.generate()

        if self.ctx.opts.verbose:
            print 'Generating Java class "' + self.filename + '"...'
        self.java_class = JavaClass(filename=self.filename,
                package=self.package, description=('The root class for namespace ' +
                    ns_arg + ' (accessible from \n * ' + name +
                    '.NAMESPACE) with prefix "' + prefix.arg + '" (' + name +
                    '.PREFIX).'),
                source=self.src)

        root_fields = [JavaValue(), JavaValue()]
        root_fields[0].set_name('NAMESPACE')
        root_fields[1].set_name('PREFIX')
        root_fields[0].value = '"' + ns_arg + '"'
        root_fields[1].value = '"' + prefix.arg + '"'
        for root_field in root_fields:
            for modifier in ('public', 'static', 'final', 'String'):
                root_field.add_modifier(modifier)
            self.java_class.add_field(root_field)

        enabler = JavaMethod(return_type='void', name='enable')
        enabler.exceptions = ['JNCException']  # XXX: Don't use add method
        enabler.add_dependency('com.tailf.jnc.JNCException')
        enabler.modifiers = ['public', 'static']
        enabler.add_javadoc('Enable the elements in this namespace to be aware')
        enabler.add_javadoc('of the data model and use the generated classes.')
        enabler.add_line('"'.join(['YangElement.setPackage(NAMESPACE, ',
                                   self.java_class.package, ');']))
        enabler.add_dependency('com.tailf.jnc.YangElement')
        enabler.add_line(name + '.registerSchema();')  # XXX: Don't import name
        self.java_class.add_enabler(enabler)

        reg = JavaMethod(return_type='void', name='registerSchema')
        reg.exceptions = ['JNCException']  # XXX: Don't use add method
        reg.add_dependency('com.tailf.jnc.JNCException')
        reg.modifiers = ['public', 'static']
        reg.add_javadoc('Register the schema for this namespace in the global')
        reg.add_javadoc('schema table (CsTree) making it possible to lookup')
        reg.add_javadoc('CsNode entries for all tagpaths')
        reg.add_line('SchemaParser parser = new SchemaParser();')
        reg.add_dependency('com.tailf.jnc.SchemaParser')
        reg.add_line('HashMap<Tagpath, SchemaNode> h = SchemaTree.create(NAMESPACE);')
        reg.add_dependency('java.util.HashMap')
        reg.add_dependency('com.tailf.jnc.Tagpath')
        reg.add_dependency('com.tailf.jnc.SchemaNode')
        reg.add_dependency('com.tailf.jnc.SchemaTree')
        schema = os.sep.join([self.ctx.opts.directory.replace('.', os.sep), name])
        reg.add_line('parser.readFile("' + schema + '.schema", h);')
        self.java_class.add_schema_registrator(reg)

        self.write_to_file()

    def generate_class(self):
        """Generates a Java class hierarchy providing an interface to a YANG
        module. Uses mutual recursion with generate_child.

        """
        stmt = self.stmt
        self.filename = normalize(stmt.arg) + '.java'
        fields = []

        self.java_class = JavaClass(filename=self.filename,
                package=self.package,
                description='This class represents a "' + self.path +
                    stmt.arg + '" element\n * from the namespace ' + self.ns,
                source=self.src,
                superclass='YangElement')

        i_children_exists = (hasattr(stmt, 'i_children')
            and stmt.i_children is not None
            and stmt.i_children != [])

        # If augment, add target module to augmented_modules dict
        if stmt.keyword == 'augment':
            if not hasattr(stmt, "i_target_node"):  # TODO: EAFP
                warn_msg = 'Target missing from augment statement'
                print_warning(warn_msg, warn_msg, self.ctx)
            else:
                target = stmt.i_target_node
                augmented_modules[target.top.arg] = target.top
            return  # XXX: Do not generate a class for the augment statement

        # TODO: preserve correct order in generated class
        expanded_i_children = []
        if i_children_exists:
            for ch in stmt.i_children:
                fields.extend(self.generate_child(ch))

            def expand(children):
                res = []
                res.extend(children)
                for ch in children:
                    sub_children = []
                    try:
                        sub_children.extend(ch.i_children)
                    except AttributeError:
                        pass
                    sub_children.extend(ch.substmts)
                    res.extend(expand(sub_children))
                return res
            expanded_i_children = expand(stmt.i_children)

        # TODO: Avoid quadratic time duplication check (maybe use a set)
        for sub in stmt.substmts:
            if sub not in expanded_i_children:
                fields.extend(self.generate_child(sub))

        if self.ctx.opts.verbose:
            print 'Generating Java class "' + self.filename + '"...'

        gen = MethodGenerator(stmt, self.ctx)

        for constructor in gen.constructors():
            self.java_class.add_constructor(constructor)

        for cloner in gen.cloners():
            self.java_class.add_cloner(cloner)

        try:
            for i, method in enumerate(gen.setters()):
                self.java_class.append_access_method(str(i), method)
        except TypeError:
            pass  # setters not implemented

        checker = gen.checker()
        if checker is not None:
            self.java_class.append_access_method('check', checker)

        support_method = gen.support_method(fields)
        if support_method is not None:
            self.java_class.add_support_method(support_method)

        if stmt.keyword != 'typedef':  # TODO: Only add key name getter when relevant
            self.java_class.add_name_getter(gen.key_names())
            self.java_class.add_name_getter(gen.children_names())
        elif stmt.keyword == 'typedef':
            type_stmt = stmt.search_one('type')
            super_type = get_types(type_stmt, self.ctx)[0]
            self.java_class.superclass = super_type

            # If supertype is derived, make sure a class for it is generated
            if type_stmt.i_typedef:
                if not self.yang_types.defined(type_stmt.i_typedef.arg):
                    typedef_generator = ClassGenerator(type_stmt.i_typedef,
                        package='src.' + get_package(type_stmt.i_typedef, self.ctx),
                        path=self.package.replace('.', os.sep) + os.sep, ns=None,
                        prefix_name=None, parent=self)
                    typedef_generator.generate()
                    self.yang_types.add(type_stmt.i_typedef.arg)
            self.yang_types.add(stmt.arg)

        self.write_to_file()

    def generate_child(self, sub):
        """Appends access methods to class for children in the YANG module.
        Returns a list which contains the name of sub if it is a container,
        otherwise an empty list is returned. Uses mutual recursion with
        generate_class.

        For this function to work, self.java_class must be defined.

        sub -- A data model subtree statement. Its parent most not be None.

        """
        fields = []
        add = self.java_class.append_access_method  # XXX: add is a function
        if sub.keyword in ('list', 'container', 'typedef'):
            child_generator = ClassGenerator(stmt=sub,
                package=self.package + '.' + camelize(sub.parent.arg),
                path=self.path + camelize(sub.parent.arg) + os.sep, ns=None,
                prefix_name=None, parent=self)
            child_generator.generate()
            if sub.keyword == 'list':
                child_gen = MethodGenerator(sub, self.ctx)
                for access_method in child_gen.parent_access_methods():
                    add(sub.arg, access_method)
            elif sub.keyword == 'container':
                fields.append(sub.arg)
                child_gen = MethodGenerator(sub, self.ctx)
                self.java_class.add_field(child_gen.child_field())
                for access_method in child_gen.parent_access_methods():
                    add(sub.arg, access_method)
        elif sub.keyword in ('leaf', 'leaf-list'):
            child_gen = MethodGenerator(sub, self.ctx)
            add(sub.arg, child_gen.access_methods_comment())
            if sub.keyword == 'leaf':
                key = sub.parent.search_one('key')
                optional = key is None or sub.arg not in key.arg.split(' ')
                # TODO: ensure that the leaf is truly optional
                add(sub.arg, child_gen.getters())
                for setter in child_gen.setters():
                    add(sub.arg, setter)
                if optional:
                    add(sub.arg, child_gen.unsetter())
                add(sub.arg, child_gen.adders())
            else:  # sub.keyword == 'leaf-list':
                add(sub.arg, child_gen.child_iterator())
                for setter in child_gen.setters():
                    add(sub.arg, setter)
                for deleter in child_gen.deleters():
                    add(sub.arg, deleter)
                add(sub.arg, child_gen.adders())
                optional = True
            if optional:
                child_gen = MethodGenerator(sub, self.ctx)
                for mark_method in child_gen.markers():
                    add(sub.arg, mark_method)
        return fields

    def write_to_file(self):
        write_file(self.package,
                   self.filename,
                   self.java_class.as_list(),
                   self.ctx)


class PackageInfoGenerator(object):
    """Used to generate package-info.java files, with meaningful content"""

    def __init__(self, directory, stmt, ctx):
        """Initializes a generator with package directory path, top level
        statement and context for options.

        stmt      -- Statement representing any YANG module subtree
        directory -- The package directory as a string
        ctx       -- Context for options

        """
        self.d = directory
        self.pkg = directory if directory[:3] != 'src' else directory[4:]
        self.pkg = self.pkg.replace(os.sep, '.')
        self.stmt = stmt
        self.ctx = ctx

    def generate_package_info(self):
        """Main generator method: generates package-info files for self.stmt
        and all of its substatements.

        """
        is_java_file = lambda s: s.endswith('.java')
        is_not_java_file = lambda s: not is_java_file(s)
        directory_listing = os.listdir(self.d)
        java_files = filter(is_java_file, directory_listing)
        dirs = filter(is_not_java_file, directory_listing)
        class_hierarchy = self.generate_javadoc(self.stmt.substmts, java_files)
        write_file(self.d, 'package-info.java',
                   self.gen_package_info(class_hierarchy), self.ctx)
        for directory in dirs:
            for sub in self.stmt.substmts:
                if normalize(sub.arg) == normalize(directory):
                    old_d = self.d
                    self.d += os.sep + directory
                    old_pkg = self.pkg
                    self.pkg += '.' + directory
                    old_stmt = self.stmt
                    self.stmt = sub

                    self.generate_package_info()

                    self.d = old_d
                    self.pkg = old_pkg
                    self.stmt = old_stmt

    @staticmethod
    def generate_javadoc(stmts, java_files):
        """Generates a list of class filenames and lists of their subclasses'
        filenames, positioned immediately after each filename if any.

        stmts      -- list of statements representing a YANG module tree node
        java_files -- list of Java class filenames that has been generated

        """
        hierarchy = []
        for stmt in stmts:
            filename = normalize(stmt.arg) + '.Java'
            if filename in java_files:
                java_files.remove(filename)
                hierarchy.append(filename)
                children = PackageInfoGenerator.generate_javadoc(stmt.substmts, java_files)
                if children:
                    hierarchy.append(children)
        return hierarchy

    @staticmethod
    def parse_hierarchy(hierarchy):
        """Returns html for a list of javadoc pages corresponding to the .java
        files in the hierarchy list.

        hierarchy -- a tree of .java files represented as a list, for example:
            ['Foo.java', ['Bar.java', ['Baz.java'], 'Qu.java']] would represent the
            hierarchy structure:
            Foo
            |   Bar
            |   |   Baz
            |   Qu

            That is, Baz is a child of Bar in the data model tree, and Bar and Qu
            are children of the top level element Foo.

        """
        res = ''
        for entry in hierarchy:
            if not isinstance(entry, list):
                body = '    <a href="' + entry[:-5] + '.html">' + entry[:-5] + '</a>'
                res += PackageInfoGenerator.html_list(body, 1, tag='li')
            else:
                body = PackageInfoGenerator.parse_hierarchy(entry)
                res += PackageInfoGenerator.html_list(body, 1)
            if body[-1:] != '\n':
                res += '\n'
        return res

    @staticmethod
    def html_list(body, indent_level, tag='ul'):
        """Returns a string representing javadoc with a <ul> html-element if ul,
        else with a <li> html-element.

        """
        body = '<' + tag + '>\n' + body
        if body[-1:] != '\n':
            body += '\n'
        body += '</' + tag + '>'
        return ' ' * 4 + body.replace('\n', '\n' + ' ' * 4)

    def gen_package_info(self, class_hierarchy):
        """Writes a package-info.java file to the package directory with a high
        level description of the package functionality and requirements.

        class_hierarchy -- A tree represented as a list as in parse_hierarchy

        """
        module = self.stmt.arg if not self.stmt.top else self.stmt.top.arg
        return ''.join([package_info.format(' ' + module, ''), self.pkg, ';'])


class JavaClass(object):
    """Encapsulates package name, imports, class declaration, constructors,
    fields, access methods, etc. for a Java Class. Also includes javadoc
    documentation where applicable.

    Implementation: Unless the 'body' attribute is used, different kind of
    methods and fields are stored in separate dictionaries so that the order of
    them in the generated class does not depend on the order in which they were
    added.

    """

    def __init__(self, filename=None, package=None, imports=None,
                 description=None, body=None, version='1.0',
                 superclass=None, interfaces=None, source='<unknown>.yang'):
        """Constructor.

        filename    -- Should preferably not contain a complete path since it is
                       displayed in a Java comment in the beginning of the code.
        package     -- Should be just the name of the package in which the class
                       will be included.
        imports     -- Should be a list of names of imported libraries.
        description -- Defines the class semantics.
        body        -- Should contain the actual code of the class if it is not
                       supplied through the add-methods
        version     -- Version number, defaults to '1.0'.
        superclass  -- Parent class of this Java class, or None
        interaces   -- List of interfaces implemented by this Java class
        source      -- A string somehow representing the origin of the class

        """
        if imports is None:
            imports = []
        self.filename = filename
        self.package = package if package[:3] != 'src' else package[4:]
        self.imports = OrderedSet()
        for i in range(len(imports)):
            self.imports.add(imports[i])
        self.description = description
        self.body = body
        self.version = version
        self.superclass = superclass
        self.interfaces = interfaces
        if interfaces is None:
            self.interfaces = []
        self.source = source
        self.fields = OrderedSet()
        self.constructors = OrderedSet()
        self.cloners = OrderedSet()
        self.enablers = OrderedSet()
        self.schema_registrators = OrderedSet()
        self.name_getters = OrderedSet()
        self.access_methods = collections.OrderedDict()
        self.support_methods = OrderedSet()
        self.attrs = [self.fields, self.constructors, self.cloners,
                      self.enablers, self.schema_registrators,
                      self.name_getters, self.access_methods,
                      self.support_methods]

    def add_field(self, field):
        """Adds a field represented as a string"""
        self.fields.add(field)

    def add_constructor(self, constructor):
        """Adds a constructor represented as a string"""
        self.constructors.add(constructor)

    def add_cloner(self, cloner):
        """Adds a clone method represented as a string"""
        if not isinstance(cloner, str):
            for import_ in cloner.imports:
                self.imports.add(import_)
        self.cloners.add(cloner)

    def add_enabler(self, enabler):
        """Adds an 'enable'-method as a string"""
        self.imports.add('com.tailf.jnc.JNCException')
        self.imports.add('com.tailf.jnc.YangElement')
        self.enablers.add(enabler)

    def add_schema_registrator(self, schema_registrator):
        """Adds a register schema method"""
        self.imports.add('com.tailf.jnc.JNCException')
        self.imports.add('com.tailf.jnc.SchemaParser')
        self.imports.add('com.tailf.jnc.Tagpath')
        self.imports.add('com.tailf.jnc.SchemaNode')
        self.imports.add('com.tailf.jnc.SchemaTree')
        self.imports.add('java.util.HashMap')
        self.schema_registrators.add(schema_registrator)

    def add_name_getter(self, name_getter):
        """Adds a keyNames or childrenNames method represented as a string"""
        self.name_getters.add(name_getter)

    def append_access_method(self, key, access_method):
        """Adds an access method represented as a string"""
        if self.access_methods.get(key) is None:
            self.access_methods[key] = []
        self.access_methods[key].append(access_method)

    def add_support_method(self, support_method):
        """Adds a support method represented as a string"""
        self.support_methods.add(support_method)

    def get_body(self):
        """Returns self.body. If it is None, fields and methods are added to it
        before it is returned."""
        if self.body is None:
            self.body = []
            if self.superclass is not None or 'Serializable' in self.interfaces:
                self.body.extend(JavaValue(
                    modifiers=['private', 'static', 'final', 'long'],
                    name='serialVersionUID', value='1L').as_list())
                self.body.append('')
            for method in flatten(self.attrs):
                if hasattr(method, 'as_list'):
                    self.body.extend(method.as_list())
                else:
                    self.body.append(method)
                self.body.append('')
            self.body.append('}')
        return self.body

    def get_superclass_and_interfaces(self):
        """Returns a string with extends and implements"""
        res = []
        if self.superclass:
            res.append(' extends ')
            res.append(self.superclass)
        if self.interfaces:
            res.append(' implements ')
            res.append(', '.join(self.interfaces))
        return ''.join(res)

    def as_list(self):
        """Returns a string representing complete Java code for this class.

        It is vital that either self.body contains the complete code body of
        the class being generated, or that it is None and methods have been
        added using the JavaClass.add methods prior to calling this method.
        Otherwise the class will be empty.

        The class name is the filename without the file extension.

        """
        # The header is placed in the beginning of the Java file
        header = [' '.join(['/* \n * @(#)' + self.filename, '      ',
                            self.version, date.today().strftime('%d/%m/%y')])]
        header.append(' *')
        header.append(' * This file has been auto-generated by JPyang, the')
        header.append(' * Java output format plug-in of pyang.')
        header.append(' * Origin: ' + self.source)
        header.append(' */')

        # package and import statement goes here
        header.append('')
        header.append('package ' + self.package + ';')
        if self.body is None:
            for method in flatten(self.attrs):
                if hasattr(method, 'imports'):
                    self.imports |= method.imports
                if hasattr(method, 'exceptions'):
                    self.imports |= map(lambda s: 'com.tailf.jnc.' + s,
                                        method.exceptions)
        if self.imports:
            prev = ''
            for import_ in self.imports.as_sorted_list():
                basepkg = import_[:import_.find('.')]
                if basepkg != prev:
                    header.append('')
                header.append('import ' + import_ + ';')
                prev = basepkg

        # Class doc-comment and declaration, with modifiers
        header.append('')
        header.append('/**')
        header.append(' * ' + self.description)
        header.append(' *')
        header.append(' '.join([' * @version',
                                self.version,
                                date.today().isoformat()]))
        header.append(' * @author Auto Generated')
        header.append(' */')
        header.append(''.join(['public class ',
                               self.filename.split('.')[0],
                               self.get_superclass_and_interfaces(),
                               ' {']))
        header.append('')
        return header + self.get_body()


class JavaValue(object):
    """A Java value, typically representing a field or a method in a Java
    class and optionally a javadoc comment.

    A JavaValue can have its attributes set using the optional keyword
    arguments of the constructor, or by using the add and set methods.

    Each instance of this class has an 'as_list' method which is used to get a
    list of strings representing lines of code that can be written to a Java
    file once all the attributes have been set set.

    """

    def __init__(self, exact=None, javadocs=None, modifiers=None, name=None,
                 value=None, imports=None, indent=4):
        """Initializes the attributes of a new Java value.

        Keyword arguments:
        exact (String list)     -- If supplied, the 'as_list' method will
                                   return this list until something has been
                                   added to this Java value.
        javadocs (String list)  -- A list of the lines in the javadoc
                                   declaration for this Java Value.
        modifiers (String list) -- A list of Java modifiers such as 'public'
                                   and 'static'. Also used to specify the type
                                   of fields.
        name (String)           -- The identifier used for this value.
        value (String)          -- Not used by methods, this is placed after
                                   the assignment operator in a field
                                   declaration.
        imports (String list)   -- A (possibly redundant) set of classes to
                                   import in the class declaring this value.
                                   This list is typically filtered by other
                                   classes so nothing gets imported unless it
                                   is required to for the Java class to
                                   compile.
        indent (Integer)        -- The indentation in the 'as_list'
                                   representation. Defaults to 4 spaces.

        """
        self.value = value
        self.indent = ' ' * indent
        self.default_modifiers = True

        self.javadocs = OrderedSet()
        if javadocs is not None:
            for javadoc in javadocs:
                self.add_javadoc(javadoc)

        self.modifiers = []
        if modifiers is not None:
            for modifier in modifiers:
                self.add_modifier(modifier)

        self.name = None
        if name is not None:
            self.set_name(name)

        self.imports = set([])
        if imports is not None:
            for import_ in imports:
                self.imports.add(import_)

        self.exact = exact
        self.default_modifiers = True

    def __eq__(self, other):
        """Returns True iff self and other represents an identical value"""
        for attr, value in vars(self).items():
            try:
                if getattr(other, attr) != value:
                    return False
            except AttributeError:
                return False
        return True

    def __ne__(self, other):
        """Returns True iff self and other represents different values"""
        return not self.__eq__(other)

    def _set_instance_data(self, attr, value):
        """Adds or assigns value to the attr attribute of this Java value.

        attr (String) -- The attribute to add or assign value to. If this Java
                         value does not have an attribute with this name, a
                         warning is printed with the msg "Unknown attribute"
                         followed by the attribute name. The value is added,
                         appended or assigned, depending on if the attribute is
                         a MutableSet, a list or something else, respectively.
        value         -- Typically a String, but can be anything, really.

        The 'exact' cache is invalidated is the attribute exists.

        """
        try:
            data = getattr(self, attr)
            if isinstance(data, list):
                data.append(value)
            elif isinstance(data, collections.MutableSet):
                data.add(value)
            else:
                setattr(self, attr, value)
        except AttributeError:
            print_warning(msg='Unknown attribute: ' + attr, key=attr)
        else:
            self.exact = None  # Invalidate cache

    def set_name(self, name):
        """Sets the identifier of this value"""
        self._set_instance_data('name', name)

    def set_indent(self, indent=4):
        """Sets indentation used in the as_list methods"""
        self._set_instance_data('indent', ' ' * indent)

    def add_modifier(self, modifier):
        """Adds modifier to end of list of modifiers. Overwrites modifiers set
        in constructor the first time it is invoked, to enable subclasses to
        have default modifiers.

        """
        if self.default_modifiers:
            self.modifiers = []
            self.default_modifiers = False
        self._set_instance_data('modifiers', modifier)

    def add_javadoc(self, line):
        """Adds line to javadoc comment, leading ' ', '*' and '/' removed"""
        self._set_instance_data('javadocs', line.lstrip(' */'))

    def add_dependency(self, import_):
        """Adds import_ to list of imports needed for value to compile."""
        _, sep, class_name = import_.rpartition('.')
        if sep:
            if class_name not in java_built_in:
                self.imports.add(import_)
                return class_name
        elif not any(x in java_built_in for x in (import_, import_[:-2])):
            self.imports.add(import_)
        return import_

    def javadoc_as_string(self):
        """Returns a list representing javadoc lines for this value"""
        lines = []
        if self.javadocs:
            lines.append(self.indent + '/**')
            lines.extend([self.indent + ' * ' + line for line in self.javadocs])
            lines.append(self.indent + ' */')
        return lines

    def as_list(self):
        """String list of code lines that this Java value consists of"""
        if self.exact is None:
            assert self.name is not None
            assert self.indent is not None
            self.exact = self.javadoc_as_string()
            declaration = self.modifiers + [self.name]
            if self.value is not None:
                declaration.append('=')
                declaration.append(self.value)
            self.exact.append(''.join([self.indent, ' '.join(declaration), ';']))
        return self.exact


class JavaMethod(JavaValue):
    """A Java method. Default behaviour is public void."""

    def __init__(self, exact=None, javadocs=None, modifiers=None,
                 return_type=None, name=None, params=None, exceptions=None,
                 body=None, indent=4):
        """Initializes the attributes of a new Java method.

        Keyword arguments:
        exact (String list)     -- If supplied, the 'as_list' method will
                                   return this list until something has been
                                   added to this Java value.
        javadocs (String list)  -- A list of the lines in the javadoc
                                   declaration for this Java Value.
        modifiers (String list) -- A list of Java modifiers such as 'public'
                                   and 'static'. Also used to specify the type
                                   of fields.
        return_type (String)    -- The return type of the method. To avoid
                                   adding the type as a required import,
                                   assign to the return_type attribute directly
                                   instead of using this argument.
        name (String)           -- The identifier used for this value.
        params (str tuple list) -- A list of 2-tuples representing the type and
                                   name of the parameters of this method. To
                                   avoid adding the type as a required import,
                                   assign to the parameters attribute directly
                                   instead of using this argument.
        exceptions (str list)   -- A list of exceptions thrown by the method.
        value (String)          -- Not used by methods, this is placed after
                                   the assignment operator in a field
                                   declaration.
        imports (String list)   -- A (possibly redundant) set of classes to
                                   import in the class declaring this value.
                                   This list is typically filtered by other
                                   classes so nothing gets imported unless it
                                   is required to for the Java class to
                                   compile.
        indent (Integer)        -- The indentation in the 'as_list'
                                   representation. Defaults to 4 spaces.

        """
        super(JavaMethod, self).__init__(exact=exact, javadocs=javadocs,
                                         modifiers=modifiers, name=name,
                                         value=None, indent=indent)
        if self.modifiers == []:
            self.add_modifier('public')

        self.return_type = 'void'
        if return_type is not None:
            self.set_return_type(return_type)

        self.parameters = OrderedSet()
        if params is not None:
            for param_type, param_name in params:
                self.add_parameter(param_type, param_name)

        self.exceptions = OrderedSet()
        if exceptions is not None:
            for exc in exceptions:
                self.add_exception(exc)

        self.body = []
        if body is not None:
            for line in body:
                self.add_line(line)

        self.exact = exact
        self.default_modifiers = True

    def set_return_type(self, return_type):
        """Sets the type of the return value of this method"""
        retval = None if not return_type else self.add_dependency(return_type)
        self._set_instance_data('return_type', retval)

    def add_parameter(self, param_type, param_name):
        """Adds a parameter to this method. The argument type is added to list
        of dependencies.

        param_type -- String representation of the argument type
        param_name -- String representation of the argument name
        """
        self._set_instance_data('parameters',
                                ' '.join([self.add_dependency(param_type),
                                          param_name]))

    def add_exception(self, exception):
        """Adds exception to method"""
        self._set_instance_data('exceptions',
                                self.add_dependency(exception))

    def add_line(self, line):
        """Adds line to method body"""
        self._set_instance_data('body', self.indent + ' ' * 4 + line)

    def as_list(self):
        """String list of code lines that this Java method consists of.
        Overrides JavaValue.as_list().

        """
        MAX_COLS = 80
        if self.exact is None:
            assert self.name is not None
            assert self.indent is not None
            self.exact = self.javadoc_as_string()
            header = self.modifiers[:]
            if self.return_type is not None:
                header.append(self.return_type)
            header.append(self.name)
            signature = [self.indent]
            signature.append(' '.join(header))
            signature.append('(')
            signature.append(', '.join(self.parameters))
            signature.append(')')
            if self.exceptions:
                signature.append(' throws ')
                signature.append(', '.join(self.exceptions))
                if sum(len(s) for s in signature) >= MAX_COLS:
                    signature.insert(-2, '\n' + (self.indent * 3)[:-1])
            signature.append(' {')
            self.exact.append(''.join(signature))
            self.exact.extend(self.body)
            self.exact.append(self.indent + '}')
        return self.exact


class MethodGenerator(object):
    """A generator for JavaMethod objects"""

    def __init__(self, stmt, ctx):
        """Sets the attributes of the method generator, depending on stmt"""
        self.stmt = stmt
        self.n = normalize(stmt.arg)
        self.n2 = camelize(stmt.arg)
        self.children = map(lambda s: normalize(s.arg), stmt.substmts)
        self.pkg = get_package(stmt, ctx)
        self.basepkg = self.pkg.partition('.')[0]
        self.rootpkg = ctx.rootpkg.split(os.sep)
        if self.rootpkg[:1] == ['src']:
            self.rootpkg = self.rootpkg[1:]  # src not part of package

        self.ctx = ctx
        self.root = None
        prefix = None
        if stmt.top is not None:
            prefix = self.stmt.top.search_one('prefix')
        if prefix is not None:
            self.root = normalize(prefix.arg)

        self.is_container = stmt.keyword == 'container'
        self.is_list = stmt.keyword == 'list'
        self.is_typedef = stmt.keyword == 'typedef'
        self.is_leaf = stmt.keyword == 'leaf'
        self.is_leaflist = stmt.keyword == 'leaf-list'
        self.is_top_level = self.stmt.parent == self.stmt.top
        assert (self.is_container or self.is_list or self.is_typedef
            or self.is_leaf or self.is_leaflist)
        self.gen = self
        if type(self) is MethodGenerator:
            if self.is_typedef:
                self.gen = TypedefMethodGenerator(stmt, ctx)
            if self.is_container:
                self.gen = ContainerMethodGenerator(stmt, ctx)
            if self.is_list:
                self.gen = ListMethodGenerator(stmt, ctx)
            if self.is_leaf or self.is_leaflist:
                self.gen = LeafMethodGenerator(stmt, ctx)

    def canonical_import(self, import_, child=False):
        """Returns a string representing a class that can be imported in Java.

        Does not handle Generics or Array types.

        """
        if import_.startswith(('java.math', 'java.util',
                               'com.tailf.jnc', self.basepkg)):
            return import_
        elif import_ in ('BigInteger', 'BigDecimal'):
            return '.'.join(['java.math', import_])
        elif import_ in java_util:
            return '.'.join(['java.util', import_])
        elif import_ == self.root:
            return '.'.join(self.rootpkg + [import_])
        elif import_ in self.children:
            type_child = self.stmt.search_one('type')
            if type_child is not None and normalize(type_child.arg) == import_:
                try:
                    typedef_pkg = get_package(type_child.i_typedef, self.ctx)
                except AttributeError:
                    typedef_pkg = get_package(type_child, self.ctx)
                return '.'.join([typedef_pkg, import_])
            return '.'.join([self.pkg, camelize(self.stmt.arg), import_])
        elif child and import_ == normalize(self.stmt.arg):
            return '.'.join([self.pkg, import_])
        else:
            return '.'.join(['com.tailf.jnc', import_])

    def fix_imports(self, method, child=False):
        res = set([])

        for dependency in method.imports:
            if dependency.startswith(('java.math', 'java.util',
                                      'com.tailf.jnc', self.basepkg)):
                res.add(dependency)
                continue
            elif dependency.endswith('>'):
                for token in filter(None, re.findall(r'\w+', dependency)):
                    res.add(self.canonical_import(token, child))
            elif dependency.endswith(']'):
                assert dependency[:-2] and dependency[-2:] == '[]'
                res.add(self.canonical_import(dependency[:-2], child))
            else:
                res.add(self.canonical_import(dependency, child))

        method.imports = res
        return method

    def _root_namespace(self, stmt_arg):
        """Returns '([Root].NAMESPACE, "[stmt.arg]");'"""
        return ['(', self.root, '.NAMESPACE, "', stmt_arg, '");']

    def _constructor_template(self):
        """Returns a constructor invoking parent constructor, without
        parameters and javadoc."""
        constructor = JavaMethod(modifiers=['public'], name=self.n)
        constructor.set_return_type(None)
        if self.is_container or self.is_list:
            call = ['super']
            call.extend(self._root_namespace(self.stmt.arg))
            constructor.add_dependency(self.root)
            constructor.add_line(''.join(call))
            if self.is_top_level:
                # Top level statement
                constructor.add_line('setDefaultPrefix();')
                setPrefix = ['setPrefix(', self.root, '.PREFIX);']
                constructor.add_line(''.join(setPrefix))
        elif self.is_typedef:
            constructor.add_line('super(value);')
        else:
            return None
        return self.fix_imports(constructor)

    def access_methods_comment(self):
        """Returns a JavaValue representing a code structuring Java comment"""
        res = ['    /* Access methods for']
        if hasattr(self.gen, 'is_optional') and self.gen.is_optional:
            res.append('optional')
        res.extend([self.stmt.keyword, 'child: "' + self.stmt.arg + '". */'])
        return JavaValue(exact=[' '.join(res)])

    def empty_constructor(self):
        """Returns parameter-free constructor as a JavaMethod object"""
        assert not self.is_typedef, "Typedefs don't have empty constructors"
        assert not self.is_leaf and not self.is_leaflist
        constructor = self._constructor_template()
        javadoc = ['Constructor for an empty ']
        javadoc.append(self.n)
        javadoc.append(' object.')
        constructor.add_javadoc(''.join(javadoc))
        return self.fix_imports(constructor)

    def constructors(self):
        """Returns a list of JavaMethods representing constructors to include
        in generated class of self.stmt

        """
        assert self.gen is not self, 'Avoid infinite recursion'
        if self.is_leaf or self.is_leaflist:
            return None
        else:
            return self.gen.constructors()

    def cloners(self):
        if self.is_typedef or self.is_leaf or self.is_leaflist:
            return []  # Typedefs, leafs and leaflists don't have clone methods
        cloners = [JavaMethod(), JavaMethod()]
        a = ('an exact', 'a shallow')
        b = ('', ' Children are not included.')
        c = ('', 'Shallow')
        for i, cloner in enumerate(cloners):
            cloner.add_javadoc('Clones this object, returning %s copy.' % a[i])
            cloner.add_javadoc('@return A clone of the object.%s' % b[i])
            cloner.set_return_type('YangElement')
            cloner.set_name('clone%s' % c[i])
            cloner.add_line('return clone%sContent(new %s());' % (c[i], self.n))
            cloner = self.fix_imports(cloner)
        return cloners

    def key_names(self):
        """Returns a method that can be used to get the keys of a statement.

        The keys are returned by the generated method as a String array
        with the identifiers of the keys in the statement of this generator,
        which should be a list or a container, or otherwise None is returned.
        If the statement does not have any keys, the generated method returns
        null.

        """
        if not (self.is_list or self.is_container):
            return None
        method = JavaMethod(modifiers=['public'], name='keyNames')
        method.set_return_type('String[]')
        method.add_javadoc('@return An array with the identifiers of any key children')
        if self.is_container or not self.gen.is_config:
            method.add_line('return null;')
        else:
            method.add_line('return new String[] {')
            for key_stmt in self.gen.key_stmts:
                method.add_line('"'.join([' ' * 4, key_stmt.arg, ',']))
            method.add_line('};')
        return self.fix_imports(method)

    def children_names(self):
        """Returns a method that can be used to get the identifiers of the
        children of the statement of this generator, excluding any keys.

        """
        if not (self.is_list or self.is_container):
            return None
        method = JavaMethod(modifiers=['public'], name='childrenNames')
        method.set_return_type('String[]')
        method.add_javadoc('@return An array with the identifiers of any children, in order.')
        is_child = lambda stmt: stmt.keyword in ('leaf', 'container',
                                                 'leaf-list', 'list')
        children = filter(is_child, self.stmt.substmts)
        method.add_line('return new String[] {')
        for child in children:
            method.add_line('"'.join([' ' * 4, child.arg, ',']))
        method.add_line('};')
        return self.fix_imports(method)

    def support_method(self, fields=None):

        if self.is_typedef or self.is_leaf or self.is_leaflist:
            return None
        add_child = JavaMethod(modifiers=['public'],
                               return_type='void',
                               name='addChild',
                               params=[('Element', 'child')])
        add_child.add_javadoc('Support method for addChild.')
        add_child.add_javadoc('Adds a child to this object.')
        add_child.add_javadoc('')
        add_child.add_javadoc('@param child The child to add')
        add_child.add_line('super.addChild(child);')
        if fields is None:
            fields = []
        for i in range(len(fields) - 1, -1, -1):
            cond = ''
            if i > 0:
                cond = 'else '
            add_child.add_line(''.join([cond, 'if (child instanceof ',
                    normalize(fields[i]), ') ', camelize(fields[i]), ' = (',
                    normalize(fields[i]), ')child;']))
            add_child.add_dependency(normalize(fields[i]))
        return self.fix_imports(add_child)

    def setters(self):
        """Returns a list of JavaMethods representing setters to include
        in generated class of self.stmt

        """
        assert self.gen is not self, 'Avoid infinite recursion'
        return self.gen.setters()

    def unsetter(self):
        """Returns an 'unset<Identifier>Value' JavaMethod for self.stmt"""
        assert self.gen is not self, 'Avoid infinite recursion'
        return self.gen.unsetter() if self.is_leaf else None

    def checker(self):
        """Returns a 'check' JavaMethod for generated class for self.stmt"""
        assert self.gen is not self, 'Avoid infinite recursion'
        return self.gen.checker() if self.is_typedef else None

    def markers(self):
        """Generates methods that enqueues operations to be performed."""
        assert self.gen is not self, 'Avoid infinite recursion'
        return None if self.is_typedef else self.gen.markers()

    def child_field(self):
        """Returns a string representing java code for a field"""
        assert self.gen is not self, 'Avoid infinite recursion'
        return self.gen.child_field() if self.is_container else None

    def _parent_template(self, method_type):
        """Returns an access method for the statement of this method generator.

        method_type -- prefix of method name

        """
        method = JavaMethod()
        if self.is_container or self.is_list:
            method.set_return_type(self.n)
        method.set_name(method_type + self.n)
        method.add_exception('JNCException')
        return self.fix_imports(method, child=True)

    def adders(self):
        """Returns a list of methods that adds an instance of the class to be
        generated from the statement of this method generator to its parent
        class.

        """
        if self.is_leaf or self.is_leaflist:
            assert self.gen != self
            return self.gen.adders()
        elif not (self.is_container or self.is_list):
            return None
        number_of_adders = 2 * (1 + self.is_list)
        res = [self._parent_template('add') for _ in range(number_of_adders)]

        for i, method in enumerate(res):
            javadoc1 = ['Adds ', self.stmt.keyword, ' entry "', self.n2, '"']
            javadoc2 = []
            if i == 0:  # Add existing object
                javadoc1.append(', using an existing object.')
                javadoc2.append(' '.join(['@param', self.n2, 'The object to add.']))
                method.add_parameter(self.n, self.n2)
                iter = self.n2 + 'Iterator'
                method.add_line(iter.join(['ElementChildrenIterator ',
                                           ' = ', '();']))
                method.add_line('while (' + iter + '.hasNext()) {')
                method.add_line(''.join(['    ', self.n, ' child = (', self.n,
                                         ')', iter, '.next();']))
                method.add_line('    if (child.keyCompare(' + self.n2 + ')) {')
                method.add_line('        YangException.throwException(true, ' + self.n2 + ');')
                method.add_dependency('com.tailf.jnc.YangException')
                method.add_line('    }')
                method.add_line('}')
            elif self.is_list and i in {1, 2} and len(res) == 4:
                # Add child with String or JNC type keys
                javadoc1.append(', with specified keys.')
                if i == 2:
                    javadoc2.append('The keys are specified as strings.')
                for key_stmt in self.gen.key_stmts:
                    javadoc2.append(''.join(['@param ', key_stmt.arg,
                        'Value Key argument of child.']))
                    param_type, _ = get_types(key_stmt, self.ctx)
                    if i == 2:
                        param_type = 'String'
                    method.add_parameter(param_type, key_stmt.arg)
                new_child = [self.n, ' ', self.n2, ' = new ', self.n, '(']
                new_child.append(', '.join([s.arg for s in self.gen.key_stmts]))
                new_child.append(');')
                method.add_line(''.join(new_child))
            else:  # Create new, for subtree filter usage
                javadoc1.append('.')
                javadoc2.append('This method is used for creating subtree filters.')
                method.add_line(' '.join([self.n, self.n2, '= new', self.n + '();']))
            method.add_javadoc(''.join(javadoc1))
            for javadoc in javadoc2:
                method.add_javadoc(javadoc)
            method.add_javadoc('@return The added child.')
            if self.is_container:
                method.add_line('this.' + self.n2 + ' = ' + self.n2 + ';')
            if self.is_list and i in {1, 2} and len(res) == 4:
                method.add_line('return ' + method.name + '(' + self.n2 + ');')
            else:
                method.add_line('insertChild(' + self.n2 + ', childrenNames());')
                method.add_line('return ' + self.n2 + ';')
            self.fix_imports(method, child=True)
        return res

    def getters(self):
        """Returns a list of JavaMethods representing getters to include
        in generated class of self.stmt.parent

        """
        assert self.gen is not self, 'Avoid infinite recursion'
        return self.gen.getters() if self.is_list or self.is_leaf else None

    def deleters(self):
        """Returns a list of JavaMethods representing deleters to include
        in generated class of self.stmt.parent

        """
        assert self.gen is not self, 'Avoid infinite recursion'
        if not (self.is_list or self.is_container or self.is_leaflist):
            return None
        else:
            return self.gen.deleters()

    def child_iterator(self):
        """Returns a java iterator method"""
        if not(self.is_leaflist or self.is_list):
            return None
        res = JavaMethod(name=(camelize(self.stmt.arg) + 'Iterator'))
        res.add_javadoc(''.join(['Iterator method for the ', self.stmt.keyword,
                                 ' "', self.stmt.arg, '".']))
        res.add_javadoc(''.join(['@return An iterator for the ',
                                 self.stmt.keyword, '.']))
        return_stmt = ['return new Element']
        if self.is_leaflist:
            res.set_return_type('ElementLeafListValueIterator')
            return_stmt.append('LeafListValue')
        else:  # List
            res.set_return_type('ElementChildrenIterator')
            return_stmt.append('Children')
        return_stmt.extend(['Iterator(children, "', self.stmt.arg, '");'])
        res.add_line(''.join(return_stmt))
        return self.fix_imports(res)

    def parent_access_methods(self):
        assert self.gen is not self, 'Avoid infinite recursion'
        if self.is_container or self.is_list:
            return self.gen.parent_access_methods()
        else:
            return None


class LeafMethodGenerator(MethodGenerator):
    """Method generator for YANG leaf and leaf-list associated methods"""

    def __init__(self, stmt, ctx):
        super(LeafMethodGenerator, self).__init__(stmt, ctx)
        assert self.is_leaf or self.is_leaflist
        self.stmt_type = stmt.search_one('type')
        self.type_str = get_types(self.stmt_type, ctx)
        self.is_string = self.type_str[1] == 'String'
        self.is_typedef = (hasattr(self.stmt_type, 'i_typedef')
                           and self.stmt_type.i_typedef is not None)
        key = stmt.parent.search_one('key')
        self.is_optional = key is None or stmt.arg not in key.arg.split(' ')

    def getters(self):
        """get<Identifier>Value method generator."""
        assert self.is_leaf
        method = JavaMethod()
        method.set_return_type(self.type_str[0])
        method.set_name('get' + self.n + 'Value')
        method.add_exception('JNCException')
        method.add_javadoc('Gets the value for child ' + self.stmt.keyword +
                           ' "' + self.stmt.arg + '".')
        method.add_javadoc('@return The value of the ''' + self.stmt.keyword + '.')
        method.add_line('return (' + method.return_type + ')getValue("' +
                        self.stmt.arg + '");')
        return [self.fix_imports(method, child=True)]

    def setters(self):
        name = 'set' + self.n + 'Value'
        num_methods = 2 + (not self.is_string)
        value_type = normalize(self.stmt_type.arg)
        if not self.is_typedef:
            value_type = self.type_str[0]  # JNC type
        res = [JavaMethod(name=name) for _ in range(num_methods)]

        for i, method in enumerate(res):
            param_names = [self.n2 + 'Value']
            method.add_exception('JNCException')
            method.add_javadoc('Sets the value for child ' + self.stmt.keyword +
                               ' "' + self.stmt.arg + '",')
            if i == 0:
                param_types = [value_type]
                if not self.is_typedef:
                    method.add_javadoc('using instance of generated typedef class.')
                else:
                    method.add_javadoc('using a JNC type value.')
                method.add_javadoc(' '.join(['@param', param_names[0],
                                             'The value to set.']))
                method.add_line(''.join(['set', normalize(self.stmt.keyword),
                                         'Value(', self.root, '.NAMESPACE,']))
                method.add_dependency(self.root)
                method.add_line('    "' + self.stmt.arg + '",')
                method.add_line('    ' + param_names[0] + ',')
                method.add_line('    childrenNames());')
            else:
                line = [name, '(new ', method.add_dependency(value_type), '(',
                        param_names[0]]
                if not self.is_string and i == 1:
                    param_types = [self.type_str[1]]
                    method.add_javadoc('using Java primitive values.')
                    if self.type_str[0] == 'com.tailf.jnc.YangDecimal64':
                        param_types.append('int')
                        param_names.append('fractionDigits')
                        line.extend([', ', param_names[-1]])
                    # FIXME: Add support for bits, leafref, instance-identifier, etc.
                    # TODO: Write functions that returns appropriate types and names
                    # FIXME: Some types may be incorrectly classified as
                    # ... string, resulting in no primitive (enumeration, bits, etc)
                else:
                    param_types = ['String']
                    method.add_javadoc('using a String value.')
                line.append('));')
                method.add_line(''.join(line))
            for param_type, param_name in zip(param_types, param_names):
                method.add_parameter(param_type, param_name)
            method.add_javadoc(' '.join(['@param', param_names[0], 'The value to set.']))
            if(len(param_types) > 1):
                method.add_javadoc('@param fractionDigits Number of decimals in value')
            self.fix_imports(method, child=True)
        return res

    def unsetter(self):
        """unset<Identifier>Value method generator"""
        method = JavaMethod()
        method.add_javadoc(' '.join(['Unsets the value for child',
                                     self.stmt.keyword,
                                     '"' + self.stmt.arg + '".']))
        method.set_name('unset' + self.n + 'Value')
        method.add_exception('JNCException')
        method.add_line('delete("' + self.stmt.arg + '");')
        return self.fix_imports(method, child=True)

    def _parent_method(self, method_type):
        """Returns a list of methods that either creates a value in the
        leaf-list of the parent YangElement instance, or deletes it, depending
        on the method_type parameter.

        method_type -- either 'create' or 'delete'

        """
        assert self.is_leaflist
        res = [self._parent_template(method_type) for _ in range(2)]

        for i, method in enumerate(res):
            method.add_javadoc(''.join([method_type.capitalize(), 's ',
                                        self.stmt.keyword, ' entry "',
                                        self.n2, '".']))
            if i == 1:
                method.add_javadoc('The value is specified as a string.')
            method.add_javadoc(''.join(['@param ', self.n2, 'Value Value to ',
                                     method_type, '.']))
            param_type = 'String'
            if i == 0:
                param_type = self.type_str[0]
            method.add_parameter(param_type, self.n2 + 'Value')
            path = ['String path = "', self.n2, '[', self.n2, 'Value]";']
            method.add_line(''.join(path))
            if method_type == 'delete':
                method.add_line('delete(path);')
            else:  # get
                method.add_line('insertChild(path);')
            self.fix_imports(method, child=True)
        return res

    def deleters(self):
        """Returns a list of methods that deletes the Leaf child, corresponding
        to the statement of this method generator, from its parent YangElement
        instance.

        """
        return self._parent_method('delete')

    def adders(self):
        method = JavaMethod(name=('add' + self.n))
        method.add_exception('JNCException')
        method.add_javadoc('This method is used for creating a subtree filter.')
        method.add_javadoc(''.join(['The added "', self.stmt.arg, '" ',
                                    self.stmt.keyword,
                                    ' will not have a value.']))
        method.add_line('set' + normalize(self.stmt.keyword) + 'Value(' +
                        self.root + '.NAMESPACE,')
        method.add_line('    "' + self.stmt.arg + '",')
        method.add_line('    null,')
        method.add_line('    childrenNames());')
        return self.fix_imports(method, child=True)

    def markers(self):
        res = []
        for op in ('replace', 'merge', 'create', 'delete'):
            res.append(self.mark(op))
        return res

    def mark(self, op):
        assert op in ('replace', 'merge', 'create', 'delete')
        mark_methods = [JavaMethod()]
        if not self.is_string and self.is_leaflist:
            mark_methods.append(JavaMethod())
        for i, mark_method in enumerate(mark_methods):
            mark_method.set_name('mark' + normalize(self.stmt.arg) + normalize(op))
            mark_method.add_exception('JNCException')
            path = self.stmt.arg
            mark_method.add_javadoc(''.join(['Marks the ', self.stmt.keyword,
                                             ' "', self.stmt.arg,
                                             '" with operation "', op, '".']))
            if self.is_leaflist:
                path += '[name=\'" + ' + self.stmt.arg + 'Value + "\']'
                javadoc = '@param ' + self.stmt.arg + 'Value The value to mark'
                param_type = self.type_str[0]
                if i == 1:
                    javadoc += ', given as a String'
                    param_type = 'String'
                mark_method.add_parameter(param_type, self.stmt.arg + 'Value')
                mark_method.add_javadoc(javadoc)
            mark_method.add_line('markLeaf' + normalize(op) + '("' + path + '");')
            self.fix_imports(mark_method, child=True)
        return mark_methods


class TypedefMethodGenerator(MethodGenerator):
    """Method generator specific to typedef classes"""

    def __init__(self, stmt, ctx=None):
        super(TypedefMethodGenerator, self).__init__(stmt, ctx)
        assert self.gen is self
        assert self.is_typedef, 'This class is only valid for typedef stmts'
        self.type = stmt.search_one('type')  # FIXME use get_type!
        self.base_type = None if not self.type else get_base_type(stmt)
        self.is_string = False
        self.needs_check = True  # Set to False to avoid redundant checks
        if self.base_type is not None:
            self.is_string = self.base_type.arg in ('string', 'enumeration')
            for s in ('length', 'path', 'range', 'require_instance'):
                setattr(self, s, self.base_type.search_one(s))
            for s in ('bit', 'enum', 'pattern'):
                setattr(self, s, self.base_type.search(s))
            # self.needs_check = self.enum or self.pattern

    def constructors(self):
        """Returns a list containing a single or a pair of constructors"""
        constructors = []
        primitive = get_types(self.type, self.ctx)[1]
        javadoc = ['@param value Value to construct the ', self.n, ' from.']

        # Iterate once if string, twice otherwise
        for i in range(1 + (not self.is_string)):
            constructor = self._constructor_template()
            javadoc2 = ['Constructor for ', self.n]
            if i == 0:
                # String constructor
                javadoc2.append(' object from a string.')
                constructor.add_parameter('String', 'value')
            else:
                # i == 1, Primitive constructor
                javadoc2.extend([' object from a ', primitive, '.'])
                tmp_primitive = constructor.add_dependency(primitive)
                constructor.add_parameter(tmp_primitive, 'value')
            constructor.add_javadoc(''.join(javadoc2))
            constructor.add_javadoc(''.join(javadoc))
            if self.needs_check:
                constructor.add_line('check();')
                constructor.add_exception('YangException')
            constructors.append(self.fix_imports(constructor))
        return constructors

    def setters(self):
        """Returns a list of set_value JavaMethods"""
        setters = []
        primitive = get_types(self.type, self.ctx)[1]
        javadoc = '@param value The value to set.'

        # Iterate once if string, twice otherwise
        for i in range(1 + (not self.is_string)):
            setter = JavaMethod(name='setValue')
            javadoc2 = ['Sets the value using a ']
            if i == 0:
                # String setter
                javadoc2.append('string value.')
                setter.add_parameter('String', 'value')
            else:
                # i == 1, Primitive setter
                javadoc2.extend(['value of type ', primitive, '.'])
                setter.add_parameter(primitive, 'value')
            setter.add_javadoc(''.join(javadoc2))
            setter.add_javadoc(javadoc)
            setter.add_line('super.setValue(value);')
            if self.needs_check:
                setter.add_line('check();')
                setter.add_exception('YangException')
            setters.append(self.fix_imports(setter, child=True))
        return setters

    def checker(self):
        """Returns a 'check' JavaMethod, which checks constraints on values"""
        if self.needs_check:
            checker = JavaMethod(name='check')
            checker.add_javadoc('Checks all restrictions (if any).')
            checker.add_exception('YangException')
            # TODO 'length', 'path', 'range', 'require_instance'
            if self.bit:
                pass  # TODO
            if self.enum:
                checker.add_line('boolean e = false;')
                for e in self.enum:
                    checker.add_line('e |= enumeration("' + e.arg + '");')
                checker.add_line('throwException( !e );')
            if self.pattern:
                if len(self.pattern) == 1:
                    checker.add_line('pattern("' + self.pattern[0].arg + '");')
                else:
                    checker.add_line('java.lang.String[] regexs = {')
                    for p in self.pattern:
                        checker.add_line('    "' + p.arg + '",')
                    checker.add_line('};')
                    checker.add_line('pattern(regexs);')
            return [self.fix_imports(checker)]
        return []


class ContainerMethodGenerator(MethodGenerator):
    """Method generator specific to classes generated from container stmts"""

    def __init__(self, stmt, ctx=None):
        super(ContainerMethodGenerator, self).__init__(stmt, ctx)
        assert self.gen is self
        assert self.is_container, 'Only valid for container stmts'

    def constructors(self):
        return [self.empty_constructor()]

    def setters(self):
        return NotImplemented

    def markers(self):
        return NotImplemented

    def child_field(self):
        """Returns a string representing java code for a field"""
        res = JavaValue(name=camelize(self.stmt.arg), value='null')
        res.add_javadoc(' '.join(['Field for child', self.stmt.keyword,
                                  '"' + self.stmt.arg + '".']))
        res.add_modifier(normalize(self.stmt.arg))
        res.add_dependency(normalize(self.stmt.arg))
        return self.fix_imports(res, child=True)

    def deleters(self):
        """Returns a list with a single method that deletes an instance of the
        class to be generated from the statement of this method generator to
        its parent class.

        """
        method = self._parent_template('delete')
        method.add_javadoc(''.join(['Deletes ', self.stmt.keyword,
                                    ' entry "', self.n2, '".']))
        method.add_javadoc('@return An array of the deleted element nodes.')
        method.add_line(''.join(['this.', self.n2, ' = null;']))
        method.add_line(''.join(['String path = "', self.n2, '";']))
        method.set_return_type('NodeSet')
        method.add_line('return delete(path);')
        return [self.fix_imports(method, child=True)]

    def parent_access_methods(self):
        res = []
        res.append(self.access_methods_comment())
        res.extend(self.adders())
        res.append(self.deleters())
        return res


class ListMethodGenerator(MethodGenerator):
    """Method generator specific to classes generated from list stmts"""

    def __init__(self, stmt, ctx):
        super(ListMethodGenerator, self).__init__(stmt, ctx)
        assert self.gen is self
        assert self.is_list, 'Only valid for list stmts'
        self.is_config = is_config(stmt)
        self.keys = []
        if self.is_config:
            self.keys = self.stmt.search_one('key').arg.split(' ')
        findkey = lambda k: self.stmt.search_one('leaf', k)
        self.key_stmts = map(findkey, self.keys)
        notstring = lambda k: get_types(k, ctx)[1] != 'String'
        self.is_string = not filter(notstring, self.key_stmts)

    def value_constructors(self):
        """Returns a list of constructors for configuration data lists"""
        assert self.is_config, 'Only called with configuration data stmts'

        constructors = []

        # Determine number of constructors
        number_of_value_constructors = 2 + (not self.is_string)
        javadoc1 = ['Constructor for an initialized ', self.n, ' object,']
        javadoc2 = ['', 'with String keys.']
        if not self.is_string:
            javadoc2.append('with keys of built in Java types.')

        # Create constructors in a loop
        for i in range(number_of_value_constructors):
            constructor = self._constructor_template()
            constructor.add_javadoc(''.join(javadoc1))
            constructor.add_javadoc(javadoc2[i])
            constructor.add_exception('JNCException')  # TODO: Add only if needed
            for key in self.key_stmts:
                javadoc = ['@param ', key.arg, 'Value Key argument of child.']
                jnc, primitive = get_types(key, self.ctx)
                jnc = constructor.add_dependency(jnc)
                setValue = [key.arg, '.setValue(']
                if i == 0:
                    # Default constructor
                    param_type = jnc
                    setValue.extend([key.arg, 'Value);'])
                else:
                    # String or primitive constructor
                    setValue.extend(['new ', jnc, '(', key.arg, 'Value));'])
                    if i == 1:
                        param_type = 'String'
                    else:
                        param_type = primitive
                newLeaf = ['Leaf ', key.arg, ' = new Leaf']
                constructor.add_dependency('Leaf')
                newLeaf.extend(self._root_namespace(key.arg))
                constructor.add_dependency(self.root)
                insertChild = ['insertChild(', key.arg, ', childrenNames());']
                constructor.add_javadoc(''.join(javadoc))
                constructor.add_parameter(param_type, key.arg + 'Value')
                constructor.add_line(''.join(newLeaf))
                constructor.add_line(''.join(setValue))
                constructor.add_line(''.join(insertChild))
            constructors.append(self.fix_imports(constructor))

        return constructors

    def constructors(self):
        # Number of constructors depends on the type of the key
        constructors = [self.empty_constructor()]
        if self.is_config or self.stmt.search_one('key') is not None:
            constructors.extend(self.value_constructors())
        return constructors

    def setters(self):
        return NotImplemented

    def markers(self):
        return NotImplemented

    def _parent_method(self, method_type):
        """Returns a list of methods that either gets an instance of the class
        to be generated from the statement of this method generator to its
        parent class, or deletes it, depending on the method_type parameter.

        method_type -- either 'get' or 'delete'

        """
        res = [self._parent_template(method_type) for _ in range(2)]

        for i, method in enumerate(res):
            javadoc1 = [method_type.capitalize(), 's ', self.stmt.keyword,
                        ' entry "', self.n2, '", with specified keys.']
            javadoc2 = []
            path = ['String path = "', self.n2]
            if i == 1:
                javadoc2.append('The keys are specified as strings.')

            for key in self.gen.key_stmts:
                javadoc2.append(''.join(['@param ', key.arg,
                    'Value Key argument of child.']))
                param_type = 'String'
                if i == 0:
                    param_type, _ = get_types(key, self.ctx)
                method.add_parameter(param_type, key.arg)
                path.extend(['[', key.arg, '=\'" + ', key.arg, ' + "\']'])
            path.append('";')

            method.add_javadoc(''.join(javadoc1))
            for javadoc in javadoc2:
                method.add_javadoc(javadoc)
            method.add_line(''.join(path))
            if method_type == 'delete':
                method.set_return_type('void')
                method.add_line('delete(path);')
            else:  # get
                method.add_line('return (' + self.n + ')searchOne(path);')
            self.fix_imports(method, child=True)
        return res

    def deleters(self):
        """Returns a list of methods that deletes an instance of the class to
        be generated from the statement of this method generator to its parent
        class.

        """
        return self._parent_method('delete')

    def getters(self):
        """Returns a list of methods that gets an instance of the class to be
        generated from the statement of this method generator to its parent
        class.

        """
        return self._parent_method('get')

    def parent_access_methods(self):
        res = []
        res.append(self.access_methods_comment())
        res.extend(self.getters())
        res.append(self.child_iterator())
        res.extend(self.adders())
        res.extend(self.deleters())
        return res


class OrderedSet(collections.MutableSet):
    """A set of unique items that preserves the insertion order.

    Created by: Raymond Hettinger 2009-03-19
    Edited by: Emil Wall 2012-08-03
    Licence: http://opensource.org/licenses/MIT
    Original source: http://code.activestate.com/recipes/576694/

    An ordered set is implemented as a wrapper class for a dictionary
    implementing a doubly linked list. It also has a pointer to the last item
    in the set (self.end) which is used by the add and _iterate methods to add
    items to the end of the list and to know when an iteration has finished,
    respectively.

    """

    def __init__(self, iterable=None):
        """Creates an ordered set.

        iterable -- A mutable iterable, typically a list or a set, containing
                    initial values of the set. If the default value (None) is
                    used, the set is initialized as empty.

        """
        self.ITEM, self.PREV, self.NEXT = range(3)
        self.end = end = []
        end += [None, end, end]         # sentinel node for doubly linked list
        self.map = {}                   # value --> [value, prev, next]
        if iterable is not None:
            self |= iterable

    def __len__(self):
        """Returns the number of items in this set."""
        return len(self.map)

    def __contains__(self, item):
        """Returns true if item is in this set; false otherwise."""
        return item in self.map

    def add(self, item):
        """Adds an item to the end of this set."""
        if item not in self:
            self.map[item] = [item, self.end[self.PREV], self.end]
            self.end[self.PREV][self.NEXT] = self.map[item]
            self.end[self.PREV] = self.map[item]

    def add_first(self, item):
        """Adds an item to the beginning of this set."""
        if item not in self:
            self.map[item] = [item, self.end, self.end[self.NEXT]]
            self.end[self.NEXT][self.PREV] = self.map[item]
            self.end[self.NEXT] = self.map[item]

    def discard(self, item):
        """Finds and discards an item from this set, amortized O(1) time."""
        if item in self:
            item, prev, after = self.map.pop(item)
            prev[self.NEXT] = after
            after[self.PREV] = prev

    def _iterate(self, iter_index):
        """Internal generator method to iterate through this set.

        iter_index -- If 1, the set is iterated in reverse order. If 2, the set
                      is iterated in order of insertion. Else IndexError.

        """
        curr = self.end[iter_index]
        while curr is not self.end:
            yield curr[self.ITEM]
            curr = curr[iter_index]

    def __iter__(self):
        """Returns a generator object for iterating the set in the same order
        as its items were added.

        """
        return self._iterate(self.NEXT)

    def __reversed__(self):
        """Returns a generator object for iterating the set, beginning with the
        most recently added item and ending with the first/oldest item.

        """
        return self._iterate(self.PREV)

    def pop(self, last=True):
        """Discards the first or last item of this set.

        last -- If True the last item is discarded, otherwise the first.

        """
        if not self:
            raise KeyError('set is empty')
        item = next(reversed(self)) if last else next(iter(self))
        self.discard(item)
        return item

    def as_sorted_list(self):
        """Returns a sorted list with the items in this set"""
        res = [x for x in self]
        res.sort()
        return res

    def __repr__(self):
        """Returns a string representing this set. If empty, the string
        returned is 'OrderedSet()', otherwise if the set contains items a, b
        and c: 'OrderedSet([a, b, c])'

        """
        if not self:
            return '%s()' % (self.__class__.__name__,)
        return '%s(%r)' % (self.__class__.__name__, list(self))

    def __eq__(self, other):
        """Returns True if other is an OrderedSet containing the same items as
        other, in the same order.

        """
        return isinstance(other, OrderedSet) and list(self) == list(other)

    def __del__(self):
        """Destructor, clears self to avoid circular reference which could
        otherwise occur due to the doubly linked list.

        """
        self.clear()

#!/usr/bin/python
# -*- coding: latin-1 -*-
"""JPyang: Java output plug-in
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

For complete functionality, invoke with:
> pyang \
    --path <yang search path>
    --format java \
    --java-package <package.name> \
    --jpyang-verbose \
    --jpyang-javadoc <javadoc directory path> \
    <file.yang>

Or, if you like to keep things simple:
> pyang -f jpyang -d <package.name> <file.yang>

"""

from __future__ import with_statement  # Not required from Python 2.6 and
# ... onwards, but kept for the sake of backwards compatibility

import optparse  # TODO Deprecated in python 2.7, should use argparse instead
# ... See http://stackoverflow.com/questions/3217673/why-use-argparse-rather-than-optparse
# ... and http://docs.python.org/dev/library/argparse.html#upgrading-optparse-code
import os
import errno
import sys
import datetime

from pyang import plugin
from pyang import util
from pyang import error
from pyang import statements


# TODO Might be more efficient to use dicts instead of set and list for these
java_reserved_words = {'abstract', 'assert', 'boolean', 'break', 'byte',
    'case', 'catch', 'char', 'class', 'const*', 'continue', 'default',
    'double', 'do', 'else', 'enum', 'extends', 'false',
    'final', 'finally', 'float', 'for', 'goto*', 'if',
    'implements', 'import', 'instanceof', 'int', 'interface', 'long',
    'native', 'new', 'null', 'package', 'private', 'protected',
    'public', 'return', 'short', 'static', 'strictfp', 'super',
    'switch', 'synchronized', 'this', 'throw', 'throws', 'transient',
    'true', 'try', 'void', 'volatile', 'while'}
"""A set of identifiers that are reserved in Java"""


immutable_stmts = {'type', 'typedef', 'namespace', 'prefix', 'organization',
    'contact', 'description', 'range'}
"""A set of statement keywords that should not have their arguments modified"""
# TODO add more keywords to immutable_stmts


outputted_warnings = []
"""A list of warning message IDs that are used to avoid duplicate warnings"""


augmented_modules = {}
"""A dict of external modules that are augmented by this module"""


def pyang_plugin_init():
    """Registers an instance of the jpyang plugin"""
    plugin.register_plugin(JPyangPlugin())


class JPyangPlugin(plugin.PyangPlugin):
    """The plug-in class of JPyang"""

    def add_output_format(self, fmts):
        """Adds 'java' and 'jpyang' as valid output formats"""
        self.multiple_modules = False
        fmts['java'] = fmts['jpyang'] = self

    def add_opts(self, optparser):
        """Adds options to pyang"""
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
            ]
        g = optparser.add_option_group('JPyang output specific options')
        g.add_options(optlist)
        self.o = optparser.parse_args()[0]

    def setup_ctx(self, ctx):
        """Called after ctx has been set up in main module. Checks if the
        jpyang help option was supplied and if not, that the -d or
        --java-package was used.

        ctx -- Context object as defined in __init__.py

        """
        if ctx.opts.jpyang_help:
            self.print_help()
            sys.exit(0)
        if ctx.opts.format in ['java', 'jpyang'] and not ctx.opts.directory:
            ctx.opts.directory = 'gen'
            print_warning(msg='Option -d (or --java-package) not set, ' + \
                'defaulting to "gen".')

    def setup_fmt(self, ctx):
        """Disables implicit errors for the Context"""
        ctx.implicit_errors = False

    def emit(self, ctx, modules, fd):
        """Deletes any previous files in the supplied directory, creates
        directory structure and generates Java code to it.

        ctx     -- Context used to get output directory
        modules -- A list of pyang Statements, should be nothing else than
                   module and/or submodule statements.
        fd      -- File descriptor ignored.

        """
        for (epos, etag) in ctx.errors[:1]:
            if (error.is_error(error.err_level(etag)) and
                etag in ('MODULE_NOT_FOUND', 'MODULE_NOT_FOUND_REV')):
                self.fatal("%s contains errors" % epos.top.arg)
            if (etag in ('TYPE_NOT_FOUND', 'FEATURE_NOT_FOUND',
                'IDENTITY_NOT_FOUND', 'GROUPING_NOT_FOUND')):
                print_warning(msg=etag.lower() + ', generated class ' + \
                    'hierarchy might be incomplete.', key=etag)
        directory = ctx.opts.directory
        d = directory.replace('.', '/')
        for module in modules:
            if module.keyword == 'module':
                if not ctx.opts.no_schema:

                    # Generate external schema
                    ns = module.search_one('namespace').arg
                    schema_generator = SchemaGenerator(module.substmts, '/', ns, ctx)
                    schema_node = schema_generator.schema_node(module)
                    schema_nodes = schema_generator.schema_nodes()
                    name = capitalize_first(module.search_one('prefix').arg)
                    write_file(d,  # XXX This might ruin directory structure
                        name + '.schema',
                        '<schema>\n' + indent( \
                            ('<node>\n' + indent(schema_node) + '\n</node>' + \
                                schema_nodes).splitlines()) + '\n</schema>',
                        [modules],
                        ctx)
                    if ctx.opts.debug or ctx.opts.verbose:
                        print 'Schema generation COMPLETE.'

                # Generate Java classes
                module = make_valid_identifiers(module)
                src = 'module "' + module.arg + '", revision: "' + \
                    util.get_latest_revision(module) + '".'
                generator = ClassGenerator(module, directory, src, ctx)
                generator.generate_classes()
                for aug_module in augmented_modules.values():
                    src = 'module "' + aug_module.arg + '", revision: "' + \
                        util.get_latest_revision(aug_module) + '".'
                    generator = ClassGenerator(aug_module, directory, src, ctx)
                    generator.generate_classes()
                augmented_modules.clear()
                if ctx.opts.debug or ctx.opts.verbose:
                    print 'Java classes generation COMPLETE.'
            else:
                print_warning(msg='Ignoring schema tree rooted at "' + \
                    module.keyword + ' ' + module.arg + '" - not a module')

        # Generate javadoc
        for module in modules:
            if module.keyword == 'module':
                module = make_valid_identifiers(module)
                package_info_generator = PackageInfoGenerator(d, module, ctx)
                package_info_generator.generate_package_info()
        javadir = ctx.opts.javadoc_directory
        if javadir:
            if ctx.opts.debug or ctx.opts.verbose:
                print 'Generating javadoc...'
            if ctx.opts.verbose:
                os.system('javadoc -d ' + javadir + ' -subpackages ' + d)
            else:
                os.system('javadoc -d ' + javadir + ' -subpackages ' + d + \
                    ' >/dev/null')
            if ctx.opts.debug or ctx.opts.verbose:
                print 'Javadoc generation COMPLETE.'
        if len(modules) != 1:
            print_warning(msg='Generating code for several modules has not' + \
                ' been tested thoroughly.', ctx=ctx)

    def fatal(self, exitCode=1):
        """Raise an EmitError"""
        raise error.EmitError(self, exitCode)

    def print_help(self):
        """Prints a description of what JPyang is and how to use it"""
        print '''
The JPyang/Java output format can be used to generate a Java class hierarchy
from a single yang data model. Each module, container, list, etc. is
represented by a .java file which can be used to retrieve and/or edit
configurations (e.g. by calling methods to add, delete or replace statements).

One way to use the Java output format plug-in of pyang is to type
$ pyang -f java -d output.package.dir <file.yang>

The two formats java and jpyang produce identical results.

Type '$ pyang --help' for more details on how to use pyang.
'''


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
            print_warning('No support for type "' + key + '", defaulting ' + \
                'to string.', key, ctx)


def write_file(d, file_name, file_content, modules, ctx):
    """Creates the directory d if it does not yet exist and writes a file to it
    named file_name with file_content in it.

    """
    d = d.replace('.', '/')
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
            print_warning(msg='Unable to change directory to ' + d + \
                '. Probably a non-directory file with same name as one of ' + \
                'the subdirectories already exists.', key=d, ctx=ctx)
        else:
            raise
    finally:
        if ctx.opts.verbose:
            print 'Writing file to: ' + os.getcwd()
        os.chdir(wd)
    with open(d + '/' + file_name, 'w+') as f:
        f.write(file_content)


def get_package(stmt, ctx):
    """Returns a string representing the package name of a java class generated
    from stmt, assuming that it has been or will be generated by JPyang.

    """
    res = ''
    try:
        tmp_stmt = stmt.parent
        while tmp_stmt.parent != None:
            res = tmp_stmt.arg + '.' + res
            tmp_stmt = tmp_stmt.parent
    except AttributeError:
        pass
    res = ctx.opts.directory.replace('/', '.') + '.' + res
    return res[:-1]


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
    
    Returns an empty string if string argument is None.

    """
    camelized_str = ''
    if string is not None:
        iterator = pairwise(string)
        for character, next_character in iterator:
            if next_character and character in '-.':
                camelized_str += capitalize_first(next_character)
                iterator.next()
            else:
                camelized_str += character
    return camelized_str


def make_valid_identifier(stmt):
    """Prepends a J character to the args field of stmt if it is a Java
    keyword. Replaces hyphens and dots with an underscore character.

    """
    if stmt and stmt.keyword not in immutable_stmts and stmt.arg:
        stmt.arg = camelize(stmt.arg)
        if stmt.arg in java_reserved_words:
            stmt.arg = 'J' + stmt.arg
    return stmt


def make_valid_identifiers(stmt):
    """Calls make_valid_identifier on stmt and its substatements"""
    stmt = make_valid_identifier(stmt)
    valid_substmts = []
    valid_i_children = []
    for sub in stmt.substmts:
        valid_substmts.append(make_valid_identifiers(sub))
    stmt.substmts = valid_substmts
    try:
        for ch in stmt.i_children:
            valid_i_children.append(make_valid_identifiers(ch))
        stmt.i_children = valid_i_children
    except AttributeError:
        pass
    return stmt


def get_types(yang_type, ctx):
    """Returns confm and primitive counterparts of the type statement yang_type

    """
    confm = 'com.tailf.confm.xs.'
    primitive = 'String'
    alt = ''
    if yang_type.arg == 'string':
        pass  # String is the default
    elif yang_type in ('binary', 'instance-identifier', 'empty'):
        primitive, alt = alt, primitive
        if yang_type.arg == 'binary':

            # confm = confm[:-3] + 'Confd.OctetList'  # Possible alternative...
            confm += 'Base64Binary'
        elif yang_type.arg == 'instance-identifier':
            confm = confm[:-3] + 'confd.ObjectRef'
        elif yang_type.arg == 'empty':
            confm = confm[:-3] + 'confd.Exists'
    elif yang_type.arg in ('int8', 'int16', 'int32', 'int64', 'uint8',
            'uint16', 'uint32', 'uint64'):
        if yang_type.arg[:1] == 'u':
            confm += 'Unsigned'
            integer_type = ['long', 'long', 'int', 'short']
        else:
            integer_type = ['long', 'int', 'short', 'byte']
        # XXX One might want to implement uint8 as short instead of byte, etc.
        if yang_type.arg[-2:] == '64':
            primitive = integer_type[0]
        elif yang_type.arg[-2:] == '32':
            primitive = integer_type[1]
        elif yang_type.arg[-2:] == '16':
            primitive = integer_type[2]
        elif yang_type.arg[-1:] == '8':
            primitive = integer_type[3]
        else:
            print_warning('Parsed ' + yang_type.arg + ' as an integer.',
                key=yang_type.arg, ctx=ctx)
            primitive = 'long'
    elif yang_type.arg == 'decimal64':
        primitive = 'double'
        # TODO Maybe this should be com.tailf.confm.confd.Decimal64
    elif yang_type.arg == 'boolean':
        primitive = 'boolean'
#    elif yang_type.arg == 'enumeration':  # Handled by else clause
#    elif yang_type.arg == 'bits':  # Handled by else clause
#    TODO add support for built-in datatypes bits, empty, identityref,
#    instance-identifier, leafref and union
#    TODO enumeration and derived datatypes?
    else:
        try:
            typedef = yang_type.i_typedef
        except AttributeError:
            type_id = get_package(yang_type, ctx) + yang_type.arg
            print_warning(key=type_id, ctx=ctx)
        else:
            package = get_package(typedef, ctx)
            confm = package + '.'
            primitive = yang_type.arg
    return confm + capitalize_first(primitive), primitive + alt


def get_base_type(stmt):
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


def extract_keys(stmt, ctx):
    """Returns the key statement of stmt and lists containing tuples with the
    confm (and primitive, respectively) type of the key and its identifier.

    stmt -- Typically a list statement
    ctx  -- Context used for passing debug flags

    """
    key = stmt.search_one('key')
    confm_keys = []
    primitive_keys = []
    only_strings = True
    for arg in key.arg.split(' '):
        key_type = stmt.search_one('leaf', arg).search_one('type')
        confm, primitive = get_types(key_type, ctx)
        confm_keys.append((confm, arg))
        primitive_keys.append((primitive, arg))
        only_strings *= primitive_keys[-1][0] == 'String'
        # XXX 'b *= a' is syntactically equivalent to b = b and a

    return key, only_strings, confm_keys, primitive_keys


def extract_names(arg):
    """Returns a tuple with arg capitalized and prepended with .java, and arg
    capitalized.

    arg -- Any string, really

    """
    capitalized = capitalize_first(arg)
    return (capitalized + '.java', capitalized)


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


def strip_first(s):
    """Returns s but with chars up to and including '.' or '/' removed"""
    return '.'.join(s.replace('/', '.').split('.')[1:])


def indent(lines, level=1):
    """Returns a string consisting of all strings in lines concatenated,
    each string prepended by a level*4 number of spaces and appended with
    a newline, except the last which has no newline.

    lines -- list of strings
    level -- indentation level (number of spaces divided by 4)

    """
    # TODO implement a more efficient version using replace on strings
    res = ''
    for line in lines:
        res += ' ' * level * 4 + line + '\n'
    return res[:-1]  # Don't include the last newline character


def java_docify(s):
    """Returns the string s, but with each row prepended by ' * '"""
    res = ''
    for row in s.splitlines():
        res += ' * ' + row + '\n'
    return res[:-1]  # Don't include the last newline character


class SchemaGenerator(object):
    """Used to generate an external XML schema from a yang module"""

    def __init__(self, stmts, tagpath, ns, ctx):
        self.stmts = stmts
        self.tagpath = tagpath
        self.ns = ns
        self.ctx = ctx

    def schema_nodes(self):
        """Generate XML schema as a list of "node" elements"""
        res = ''
        for stmt in self.stmts:
            if in_schema(stmt):
                old_stmts = self.stmts
                self.stmts = stmt.substmts
                old_tagpath = self.tagpath
                self.tagpath += stmt.arg + '/'
                res += ('<node>\n' +
                        indent(self.schema_node(stmt)) +
                        '\n</node>' +
                        self.schema_nodes())
                self.stmts = old_stmts
                self.tagpath = old_tagpath
        return res
    
    
    def schema_node(self, stmt):
        """Generate "node" element content for an XML schema"""
        if self.ctx.opts.verbose:
            print 'Generating schema node "' + self.tagpath + '"...'
        res = []
        res.append('<tagpath>' + self.tagpath + '</tagpath>')
        res.append('<namespace>' + self.ns + '</namespace>')
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
        res.append('<min_occurs>' + min_occurs + '</min_occurs>')  # TODO correct?
        res.append('<max_occurs>' + max_occurs + '</max_occurs>')  # TODO correct?
    
        children = ''
        for ch in stmt.substmts:
            if ch.keyword in ('container', 'list', 'leaf', 'leaf-list'):
                children += ch.arg + ' '
        res.append('<children>' + children[:-1] + '</children>')
    
        res.append('<flags>0</flags>')
        res.append('<desc></desc>')
        return res


class YangType(object):
    """Provides an interface to maintain a list of defined yang types"""

    def __init__(self):
        self.defined_types = ['empty', 'int8', 'int16', 'int32', 'int64',
            'uint8', 'uint16', 'uint32', 'uint64', 'binary', 'bits', 'boolean',
            'decimal64', 'enumeration', 'identityref', 'instance-identifier',
            'leafref', 'string', 'union']
        """List of types represented by a confm.xs or generated class"""

    def defined(self, yang_type):
        """Returns true if yang_type is defined, else false"""
        return (yang_type in self.defined_types)

    def add(self, yang_type):
        """Gives yang_type "defined" status in this instance of YangType"""
        self.defined_types.append(yang_type)


class ClassGenerator(object):
    """Used to generate java classes from a yang module"""

    def __init__(self, module, package, src, ctx):
        """Constructor.

        module    -- A data model tree, parsed from a YANG model
        package   -- Name of Java package, also used as path to where files
                     should be written
        src       -- Filename of parsed yang module, or the module name and
                     revision if filename is unknown
        ctx       -- Context used to fetch option parameters

        """
        self.module = module
        self.package = package
        self.src = src
        self.ctx = ctx
        self.yang_types = YangType()

    def generate_classes(self):
        """Generates a Java class hierarchy allowing for netconf communication
        using libraries such as confm and inm.

        """
        if self.module.keyword == 'module':
            ns_arg = self.module.search_one('namespace').arg
            prefix = self.module.search_one('prefix')
        elif self.module.keyword == 'submodule':
            parent_module = self.module.search_one('belongs-to')
            prefix = parent_module.search_one('prefix')
            ns_arg = '<unknown/prefix: ' + prefix.arg + '>'
        (filename, name) = extract_names(prefix.arg)

        for stmt in self.module.substmts:
            if stmt.keyword in ['container', 'list', 'augment', 'typedef']:  # TODO other top-level stmts
                self.generate_class(stmt, '', ns_arg, name, top_level=True)

        if self.ctx.opts.verbose:
            print 'Generating Java class "' + filename + '"...'
        write_file(self.package, filename,
            java_class(filename, self.package,
                ['com.tailf.confm.*', 'com.tailf.inm.*', 'java.util.Hashtable'],
                'The root class for namespace ' + ns_arg + \
                ' (accessible from \n * ' + name + '.NAMESPACE) with prefix "' + \
                prefix.arg + '" (' + name + '.PREFIX).',
                class_fields(ns_arg, prefix.arg) + enable(name) + register_schema(name),
                source=self.src),
            [self.module],
            self.ctx)

    def generate_class(self, stmt, path, ns, prefix_name, top_level=False):
        """Generates a Java class hierarchy providing an interface to a YANG module

        stmt        -- A data model subtree
        path        -- The XPath of stmt in the original module
        ns          -- The XML namespace of the module
        prefix_name -- The module prefix
        top_level   -- Whether or not this is a top-level statement

        """
        (filename, name) = extract_names(stmt.arg)
        access_methods = constructors = cloners = support_methods = names = ''
        fields = []
        mods = ' extends Container'
        i_children_exists = (hasattr(stmt, 'i_children')
            and stmt.i_children != None
            and stmt.i_children != [])

        if stmt.keyword == 'augment':
            if not hasattr(stmt, "i_target_node"):  # TODO: EAFP
                warn_msg = 'Target missing from augment statement'
                print_warning(warn_msg, warn_msg, self.ctx)
            else:
                target = stmt.i_target_node
                augmented_modules[target.top.arg] = target.top
            return

        # TODO preserve correct order in generated class
        expanded_i_children = []
        if i_children_exists:
            for ch in stmt.i_children:
                tmp_access_methods, tmp_fields = self.generate_child(ch, path, ns, prefix_name)
                access_methods += tmp_access_methods
                fields.extend(tmp_fields)

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

        # TODO Avoid quadratic time duplication check (maybe use a set)
        for sub in stmt.substmts:
            if sub not in expanded_i_children:
                tmp_access_methods, tmp_fields = self.generate_child(sub, path, ns, prefix_name)
                access_methods += tmp_access_methods
                fields.extend(tmp_fields)

        if self.ctx.opts.verbose:
            print 'Generating Java class "' + filename + '"...'
        if stmt.keyword != 'typedef':
            support_methods = support_add(fields)
            names = key_names(stmt) + children_names(stmt)
        if stmt.keyword == 'container':
            constructors = constructor(stmt, self.ctx, set_prefix=top_level,
                root=prefix_name)
            cloners = clone(name, shallow=False) + clone(name, shallow=True)
        elif stmt.keyword == 'list':
            key, only_strings, confm_keys, primitive_keys = extract_keys(stmt, self.ctx)
            constructors = constructor(stmt, self.ctx, root=prefix_name,
                set_prefix=top_level, throws="\n        throws INMException") + \
            constructor(stmt, self.ctx, root=prefix_name, set_prefix=top_level,
                mode=1, args=confm_keys, throws='''
            throws INMException''') + \
            constructor(stmt, self.ctx, root=prefix_name, set_prefix=top_level,
                mode=2, args=primitive_keys, throws='''
            throws INMException''')
            if not only_strings:
                constructors += constructor(stmt, self.ctx, root=prefix_name,
                    set_prefix=top_level, mode=3, args=primitive_keys, throws='''
            throws INMException''')
            cloners = clone(name, map(capitalize_first, key.arg.split(' ')),
                shallow=False) + \
                clone(name, map(capitalize_first, key.arg.split(' ')), shallow=True)
        elif stmt.keyword == 'typedef':
            type_stmt = stmt.search_one('type')

            # If supertype is derived, make sure a class for it is generated
            if type_stmt.i_typedef:
                print type_stmt.i_typedef.arg
                if not self.yang_types.defined(type_stmt.i_typedef.arg):
                    print 'defined'
                    old_package = self.package
                    self.package = get_package(type_stmt.i_typedef, self.ctx)
                    path = self.package.replace('.', '/') + '/'
                    self.generate_class(type_stmt.i_typedef, path, ns, prefix_name)
                    self.package = old_package
                    self.yang_types.add(type_stmt.i_typedef.arg)

            # Extract types to use in constructors, etc.
            super_type = get_types(type_stmt, self.ctx)[0]
            mods = ' extends ' + super_type
            base_type = get_base_type(stmt)
            primitive = get_types(base_type, self.ctx)[1]
            constructors = typedef_constructor(stmt)
            spec = ', using a string value'
            arg = 'String ' + stmt.arg + 'Value'
            body = 'super.setValue(' + stmt.arg + '''Value);
            check();'''

            # XXX Intentionally overwrite access_methods
            access_methods = set_value(stmt, spec1=spec, argument=arg, body=body)
            """ stmt     -- The statement to set the value for
                spec1    -- Text to insert before parameter listing
                spec2    -- parameter description
                argument -- Full argument listing of method
                body     -- The code to be put in the method body"""
            if primitive != 'String':
                constructors += typedef_constructor(stmt, primitive)
                spec = ', using ' + primitive + ' value'
                arg = primitive + ' ' + stmt.arg + 'Value'
                access_methods += set_value(stmt, spec1=spec, argument=arg,
                    body=body)
            access_methods += check()
#            print 'typedef ' + stmt.arg
#            print 'package: ' + package + ', filename: ' + filename
            self.yang_types.add(stmt.arg)
        write_file(self.package, filename,
            java_class(filename, self.package,
                ['com.tailf.confm.*', 'com.tailf.inm.*', 'java.util.Hashtable'],
                # TODO Hashtable not used in generated code
                
                'This class represents a "' + path + stmt.arg +
                '" element\n * from the namespace ' + ns,
                constructors + cloners + names + access_methods + support_methods,
                source=self.src,
                modifiers=mods
            ),
            [stmt],
            self.ctx)

    def generate_child(self, sub, path, ns, prefix_name):
        """Returns a tuple of two strings representing java methods and fields
        corresponding to access methods to the sub statement. Uses mutual recursion
        with generate_class.

        sub         -- A data model subtree statement. Its parent most not be None.
        path        -- The XPath of stmt in the original module
        ns          -- The XML namespace of the module
        prefix_name -- The module prefix

        """
        access_methods = ''
        fields = []
        if sub.keyword in ['list', 'container']:
            old_package = self.package
            self.package += '.' + sub.parent.arg
            self.generate_class(sub, path + sub.parent.arg + '/', ns, prefix_name)
            self.package = old_package
            if sub.keyword == 'list':
                key, _, confm_keys, _ = extract_keys(sub, self.ctx)
                access_methods += access_methods_comment(sub) + \
                get_stmt(sub, confm_keys) + \
                get_stmt(sub, confm_keys, string=True) + \
                child_iterator(sub) + \
                add_stmt(sub, args=[(sub.arg, sub.arg)]) + \
                add_stmt(sub, args=confm_keys) + \
                add_stmt(sub, args=confm_keys, string=True) + \
                add_stmt(sub, args=[]) + \
                delete_stmt(sub, args=confm_keys) + \
                delete_stmt(sub, args=confm_keys, string=True)
            elif sub.keyword == 'container':
                fields.append(sub.arg)
                access_methods += access_methods_comment(sub) + \
                child_field(sub) + \
                add_stmt(sub, args=[(sub.parent.arg + '.' + sub.arg, sub.arg)],
                    field=True) + \
                add_stmt(sub, args=[], field=True) + \
                delete_stmt(sub)
        elif sub.keyword in ['leaf', 'leaf-list']:
            type_stmt = sub.search_one('type')
            if type_stmt.i_typedef:
                if self.yang_types.defined(type_stmt.i_typedef.arg):
                    old_package = self.package
                    self.package = get_package(type_stmt.i_typedef, self.ctx)
                    self.generate_class(type_stmt.i_typedef,
                        self.package.replace('.', '/') + '/', ns, prefix_name)
                    self.package = old_package
            type_str1, type_str2 = get_types(type_stmt, self.ctx)
            if sub.keyword == 'leaf':
                key = sub.parent.search_one('key')
                if key is not None and sub.arg in key.arg.split(' '):
                    temp = statements.Statement(None, None, None, 'key',
                        arg=sub.arg)  # TODO Copy sub instead?
                    optional = False
                    
                    # Pass temp to avoid multiple keys
                    access_methods += access_methods_comment(temp, optional)
                else:
                    optional = True
                    # TODO ensure that the leaf is truly optional
                    
                    access_methods += access_methods_comment(sub, optional)
                access_methods += get_value(sub, ret_type=type_str1) + \
                    set_leaf_value(sub, prefix=prefix_name, arg_type=type_str1) + \
                    set_leaf_value(sub, prefix='', arg_type='String',
                        confm_type=type_str1)
                if type_str2 != 'String':
                    access_methods += set_leaf_value(sub, prefix='',
                        arg_type=type_str2, confm_type=type_str1)
                if optional:
                    access_methods += unset_value(sub)
                access_methods += add_value(sub, prefix_name)
                if optional:
                    access_methods += mark(sub, 'replace') + \
                    mark(sub, 'merge') + \
                    mark(sub, 'create') + \
                    mark(sub, 'delete')
            elif sub.keyword == 'leaf-list':
                access_methods += access_methods_comment(sub, optional=False) + \
                    child_iterator(sub) + \
                    set_leaf_value(sub, prefix=prefix_name, arg_type=type_str1) + \
                    set_leaf_value(sub, prefix='', arg_type='String',
                        confm_type=type_str1)
                if type_str2 != 'String':
                    access_methods += set_leaf_value(sub, prefix='',
                        arg_type=type_str2, confm_type=type_str1)
                access_methods += delete_stmt(sub,
                        args=[(type_str1, sub.arg + 'Value')], keys=False) + \
                    delete_stmt(sub, args=[(type_str1, sub.arg + 'Value')],
                        string=True, keys=False) + \
                    add_value(sub, prefix_name) + \
                    mark(sub, 'replace', arg_type=type_str1) + \
                    mark(sub, 'replace', arg_type='String') + \
                    mark(sub, 'merge', arg_type=type_str1) + \
                    mark(sub, 'merge', arg_type='String') + \
                    mark(sub, 'create', arg_type=type_str1) + \
                    mark(sub, 'create', arg_type='String') + \
                    mark(sub, 'delete', arg_type=type_str1) + \
                    mark(sub, 'delete', arg_type='String')
#            elif sub.keyword == 'type':
#                print 'type ' + sub.arg + ':'
#                for ch in sub.substmts:
#                    print '  ' + ch.keyword + ' ' + ch.arg
#            else:
#                pass  # print '(' + sub.keyword + ' ' + sub.arg + ')'
        return access_methods, fields


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
        self.stmt = stmt
        self.ctx = ctx

    def generate_package_info(self):
        """Main generator method: generates package-info content and writes it
        to a file
        
        """
        is_java_file = lambda s: s.endswith('.java')
        is_not_java_file = lambda s: not is_java_file(s)
        directory_listing = os.listdir(self.d)
        java_files = filter(is_java_file, directory_listing)
        dirs = filter(is_not_java_file, directory_listing)
        class_hierarchy = self.generate_javadoc(self.stmt.substmts, java_files)
        write_file(self.d, 'package-info.java', self.gen_package_info(class_hierarchy,
            self.d.replace('/', '.')), self.stmt, self.ctx)
        for directory in dirs:
            for sub in self.stmt.substmts:
                # XXX refactor
                if camelize(capitalize_first(sub.arg)) == camelize( \
                        capitalize_first(directory).replace('.',
                        '?')).replace('?', '.'):
                    old_d = self.d
                    self.d += '/' + directory
                    old_stmt = self.stmt
                    self.stmt = sub
                    self.generate_package_info()
                    self.d = old_d
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
            filename = extract_names(stmt.arg)[0]
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
            if PackageInfoGenerator.is_not_list(entry):
                body = '    <a href="' + entry[:-5] + '.html">' + entry[:-5] + '</a>'
                res += PackageInfoGenerator.html_list(body, 1, tag='li')
            else:
                body = PackageInfoGenerator.parse_hierarchy(entry)
                res += PackageInfoGenerator.html_list(body, 1)
            if body[-1:] != '\n':
                res += '\n'
        return res

    @staticmethod
    def is_not_list(entry):
        """Returns False iff entry is instance of list"""
        return not isinstance(entry, list)

    @staticmethod
    def html_list(body, indent_level, tag='ul'):
        """Returns a string representing javadoc with a <ul> html-element if ul,
        else with a <li> html-element.
    
        """
        body = '<' + tag + '>\n' + body
        if body[-1:] != '\n':
            body += '\n'
        body += '</' + tag + '>'
        return indent(body.split('\n'), indent_level)

    def gen_package_info(self, class_hierarchy, package):
        """Writes a package-info.java file to the package directory with a high
        level description of the package functionality and requirements.
    
        class_hierarchy -- A tree represented as a list as in parse_hierarchy
        ctx             -- Context used only for debugging purposes
    
        """
        if self.ctx.opts.verbose:
            print 'Generating package description package-info.java...'
        src = source = ''
        decapitalize = lambda s: s[:1].lower() + s[1:] if s else ''
        top_level_entries = filter(self.is_not_list, class_hierarchy)
        for entry in top_level_entries:
            module_arg = decapitalize(entry[:-5])
    #        rev = ctx.revs[module_arg][-1:][:0]
    #        if not rev:
            rev = 'unknown'
            src += 'module "' + module_arg + '" (rev "' + rev + '"), '
        if len(top_level_entries) > 1:
            source += 's'
        source += '\n' + src[:-2]
        html_hierarchy = self.html_list(self.parse_hierarchy(class_hierarchy), 0)
        specification = '''
    This class hierarchy was generated from the Yang module''' + source + \
    ' by the <a target="_top" href="https://github.com/Emil-Tail-f/JPyang">' + \
    'JPyang</a> plugin of <a target="_top" ' + \
    '''href="http://code.google.com/p/pyang/">pyang</a>.
    The generated classes may be used to manipulate pieces of configuration data
    with NETCONF operations such as edit-config, delete-config and lock. These
    operations are typically accessed through the ConfM Java library by
    instantiating Device objects and setting up NETCONF sessions with real devices
    using a compatible YANG model.
    
    '''
        # XXX These strings should probably be rewritten for code readability and
        # ... to comply with the actual functionality of the class
        return ('/**' + java_docify(specification + html_hierarchy) + '''
     *
     * @see <a target="_top" href="https://github.com/Emil-Tail-f/JPyang">''' + \
        'JPyang project page</a>\n * @see <a target="_top" ' + \
        'href="ftp://ftp.rfc-editor.org/in-notes/rfc6020.txt">' + \
        'RFC 6020: YANG - A Data Modeling Language for the Network ' + \
        'Configuration Protocol (NETCONF)</a>\n * @see <a target="_top" ' + \
        'href="ftp://ftp.rfc-editor.org/in-notes/rfc6241.txt">RFC 6241: ' + \
        'Network Configuration Protocol (NETCONF)</a>\n * @see <a ' + \
        'target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc6242.txt">' + \
        'RFC 6242: Using the NETCONF Protocol over Secure Shell (SSH)</a>\n' + \
        ' * @see <a target="_top" href="http://www.tail-f.com">Tail-f ' + \
        'Systems</a>\n */\npackage ' + strip_first(self.d) + ';')


def java_class(filename, package, imports, description, body, version='1.0',
               modifiers='', source='<unknown>.yang'):
    """The java_class function returns a string representing Java code for a
    class.

    filename    -- Should preferably not contain a complete path since it is
                   displayed in a Java comment in the beginning of the code.
    package     -- Should be just the name of the package in which the class
                   will be included.
    imports     -- Should be a list of names of imported libraries.
    description -- Defines the class semantics.
    body        -- Should contain the actual code of the class
    version     -- Version number, defaults to '1.0'.
    modifiers   -- Can contain Java statements
                   such as ' implements Serializable' or ' extends Element'.
    source      -- A string somehow representing the origin of the class

    """
    # Fetch current date and set date format
    time = datetime.date.today()
    date = str(time.day) + '/' + str(time.month) + '/' + str(time.year % 100)
    # The header is placed in the beginning of the Java file
    header = '/* \n * @(#)' + filename + ' ' * 8 + version + ' ' + date + '''
 *
 * This file has been auto-generated by JPyang, the Java output format plug-in
 * of pyang. Origin: ''' + source + '\n */'
    # Change date format for class doc comment
    date = str(time.year) + '-' + str(time.month) + '-' + str(time.day)
    class_doc = '''/**
 * ''' + description + '''
 *
 * @version    ''' + version + ' ' + date + '''
 * @author    Auto Generated
 */'''
    # package and import statement goes here
    header += '\n\npackage ' + strip_first(package) + ';\n'
    if len(imports) > 0:
        header += '\n'
    for im in imports:
        header += 'import ' + im + ';\n'
    header += '\n' + class_doc + '\n'
    # Here is the class declaration, with modifiers.
    # The class name is the filename without the file extension.
    class_decl = 'public class ' + filename.split('.')[0] + modifiers + ' {\n'
    return header + class_decl + body + '\n}'


def constructor(stmt, ctx, root='', set_prefix=False, mode=0, args=[],
    throws=''):
    """The constructor function returns a string representing Java code for a
    constructor of a Java class corresponding to the stmt parameter.

    stmt        -- Typically a module, submodule, container or list Statement
    ctx         -- Context used to identify debug and verbose flags
    root        -- Path to class containing the XML namespace prefix of the
                   YANG module. If not set stmt name will be used.
    set_prefix  -- Set to true to add setDefaultPrefix and setPrefix calls
    mode        -- 0: No arguments
                   1: ConfM arguments
                   2: String arguments
                   3: Java primitive arguments
    args        -- A list of tuples containing the argument type(s) and
                   name(s). If empty, the method will have no argument. The
                   argument name(s) should be supplied without the 'Value'
                   suffix. Typically used as a (set of) key(s) in the method.
                   Note that mode must be > 0 for this to have an effect.
    throws      -- Typically 'throws INMException', prepended with a newline
                   and spaces for indentation.

    """
    name = capitalize_first(stmt.arg)
    setters = docstring = inserts = arguments = ''
    MAX_COLS = 80 - len('    public ' + name + '(')
    if root == '':
        root = name
    if set_prefix:
        setters = '''
        setDefaultPrefix();
        setPrefix(''' + root + '.PREFIX);'
    if not args or mode == 0:
        obj_status = 'empty '
    else:
        obj_status = 'initialized '
        values = []
        if mode == 1:  # ConfM arguments
            for (arg_type, arg_name) in args:
                values.append(arg_name + 'Value')
        else:  # String or Java primitive arguments
            if mode == 2:
                docstring = '\n     * with Strings for the keys.'
            else:
                docstring = '\n     * with primitive Java types.'
            for (arg_type, arg_name) in args:
#                tmp_list_confm = []
#                get_types(arg_type, tmp_list_confm, [], ctx)
#                decl = 'new ' + tmp_list_confm[0][0] + '('
                decl = 'new String('  # FIXME should call get_types with a stmt
                values.append(decl + arg_name + 'Value)')
        for (arg_type, arg_name), value in zip(args, values):
            # TODO http://en.wikipedia.org/wiki/Loop_unswitching

            inserts += '''
        // Set key element: ''' + arg_name + '''
        Leaf ''' + arg_name + ' = new Leaf(' + root + '.NAMESPACE, "' + \
            arg_name + '''");
        ''' + arg_name + '.setValue(' + value + ''');
        insertChild(''' + arg_name + ', childrenNames());'
            arg_name = arg_name + 'Value'
            docstring += '\n     * @param ' + arg_name + \
                ' Key argument of child.'
            if mode == 2:  # String arguments
                tmp = 'String ' + arg_name + ', '
            else:  # ConfM or Java primitive arguments
                tmp = arg_type + ' ' + arg_name + ', '
            if len(tmp) > MAX_COLS:
                tmp = '\n' + ' ' * 8 + tmp
                MAX_COLS = 80 - len(tmp)
            else:
                MAX_COLS -= len(tmp)
            arguments += tmp
        arguments = arguments[:-2]  # Skip the last ', '
    return '''
    /**
     * Constructor for an ''' + obj_status + name + ' object.' + docstring + \
     '''
     */
    public ''' + name + '(' + arguments + ')' + throws + ''' {
        super(''' + root + '.NAMESPACE, "' + stmt.arg + '");' + \
        inserts + setters + '''
    }
'''


def typedef_constructor(stmt, arg='String'):
    return '''
    /**
     * Constructor for ''' + stmt.arg + ' object from a ' + arg.lower() + '''.
     * @param value Value to construct the ''' + stmt.arg + ''' from.
     */
    public ''' + capitalize_first(stmt.arg) + '(' + arg + ''' value)
        throws ConfMException {
        super(value);
        check();
    }
'''


def clone(class_name, key_names=[], shallow='False'):
    """Returns a string representing a Java clone method. Iff key_names is
    empty, get<key_name>Value() methods are called to fetch constructor
    arguments and null is returned if an INMException is raised. If shallow is
    set, the cloneShallowContent method is invoked instead of cloneContent.

    class_name -- The name of the class to clone instances of
    key_names  -- Key identifier(s)
    shallow    -- Boolean flag for which clone method to use

    """
    try_stmt = children = cast = ''
    if key_names:
        try_stmt = 'try {\n' + ' ' * 12
        catch_stmt = '('
        MAX_COLS = 80 - 44 - len(class_name)
        for key_name in key_names:
            tmp = 'get' + key_name + 'Value(), '
            if len(tmp) > MAX_COLS:
                tmp = '\n' + ' ' * 16 + tmp
                MAX_COLS = 80 - len(tmp)
            else:
                MAX_COLS -= len(tmp)
            catch_stmt += tmp
        catch_stmt = catch_stmt[:-2] + '''));
        } catch (INMException e) { return null; }
    }\n'''
    else:
        catch_stmt = '());\n    }\n'
    if not shallow:
        copy = 'n exact'
        signature = 'Object clone()'
        cast = '(' + class_name + ')'  # TODO Not always required
        method = 'cloneContent'
    else:
        copy = ' shallow'
        children = ' Children are not included.'
        signature = 'Element cloneShallow()'
        method = 'cloneShallowContent'
    return '''
    /**
     * Clones this object, returning a''' + copy + ''' copy.
     * @return A clone of the object.''' + children + '''
     */
    public ''' + signature + ''' {
        ''' + try_stmt + 'return ' + cast + method + '(new ' + class_name + \
            catch_stmt


def key_names(stmt):
    """Returns a string representing a Java method that returns a String[]
    with the identifiers of the keys in stmt. If stmt does not have any keys,
    null is returned.

    stmt -- A pyang Statement, typically a list or a container

    """
    keys = stmt.search('key')
    if not keys:
        res = 'return null'
    else:
        res = 'return new String[] {\n'

        # Add keys to res, one key per line, indented by 12 spaces
        for key_str in keys:
            for key in key_str.arg.split(' '):
                res += ' ' * 12 + '"' + key + '",\n'
        res = res[:-2] + '\n' + ' ' * 8 + '}'
    return '''
    /**
     * Structure information which specifies
     * the keys for the list entries.
     */
    public String[] keyNames() {
        ''' + res + ''';
    }
'''  # TODO Add support for multiple keys


def children_names(stmt):
    """Returns a string representing a java method that returns a String[]
    with the identifiers of the children of stmt, excluding any keys.

    stmt -- A pyang Statement, typically a list or a container

    """
    children = filter(lambda x:  # x.keyword != 'key' and # TODO add more
        x.keyword != 'key',
        stmt.substmts)
    names = [ch.arg for ch in children]
    if len(names) > 0:
        names = repr(names)[1:-1].replace("'", '"').replace(', ',
            ',\n' + ' ' * 12)
    else:
        names = ''
    return '''
    /**
     * Structure information with the names of the children.
     * Makes it possible to order the children.
     */
    public String[] childrenNames() {
        return new String[] {
            ''' + names + '''
        };
    }
'''  # FIXME What if there are no children?


def class_fields(ns_arg, prefix_arg):
    """Returns a string representing java code for two fields"""
    return '''
    public static final String NAMESPACE = "''' + ns_arg + '''";

    public static final String PREFIX = "''' + prefix_arg + '''";
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
        ''' + prefix_name + '''.registerSchema();
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
        java.net.URL schemaUrl = loader.getSystemResource("''' + \
            prefix_name + '''.schema");
        SchemaParser parser = new SchemaParser();
        Hashtable h = CsTree.create(NAMESPACE);
        if (schemaUrl == null)
            parser.readFile("''' + prefix_name + '''.schema", h);
        else
            parser.readFile(schemaUrl, h);
    }
'''


def access_methods_comment(stmt, optional=False):
    """Returns a string representing a java comment for code structure"""
    if optional:
        opt = 'optional '
    else:
        opt = ''
    return '''
    /**
     * -------------------------------------------------------
     * Access methods for ''' + opt + stmt.keyword + \
     ' child: "' + stmt.arg + '''".
     * -------------------------------------------------------
     */
'''


def child_field(stmt):
    """Returns a string representing java code for a field"""
    return '''
    /**
     * Field for child ''' + stmt.keyword + ' "' + stmt.arg + '''".
     */
    public ''' + capitalize_first(stmt.arg) + ' ' + stmt.arg + ''' = null;
'''


def get_stmt(stmt, keys, string=False):
    """Get method generator. Similar to add_stmt (see below), but does not
    allow parameter-free methods to be generated.

    stmt   -- Typically a list statement
    keys   -- A list of key types and the corresponding identifiers
    string -- if set to True, keys are specified as Strings in method

    """
    name = capitalize_first(stmt.arg)
    spec = arguments = xpath = ''
    if string:
        spec = '\n     * The keys are specified as Strings'
    for (key_type, key_name) in keys:
        spec += '\n     * @param ' + key_name + ' Key argument of child.'
        if string:
            arguments += 'String ' + key_name + ', '
        else:
            arguments += key_type + ' ' + key_name + ', '
        xpath += '[' + key_name + '=\'" + ' + key_name + ' + "\']'
    return '''
    /**
     * Get method for ''' + stmt.keyword + ' entry: "' + stmt.arg + '''".
     * Return the child with the specified keys ''' + \
     '(if any).' + spec + '''
     * @return The ''' + stmt.keyword + ''' entry with the specified keys.
     */
    public ''' + name + ' get' + name + '(' + arguments[:-2] + ''')
        throws INMException {
        String path = "''' + stmt.arg + xpath + '''";
        return (''' + name + ''')getListContainer(path);
    }
'''


def get_value(stmt, ret_type='com.tailf.confm.xs.String'):
    """get<Identifier>Value method generator. Similar to get_stmt (see below),
    but allows parameter-free methods to be generated.

    stmt     -- Typically a leaf statement
    ret_type -- The type of the return value of the generated method

    """
    name = capitalize_first(stmt.arg)
    return '''
    /**
     * Return the value for child ''' + stmt.keyword + ' "' + stmt.arg + '''".
     * @return The value of the ''' + stmt.keyword + '''.
     */
    public ''' + ret_type + ' get' + name + '''Value()
        throws INMException {
        return (''' + ret_type + ')getValue("' + stmt.arg + '''");
    }
'''


def set_leaf_value(stmt, prefix='', arg_type='', confm_type=''):
    """set<Identifier>Value method generator, specifically for leafs.

    stmt       -- Typically a leaf statement
    prefix     -- Namespace prefix of module, empty if the setLeafValue or
                  setLeafListValue methods are not to be used in the method
    arg_type   -- Type of method parameter, empty if parameter free
    confm_type -- Type to use internally, empty if the setIdValue method is not
                  to be used in the method

    """
    name = capitalize_first(stmt.arg)
    spec1 = spec2 = ''
    MAX_COLS = 80 - len('     * Sets the value for child ' + stmt.keyword + \
        ' "' + stmt.arg + '",.')  # Space left to margin

    # Add different comments depending on argument type
    if arg_type == 'String':
        spec1 = ', using a string value'
        spec2 = 'string representation of the '
    elif arg_type == '':
        pass
    else:
        spec1 = ', using the java primitive value'

    # Do linebreak if neccessary
    if len(spec1) > MAX_COLS:
        spec1 = ',\n     * ' + spec1[2:]

    # Register namespace if prefix is not empty, else use generated set-method
    if prefix:
        if stmt.keyword == 'leaf-list':
            body = 'setLeafListValue('
        else:
            body = 'setLeafValue('
        body += prefix + '''.NAMESPACE,
            "''' + stmt.arg + '''",
            ''' + stmt.arg + '''Value,
            childrenNames());'''
    else:
        body = 'set' + name + 'Value(new ' + confm_type + '(' + stmt.arg + \
            'Value));'

    # Prepare method argument listing
    argument = arg_type + ' ' + stmt.arg + 'Value'
    if arg_type == '':
        # Special case of no argument

        argument = ''
        body = 'set' + name + 'Value(new ' + name + '());'
    return set_value(stmt, name, spec1, spec2, argument, body)


def set_value(stmt, nameID='', spec1='', spec2='', argument='', body=''):
    """set<Identifier>Value method generator.

    stmt     -- The statement to set the value for
    nameID   -- Identifier in method name
    spec1    -- Text to insert before parameter listing
    spec2    -- parameter description
    argument -- Full argument listing of method
    body     -- The code to be put in the method body

    """
    if argument:
        spec1 += '''.
     * @param ''' + stmt.arg + 'Value The ' + spec2 + 'value to set'
    return '''
    /**
     * Sets the value for child ''' + stmt.keyword + ' "' + stmt.arg + '"' + \
        spec1 + '''.
     */
    public void set''' + nameID + 'Value(' + argument + ''')
        throws INMException {
        ''' + body + '''
    }
'''


def unset_value(stmt):
    """unset<Identifier> method generator"""
    return '''
    /**
     * Unsets the value for child ''' + stmt.keyword + ' "' + stmt.arg + '''".
     */
    public void unset''' + capitalize_first(stmt.arg) + '''Value()
        throws INMException {
        delete("''' + stmt.arg + '''");
    }
'''


def add_value(stmt, prefix):
    """add<Identifier> method generator, designed for leaf statements. Not to
    be confused with the add_stmt function which is similar but does not
    contain a call to the setLeafValue function.

    stmt   -- Typically a leaf statement
    prefix -- Namespace prefix of module

    """
    name = value_type = ''
    if stmt.keyword == 'leaf-list':
        name = 'Empty'
        value_type = 'List'
    name += capitalize_first(stmt.arg)
    return '''
    /**
     * This method is used for creating a subtree filter.
     * The added "''' + stmt.arg + '" ' + stmt.keyword + \
        ''' will not have a value.
     */
    public void add''' + name + '''()
        throws INMException {
        setLeaf''' + value_type + 'Value(' + prefix + '''.NAMESPACE,
            "''' + stmt.arg + '''",
            null,
            childrenNames());
    }
'''


def mark(stmt, op, arg_type='String'):
    """Generates a method that enqueues an operation to be performed on an
    element.

    stmt     -- Typically a leaf or leaf-list statement. If stmt is not a leaf-
                list, the method will have no argument.
    op       -- The operation 'replace', 'merge', 'create' or 'delete'
    arg_type -- Type of argument, if any.

    """
    spec = op + '"'
    path = stmt.arg
    argument = ''
    if stmt.keyword == 'leaf-list':
        spec += '''.
     * @param ''' + stmt.arg + 'Value The value to mark'
        if arg_type == 'String':
            spec += ', given as a String'
        argument = arg_type + ' ' + stmt.arg + 'Value'
        path += '[name=\'" + ' + stmt.arg + 'Value+"\']'
    return '''
    /**
     * Marks the "''' + stmt.arg + '" ' + stmt.keyword + \
        ' with operation "' + spec + '''.
     */
    public void mark''' + capitalize_first(stmt.arg) + capitalize_first(op) + \
        '(' + argument + ''')
        throws INMException {
        markLeaf''' + capitalize_first(op) + '("' + path + '''");
    }
'''


def child_iterator(substmt):
    """Returns a string representing a java iterator method for the substmt"""
    if substmt.keyword == 'leaf-list':
        iterator_type = 'LeafListValue'
    else:
        iterator_type = 'Children'
    return '''
    /**
     * Iterator method for the ''' + substmt.keyword + ' "' + substmt.arg + \
        '''".
     * @return An iterator for the ''' + substmt.keyword + '''.
     */
    public Element''' + iterator_type + 'Iterator ' + substmt.arg + \
        '''Iterator() {
        return new Element''' + iterator_type + 'Iterator(children, "' + \
        substmt.arg + '''");
    }
'''


def add_stmt(stmt, args=[], field=False, string=False):
    """Generates add-method for stmt, optionally parametrized by an
    argument of specified type and with customizable comments.

    stmt   -- The YANG statement that needs an adder
    args   -- A list of tuples, each tuple containing an arg_type and an
              arg_name. Each arg_type corresponds to a method argument type
              which is also used to deduce what the method should do:
              1. If arg_type is the same as stmt.arg, the produced method
                 will add its argument as a child instead of creating a new
                 one. No cloning occurs.
              2. Tail-f ConfM types produces a method that adds a new stmt
                 with that key of the Tail-f String type, unless string is set.
              Each arg_name corresponds to a method argument name. The names of
              the method's arguments are typically key identifiers or a single
              lowercase stmt name. Setting args to an empty list produces a
              method with no argument, which can be used to create subtree
              filters.
    string -- If set to True, the keys are specified with the ordinary String
              type instead of the Tail-f ConfM String type.
    field  -- If set to True, the statement is added to a field, typically
              in a class representing a YANG container element.

    """
    name = capitalize_first(stmt.arg)
    spec2 = spec3 = ''
    if len(args) == 1 and args[0][0] == stmt.arg:
        spec1 = '.\n     * @param ' + args[0][1] + \
            ' Child to be added to children'
        spec2 = name + ' ' + stmt.arg + ', '
    else:
        spec3 = '\n' + ' ' * 8 + name + ' ' + stmt.arg + \
            ' = new ' + name + '('
        if not args:
            spec1 = '''.
     * This method is used for creating subtree filters'''
            spec3 += ');'
        else:
            spec1 = ', with given key arguments'
            if string:
                spec1 += '.\n     * The keys are specified as strings'
            for (arg_type, arg_name) in args:
                spec1 += '.\n     * @param ' + arg_name + \
                    ' Key argument of child'
                if string:
                    spec2 += 'String ' + arg_name + ', '
                else:
                    spec2 += arg_type + ' ' + arg_name + ', '
                spec3 += arg_name + ', '
            spec3 = spec3[:-2] + ');'
    if field:
        spec3 += '\n' + ' ' * 8 + 'this.' + stmt.arg + ' = ' + stmt.arg + ';'
    return '''
    /**
     * Adds ''' + stmt.keyword + ' entry "' + stmt.arg + '"' + spec1 + '''.
     * @return The added child.
     */
    public ''' + name + ' add' + name + '(' + spec2[:-2] + ''')
        throws INMException {''' + spec3 + '''
        insertChild(''' + stmt.arg + ''', childrenNames());
        return ''' + stmt.arg + ''';
    }
'''


def delete_stmt(stmt, args=[], string=False, keys=True):
    """Delete method generator. Similar to add_stmt (see above).

    stmt   -- Typically a list or container statement
    args   -- A list of tuples, each tuple containing an arg_type and an
              arg_name. Each arg_type corresponds to a method argument type.
              Each arg_name corresponds to a method argument name. The names of
              the method's arguments are typically key identifiers or a single
              lowercase stmt name. Setting args to an empty list produces a
              method with no argument.
    string -- If set to True, the keys are specified with the ordinary String
              type instead of the Tail-f ConfM String type.

    """
    spec1 = spec2 = spec3 = arguments = ''
    if args:
        if keys:
            spec1 = '", with specified keys.'
            if string:
                spec1 += '\n     * The keys are specified as Strings'
        elif string:
            spec1 += '\n     * The value is specified as a String'
        for (arg_type, arg_name) in args:
            # TODO http://en.wikipedia.org/wiki/Loop_unswitching

            if string:
                arguments += 'String ' + arg_name + ', '
            else:
                arguments += arg_type + ' ' + arg_name + ', '
            if keys:
                spec1 += '\n     * @param ' + arg_name + \
                    ' Key argument of child.'
                spec3 += '[' + arg_name + '=\'" + ' + arg_name + ' + "\']'
            else:
                spec1 += '\n     * @param ' + arg_name + \
                    ' Child to be removed.'
                spec3 += '[name=\'" + ' + arg_name + ' + "\']'
    else:
        spec1 = ''
        spec2 = 'this.' + stmt.arg + ' = null;\n        '
    return '''
    /**
     * Deletes ''' + stmt.keyword + ' entry "' + stmt.arg + spec1 + '''"
     */
    public void delete''' + capitalize_first(stmt.arg) + '(' + arguments[:-2] + ''')
        throws INMException {
        ''' + spec2 + 'String path = "' + stmt.arg + spec3 + '''";
        delete(path);
    }
'''


def check(regexp=''):
    return '''
    /**
     * Checks all restrictions (if any).
     */
    public void check()
        ''' + regexp + '''throws ConfMException {
    }
'''


def support_add(fields=[]):
    """Generates an addChild method.

    fields -- a list of fields in the generated class

    """
    assignments = ''
    for i in range(len(fields) - 1, -1, -1):
        assignments += 'if ($child instanceof ' + capitalize_first(fields[i]) + \
            ') ' + fields[i] + ' = (' + capitalize_first(fields[i]) + ')$child;'
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
        ''' + assignments + '''
    }
'''  # TODO '$' should be removed unless it is actually needed
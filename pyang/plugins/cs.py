"""confspec output plugin"""

from xml.sax.saxutils import quoteattr
from xml.sax.saxutils import escape

import optparse
import re
import copy
import sys

import pyang
from pyang import plugin
from pyang import util
from pyang import statements
from pyang import error
from pyang.translators import xsd
from pyang import xpath
from pyang.plugins import tailf as tailf_plugin

# NOT YET IMPLEMENTED
# -------------------
#  o  anyxml
#
# TODO
# ----
# o  check for operational data without a tailf:callpoint
#    (currently reported in link phase by confdc)
# o  check path-filter, dependency validity
#    (currently reported in link phase by confdc)

tailf = 'tailf-common'
"""Name of tail-f YANG extensions module"""

xsd_mod = 'tailf-xsd-types'
"""Name of XSD data type library module"""

smiv2 = 'ietf-yang-smiv2'
"""Name of SMIv2 extensions module"""

yang_types = 'ietf-yang-types'
"""Name of YANG data type library module"""

snmpv2_tc = 'SNMPv2-TC'

istr = '  '
"""Indentation spacing"""

cli_map = {
    'cli-show-no'                   : ((1L << 0), None, False),
    'cli-show-config'               : ((1L << 1), None, False),
    'cli-mode-name'                 : ((1L << 2), 'string', False),
    'cli-mode-name-actionpoint'     : ((1L << 3), 'string', False),
    'cli-add-mode'                  : ((1L << 4), None, False),
    'cli-suppress-mode'             : ((1L << 5), None, False),
    'cli-suppress-table'            : ((1L << 6), None, False),
    'cli-suppress-key-abbreviation' : ((1L << 7), None, False),
    'cli-allow-key-abbreviation'    : ((1L << 8), None, False),
    'cli-table-legend'              : ((1L << 9), 'string', False),
    'cli-completion-actionpoint'    : ((1L << 10), 'string', True),
    'cli-completion-id'             : ((1L << 11), 'string', False),
    'cli-allow-range'               : ((1L << 12), None, False),
    'cli-suppress-range'            : ((1L << 13), None, False),
    'cli-allow-wildcard'            : ((1L << 14), None, False),
    'cli-suppress-wildcard'         : ((1L << 15), None, False),
    'cli-delayed-auto-commit'       : ((1L << 16), None, False),
    'cli-preformatted'              : ((1L << 17), None, False),
    'cli-enforce-table'             : ((1L << 18), None, False),
    'cli-drop-node-name'            : ((1L << 19), None, False),
    'cli-compact-syntax'            : ((1L << 20), None, False),
    'cli-column-stats'              : ((1L << 21), None, False),
    'cli-column-width'              : ((1L << 22), 'uint32', False),
    'cli-column-align'              : ((1L << 23), 'atom', False),
    'cli-incomplete-command'        : ((1L << 24), None, False),
    'cli-full-command'              : ((1L << 25), None, False),
    'cli-sequence-commands'         : ((1L << 26), None, True),
    'cli-incomplete-show-path'      : ((1L << 27), None, True),
    'cli-min-keys'                  : ((1L << 28), 'uint32', False),
    'cli-full-show-path'            : ((1L << 29), None, True),
    'cli-max-keys'                  : ((1L << 30), 'uint32', False),
    'cli-suppress-show-path'        : ((1L << 31), None, False),
    'cli-suppress-show-match'       : ((1L << 32), None, False),
    'cli-no-key-completion'         : ((1L << 33), None, False),
    'cli-no-match-completion'       : ((1L << 34), None, False),
    'cli-compact-stats'             : ((1L << 35), None, True),
    'cli-wrap'                      : ((1L << 36), None, False),
    'cli-width'                     : ((1L << 37), 'uint32', False),
    'cli-delimiter'                 : ((1L << 38), 'string', False),
    'cli-prettify'                  : ((1L << 39), None, False),
    'cli-spacer'                    : ((1L << 40), 'string', False),
    'cli-custom-range'              : ((1L << 41), None, True),
    'cli-custom-range-actionpoint'  : ((1L << 42), 'string', True),
    'cli-range-type'                : ((1L << 43), 'type-name', False),
    'cli-show-template'             : ((1L << 44), 'string', True),
    'cli-show-template-legend'      : ((1L << 45), 'string', False),
    'cli-show-template-enter'       : ((1L << 46), 'string', False),
    # NOTE: cli-default-order is handled in cs_secondary_index, not in
    # the generic cli_attrs function
    'cli-default-order'             : ((1L << 47), None, False),
    'cli-multi-value'               : ((1L << 48), None, True),
    'cli-suppress-validation-warning-prompt' : ((1L << 49), None, False),
    'cli-suppress-key-sort'         : ((1L << 50), None, False),
    'cli-run-template'             : ((1L << 51), 'string', False),
    'cli-run-template-legend'      : ((1L << 52), 'string', False),
    'cli-run-template-enter'       : ((1L << 53), 'string', False),
    'cli-display-empty-config'     : ((1L << 54), 'string', False),
    'cli-value-display-template'   : ((1L << 55), 'string', False),
    'cli-expose-key-name'          : ((1L << 56), None, False),
    'cli-show-order-taglist'       : ((1L << 57), 'string', False),
    'cli-show-order-tag'           : ((1L << 58), 'string', False),
    'cli-break-sequence-commands'  : ((1L << 59), None, False),
    'cli-show-template-footer'     : ((1L << 60), 'string', False),
    'cli-run-template-footer'      : ((1L << 61), 'string', False),
    'cli-table-footer'             : ((1L << 62), 'string', False),
    'cli-multi-word-key'           : ((1L << 63), None, True),
    'cli-max-words'                : ((1L << 64), 'uint32', False),
    'cli-autowizard'               : ((1L << 65), None, False),
    'cli-suppress-show-conf-path'  : ((1L << 66), None, False),
    'cli-key-format'               : ((1L << 67), 'string', False),
    'cli-list-syntax'              : ((1L << 68), None, True),
    'cli-suppress-list-no'         : ((1L << 69), None, False),
    'cli-suppress-no'              : ((1L << 70), None, False),
    'cli-full-no'                  : ((1L << 71), None, False),
    'cli-incomplete-no'            : ((1L << 72), None, False),
    'cli-flat-list-syntax'         : ((1L << 73), None, True),
    'cli-flatten-container'        : ((1L << 74), None, False),
    'cli-custom-range-enumerator'  : ((1L << 75), 'string', True),
    'cli-reset-siblings'           : ((1L << 76), None, False),
    'cli-hide-in-submode'          : ((1L << 77), None, False),
    'cli-prefix-key'               : ((1L << 78), None, True),
    'cli-show-with-default'        : ((1L << 79), None, False),
    'cli-reset-all-siblings'       : ((1L << 80), None, False),
    'cli-reset-container'          : ((1L << 81), None, False),
    'cli-exit-command'             : ((1L << 82), 'string', True),
    # x_cli_info is not a real statement.  it is used when tailf:info is
    # found as a substatement to some other cli statement.
    'x_cli_info'                   : ((1L << 83), 'string', False),
    'cli-boolean-no'               : ((1L << 84), None, True),
    'cli-optional-in-sequence'     : ((1L << 85), None, False),
    'cli-allow-join-with-key'      : ((1L << 86), None, True),
    'cli-display-joined'           : ((1L << 87), None, False),
    'cli-trim-default'             : ((1L << 88), None, False),
    'cli-range-list-syntax'        : ((1L << 89), None, False),
    'cli-reversed'                 : ((1L << 90), None, False),
    'cli-multi-word'               : ((1L << 91), None, True),
    'cli-disallow-value'           : ((1L << 92), 'string', False),
    'cli-before-key'               : ((1L << 93), 'uint32', False),
    'cli-suppress-silent-no'       : ((1L << 94), 'string', False),
    'cli-range-delimiters'         : ((1L << 95), 'string', False),
    'cli-min-column-width'         : ((1L << 96), 'uint32', False),
    # 'cli-synthetic'              : ((1L << 97), None, False),
    'cli-oper-info'                : ((1L << 98), 'string', False),
    'cli-custom-error'             : ((1L << 99), 'string', False),
    'cli-remove-before-change'     : ((1L << 100), None, False),
    'cli-show-long-obu-diffs'      : ((1L << 101), None, False),
    'cli-no-value-on-delete'       : ((1L << 102), None, False),
    'cli-no-name-on-delete'        : ((1L << 103), None, False),
    'cli-replace-all'              : ((1L << 104), None, False),
    'cli-configure-mode'           : ((1L << 105), None, False),
    'cli-operational-mode'         : ((1L << 106), None, False),
    'cli-auto-legend'              : ((1L << 107), None, False),
    }
"""<cli-stmt>: <flag> <argtype> <has-substatements>"""

re_sibling = re.compile("current\s*\(\s*\)/\s*(\.\./[a-zA-Z0-9_:-]*)")
re_string_dh = re.compile("^(\d+(a|t))+$")

flag_map = {
    'junos-val-as-xml-tag'         : ((1L << 58), None),
    'junos-val-with-prev-xml-tag'  : ((1L << 59), None),
    'snmp-delete-value'            : ((1L << 61), 'snmp-send-delete-value'),
    'snmp-ned-modification-dependent' : ((1L << 62), None),
    }

def pyang_plugin_init():
    plugin.register_plugin(CSPlugin())

class CSPlugin(plugin.PyangPlugin):
    def setup_ctx(self, ctx):
        ctx.tailf_ann_mods = {}

    def add_opts(self, optparser):
        optlist = [
            optparse.make_option("-F", "--feature",
                                 metavar="FEATURE",
                                 dest="tailf_features",
                                 default=[],
                                 action="append",
                                 help="Features to support, default all"),
            optparse.make_option("--no-features",
                                 dest="no_features",
                                 action="store_true",
                                 help="Support no features"),
            optparse.make_option("", "--deviation",
                                 metavar="DEVIATION",
                                 dest="tailf_deviations",
                                 default=[],
                                 action="append",
                                 help="Deviation modules"),
            optparse.make_option("", "--cs-use-description",
                                 dest="cs_use_description",
                                 action="store_true",
                                 help="Do not ignore 'description' statements"),
            optparse.make_option("", "--cs-always-use-description",
                                 dest="cs_always_use_description",
                                 action="store_true",
                                 help="Use 'description' statements instead of"\
                                     " tailf:info"),
            ]
        if hasattr(optparser, 'tailf_opts'):
            g = optparser.tailf_opts
        else:
            g = optparser.add_option_group("Tail-f specific options")
            optparser.tailf_opts = g
        g.add_options(optlist)
        self.mods = []

    def add_output_format(self, fmts):
        fmts['cs'] = self

    def pre_load_modules(self, ctx):
        # load annotation modules
        for filename in ctx.opts.tailf_annotate:
            try:
                ffd = file(filename)
                text = ffd.read()
            except IOError, ex:
                sys.stderr.write("error %s: %s\n" % (filename, str(ex)))
                sys.exit(1)
            # add the module, but abort the validation early
            # (see tailf.v_pre_import())
            m = ctx.add_module(filename, text)
            if m is not None:
                self.mods.append(m.arg)
                ctx.tailf_ann_mods[filename] = (m, False)

    def pre_validate_ctx(self, ctx, modules):
        if len(modules) == 0:
            return
        module = modules[0]
        self.mods.extend([module.arg] + [i.arg for i in module.search('include')])
        # make sure validate_module continues for the annotation modules
        # (see tailf.v_pre_import())
        ctx.opts.tailf_annotate = []

        # apply deviations
        module.cs_deviations = []
        for filename in ctx.opts.tailf_deviations:
            try:
                ffd = file(filename)
                text = ffd.read()
            except IOError, ex:
                sys.stderr.write("error %s: %s\n" % (filename, str(ex)))
                sys.exit(1)
            if filename in ctx.tailf_ann_mods:
                # the module contains both annotations and deviations;
                # it is already added above
                (m, _validated) = ctx.tailf_ann_mods[filename]
                ctx.tailf_ann_mods[filename] = (m, True)
                statements.validate_module(ctx, m)
            else:
                m = ctx.add_module(filename, text)
            if m is not None:
                self.mods.append(m.arg)
                # mark this as a deviation on our module only if it actually
                # deviates our module.
                for dev in m.search('deviation'):
                    if (dev.i_target_node is not None and
                        dev.i_target_node.i_module.i_modulename == module.arg):
                        module.cs_deviations.append(m.arg)
                        break
        
        # apply annotations
        for f in ctx.tailf_ann_mods:
            (m, validated) = ctx.tailf_ann_mods[f]
            if not validated:
                statements.validate_module(ctx, m)
            # add any imports used by the annotation module
            # to the main module, so that -f yang (etc) works
            for s in m.search('import'):
                if (s.arg != module.arg and
                    module.search_one('import', s.arg) is None):
                    module.substmts.insert(
                        module.substmts.index(module.search_one('prefix'))+1, s)


    def emit(self, ctx, modules, fd):
        module = modules[0]
        # cannot do confspec unless everything is ok for our module
        # and submodules
        for (epos, etag, eargs) in ctx.errors:
            if (error.is_error(error.err_level(etag)) and
                (epos.top.arg in self.mods or
                 etag in ('MODULE_NOT_FOUND', 'MODULE_NOT_FOUND_REV'))):
                fatal("%s contains errors" % epos.top.arg)
        if module.keyword == 'submodule':
            fatal("cannot compile submodules; compile the module instead")
        # we also need to have all other modules found
        for pre in module.i_prefixes:
            (modname, revision) = module.i_prefixes[pre]
            mod = statements.modulename_to_module(module, modname, revision)
            if mod == None:
                fatal("cannot find module %s" % modname)
        # make feature map
        # and make sure all features requested are defined
        if ctx.opts.no_features:
            ctx.tailf_features = []
        elif ctx.opts.tailf_features == []:
            # all features
            ctx.tailf_features = 'ALL'
        else:
            # check that all given features are defined
            for f in ctx.opts.tailf_features:
                if f.find(":") != -1:
                    [prefix,name] = f.split(':', 1)
                else:
                    prefix = None
                    name = f
                if prefix is None or module.i_prefix == prefix:
                    # check local features
                    pmodule = module
                else:
                    # this is a prefixed name, check the imported modules
                    try:
                        (modulename, revision) = module.i_prefixes[prefix]
                        pmodule = statements.modulename_to_module(module,
                                                                  modname,
                                                                  revision)
                    except KeyError:
                        fatal("prefix '%s' from feature '%s' is not found" %
                              (prefix, f))
                if name not in pmodule.i_features:
                    fatal("feature '%s' not defined in %s" % (f, modname))
            ctx.tailf_features = ctx.opts.tailf_features
        emit_cs(ctx, module, fd)
        
def fatal(s):
    raise error.EmitError(s)

def warning(s):
    print >> sys.stderr, "warning: " + s
    
def nyi(s, pos):
    fatal("%s: %s not yet supported by ConfD" % (pos, s))

def make_typedefs(ctx, module, m):
    """Create top-level typedefs for all locally defined types."""
    for c in m.search('typedef'):
        if hasattr(c, 'i_leafref') and c.i_leafref is not None:
            pass
        else:
            c.i_cs_type_name = c.arg

    def gen_typedef_name(name):
        i = 0
        tname = name + '_' + str(i)
        while tname in module.i_all_typedefs:
            i = i + 1
            tname = name + '_' + str(i)
        return tname
            
    def chk_type(stmt, type):
        if type.arg.find(":") != -1:
            # complex case. the other type has a prefix, i.e.
            # is imported. we might not even import that module.
            # we have to add an import in order to cover
            # this case properly
            [prefix, _name] = type.arg.split(':', 1)
            (othermodname, otherrevision) = \
                           type.i_module.i_prefixes[prefix]
            # first, check if we already have the module
            # imported
            ourprefix = util.dictsearch((othermodname,otherrevision),
                                        module.i_prefixes)
            if ourprefix is None:
                newprefix = gen_new_import(ctx, module, othermodname,
                                           otherrevision)
                newmod = ctx.get_module(othermodname, otherrevision)
                newmod.i_cs_prefix = newprefix
        if type.parent.keyword != 'typedef' and type.arg == 'identityref':
            # inline - replace with typedef
            new_type = type.copy()
            base = gen_new_typedef(module, new_type).arg
            type.i_cs_type_name = base
            type.i_is_derived = True
            return
        if (type.i_typedef is not None and
            type.i_typedef.i_module != module and
            type.i_typedef.parent.keyword not in ('module', 'submodule')):
            # inline (non-exported) from another module
            new_type = type.i_typedef.search_one('type').copy()
            new_typedef = gen_new_typedef(module, new_type)
            base = new_typedef.arg
            type.i_cs_type_name = base
            type.i_is_derived = True
            type = new_type
        if type.i_is_derived == False or hasattr(type, 'i_cs_type_name'):
            if ((type.i_typedef is not None and
                 type.i_typedef.i_module.arg == xsd_mod and
                 type.i_typedef.arg == 'decimal' and
                 type.parent.keyword != 'typedef' and
                 ((type.search_one((xsd_mod, 'fraction-digits')) is not None) or
                  (type.search_one((xsd_mod, 'total-digits')) is not None))) or
                (type.parent.keyword != 'typedef' and
                 (type.parent.search_one((tailf, 'suppress-echo')) is not None or
                  type.parent.search_one((tailf, 'typepoint')) is not None))):
                # inline - replace with typedef
                new_type = type.copy()
                base = gen_new_typedef(module, new_type).arg
                type.i_cs_type_name = base
                type.i_is_derived = True
            # no restrictions or already handled; no need to change this
            if type.search_one((tailf, 'info')) is not None:
                # inline - replace with typedef
                if (hasattr(stmt, 'i_leafref_ptr') and
                    stmt.i_leafref_ptr is not None):
                    (ref, _pos) = stmt.i_leafref_ptr
                    new_type = ref.search_one('type').copy()
                    # if the referenced type had a tailf:info; replace it
                    # with the new tailf:info
                    old_info = new_type.search_one((tailf, 'info'))
                    if old_info is not None:
                        idx = new_type.substmts.index(old_info)
                        del new_type.substmts[idx]
                    new_type.substmts.append(type.search_one((tailf, 'info')))
                else:
                    if hasattr(type, 'i_cs_type_name'):
                        # An inline type with i_cs_type_name set means we've
                        # already created a typedef for it, and with no further
                        # restrictions added, no need for an extra type.
                        return
                    new_type = type.copy()
                base = gen_new_typedef(module, new_type).arg
                type.i_cs_type_name = base
            return
        if ((type.search_one('fraction-digits') is not None) and 
            (type.search_one('range') is not None)):
            # confdc cannot handle fraction-digits and ranges in the same type
            # construct a dummy base type with only the fraction-digits
            new_type = type.copy()
            # remove range and generate the base type with fraction-digits
            # restriction
            range_stmt = new_type.search_one('range')
            new_type.substmts.remove(range_stmt)
            new_type.i_ranges = []
            typedef = gen_new_typedef(module, new_type)
            base = typedef.arg
            # prevent generation of extra typedef 
            new_type.parent = typedef
            chk_type(typedef, new_type)

            # remove fraction-digits and keep the range restriction
            p = type.search_one('fraction-digits')
            type.substmts.remove(p)

            type.arg = base
            type.i_cs_type_name = base
            
        if len(type.search('enum')) > 0:
            # enum
            if type.parent.keyword != 'typedef':
                # inline - replace with typedef
                new_type = type.copy()
                base = gen_new_typedef(module, new_type).arg
                type.i_cs_type_name = base
        elif len(type.search('type')) > 0:
            # union - replace each member type with typedef
            new_type = type.copy()
            for t in new_type.search('type'):
                chk_type(stmt, t)
            base = gen_new_typedef(module, new_type).arg
            type.i_cs_type_name = base
        elif type.parent.keyword != 'typedef' and type.arg != 'leafref':
            # inline - replace with typedef
            new_type = type.copy()
            base = gen_new_typedef(module, new_type).arg
            type.i_cs_type_name = base
        elif type.search_one((tailf, 'info')) is not None:
            # inline - replace with typedef
            if hasattr(stmt, 'i_leafref_ptr') and stmt.i_leafref_ptr is not None:
                (ref, _pos) = stmt.i_leafref_ptr
                new_type = ref.search_one('type').copy()
                # if the referenced type had a tailf:info; replace it
                # with the new tailf:ifno
                old_info = new_type.search_one((tailf, 'info'))
                if old_info is not None:
                    idx = new_type.substmts.index(old_info)
                    del new_type.substmts[idx]
                new_type.substmts.append(type.search_one((tailf, 'info')))
            else:
                new_type = type.copy()
            base = gen_new_typedef(module, new_type).arg
            type.i_cs_type_name = base

    def add_typedef(obj):
        for t in obj.search('typedef'):
            # put locally defined typedefs on top-level
            t.i_cs_type_name = gen_typedef_name(t.arg)
            module.i_local_typedefs.append(t)
            module.i_all_typedefs[t.i_cs_type_name] = t
            type = t.search_one('type')
            chk_type(obj, type)
        if obj.keyword in ('leaf', 'leaf-list'):
            chk_type(obj, obj.search_one('type'))
        for c in obj.search('grouping'):
            add_typedef(c)
        if hasattr(obj, 'i_children'):
            for c in obj.i_children:
                add_typedef(c)

    for c in m.search('typedef'):
        if hasattr(c, 'i_leafref') and c.i_leafref is not None:
            pass
        else:
            chk_type(c, c.search_one('type'))
    for c in (m.i_children +
              m.search('augment') +
              m.search('grouping')):
        add_typedef(c)

def emit_cs(ctx, module, fd):
    module.i_cs_namespace = module.search_one('namespace').arg
    module.i_cs_prefix = module.search_one('prefix').arg

    ctx.i_cs_top_module = module

    # could be used for a tailf:enum-hash extension
    # (see also undocumented confdc --yang-enum-hash)
    ctx.i_no_enum_id = False

    # initialize some CS specific variables
    module.i_gen_typedef = []
    module.i_gen_import = []
    module.i_gen_augment_idx = 0
    module.i_local_typedefs = []
    module.i_all_typedefs = {}
    module.i_gen_typedef_n = 0
    module.i_unique_tags_n = 0

    mods = [module]
    for i in module.search('include'):
        subm = ctx.get_module(i.arg)
        if subm is not None:
            mods.append(subm)
            # make sure the top module imports all modules imported by the
            # submodule
            for subimp in subm.search('import'):
                p = subimp.search_one('prefix').arg
                (othermodname, otherrevision) = subm.i_prefixes[p]
                ourprefix = util.dictsearch((othermodname,otherrevision),
                                            module.i_prefixes)
                if ourprefix is None:
                    # we don't have a prefix for this module
                    newprefix = gen_new_import(ctx, module, othermodname,
                                               otherrevision)
                    newmod = ctx.get_module(othermodname, otherrevision)
                    newmod.i_cs_prefix = newprefix

    # make sure we "import" all modules imported by our modules,
    # recursively.  the reason for this is that we might generate a
    # type reference for leafreafs, and for this the module must be
    # imported.  unused imports are ignored by confdc.
    handled = []
    def add_import(othermodname, otherrevision):
        if (othermodname, otherrevision) not in handled:
            # new module
            handled.append((othermodname, otherrevision))
            ourprefix = util.dictsearch((othermodname,otherrevision),
                                        module.i_prefixes)
            if ourprefix is None:
                # we don't have a prefix for this module
                newprefix = gen_new_import(ctx, module, othermodname,
                                           otherrevision)
                newmod = ctx.get_module(othermodname, otherrevision)
                newmod.i_cs_prefix = newprefix
            else:
                newmod = ctx.get_module(othermodname, otherrevision)

            for i in newmod.search('include'):
                subm = ctx.get_module(i.arg)
                if subm is not None:
                    for imp in subm.search('import'):
                        r = imp.search_one('revision-date')
                        if r is not None:
                            r = r.arg
                        add_import(imp.arg, r)

            for imp in newmod.search('import'):
                r = imp.search_one('revision-date')
                if r is not None:
                    r = r.arg
                add_import(imp.arg, r)
    for imp in module.search('import'):
        r = imp.search_one('revision-date')
        if r is not None:
            r = r.arg
        add_import(imp.arg, r)


    for m in mods:
        for t in m.search('typedef'):
            module.i_all_typedefs[t.arg] = t

    # create unique tags
    def set_i_unique_tag(s):
        for u in s.search('unique'):
            # skip unique for keys
            if (hasattr(u, 'i_leafs') and
                hasattr(u.parent, 'i_key') and
                set(u.i_leafs) == set(u.parent.i_key)):
                pass
            else:
                i = module.i_unique_tags_n
                u.i_unique_tag = "u" + str(i)
                module.i_unique_tags_n = i + 1
        return

    statements.iterate_i_children(module, set_i_unique_tag)
    for a in module.search('augment'):
        statements.iterate_i_children(a, set_i_unique_tag)

    # first, create top-level typedefs of local typedefs
    for m in mods:
        make_typedefs(ctx, module, m)

    prefixes = [module.i_cs_prefix] + [p for p in module.i_prefixes]
    if module.i_cs_prefix in ['xs', 'yin', 'nc', 'ncn']:
        i = 0
        pre = "p" + str(i)
        while pre in prefixes:
            i = i + 1
            pre = "p" + str(i)
        prefixes.append(pre)
        module.i_cs_prefix = pre

    idstr = '          id="%s"\n' % module.i_cs_namespace
    idstmt = module.search_one((tailf, 'id'))
    if idstmt is not None:
        if idstmt.arg == "":
            idstr = ""
        else:
            idstr = '          id="%s"\n' % idstmt.arg
        
    fd.write('<?xml version="1.0" encoding="UTF-8"?>\n')
    fd.write('\n<!-- Generated from %s by pyang -f cs -->\n\n' % module.pos.ref)
    fd.write('<confspec xmlns="http://tail-f.com/ns/confspec/1.0"\n')
    fd.write('          xmlns:confd="http://tail-f.com/ns/confd/1.0"\n')
    fd.write('          xmlns:confspec="http://tail-f.com/ns/confspec/1.0"\n')
    fd.write('          xmlns:xs="http://www.w3.org/2001/XMLSchema"\n')
    fd.write('          xmlns:yang="urn:ietf:params:xml:ns:yang:ietf-yang-types"\n')
    fd.write('          targetNamespace="%s"\n' % module.i_cs_namespace)
    fd.write('          prefix="%s"\n' % module.i_cs_prefix)
    fd.write(idstr)
    fd.write('          allowEnumConflicts="true"\n')
    fd.write('          yangModuleName="%s"' % module.arg)
    if len(module.i_features) > 0:
        fd.write('\n          yangFeatures="%s"' %  \
                     " ".join([f for f in module.i_features \
                                   if use_feature(f, ctx)]))
    if module.search_one('deviation') is not None:
        fd.write('\n          yangHasDeviations="true"')
    if len(module.cs_deviations) > 0:
        fd.write('\n          yangDeviations="%s"' %  \
                     " ".join([f for f in module.cs_deviations]))
    rev = get_latest_revision(module)
    if rev is not None:
        fd.write('\n          version="%s"' % rev)
        fd.write('\n          yangRevision="%s"' % rev)
    handled_modules = []
    for m in mods:
        for pre in m.i_prefixes:
            (modname, revision) = m.i_prefixes[pre]
            mod = statements.modulename_to_module(m, modname, revision)
            if mod.keyword == 'submodule' or mod in handled_modules:
                continue
            handled_modules.append(mod)
            if pre in ['xs', 'yin', 'nc', 'ncn']:
                # someone uses one of our prefixes
                # generate a new prefix for that module
                i = 0
                pre = "p" + str(i)
                while pre in prefixes:
                    i = i + 1
                    pre = "p" + str(i)
                prefixes.append(pre)
            mod.i_cs_prefix = pre
            if mod == module:
                uri = mod.i_cs_namespace
            else:
                uri = mod.search_one('namespace').arg
            if mod.arg not in [tailf, xsd_mod, yang_types]:
                fd.write('\n          xmlns:' + pre + '=' + quoteattr(uri))
    x = module.search_one((tailf, 'snmp-oid'))
    if x is None:
        x = module.search_one((smiv2, 'oid'))
    if x is not None:
        fd.write('\n          snmpOID="%s"' % x.arg)
    x = module.search_one((tailf, 'snmp-mib-module-name'))
    if x is not None:
        fd.write('\n          snmpMIBModule="%s"' % x.arg)

    fd.write('>\n\n')

    print_desc(fd, '  ', get_descr(module, ctx, True))

    # print typedefs
    got_typedefs = False
    for m in mods:
        if m.search_one('typedef') is not None:
            got_typedefs = True
    if got_typedefs:
        fd.write(istr + '<!-- YANG typedefs -->\n\n')
        for m in mods:
            for c in m.search('typedef'):
                desc = get_descr(c, ctx)
                if hasattr(c, 'i_cs_type_name'):
                    print_simple_type(ctx, fd, istr, c.search_one('type'),
                                      ' name="%s"' % c.i_cs_type_name,
                                      desc)
                    fd.write('\n')

    # print identities
    if len(module.i_identities) > 0:
        fd.write(istr + '<!-- YANG identities -->\n\n')
        for iname in module.i_identities:
            i = module.i_identities[iname]
            attrs = ""
            if i.search_one('base'):
                attrs += ' base="%s"' % i.search_one('base').arg
            x = i.search_one((tailf, 'id-value'))
            if x is not None:
                attrs += ' idValue="%s"' % x.arg
            fd.write(istr + '<identity name="%s"%s' % (iname, attrs))
            c = get_descr(i, ctx)
            if c is not None:
                fd.write('>\n')
                fd.write(istr*2 + '<desc%s>%s</desc>\n' % c)
                fd.write(istr + '</identity>\n')
            else:
                fd.write('/>\n')
            
        fd.write('\n')

    # print locally defined typedefs
    if len(module.i_local_typedefs) > 0:
        fd.write(istr + '<!-- local YANG typedefs -->\n\n')
    for c in module.i_local_typedefs:
        print_simple_type(ctx, fd, istr, c.search_one('type'),
                          ' name="%s"' % c.i_cs_type_name,
                          get_descr(c, ctx))
        fd.write('\n')

    # check for augments
    for m in mods:
        augment = [a for a in m.search('augment') \
                   if (a.i_target_node.i_module.i_modulename !=
                       module.i_modulename)]
        for a in augment:
            astr =  translate_prefixes(a.arg,
                                       a.i_orig_module.i_prefixes,
                                       a.i_orig_module.i_prefix,
                                       a.i_orig_module,
                                       ctx.i_cs_top_module.i_prefixes,
                                       ctx.i_cs_top_module.i_prefix,
                                       ctx.i_cs_top_module.arg,
                                       no_default = True)
            fd.write(istr + '<augment targetNode="%s">\n' % astr)
            print_children(ctx, fd, a.i_children, istr*2)
            fd.write(istr + '</augment>\n\n')
    
    # print data definitions
    print_children(ctx, fd, module.i_children, istr)

    # then print all generated 'dummy' simpleTypes, if any
    if len(module.i_gen_typedef) > 0:
        fd.write('\n  <!-- locally generated simpleType helpers -->\n\n')
    for c in module.i_gen_typedef:
        print_simple_type(ctx, fd, istr, c.search_one('type'),
                          ' name="%s" isGeneratedByPyang="true"' % c.arg, None)
        fd.write('\n')

    fd.write('</confspec>\n')

def get_latest_revision(module):
    revs = [r.arg for r in module.search('revision')]
    revs.sort()
    if len(revs) > 0:
        return revs[-1]
    else:
        return None

def gen_new_typedef(module, new_type):
    i = module.i_gen_typedef_n
    name = "t" + str(i)
    while name in module.i_all_typedefs:
        i = i + 1
        name = "t" + str(i)
    module.i_gen_typedef_n = i + 1
    typedef = statements.Statement(module, module, new_type.pos,
                                   'typedef', name)
    typedef.i_cs_type_name = name
    typedef.substmts.append(new_type)
    module.i_gen_typedef.append(typedef)
    module.i_all_typedefs[name] = typedef
    return typedef

def gen_new_import(ctx, module, modname, revision):
    i = 0
    m = ctx.get_module(modname, revision)
    if m is not None and m.i_prefix not in module.i_prefixes:
        pre = m.i_prefix
    else:
        pre = "p" + str(i)
        while pre in module.i_prefixes:
            i = i + 1
            pre = "p" + str(i)
    module.i_prefixes[pre] = (modname, revision)
    imp = statements.Statement(module, module, None, 'import', modname)
    if revision is not None:
        rev = statements.Statement(module, imp, None, 'revision-date',
                                   revision)
        imp.substmts.append(rev)
    module.i_gen_import.append(imp)
    return pre

def print_simple_type(ctx, fd, indent, type, attrstr, descr):
    """copy & paste from xsd.py, with confspec modifications:
          - confspec has a bitsType
          - confspec does not handle inline simpleTypes
    """

    bits = type.search('bit')
    maxpos = 0
    for bit in bits:
        if bit.i_position > maxpos:
            maxpos = bit.i_position
    if bits != []:
        if maxpos < 32:
            szstr = ' size="32"'
        elif maxpos < 64:
            szstr = ' size="64"'
        else:
            fatal("cannot convert bit sets with more than 64 bits: " + 
                  str(type.pos))
        fd.write(indent + '<bitsType%s%s>\n' % (attrstr, szstr))
        print_desc(fd, indent + istr, descr)
        for bit in type.search('bit'):
            fd.write(indent + istr + '<field label="%s" bit="%s"/>\n' % \
                         (bit.arg, bit.i_position))
        fd.write(indent + '</bitsType>\n')
        return
    fd.write(indent + '<xs:simpleType%s>\n' % attrstr)
    suppress_echo = type.parent.search_one((tailf, 'suppress-echo'))
    if descr is None:
        descr = get_descr(type, ctx)
    if descr is None:
        x = type.search_one('range')
        if x is not None:
            descr = get_descr(x, ctx)
    if descr is None:
        x = type.search_one('length')
        if x is not None:
            descr = get_descr(x, ctx)
    if descr is None:
        x = type.search_one((tailf, 'value-length'))
        if x is not None:
            descr = get_descr(x, ctx)
    if descr is None:
        for x in type.search('pattern'):
            d = get_descr(x, ctx)
            if d is not None:
                descr = d
    if descr is not None or suppress_echo is not None:
        fd.write(indent + istr + '<xs:annotation>\n')
        if descr is not None:
            (astr, dstr) = descr
            fd.write(indent + istr*2 + '<xs:documentation>')
            fd.write(dstr)
            fd.write('</xs:documentation>\n')
        if suppress_echo is not None:
            fd.write(indent + istr*2 +
                     '<xs:appinfo confspec:suppressEcho="%s"/>\n' % \
                         suppress_echo.arg)
        fd.write(indent + istr + '</xs:annotation>\n')
    typepoint = cs_typepoint(type.parent, indent + istr)
    fd.write(typepoint)
    if hasattr(type, 'i_cs_type_name'):
        base = type.i_cs_type_name
    elif type.search('enum') != []:
        base = 'xs:string'
    elif ((type.i_typedef is not None) and (":" not in type.arg)):
        base = type.i_typedef.i_cs_type_name
    else:
        base = cs_type_name(ctx, type)
    if typepoint != '':
        pass
    elif hasattr(type, 'i_cs_no_restrictions'):
        fd.write(indent + istr + '<xs:restriction base="%s"/>\n' % base)
    elif type.search('type') != [] and not hasattr(type,'i_cs_type_name'):
        fd.write(indent + istr + '<xs:union memberTypes="')
        for t in type.search('type'):
            if hasattr(t, 'i_cs_type_name'):
                member = t.i_cs_type_name
            elif t.i_typedef is not None and ":" not in t.arg:
                member = t.i_typedef.i_cs_type_name
            else:
                member = cs_type_name(ctx, t)
            fd.write(member + " ")
        fd.write('"/>\n')
    else:
        idbase = type.search_one('base')
        if idbase is not None:
            if ":" not in idbase.arg:
                b = "%s:%s" % (ctx.i_cs_top_module.i_prefix, idbase.arg)
            else:
                b = idbase.arg
            idbasestr = " confd:identity-base='%s'" % b
        else:
            idbasestr = ""

        dhstr = None
        tfdh = type.parent.search_one((tailf, 'display-hint'))
        dh = type.parent.search_one((smiv2, 'display-hint'))
        if tfdh is not None:
            base = 'xs:base64Binary'
            dhstr = '<xs:displayHint value=%s isTailf="true"/>\n' % \
                quoteattr(tfdh.arg)
        elif dh is not None and type.arg == 'string':
            # If the DISPLAY-HINT tells us that this is plain ascii or utf8,
            # then keep 'string' as internal ConfD type.
            # Otherwise, use 'binary' as internal ConfD type, with
            # the display hint as a facet
            if re_string_dh.match(dh.arg) is None:
                # this is NOT a pure string
                base = 'xs:base64Binary'
                dhstr = '<xs:displayHint value=%s/>\n' % quoteattr(dh.arg)

        fd.write(indent + istr + '<xs:restriction base="%s"%s>\n' % \
                 (base, idbasestr))

        if dhstr is not None:
            fd.write(indent + istr*2 + dhstr)

        if type.search('pattern') != []:
            def print_pattern(indent, str, errmsg, errapptag):
                fd.write(indent + istr + '<xs:pattern value=')
#                str2 = re.sub(r'\\p{IsBasicLatin}', r'[\x00-\x7F]', str)
#                qstr = quoteattr(str2)
                qstr = quoteattr(str)
                fd.write(qstr)
                if errmsg is None and errapptag is None:
                    fd.write('/>\n')
                else:
                    fd.write('>\n')
                    fd.write(fmt_err_info(indent + istr*2, errmsg, errapptag))
                    fd.write(indent + istr + '</xs:pattern>\n')
            
            for p in type.search('pattern'):
                patstr = p.arg
                errmsg = p.search_one('error-message')
                errapptag = p.search_one('error-app-tag')
                print_pattern(indent + istr, patstr, errmsg, errapptag)

        if len(type.search('enum')) > 0:
            for e in type.search('enum'):
                idvalue =  e.i_value
                if e.search_one((tailf, 'id-value')) is not None:
                    idvalue = e.search_one((tailf, 'id-value')).arg
                descr = get_descr(e, ctx)
                attrs = ' value=%s' % quoteattr(e.arg)
                if not ctx.i_no_enum_id:
                    attrs += ' idValue="%s"' % idvalue
                x = e.search_one((tailf, 'code-name'))
                if x is not None:
                    attrs += ' codeName="%s"' % x.arg
                fd.write(indent + istr*2 + '<xs:enumeration' + attrs)
                if descr is not None:
                    (astr, dstr) = descr
                    fd.write('>\n')
                    fd.write(indent + istr*3 + '<xs:annotation>\n')
                    fd.write(indent + istr*4 + '<xs:documentation>')
                    fd.write(dstr)
                    fd.write('</xs:documentation>\n')
                    fd.write(indent + istr*3 + '</xs:annotation>\n')
                    fd.write(indent + istr*2 + '</xs:enumeration>\n')
                else:
                    fd.write('/>\n')

        elif ((type.search_one('length') is not None) or
              (type.search_one((tailf, 'value-length')) is not None)):
            # other cases in union above
            ignore = False
            l = type.search_one('length')
            if type.i_typedef is not None:
                type_name = (type.i_typedef.i_module.arg, type.i_typedef.arg)
                if type_name in tailf_plugin.tailf_value_length_types:
                    l = type.search_one((tailf, 'value-length'))
                    if l is None:
                        # it is a value-length type, but it doesn't have a
                        # tailf:value-length restriction - ignore the length
                        # restriction.
                        ignore = True
            if ignore:
                pass
            else: 
                if l is not None:
                    errmsg = l.search_one('error-message')
                    errapptag = l.search_one('error-app-tag')
                fd.write(indent + istr*2 + '<xs:length value=')
                sep = '"'
                for (lo,hi) in type.i_lengths:
                    if hi is None:
                        hi = lo
                    if lo == hi:
                        # FIXME write actual min/max value instead of skipping
                        if lo not in ['min','max']:
                            fd.write('%s%s' % (sep, lo))
                            sep = ' | '
                    else:
                        fd.write('%s%s .. %s' % (sep, lo, hi))
                        sep = ' | '
                fd.write('">\n')
                fd.write(fmt_err_info(indent + istr*3, errmsg, errapptag))
                fd.write(indent + istr*2 + '</xs:length>\n')
        elif type.search_one('range') is not None:
            r = type.search_one('range')
            errmsg = r.search_one('error-message')
            errapptag = r.search_one('error-app-tag')
            fd.write(indent + istr*2 + '<xs:range value=')
            sep = '"'
            for (lo,hi) in type.i_ranges:
                if hi is None:
                    hi = lo
                if lo == hi:
                    # FIXME write actual min/max value instead of skipping
                    if lo not in ['min','max']:
                        fd.write('%s%s' % (sep, lo))
                        sep = ' | '
                else:
                    fd.write('%s%s .. %s' % (sep, lo, hi))
                    sep = ' | '
            fd.write('"')
            step = r.search_one((tailf, 'step'))
            if step is not None:
                fd.write(' step="%s"' % step.arg)
            fd.write('>\n')
            fd.write(fmt_err_info(indent + istr*3, errmsg, errapptag))
            fd.write(indent + istr*2 + '</xs:range>\n')
        fraction_digits = type.search_one('fraction-digits')
        if fraction_digits is not None:
            fd.write(indent + istr*2 +
                     '<xs:fractionDigits value="%s"/>\n' % fraction_digits.arg)
        if (type.i_typedef is not None and
            type.i_typedef.i_module.arg == xsd_mod and
            type.i_typedef.arg == 'decimal' and
            type.parent.keyword != 'typedef' and
            ((type.search_one((xsd_mod, 'fraction-digits')) is not None) or
             (type.search_one((xsd_mod, 'total-digits')) is not None))):
            f = type.search_one((xsd_mod, 'fraction-digits'))
            if f is not None:
                fd.write(indent + istr*2 +
                         '<xs:fractionDigits value="%s"/>\n' % f.arg)
            f = type.search_one((xsd_mod, 'total-digits'))
            if f is not None:
                fd.write(indent + istr*2 +
                         '<xs:totalDigits value="%s"/>\n' % f.arg)
                
        fd.write(indent + istr + '</xs:restriction>\n')
    fd.write(indent + '</xs:simpleType>\n')

def fmt_err_info(indent, errmsg, errapptag, errapptagstr=None):
    r = ''
    if errmsg is not None:
        r += indent + '<errorMessage>' + \
             escape(errmsg.arg) + '</errorMessage>\n'
    if errapptag is not None:
        r += indent + '<errorAppTag>' + \
            escape(errapptag.arg) + '</errorAppTag>\n'
    elif errapptagstr is not None:
        r += indent + '<errorAppTag>' + \
            escape(errapptagstr) + '</errorAppTag>\n'
    return r
        
def print_children(ctx, fd, children, indent,
                   nokey=False, expand_leafref=False, allowanyxml=False):
    for c in children:
        c.cs_is_key = False
        indexed_view = False
        doit = True
        for f in c.search('if-feature'):
            if not use_feature(f.arg, ctx):
                doit = False
        if hasattr(c, 'i_uses'):
            for u in c.i_uses:
                for f in u.search('if-feature'):
                    if not use_feature(f.arg, ctx):
                        doit = False
        if not doit:
            continue
        
        cn = c.keyword
        if cn == 'notification':
            fd.write('\n' + indent + '<notification name="%s">\n' % c.arg)
            print_children(ctx, fd, c.i_children, indent + istr,
                           nokey=True, expand_leafref=True, allowanyxml=True)
            fd.write(indent + '</notification>\n')
        elif cn == 'rpc':
            print_actions(ctx, fd, indent, [c], is_rpc=True)
        elif cn == (tailf, 'error-info'):
            fd.write('\n' + indent + '<errorInfo>\n')
            print_children(ctx, fd, c.i_children, indent + istr,
                           nokey=True, expand_leafref=True)
            fd.write(indent + '</errorInfo>\n')
        elif cn in ['container', 'list', 'leaf', 'leaf-list',
                    (tailf, 'symlink')]:
            attrs = ""
            extra = ""
            snmp_extra = ""
            must = ""
            when = ""
            extbase = None
            if cn in ['leaf']:
                if ((c.parent.keyword == 'list') and
                    (c in c.parent.i_key) and
                    not nokey):
                    attrs += ' key="true"'
                    if c.parent.search_one((tailf, 'indexed-view')):
                        indexed_view = True
                    c.cs_is_key = True
                if ((c.cs_is_key == False) and
                    (c.i_default is None) and
                    (c.search_one('mandatory') is None or
                     c.search_one('mandatory').arg != 'true')):
                    attrs += ' minOccurs="0"'
                if len(c.i_uniques) > 1:
                    fatal(str(c.pos) + " confd does not support more than one " \
                          "unique tag per leaf")
                for u in c.i_uniques:
                    if hasattr(u, 'i_unique_tag'):
                        attrs += ' unique="%s"' % u.i_unique_tag
            elif cn in [(tailf, 'symlink')]:
                path = c.search_one((tailf, 'path'))
                pstr = translate_prefixes(path.arg,
                                          path.i_orig_module.i_prefixes,
                                          path.i_orig_module.i_prefix,
                                          path.i_orig_module,
                                          ctx.i_cs_top_module.i_prefixes,
                                          ctx.i_cs_top_module.i_prefix,
                                          ctx.i_cs_top_module.arg,
                                          no_default = True)
                attrs += ' symlink=%s' % quoteattr(pstr)
                xx = c.search_one((tailf, 'inherit-set-hook'))
                if xx is not None:
                    attrs += ' inheritSetHook="%s"' % xx.arg
            elif cn in ['container']:
                attrs += ' isContainer="true"'
                if c.search_one('presence') is not None:
                    attrs += ' minOccurs="0"'
            elif cn in ['list', 'leaf-list']:
                x = c.search_one('min-elements')
                if x is not None:
                    attrs += ' minOccurs="%s"' % x.arg
                else:
                    attrs += ' minOccurs="0"'
                x = c.search_one('max-elements')
                if x is not None:
                    attrs += ' maxOccurs="%s"' % x.arg
                else:
                    attrs += ' maxOccurs="unbounded"'
                x = c.search_one('ordered-by')
                if x is not None:
                    if x.arg == 'user' and not nokey:
                        attrs += ' orderedBy="%s"' % x.arg
            if cn == 'leaf-list':
                attrs += ' isLeafList="true"'
            if cn in ['leaf', 'leaf-list']:
                x = c.search_one((tailf, 'default-ref'))
                if x is None:
                    type_ = c.search_one('type')
                    (cattrs, must) = cs_type_attrs(ctx, c, type_, indent + istr,
                                                   expand_leafref)
                    attrs += cattrs
                elif c.cs_is_key == False:
                    attrs += ' defaultRef="%s"' % x.arg
            if cn == 'leaf' and c.cs_is_key is False and c.i_default is not None:
                if type(c.i_default) == type(1):
                    attrs += ' default="%d"' % c.i_default
                else:
                    attrs += ' default=%s' % quoteattr(c.i_default_str)
                    
            callpoint = cs_callpoint(ctx, c, indent + istr)
            validate = cs_validate(ctx, c, indent + istr)
            secondary_index, ddo = cs_secondary_index(c, indent + istr)
            display_when = cs_display_when(c, indent + istr)
            if c.i_config == True:
                must += cs_must(ctx, c, indent + istr)
                when += cs_when(ctx, c, indent + istr)
            x = c.search_one('config')
            if x is not None:
                attrs += ' config="%s"' % x.arg
            x = c.search_one('status')
            if x is not None:
                attrs += ' status="%s"' % x.arg
            x = c.search_one((tailf, 'sort-order'))
            if x is not None:
                attrs += ' sortOrder="%s"' % conv_sort_order(x.arg)
            elif cn == 'list' and c.search_one((smiv2, 'oid')) is not None:
                # the list is an SNMP table
                x = c.search_one((smiv2, 'implied'))
                if x is not None:
                    attrs += ' sortOrder="snmp_implied"'
                else:
                    attrs += ' sortOrder="snmp"'
            x = c.search_one((tailf, 'id-value'))
            if x is not None:
                attrs += ' idValue="%s"' % x.arg
            x = c.search((tailf, 'hidden'))
            if len(x) > 0:
                attrs += ' hidden="%s"' % '|'.join([y.arg for y in x])

            attrs += flag_attrs(c)

            # this one is special - need to set the old attribute,
            # but we'll also set the generic cli_flags flag.
            x = c.search_one((tailf, 'cli-show-config'))
            if x is not None:
                attrs += ' cliCShowConfig="true"'
                
            c_attrs, c_extra = cli_attrs(c)
            attrs += c_attrs
            extra += c_extra

            x = c.search_one((tailf, 'display-groups'))
            if x is not None:
                attrs += ' displayGroups="%s"' % x.arg
            if ddo is not None:
                attrs += ' displayDefaultOrder="%s"' % ddo
            x = c.search_one((tailf, 'alt-name'))
            if x is not None:
                attrs += ' cliName="%s"' % x.arg
            x = c.search_one((tailf, 'display-status-name'))
            if x is not None:
                attrs += ' printName="%s"' % x.arg
            x = c.search_one((tailf, 'display-column-name'))
            if x is not None:
                attrs += ' columnName="%s"' % x.arg
            x = c.search_one((tailf, 'cli-instance-info-leafs'))
            if x is not None:
                attrs += ' instanceInfoElems="%s"' % x.arg
            x = c.search_one((tailf, 'java-class-name'))
            if x is not None:
                attrs += ' generatedName="%s"' % x.arg
            x = c.search_one((tailf, 'snmp-oid'))
            if x is not None:
                attrs += ' snmpOID="%s"' % x.arg
            elif hasattr(c, 'i_smi_oid'):
                # the tailf:snmp-oid and smiv2:oid works differently
                # in one case.  on the list, tailf:snmp-oid is of
                # the table, but the smiv2:oid is of the entry
                # also, in tailf, there is no explicit oid on the
                # surrounding container
                if (cn == 'container' and
                    len(c.i_children) == 1 and
                    c.i_children[0].keyword == 'list'):
                    # surrounding container, don't add snmpOID
                    pass
                else:
                    if cn == 'list':
                        # strip last oid part
                        poid = c.i_smi_oid[:-1]
                    else:
                        poid = c.i_smi_oid
                    oid = '.'.join([str(subid) for subid in poid])
                    attrs += ' snmpOID="%s"' % oid
            else:
                x = c.search_one((tailf, 'snmp-exclude-object'))
                if x is not None:
                    attrs += ' snmpOID="_NONE_"'
            x = c.search_one((tailf, 'snmp-mib-module-name'))
            if x is not None:
                attrs += ' snmpMIBModule="%s"' % x.arg
            x = c.search((tailf, 'snmp-name'))
            if len(x) > 0:
                attrs += ' snmpId="%s"' % ",".join([z.arg for z in x])
            x = c.search_one((tailf, 'snmp-row-status-column'))
            if x is not None:
                attrs += ' snmpRowStatusColumn="%s"' % x.arg
            elif cn == 'list' and c.search_one((smiv2, 'oid')) is not None:
                # this is an SNMP list; see if it has a RowStatus column 
                for ch in c.i_children:
                    x = ch.search_one('type')
                    if hasattr(x, 'i_typedef') and x.i_typedef is not None:
                        if (x.i_typedef.i_module.i_modulename == 'SNMPv2-TC'
                            and x.i_typedef.arg == 'RowStatus'
                            and hasattr(ch, 'i_smi_oid')):
                            c.i_tailf_rowstatus_col = ch.i_smi_oid[-1]
                            attrs += ' snmpRowStatusColumn="%d"' % \
                                     c.i_tailf_rowstatus_col
            
            x = c.search_one((tailf, 'sort-priority'))
            if x is not None:
                attrs += ' sortPriority="%s"' % x.arg
            x = c.search_one((tailf, 'writable'))
            if x is not None:
                attrs += ' writable="%s"' % x.arg
            x = c.search_one((tailf, 'key-default'))
            if x is not None:
                attrs += ' keyDefault=%s' % quoteattr(x.arg)
            x = c.search_one((tailf, 'snmp-lax-type-check'))
            if x is not None:
                attrs += ' snmpLaxTypeCheck=%s' % quoteattr(x.arg)
            x = c.search_one((tailf, 'snmp-delete-value'))
            if x is not None and x.i_tailf_val is not None:
                if type(x.i_tailf_val) == type(0):
                    valstr = '%s' % x.i_tailf_val
                else:
                    valstr = '"%s"' % x.i_tailf_val
                extra += ',{snmp_delete_val, %s}' % valstr
            x = c.search_one((tailf, 'snmp-ned-modification-dependent'))
            if (x is not None and hasattr(x, 'i_tailf_set_before_leaf') and 
                x.i_tailf_set_before_leaf is not None):
                refleaf = x.i_tailf_set_before_leaf.parent
                oid = refleaf.search_one((tailf, 'snmp-oid'))
                if oid is None:
                    oid = refleaf.search_one((smiv2, 'oid'))
                val = x.i_tailf_set_before_leaf.i_tailf_val
                if oid is not None and val is not None:
                    snmp_extra += ',{dep, {%s, %d, %s}}' % \
                        (eoid(oid.arg), x.i_tailf_set_before_nkeys, val)
            x = c.search_one((tailf, 'snmp-ned-accessible-column'))
            if x is not None and hasattr(x, 'i_tailf_accessible_column'):
                snmp_extra += ',{accessible_col, %d}' % \
                    x.i_tailf_accessible_column
            elif cn == 'list' and c.search_one((smiv2, 'oid')) is not None:
                # this is an SNMP list.
                # if there is no RowStatus column, and there is an
                # index that is accessible, then auto-generate an
                # accessible_col.
                col = None
                x = c.search_one((tailf, 'snmp-row-status-column'))
                if x is None:
                    for ch in c.i_children:
                        if (col == None and
                            hasattr(ch, 'i_is_key') and
                            hasattr(ch, 'i_smi_oid')):
                            # this is a key which has an oid
                            x = ch.search_one((smiv2, 'max-access'))
                            if x is not None and x.arg != 'not-accessible':
                                col = ch.i_smi_oid[-1]
                        x = ch.search_one('type')
                        if hasattr(x, 'i_typedef') and x.i_typedef is not None:
                            if (x.i_typedef.i_module.i_modulename == 'SNMPv2-TC'
                                and x.i_typedef.arg == 'RowStatus'):
                                col = None
                if col is not None:
                    snmp_extra += ',{accessible_col, %d}' % col

            x = c.search_one((tailf, 'ncs-device-type'))
            if x is not None:
                extra += ',{ncs_device_type, \'%s\'}' % x.arg
                
            if cn in ['container', 'list']:
                # find all leafrefs that have refrences to some other leafref
                # keep the ones that do and to which noone refers in root_nodes
                root_nodes = []
                seen = []
                for ch in c.i_children:
                    if hasattr(ch, 'i_derefed_leaf'):
                        if ch.i_derefed_leaf in root_nodes:
                            root_nodes.remove(ch.i_derefed_leaf)
                        seen.append(ch.i_derefed_leaf)
                        if ch not in seen:
                            root_nodes.append(ch)
                # for each root, create a leafref_group with the chain of
                # leafs from the root and down
                leafref_groups = []
                for r in root_nodes:
                    g = []
                    def add_to_group(x):
                        if hasattr(x, 'i_derefed_leaf'):
                            add_to_group(x.i_derefed_leaf)
                        g.append(x.arg)
                    add_to_group(r)
                    leafref_groups.append(g)
                # add them to the extra field
                if len(leafref_groups) > 0:
                    gs = []
                    for g in leafref_groups:
                        ss = "["
                        ss += ','.join(["'%s'" % childname for childname in g])
                        ss += "]"
                        gs.append(ss)
                    s = ','.join(gs)
                    extra += ',{leafref_groups, [%s]}' % s

            if extra != "":
                # extraFlags is a weird name for a term...
                attrs += " extraFlags=%s" % quoteattr("[%s]." % extra[1:])

            if snmp_extra != "":
                attrs += " snmpExtraInfo=%s" % \
                    quoteattr("[%s]." % snmp_extra[1:])

            fd.write(indent + '<elem name="%s"%s' % (c.arg, attrs))

            if cn in ['container', 'list', 'rpc', 'notification']:
                fd.write('>\n')
                print_desc(fd, indent + istr, get_descr(c, ctx))
                fd.write(must)
                fd.write(when)
                fd.write(callpoint)
                fd.write(validate)
                fd.write(secondary_index)
                fd.write(display_when)
                if cn == 'rpc':
                    ci = c.search_one('input', children=c.i_children)
                    if ci is not None:
                        chs = ci.i_children
                    else:
                        chs = []
                    nokey = True
                elif cn == 'list':
                    # sort children so that all keys come first
                    chs = []
                    for k in c.i_key:
                        chs.append(k)
                    for k in c.i_children:
                        if k not in chs:
                            chs.append(k)
                else:
                    chs = c.i_children
                print_children(ctx, fd, chs, indent + istr,
                               nokey, expand_leafref, allowanyxml)
                fd.write(indent + '</elem>\n')
            elif cn in ['leaf', 'leaf-list', (tailf, 'symlink')]:
                deep = print_desc(fd, indent + istr, get_descr(c, ctx), '>\n')
                if (deep == False and callpoint == '' and
                    validate == '' and secondary_index == '' and
                    display_when == '' and must == '' and when == '' and
                    indexed_view == False):
                    fd.write('/>\n')
                else:
                    if deep == False:
                        fd.write('>\n')
                    fd.write(must)
                    fd.write(when)
                    fd.write(callpoint)
                    fd.write(validate)
                    fd.write(secondary_index)
                    fd.write(display_when)
                    if indexed_view:
                        fd.write(indent + istr + '<indexedView/>\n')
                    fd.write(indent + '</elem>\n')
        elif cn == 'choice':
            mino = ' minOccurs="0"'
            if c.search_one('mandatory') is not None:
                if c.search_one('mandatory').arg == 'true':
                    mino = ' minOccurs="1"'
            if c.search_one('default') is not None:
                defstr = ' default="%s"' % c.search_one('default').arg
            else:
                defstr = ''
            fd.write(indent + '<choice name="%s"%s%s>\n' %
                     (c.arg, defstr, mino))
            for child in c.i_children:
                doit = True
                for f in child.search('if-feature'):
                    if not use_feature(f.arg, ctx):
                        doit = False
                if hasattr(child, 'i_uses'):
                    for u in child.i_uses:
                        for f in u.search('if-feature'):
                            if not use_feature(f.arg, ctx):
                                doit = False
                if not doit:
                    continue
                fd.write(indent + istr + '<case name="%s">\n' % child.arg)
                print_children(ctx, fd, child.i_children, indent + istr*2,
                               nokey, expand_leafref, allowanyxml)
                fd.write(indent + istr + '</case>\n')
            fd.write(indent + '</choice>\n')
        elif cn == 'case':
            # this can happen when augment adds a case
            fd.write(indent + '<case name="%s">\n' % c.arg)
            print_children(ctx, fd, c.i_children, indent + istr,
                           nokey, expand_leafref, allowanyxml)
            fd.write(indent + '</case>\n')
        elif cn == (tailf, 'action'):
            print_actions(ctx, fd, indent, [c])
        elif cn == 'anyxml' and not allowanyxml:
            nyi("anyxml", c.pos)
        elif cn == 'anyxml':
            fd.write(indent + istr + \
                     '<elem name="%s" minOccurs="0" type="xs:string"/>' % c.arg)
            
def flag_attrs(stmt):
    flags = 0
    for s in flag_map.keys():
        x = stmt.search_one((tailf, s))
        if x is not None:
            (flag, subs) = flag_map[s]
            if subs is None or x.search_one((tailf, subs)) is not None:
                flags |= flag
    if flags == 0:
        return ''
    else:
        return ' hexFlags="%x"' % flags

def cli_attrs(c):
    def chk_for_cli_stmts(stmt, in_cli_statement=False):
        flags = 0
        extra = ""
        for s in cli_map.keys():
            x = stmt.search_one((tailf, s))
            if s == 'x_cli_info' and in_cli_statement:
                x = stmt.search_one((tailf, 'info'))
            if x is not None:
                (flag, argtype, recurse) = cli_map[s]
                flags |= flag
                key = re.sub('-', '_', s)
                if argtype is not None:
                    if argtype == 'string':
                        val = '"%s"' % x.arg
                    elif argtype == 'type-name':
                        val = "{'%s','%s'}" % (x.i_type_namespace, x.i_type_name)
                    else:
                        val = x.arg
                    extra += ",{%s,%s}" % (key, val)
                if recurse:
                    rflags, rextra = chk_for_cli_stmts(x, True)
                    if rextra != "":
                        extra += ",{%s_r,%s,[%s]}" % (key, rflags, rextra[1:])
                    else:
                        extra += ",{%s_r,%s,[]}" % (key, rflags)
        return flags, extra

    flags, extra = chk_for_cli_stmts(c)
    attrs = ""
    if (flags != 0) and (flags < (1 << 64)):
        attrs += ' cliFlags="%s"' % flags
    elif (flags != 0):
        attrs += ' cliHexFlags="%x"' % flags
    return attrs, extra

def print_desc(fd, indent, descr, lazy=''):
    if descr is not None:
        fd.write(lazy)
        fd.write(indent + '<desc%s>%s' % descr)
        fd.write('</desc>\n')
        return True
    else:
        return False

def print_actions(ctx, fd, indent, actions, is_rpc=False):
    if is_rpc:
        elem = 'rpc'
    else:
        elem = 'action'
    for a in actions:
        attrs = ' name="%s"' % a.arg
        c = a.search((tailf, 'hidden'))
        if len(c) > 0:
            attrs += ' hidden="%s"' % '|'.join([x.arg for x in c])
        c_attrs, c_extra = cli_attrs(a)
        attrs += c_attrs
        # The extra params are ignored as of now. We may need
        # them at a later point.
        # extra += c_extra
        fd.write(indent + '<%s%s>\n' % (elem, attrs))
        print_desc(fd, indent + istr, get_descr(a, ctx))

        actp = print_action_point(fd, indent + istr, a)
        execp = print_exec(fd, indent + istr, a)
        if not (actp or execp):
            fd.write(indent + '<actionpoint id="nyi" type="internal"/>\n')

        fd.write(cs_display_when(a, indent + istr))
        
        c = a.search_one((tailf, 'confirm-text'))
        if c is not None:
            x = c.search_one((tailf, 'confirm-default'))
            attrs = ''
            if x is not None:
                if x.arg == "true":
                    xVal = "yes"
                else:
                    xVal = "no"
                attrs = ' defaultOption="%s"' % xVal
            fd.write(indent + istr + '<confirmText%s>%s</confirmText>\n' % \
                         (attrs, escape(c.arg)))

        raw_xml = False
        ex = a.search_one((tailf, 'exec'))
        if ex is not None:
            if ex.search_one((tailf, 'raw-xml')) is not None:
                raw_xml = True
                
        if raw_xml is False:
            fd.write(indent + istr + "<params>\n")
            c = a.search_one('input', children=a.i_children)
            if c is not None:
                print_children(ctx, fd, c.i_children, indent + istr*2,
                               nokey=True, expand_leafref='no_must')
            fd.write(indent + istr + "</params>\n")
            fd.write(indent + istr + "<result>\n")
            c = a.search_one('output', children=a.i_children)
            if c is not None:
                print_children(ctx, fd, c.i_children, indent + istr*2,
                               nokey=True, expand_leafref=True,
                               allowanyxml=True)
            fd.write(indent + istr + "</result>\n")
        fd.write(indent + '</%s>\n' % elem)
            
def print_action_point(fd, indent, s):
    ap = s.search_one((tailf, 'actionpoint'))
    if ap is not None:
        apt = "external"
        c = ap.search_one((tailf, 'internal'))
        if c is not None:
            apt = "internal"
        fd.write(indent + '<actionpoint id="%s" type="%s">\n' % (ap.arg, apt))
        fd.write(fmt_opaque(indent + istr, ap))
        fd.write(indent + '</actionpoint>\n')
        return True
    else:
        return False

        
def fmt_opaque(indent, s):
    c = s.search_one((tailf, 'opaque'))
    if c is not None:
        return indent + '<opaqueData>%s</opaqueData>\n' % escape(c.arg)
    else:
        return ""

def print_exec(fd, indent, s):
    ex = s.search_one((tailf, 'exec'))
    if ex is not None:
        fd.write(indent + '<exec>\n')
        fd.write(indent + istr + '<osCommand>%s</osCommand>\n' % escape(ex.arg))
        a = ex.search_one((tailf, 'args'))
        if a is not None:
            fd.write(indent + istr + '<args>%s</args>\n' % escape(a.arg))
        fd.write(indent + istr + '<options>\n')
        o = ex.search_one((tailf, 'uid'))
        if o is not None:
            fd.write(indent + istr*2 + '<uid>%s</uid>\n' % o.arg)
        o = ex.search_one((tailf, 'gid'))
        if o is not None:
            fd.write(indent + istr*2 + '<gid>%s</gid>\n' % o.arg)
        o = ex.search_one((tailf, 'wd'))
        if o is not None:
            fd.write(indent + istr*2 + '<wd>%s</wd>\n' % o.arg)
        o = ex.search_one((tailf, 'global-no-duplicate'))
        if o is not None:
            fd.write(indent + istr*2 + \
                         '<globalNoDuplicate>%s</globalNoDuplicate>\n' % \
                         escape(o.arg))
        o = ex.search_one((tailf, 'interruptible'))
        if o is not None:
            fd.write(indent + istr*2 + \
                         '<interruptible>%s</interruptible>\n' % o.arg)
        o = ex.search_one((tailf, 'interrupt'))
        if o is not None:
            fd.write(indent + istr*2 + '<interrupt>%s</interrupt>\n' % o.arg)
        r = ex.search_one((tailf, 'raw-xml'))
        if r is not None:
            o = r.search_one((tailf, 'batch'))
            if o is not None:
                fd.write(indent + istr*2 + '<batch/>\n')

        fd.write(indent + istr + '</options>\n')
        fd.write(indent + '</exec>\n')
        return True
    else:
        return False

yang_to_cs_types = \
    {'tailf:size':'confd:size',
     'tailf:octet-list':'confd:octetList',
     'tailf:hex-list':'confd:hexList',
     'tailf:md5-digest-string':'confd:MD5DigestString',
     'tailf:des3-cbc-encrypted-string':'confd:DES3CBCEncryptedString',
     'tailf:aes-cfb-128-encrypted-string':'confd:AESCFB128EncryptedString',
     'yang:date-and-time':'xs:dateTime',
     }

def cs_type_name(ctx, type, curmod = None):
    if curmod is None:
        curmod = type.i_module
    if type.arg == 'empty':
        # do not call cs_type_name for empty
        assert False
    elif type.arg == 'leafref':
        # do not call cs_type_name for leafref
        #        assert False
        pass
    elif type.arg == 'instance-identifier':
        return "confd:objectRef"
    elif type.arg == 'decimal64':
        return "confd:decimal64"
    elif type.arg == 'identityref':
        return "confd:identityref"
    elif type.arg in xsd.yang_to_xsd_types:
        return "xs:%s" % xsd.yang_to_xsd_types[type.arg]
    elif ((type.i_typedef is not None) and
          (":" in type.arg) and
          (type.i_typedef.i_module.arg == tailf)):
        [_prefix, name] = type.arg.split(':', 1)
        name = 'tailf:' + name
        if name in yang_to_cs_types:
            return yang_to_cs_types[name]
        else:
            return type.arg
    elif ((type.i_typedef is not None) and
          (":" in type.arg) and
          (type.i_typedef.i_module.arg == yang_types)):
        [_prefix, name] = type.arg.split(':', 1)
        name = 'yang:' + name
        if name in yang_to_cs_types:
            return yang_to_cs_types[name]
        else:
            return name
    elif ((type.i_typedef is not None) and
          (":" in type.arg) and
          (type.i_typedef.i_module.arg == xsd_mod)):
        [_prefix, name] = type.arg.split(':', 1)
        return 'xs:' + name
    elif type.i_typedef is not None:
        if hasattr(type.i_typedef, 'i_cs_type_name'):
            # the type is in our own module
            return "%s" % type.i_typedef.i_cs_type_name
        elif type.i_typedef.parent.keyword == 'module':
            # top-level (exported) typedef
            return "%s:%s" % (type.i_typedef.i_module.i_cs_prefix,
                              type.i_typedef.arg)
        elif type.i_typedef.parent.keyword == 'submodule':
            if (type.i_typedef.parent.i_modulename == curmod.arg):
                # top-level in our own module
                return type.i_typedef.arg
            else:
                # top-level (exported) in submodule
                othermod = ctx.get_module(type.i_typedef.parent.i_modulename)
                return "%s:%s" % (othermod.i_cs_prefix, type.i_typedef.arg)
        else:
            # cannot happen
            assert False
    else:
        return type.arg

def cs_type_attrs(ctx, stmt, type, indent, expand_leafref=False, curmod=None):
    if curmod is None:
        curmod = stmt.i_module
    def chk_sibling_leafref():
        # check if a sibling to us is also a leafref, that refers
        # to us with a deref() (or explicit current)
        siblings = stmt.parent.i_children
        for s in siblings:
            if s == stmt:
                continue
            if hasattr(s, 'i_derefed_leaf'):
                if s.i_derefed_leaf == stmt:
                    # this sibling derefs us
                    return s
        return None

    attrs = ''
    must = ''
    if type.i_is_derived == False or (hasattr(stmt, 'i_leafref') and
                                      stmt.i_leafref is not None):
        if type.arg == 'empty':
            pass
        elif ((hasattr(stmt, 'i_leafref')
               and stmt.i_leafref is not None
               and stmt.i_leafref_ptr is not None)
              or hasattr(stmt, 'tailf_non_strict_leafref')):
            if hasattr(stmt, 'tailf_non_strict_leafref'):
                expand_leafref = 'no_must'
                leafref = stmt.tailf_non_strict_leafref
            else:
                leafref = stmt.i_leafref
            pathstr = leafref.i_expanded_path
            path_list = leafref.i_path_list
            pathstr = translate_prefixes(pathstr,
                                         type.i_orig_module.i_prefixes,
                                         type.i_orig_module.i_prefix,
                                         type.i_orig_module,
                                         ctx.i_cs_top_module.i_prefixes,
                                         ctx.i_cs_top_module.i_prefix,
                                         ctx.i_cs_top_module.arg,
                                         no_default = True)
            pathstr_no_preds = re.sub("\[.*?\]", "", pathstr)
            
            (ref, _pos) = stmt.i_leafref_ptr
            if expand_leafref == True:
                return cs_type_attrs(ctx, ref, ref.search_one('type'),
                                     indent, True, curmod)
            elif expand_leafref == 'no_must':
                attrs += ' leafref=%s' % quoteattr(pathstr)
                (cattrs, must) = \
                    cs_type_attrs(ctx, ref, ref.search_one('type'), indent,
                                  True, curmod)
                attrs += cattrs
                return (attrs, must)
            else:
                attrs += ' leafref=%s' % quoteattr(pathstr)
                sibling_deps = re_sibling.findall(stmt.i_leafref.i_expanded_path)
                must_expr = rwpath(pathstr, chk_sibling_leafref())
                must += indent + '<must value=%s>\n' % quoteattr(must_expr)
                # NOTE: the dependency to the full path MUST be first; see
                # confd_loading:fix_validation_deps_aux() 
                must += indent + istr + '<dependency>%s</dependency>\n' % \
                    escape(pathstr_no_preds)
                for dep in sibling_deps:
                    must += indent + istr + '<dependency>%s</dependency>\n' % \
                        escape(dep)
                # FIXME: should add a tailf stmt to override the
                # following default errorMessage.
                must += indent + istr + \
                    '<errorMessage>__pyang_leafref__</errorMessage>\n'
                must += indent + '</must>\n'

                (cattrs, _must) = \
                    cs_type_attrs(ctx, ref, ref.search_one('type'), indent,
                                  True, curmod)
                attrs += cattrs
                return (attrs, must)
        elif hasattr(type, 'i_cs_type_name'):
            attrs += ' type="%s"' % type.i_cs_type_name
        else:
            attrs += ' type="%s"' % cs_type_name(ctx, type, curmod)
    elif hasattr(type, 'i_cs_type_name'):
        # a derived type w/ a new generated typedef
        attrs += ' type="%s"' % type.i_cs_type_name
    else:
        attrs += ' type="%s"' % cs_type_name(ctx, type, curmod)
    
    x = type.search_one('require-instance')
    if x is not None:
        attrs += ' exists="%s"' % x.arg
    elif type.arg == 'instance-identifier':
        attrs += ' exists="true"'
    x = type.search_one((tailf, 'path-filters'))
    if x is not None:
        attrs += ' path_filters=%s' % quoteattr(x.arg)
        y = x.search_one((tailf, 'no-subtree-match'))
        if y is not None:
            attrs += ' no_subtree="true"'

    return (attrs, must)

def cs_must(ctx, s, indent):
    ms = s.search('must')
    r = ''
    for m in ms:
        def tr(s):
            return translate_prefixes(s,
                                      m.i_orig_module.i_prefixes,
                                      m.i_orig_module.i_prefix,
                                      m.i_orig_module,
                                      ctx.i_cs_top_module.i_prefixes,
                                      ctx.i_cs_top_module.i_prefix,
                                      ctx.i_cs_top_module.arg,
                                      no_default = False)
        v = m.search_one((tailf, 'validate'))
        if v is not None:
            # ignore the must statement
            continue
        attr = ''
        xr = m.search_one((tailf, 'xpath-root'))
        if xr is not None:
            attr += ' xpath_root="%s"' % xr.arg
        mstr = tr(m.arg)
        r += indent + '<must value=%s%s>\n' % (quoteattr(mstr), attr)
        errmsg = m.search_one('error-message')
        errapptag = m.search_one('error-app-tag')
        errapptagstr = None
        if errapptag is None:
            errapptagstr = "must-violation"
        r += fmt_err_info(indent + istr, errmsg, errapptag, errapptagstr)
        deps = m.search((tailf, 'dependency'))
        for d in deps:
            r += indent + istr + '<dependency>%s</dependency>\n' % \
                 escape(tr(d.arg))
        r += indent + '</must>\n'
    return r

def cs_when(ctx, s, indent):
    def mk_when_str(w, ctx_node_up=False, defprefix=None):
        r = ''
        if w is None:
            return ''
        if defprefix is None:
            defprefix = ctx.i_cs_top_module.i_prefix
        def tr(s):
            return translate_prefixes(s,
                                      w.i_orig_module.i_prefixes,
                                      w.i_orig_module.i_prefix,
                                      w.i_orig_module,
                                      ctx.i_cs_top_module.i_prefixes,
                                      defprefix,
                                      ctx.i_cs_top_module.arg,
                                      no_default = False)
        wstr = tr(w.arg)

        def add_prefix(pre, path):
            if len(path) > 0 and path[0] == '/':
                # absolute path
                return path;
            else:
                return pre + path

        if ctx_node_up:
            attr = " ctx_node_up='true'"
            pre = "../"
        else:
            attr = ""
            pre = ""
        xr = w.search_one((tailf, 'xpath-root'))
        if xr is not None:
            attr += ' xpath_root="%s"' % xr.arg
        r += indent + '<when value=%s%s>\n' % (quoteattr(wstr), attr)
        deps = w.search((tailf, 'dependency'))
        if deps == []:
            # special case - tailf.py has made sure that the expression
            # is a simple tagpath; add auto-dependency
            dep = w.i_tailf_tagpath
            r += indent + istr + '<dependency>%s</dependency>\n' % \
                add_prefix(pre, escape(tr(dep)))
        for d in deps:
            r += indent + istr + '<dependency>%s</dependency>\n' % \
                add_prefix(pre, escape(tr(d.arg)))
        r += indent + '</when>\n'
        return r

    def mk_when_str_from_stmt(s, ctx_node_up=False):
        w = s.search_one('when')
        if w is None:
            return ''
        # Since we inherit when stmts and dependencies from the
        # module that we augment into, we need to fix all prefixes
        # used in that module to the prefixes we use.  This is taken
        # care of by translate_prefixes.  But we also need to change
        # all non-prefixed nodes found there to use our prefix for
        # that module.  Thus we set defprefix.
        defprefix = None
        for pre in ctx.i_cs_top_module.i_prefixes:
            (m,rev) = ctx.i_cs_top_module.i_prefixes[pre]
            if m == w.i_module.arg:
                defprefix = pre

        return mk_when_str(w, ctx_node_up, defprefix)
        
    r = ''
    # first, add the node's own when stmt
    w = s.search_one('when')
    r += mk_when_str(w)

    # then add the parent's when stmt if the parent is case
    if s.parent.keyword == 'case':
        r += mk_when_str_from_stmt(s.parent, ctx_node_up=True)
        if s.parent.parent.keyword == 'choice':
            r += mk_when_str_from_stmt(s.parent.parent, ctx_node_up=True)
    # then add the parent's when stmt if the parent is choice
    elif s.parent.keyword == 'choice':
        r += mk_when_str_from_stmt(s.parent, ctx_node_up=True)

    # then add the parent's when stmt if the parent is uses
    if hasattr(s, 'i_uses'):
        for u in s.i_uses:
            w = u.search_one('when')
            tailf_plugin.v_chk_when(ctx, u)
            r += mk_when_str(w, ctx_node_up=True)
 
    # then add the parent's when stmt if the parent is augment
    if hasattr(s, 'i_augment'):
        w = s.i_augment.search_one('when')

        defprefix = None
        for pre in ctx.i_cs_top_module.i_prefixes:
            (m,rev) = ctx.i_cs_top_module.i_prefixes[pre]
            if m == s.i_augment.i_module.arg:
                defprefix = pre

        r += mk_when_str(w, ctx_node_up=True, defprefix=defprefix)

    return r

def cs_callpoint(ctx, s, indent):
    cps = s.search((tailf, 'callpoint'))
    add_default_oper = True
    r = ''
    x = s.search_one('type')
    if hasattr(x, 'i_typedef') and x.i_typedef is not None:
        if (x.i_typedef.i_module.i_modulename == 'SNMPv2-TC'
            and x.i_typedef.arg == 'RowStatus'
            and s.i_config == False):
            r += indent
            r += '<callpoint id="snmpa_row_status" type="internal"/>\n'
            add_default_oper = False
    for cp in cps:
        cpt = "external"
        c = cp.search_one((tailf, 'internal'))
        if c is not None:
            cpt = "internal"
        r += indent
        r += '<callpoint id="%s" type="%s"' % (cp.arg, cpt)
        c = cp.search_one((tailf, 'cache'))
        if c is not None:
            r += ' cache="%s"' % c.arg
            i = c.search_one((tailf, 'timeout'))
            if i is not None:
                r += ' cacheTimeout="%s"' % i.arg
        c = cp.search_one((tailf, 'transform'))
        if c is not None:
            r += ' transform="%s"' % c.arg
        c = cp.search_one((tailf, 'transaction-hook'))
        if c is not None:
            r += ' transactionHook="%s"' % c.arg
            i = c.search_one((tailf, 'invocation-mode'))
            if i is not None:
                r += ' invocationMode="%s"' % i.arg
        c = cp.search_one((tailf, 'set-hook'))
        if c is not None:
            r += ' setHook="%s"' % c.arg
        c = cp.search_one((tailf, 'config'))
        if c is not None:
            r += ' config="%s"' % c.arg
            if c.arg == "false":
                add_default_oper = False
        else:
            add_default_oper = False
        r += '>\n'
        c = get_descr(cp, ctx)
        if c is not None:
            r += indent + istr + '<desc%s>%s</desc>\n' % c
        r += fmt_opaque(indent + istr, cp)
        r += indent + '</callpoint>\n'
    cdb = s.search_one((tailf, 'cdb-oper'))
    if cdb is not None:
        add_default_oper = False
        r += indent
        r += '<callpoint type="cdb" config="false"'
        c = cdb.search_one((tailf, 'persistent'))
        if c is not None:
            r += ' persistent="%s"' % c.arg 
        r += '>\n'
        c = get_descr(cdb, ctx)
        if c is not None:
            r += indent + istr + '<desc%s>%s</desc>\n' % c
        r += indent + '</callpoint>\n'
    if (s.i_config == False
        and (s.parent.parent is None or s.parent.i_config == True)
        and add_default_oper):
        # s is first config false - add default callpoint
        # first check all parents to make sure no callpoint is
        # defined on any ancestor
        def is_node_cp(n):
            cp = n.search_one((tailf, 'callpoint'))
            if cp is not None:
                if cp.search_one((tailf, 'transform')):
                    return True
                c = cp.search_one((tailf, 'config'))
                if c is not None:
                    if c.arg == "false":
                        return True
                else:
                    return True
            if n.parent is not None:
                return is_node_cp(n.parent)
            else:
                return False
        if not is_node_cp(s.parent):
            r += indent + '<callpoint type="cdb" config="false"/>\n'
    return r

def cs_validate(ctx, s, indent):
    vs = s.search((tailf, 'validate'))
    r = ''
    for v in vs:
        def tr(s):
            return translate_prefixes(s,
                                      v.i_orig_module.i_prefixes,
                                      v.i_orig_module.i_prefix,
                                      v.i_orig_module,
                                      ctx.i_cs_top_module.i_prefixes,
                                      ctx.i_cs_top_module.i_prefix,
                                      ctx.i_cs_top_module.arg,
                                      no_default = False)
        vpt = "external"
        c = v.search_one((tailf, 'internal'))
        if c is not None:
            vpt = "internal"
        r += indent
        r += '<validate id="%s" type="%s"' % (v.arg, vpt)
        c = v.search_one((tailf, 'call-once'))
        if c is not None:
            r += ' callOnce="%s"' % c.arg
        p = v.search_one((tailf, 'priority'))
        if p is not None:
            r += ' priority="%s"' % p.arg
            
        desc = get_descr(v, ctx)
        deps = v.search((tailf, 'dependency'))
        r += '>\n'
        if desc is not None:
            r += indent + istr + '<desc%s>%s</desc>\n' % desc
        for d in deps:
            r += indent + istr + '<dependency>%s</dependency>\n' % \
                 escape(tr(d.arg))
        r += fmt_opaque(indent + istr, v)
        r += indent + '</validate>\n'
    for m in s.search('must'):
        r += cs_validate(ctx, m, indent)
    return r

def cs_secondary_index(s, indent):
    r = ''
    ddo = None
    for x in s.search((tailf, 'secondary-index')):
        r += indent
        r += '<secondaryIndex name="%s"' % x.arg
        c = x.search_one((tailf, 'index-leafs'))
        r += ' indexElems="%s"' % c.arg
        c = x.search_one((tailf, 'sort-order'))
        if c is not None:
            r += ' sortOrder="%s"' % conv_sort_order(c.arg)
        r += '/>\n'
        if x.search_one((tailf, 'display-default-order')) is not None:
            ddo = x.arg
    return r, ddo

def cs_display_when(s, indent):
    x = s.search_one((tailf, 'display-when'))
    r = ''
    if x is not None:
        attr = ''
        xr = x.search_one((tailf, 'xpath-root'))
        if xr is not None:
            attr += ' xpath_root="%s"' % xr.arg
        r += indent
        r += '<displayWhen value=%s%s/>\n' % (quoteattr(x.arg), attr)
    return r

def cs_typepoint(s, indent):
    x = s.search_one((tailf, 'typepoint'))
    r = ''
    if x is not None:
        r += indent
        r += '<typepoint id="%s"/>\n' % x.arg
    return r

def search_val(s, keyword, default=None):
    r = s.search_one(keyword)
    if r is None:
        return default
    else:
        return r.arg

def conv_sort_order(s):
    if s == 'snmp-implied':
        return 'snmp_implied'
    else:
        return s

def use_feature(f, ctx):
    return ctx.tailf_features == 'ALL' or f in ctx.tailf_features

def get_descr(c, ctx, force=False):
    if ctx.opts.cs_always_use_description:
        descr = c.search_one('description')
        if descr is not None:
            return ('', escape(descr.arg))
    descr = c.search_one((tailf, 'info'))
    if descr is not None:
        return ('', escape(descr.arg))
    descr = c.search_one((tailf, 'info-html'))
    if descr is not None:
        return (' type="html"', escape(descr.arg))
    if force or ctx.opts.cs_use_description:
        descr = c.search_one('description')
        if descr is not None:
            return ('', escape(descr.arg))
    return None

def translate_prefixes(s, oldmap, oldmodprefix, oldmod,
                       newmap0, newmodprefix, newmodname,
                       no_default):
    # create reverse prefix map (module,revision) -> prefix
    newmap = {}
    for (k, v) in newmap0.items():
        newmap[v] = k

    ourmodprefix = None
    def set_ourmod_prefix():
        if oldmod.arg != newmodname:
            # we are a module importing another module,
            # use our prefix for that module.
            if oldmod.keyword == 'module':
                ourmodprefix = newmap[oldmap[oldmodprefix]]
            else:
                # find the module that this submodule belongs to
                # in our map of prefixes
                belongs_to = oldmod.search_one('belongs-to').arg
                for (p, (v, _r)) in newmap0.items():
                    if v == belongs_to:
                        ourmodprefix = p
        else:
            ourmodprefix = newmodprefix
        return ourmodprefix

    def change_prefix((tokname, s)):
        if tokname == 'name' or tokname == 'prefix-match':
            i = s.find(':')
            if i != -1:
                prefix = s[:i]
                rest = s[i:]
                if prefix == oldmodprefix:
                    if ourmodprefix is None:
                        newprefix = set_ourmod_prefix()
                    else:
                        newprefix = ourmodprefix
                else:
                    newprefix = newmap[oldmap[prefix]]
                return newprefix + rest
            elif no_default == True:
                # no prefix found, this means current module; don't add prefix
                return s
            else:
                # no prefix found, this means current module; add prefix
                return newmodprefix + ':' + s
        else:
            return s

    toks = xpath.tokens(s)
    ls = [change_prefix(tok) for tok in toks]
    return ''.join(ls)

## rewrite a leafref path to a must expression that ConfD will
## optmize into one single lookup.
## the idea is that we change the path /x/y/z into /x/y[z=current()], and
## use it as a must expression with a dependency.
## corner cases:
##   if the leafref is to a top-level node, /x, we rewrite it to . = /x
##   ditto if the leafref is to a leaf directly after a ..
def rwpath(s, sibling):
    toks = xpath.tokens(s)
    i = len(toks)-1
    # skip ws
    while toks[i][0] == 'whitespace':
        i -= 1
    # skip the name
    i -= 1
    # skip ws
    while toks[i][0] == 'whitespace':
        i -= 1
    # i now points to '/'.  replace with '['
    if i == 0:
        # top-level node, cannot add predicate
        return ". = " + s
    if toks[i-1][0] == '..':
        # up, cannot add predicate
        return ". = " + s
    toks[i] = ('[', '[')
    # append "= current()]"
    toks.extend(xpath.tokens("=current()]"))

    if sibling:
        # prepend "../sibling or "
        toks = xpath.tokens("../%s or " % sibling.arg) + toks

    ls = [x for (_tokname, x) in toks]
    return ''.join(ls)

def eoid(s):
    subids = s.split('.')
    return '[' + ','.join(subids) + ']'

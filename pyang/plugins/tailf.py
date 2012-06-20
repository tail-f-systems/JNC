"""Tail-f vendor-specific extensions plugin"""

import optparse
import re
import copy
import sys

import pyang
from pyang import plugin
from pyang import syntax
from pyang import grammar
from pyang import error
from pyang import statements
from pyang import types
from pyang import util
from pyang import xpath
from pyang.error import err_add
from pyang.util import attrsearch

tailf = 'tailf-common'
yang = 'ietf-yang-types'
xs = 'tailf-xsd-types'

smiv2 = 'ietf-yang-smiv2'
"""Name of SMIv2 extensions module"""


re_tailf_identifier = re.compile("^[A-Za-z_][A-Za-z0-9_-]*$")
re_tailf_snmp_identifier = re.compile("^[A-Za-z_][A-Za-z0-9_-]*(:[A-Za-z_][A-Za-z0-9_-]*)*$") 
re_tailf_oid = re.compile("^(([0-1](\.[1-3]?[0-9]))"
                          + "|(2.(0|([1-9]\d*)))"
                          + "|([A-Za-z_][A-Za-z0-9_-]*))?"
                          + "(\.(0|([1-9]\d*)))+$")

path_equality_expr = syntax.node_id + r"\s*=\s*.*"
path_predicate = r"\[\s*" + path_equality_expr + r"\s*\]"
absolute_path_arg = "(/" + syntax.node_id + "(" + path_predicate + ")*)+"
descendant_path_arg = syntax.node_id + "(" + path_predicate + ")*" + \
                      "(?:" + absolute_path_arg + ")?"

one_descendant_path_arg = syntax.node_id + "(" + path_predicate + ")*"

re_tailf_path = re.compile("^" + absolute_path_arg + "$")
re_descendant_path = re.compile("^" + descendant_path_arg + "$")
re_one_descendant_path = re.compile("^" + one_descendant_path_arg + "$")

re_tailf_md5_digest = re.compile("^\$1\$.+\$.+$")

relative_schema_nodeid = r"(\.\./)*" + syntax.descendant_schema_nodeid
default_ref_arg = "(" + syntax.absolute_schema_nodeid + \
                  "|" + relative_schema_nodeid + ")"

re_default_ref = re.compile("^" + default_ref_arg + "$")


def chk_tailf_identifier(s):
    return re_tailf_identifier.search(s) is not None

def chk_tailf_default_ref(s):
    return re_default_ref.search(s) is not None

def chk_tailf_snmp_identifier(s): 
    return re_tailf_snmp_identifier.search(s) is not None 

def chk_tailf_positive_decimal(s):
    try:
        v = int(s, 0)
        if v >= 1 and v <= 4294967295:
            return True
        else:
            return False
    except ValueError:
        return False

def chk_tailf_hook_type(s):
    return s in ['subtree', 'object', 'node']

def chk_tailf_invocation_mode(s):
    return s in ['per-operation', 'per-transaction']

def chk_tailf_sort_order_type(s):
    return s in ['normal', 'snmp', 'snmp-implied']

def chk_tailf_oid(s):
    return re_tailf_oid.search(s) is not None

def chk_tailf_path(s):
    return re_tailf_path.search(s) is not None

def chk_tailf_interrupt_type(s):
    return s in ['sigkill', 'sigint', 'sigterm']

def chk_tailf_opaque(s):
    return len(s) >= 1 and len(s) <= 255

def chk_tailf_cli_column_align_type(s):
    return s in ['left', 'center', 'right']

def chk_tailf_annotate_arg(s):
    if s == '*':
        return True
    else:
        f = syntax.arg_type_map['schema-nodeid']
        return f(s)

def chk_tailf_annotate_statement_arg(s):
    return re_one_descendant_path.search(s) is not None


def chk_tailf_disallow_arg(s):
    # check that it's syntactically correct
    try:
        import libxml2
        try:
            re = libxml2.regexpCompile(s)
            return True
        except libxml2.treeError, v:
            return False
    except ImportError:
        return True


def pyang_plugin_init():
    # register the plugin
    plugin.register_plugin(TailfPlugin())

    # register syntax rules for extension arguments
    syntax.add_arg_type('tailf-identifier', chk_tailf_identifier)
    syntax.add_arg_type('tailf-default-ref', chk_tailf_default_ref)
    syntax.add_arg_type('tailf-snmp-identifier', chk_tailf_snmp_identifier) 
    syntax.add_arg_type('positive-decimal', chk_tailf_positive_decimal)
    syntax.add_arg_type('tailf-hook-type', chk_tailf_hook_type)
    syntax.add_arg_type('tailf-invocation-mode', chk_tailf_invocation_mode)
    syntax.add_arg_type('tailf-sort-order-type', chk_tailf_sort_order_type)
    syntax.add_arg_type('tailf-oid', chk_tailf_oid)
    syntax.add_arg_type('tailf-path', chk_tailf_path)
    syntax.add_arg_type('tailf-interrupt-type', chk_tailf_interrupt_type)
    syntax.add_arg_type('tailf-opaque', chk_tailf_opaque)
    syntax.add_arg_type('tailf-cli-column-align-type',
                        chk_tailf_cli_column_align_type)
    syntax.add_arg_type('tailf-annotate-arg', chk_tailf_annotate_arg)
    syntax.add_arg_type('tailf-annotate-statement-arg',
                        chk_tailf_annotate_statement_arg)
    syntax.add_arg_type('tailf-disallow-pattern', chk_tailf_disallow_arg)

    # register the name of the YANG module where the extensions are defined
    grammar.register_extension_module(tailf)

    # register grammar rules for all our extension statements
    for (stmt, occurance, (arg, rules), add_to_stmts) in tailf_stmts:
        grammar.add_stmt((tailf, stmt), (arg, rules))
        grammar.add_to_stmts_rules(add_to_stmts, [((tailf, stmt), occurance)])

    statements.add_data_keyword((tailf, 'symlink'))
    statements.add_data_keyword((tailf, 'action'))
    statements.add_keyword_with_children((tailf, 'action'))
    statements.add_keywords_with_no_explicit_config((tailf, 'action'))
    statements.add_data_keyword((tailf, 'error-info'))
    statements.add_keyword_with_children((tailf, 'error-info'))
    statements.add_keywords_with_no_explicit_config((tailf, 'error-info'))

    tailf_has_when_must = {
        'container': True,
        'list': True,
        'leaf': True,
        'leaf-list': True,
        'choice': True,
        'case': True,
        'augment': True,
        'uses': True,
        'anyxml': True,
        }

    statements.add_validation_var('$tailf_has_when_must',
                                  lambda keyword: keyword in tailf_has_when_must)

    # register validation code for our extensions
    statements.add_validation_fun('grammar',
                                  ['import'],
                                  lambda ctx, s: v_import(ctx, s))

    statements.add_validation_phase('tailf_pre_import', before='import')
    statements.add_validation_fun('tailf_pre_import',
                                  ['module'],
                                  lambda ctx, s: v_pre_import(ctx, s))

    statements.add_validation_phase('tailf_ann', before='expand_1')
    statements.add_validation_phase('tailf_checks', before='unused')

    statements.add_validation_phase('tailf_checks0', after='inherit_properties')
    statements.set_phase_i_children('tailf_checks0')

    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'sort-order')],
                                  lambda ctx, s: v_chk_sort_order(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'default-ref')],
                                  lambda ctx, s: v_chk_default_ref(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'index-leafs')],
                                  lambda ctx, s: v_chk_index_leafs(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'display-default-order')],
                                  lambda ctx, s: v_chk_display_default_order(ctx,
                                                                             s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cache')],
                                  lambda ctx, s: v_chk_cache(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cdb-oper')],
                                  lambda ctx, s: v_chk_store_in_cdb(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'call-once')],
                                  lambda ctx, s: v_chk_call_once(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cli-show-no')],
                                  lambda ctx, s: v_chk_cli_show_no(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cli-autowizard')],
                                  lambda ctx, s: v_chk_cli_autowizard(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'path-filters')],
                                  lambda ctx, s: v_chk_instance_id(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'value-length')],
                                  lambda ctx, s: v_chk_value_length(ctx, s))
    statements.add_validation_fun('type_2',
                                  [(tailf, 'step')],
                                  lambda ctx, s: v_chk_step(ctx, s))
    statements.add_validation_fun('tailf_ann',
                                  ['module', 'submodule'],
                                  lambda ctx, s: v_annotate_module(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'annotate')],
                                  lambda ctx, s: v_chk_annotate(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'action'), 'rpc'],
                                  lambda ctx, s: v_chk_action(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'raw-xml')],
                                  lambda ctx, s: v_chk_raw_xml(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'path')],
                                  lambda ctx, s: v_chk_path(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  ['typedef'],
                                  lambda ctx, s: v_chk_typedef(ctx, s))
    statements.add_validation_fun('tailf_checks0',
                                  ['$tailf_has_when_must'],
                                  lambda ctx, s: v_chk_when(ctx, s))
    statements.add_validation_fun('tailf_checks0',
                                  ['$tailf_has_when_must'],
                                  lambda ctx, s: v_chk_must(ctx, s))
    statements.add_validation_fun('reference_2',
                                  ['leaf'],
                                  lambda ctx, s: v_chk_key_default(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'display-when')],
                                  lambda ctx, s: v_chk_display_when(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'info')],
                                  lambda ctx, s: v_chk_info(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'info-html')],
                                  lambda ctx, s: v_chk_info_html(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cli-mode-name'),
                                   (tailf, 'cli-mode-name-actionpoint'),
                                   (tailf, 'cli-custom-range-actionpoint'),
                                   (tailf, 'cli-custom-range-enumerator'),
                                   (tailf, 'cli-delayed-auto-commit')],
                                  lambda ctx, s: v_chk_cli_is_mode(ctx, s))
    statements.add_validation_fun('reference_2', ['leaf'],
                                  lambda ctx, s: v_chk_cli_is_key_leaf(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'dependency')],
                                  lambda ctx, s: v_chk_dependency(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cli-compact-syntax')],
                                  lambda ctx, s: v_chk_cli_is_config(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cli-range-type')],
                                  lambda ctx, s: v_chk_cli_range_type(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cli-range-list-syntax')],
                                  lambda ctx, s: v_chk_cli_range_list_syntax(ctx,
                                                                             s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'cli-prefix-key')],
                                  lambda ctx, s: v_chk_cli_prefix_key(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'writable')],
                                  lambda ctx, s: v_chk_writable(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'snmp-delete-value')],
                                  lambda ctx, s: v_chk_snmp_delete_value(ctx, s))
    statements.add_validation_fun('tailf_checks',
                                  [(tailf, 'non-strict-leafref')],
                                  lambda ctx, s: v_chk_non_strict_leafref(ctx,
                                                                          s))
    
    statements.add_validation_fun(
        'tailf_checks',
        [(tailf, 'snmp-ned-accessible-column')],
        lambda ctx, s: v_chk_snmp_ned_accessible_column(ctx, s))
    statements.add_validation_fun(
        'tailf_checks',
        [(tailf, 'snmp-ned-num-instances')],
        lambda ctx, s: v_chk_snmp_ned_num_instances(ctx, s))
    statements.add_validation_fun(
        'tailf_checks',
        [(tailf, 'snmp-ned-set-before-row-modification')],
        lambda ctx, s: v_chk_snmp_ned_set_before_row_modification(ctx, s))
    statements.add_validation_fun(
        'tailf_checks',
        [(tailf, 'snmp-ned-modification-dependent')],
        lambda ctx, s: v_chk_snmp_ned_modification_dependent(ctx, s))

    # register special error codes
    error.add_error_code('TAILF_BAD_SORT_ORDER', 1,
                         "sort-order cannot be used on ordered-by user lists")
    error.add_error_code('TAILF_BAD_DEFAULT_REF', 1,
                         "default-ref cannot be used on a leaf which %s")
    error.add_error_code('TAILF_BAD_INDEX_LEAF', 1,
                         "secondary-index leaf '%s' %s")
    error.add_error_code('TAILF_DUPLICATE_INDEX_LEAF', 2,
                         "the leaf '%s' must not be listed more than once")
    error.add_error_code('TAILF_BAD_CALL_ONCE', 1,
                         "call-once can only be used for lists")
    error.add_error_code('TAILF_DEPRECATED', 4,
                         "%s has been deprecated. %s")
    error.add_error_code('TAILF_BAD_CLI_SHOW_NO', 1,
                         "parent leaf must be optional")
    error.add_error_code('TAILF_BAD_CLI_AUTOWIZARD', 1,
                         "parent leaf must be optional")
    error.add_error_code('TAILF_BAD_INSTANCE_IDENTIFIER', 1,
                         "%s can only be used for types 'instance-identifier'")
    error.add_error_code('TAILF_BAD_VALUE_LENGTH_TYPE', 1,
                         "%s can not be used for type '%s'")
    error.add_error_code('TAILF_BAD_STEP', 1,
                         "%s is invalid (must be greater than 0)")
    error.add_error_code('TAILF_CACHE_CONFIG', 1,
                         "when cache is true, config must be false")
    error.add_error_code('TAILF_BAD_KEY_DEFAULT', 1,
                         "key-default can only be given to key leafs")
    error.add_error_code('TAILF_NEED_KEY_DEFAULT', 1,
                         "needs a key-default since the key leaf at %s has one")
    error.add_error_code('TAILF_CONTAINER_MUST_BE_MODE', 1,
                         "The container %s must have tailf:cli-add-mode.")
    error.add_error_code('TAILF_LIST_MUST_BE_MODE', 1,
                         "The list %s must not have tailf:cli-suppress-mode.")
    error.add_error_code('TAILF_MUST_BE_KEY', 1,
                         "When %s is given, the leaf must be a list key.")
    error.add_error_code('TAILF_MUST_BE_OPTIONAL', 1,
                         "When %s is given, the leaf must not be"
                         " mandatory or a list key.")
    error.add_error_code('TAILF_BAD_KEY', 1,
                         "When %s is given, the list must have exactly one"
                         " integer-based key.")
    error.add_error_code('TAILF_BAD_INT_TYPE', 1,
                         "When %s is given, the type must be integer-based.")
    error.add_error_code('TAILF_BAD_LEAFREF_TYPE', 1,
                         "The leafref's target type '%s' is not the same as"
                         " the given type '%s'")
    error.add_error_code('TAILF_MUST_BE_CONFIG', 1,
                         "When %s is given, the node must be config.")
    error.add_error_code('TAILF_LEAF_LIST_MUST_HAVE_RANGE', 1,
                         "The cli-range-list-syntax statement must be"
                         " used with %s for leaf-lists.")
    error.add_error_code('TAILF_MUST_BE_CONFIG_FALSE', 1,
                         "When %s is given, the node must not be config.")
    error.add_error_code('TAILF_MULTIPLE_DEFAULT_ORDER', 1,
                         "Another display-default-order statement is"
                         " given at %s.")
    error.add_error_code('TAILF_WHEN_NEED_DEPENDENCY', 1,
                         "The 'when' expression must have a tailf:dependency")
    error.add_error_code('TAILF_MUST_NEED_DEPENDENCY', 4,
                         "The 'must' expression should have a tailf:dependency")
    error.add_error_code('TAILF_BAD_ANNOTATE', 1,
                         "Illegal annotation.")
    error.add_error_code('TAILF_ANNOTATATION_ERROR', 1,
                         "Annotated node not found: %s.")
    error.add_error_code('TAILF_INVALID_ANNOTATE', 1,
                         "The statement %s cannot be annotated into a %s.")
    error.add_error_code('TAILF_BAD_VALUE', 1,
                         "The value is not valid"
                         " according to the YANG type.")
    error.add_error_code('TAILF_BAD_SNMP_DELETE_VALUE', 1, 
                         "The snmp-delete-value cannot be valid.")
    error.add_error_code('TAILF_BAD_SNMP_DELETE_VALUE_ENUM', 1,
                         "The snmp-delete-value for an enumeration"
                         " must be an unused integer.")
    error.add_error_code('TAILF_BAD_COLUMN', 1,
                         "%s is not found in the list %s.")
    error.add_error_code('TAILF_NO_OID', 1,
                         "%s does not have an OID.")
    error.add_error_code('TAILF_NOT_IN_ACTION', 1,
                         "%s is not allowed in an action.")
    error.add_error_code('TAILF_NEED_ACTIONPOINT', 4,
                         "The %s %s has no actionpoint or exec.  Will give " \
                             "run-time error if invoked.")
    

class TailfPlugin(plugin.PyangPlugin):
    def setup_ctx(self, ctx):
        ctx.tailf_ann_mods = {}

    def add_opts(self, optparser):
        optlist = [
            optparse.make_option("-a", "--annotate",
                                 metavar="FILENAME",
                                 dest="tailf_annotate",
                                 default=[],
                                 action="append",
                                 help="Module with annotations"),
            ]
        sanitize_optlist = [
            optparse.make_option("--tailf-sanitize",
                                 dest="tailf_sanitize",
                                 action="store_true",
                                 help="Remove tailf-specific annotations"),
            optparse.make_option("--tailf-remove-body",
                                 dest="tailf_remove_body",
                                 action="store_true",
                                 help="Keep only typedefs and groupings " \
                                 "in sanitation"),
            optparse.make_option("--tailf-keep-actions",
                                 dest="tailf_keep_actions",
                                 action="store_true",
                                 help="Keep tailf:action in sanitation"),
            optparse.make_option("--tailf-keep-info",
                                 dest="tailf_keep_info",
                                 action="store_true",
                                 help="Keep tailf:info in sanitation"),
            optparse.make_option("--tailf-keep-tailf-typedefs",
                                 dest="tailf_keep_typedefs",
                                 action="store_true",
                                 help="Keep types from tailf-common " \
                                 "in sanitation"),
            optparse.make_option("--tailf-keep-symlink-when",
                                 dest="tailf_keep_when",
                                 action="store_true",
                                 help="Keep when statements in symlinks " \
                                 "in sanitation"),
            optparse.make_option("--tailf-keep-symlink-must",
                                 dest="tailf_keep_must",
                                 action="store_true",
                                 help="Keep must statements in symlinks " \
                                 "in sanitation"),
            ]
        if hasattr(optparser, 'tailf_opts'):
            g = optparser.tailf_opts
        else:
            g = optparser.add_option_group("Tail-f specific options")
            optparser.tailf_opts = g
        g.add_options(optlist)
        if hasattr(optparser, 'tailf_sanitize_opts'):
            g = optparser.tailf_sanitize_opts
        else:
            g = optparser.add_option_group("Tail-f Sanitation options")
            optparser.tailf_sanitation_opts = g
        g.add_options(sanitize_optlist)

    def post_validate_ctx(self, ctx, modules):
        if not ctx.opts.tailf_sanitize:
            return
        for (epos, etag, eargs) in ctx.errors:
            if (epos.top in modules and
                error.is_error(error.err_level(etag))):
                return

        for m in modules:
            sanitize(ctx, m)

tailf_stmts = [
    
    ('use-in', '*',
     ('string', []),
     ['extension']),

    ('substatement', '*',
     ('string', []),
     ['extension']),

    ('arg-type', '?',
     (None, [('type', '1'),
              ('default', '?')]),
     ['argument']),

    ('annotate-module', '*',
     ('string', [((tailf, 'snmp-mib-module-name'), '?'),
                 ((tailf, 'snmp-oid'), '?'),
                 ((tailf, 'id'), '?'),
                 ((tailf, 'annotate-statement'), '*')]),
     ['module']),

    ('callpoint', '*',
     ('tailf-identifier', [('description', '?'),
                           ((tailf, 'cache'), '?'),
                           ((tailf, 'config'), '?'),
                           ((tailf, 'internal'), '?'),
                           ((tailf, 'opaque'), '?'),
                           ((tailf, 'set-hook'), '?'),
                           ((tailf, 'transaction-hook'), '?'),
                           ((tailf, 'transform'), '?'),
                           ]),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('cache', '?',
     ('boolean', [((tailf, 'timeout'), '?')]),
     []),

    ('timeout', '?',
     ('positive-decimal', []),
     []),

    ('config', '?',
     ('boolean', []),
     []),

    ('internal', '?',
     (None, []),
     []),

    ('opaque', '?',
     ('tailf-opaque', []),
     []),

    ('set-hook', '?',
     ('tailf-hook-type', []),
     []),

    ('transaction-hook', '?',
     ('tailf-hook-type', [((tailf, 'invocation-mode'), '?')]),
     []),

    ('invocation-mode', '?',
     ('tailf-invocation-mode', []),
     []),

    ('transform', '?',
     ('boolean', []),
     []),

    ('cdb-oper', '?',
     (None, [('description', '?'),
             ((tailf, 'persistent'), '?'),
             ]),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('persistent', '?',
     ('boolean', []),
     []),

    ('id-value', '?',
     ('positive-decimal', []),
     ['leaf', 'leaf-list', 'list', 'container',
      'enum', 'rpc', 'notification', 'identity']),

    ('default-ref', '?',
     ('tailf-default-ref', []),
     ['leaf', 'refine']),

    ('sort-order', '?',
     ('tailf-sort-order-type', []),
     ['list', 'leaf-list']),

    ('symlink', '*',
     ('identifier', [((tailf, 'path'), '1'),
                     ((tailf, 'alt-name'), '?'),
                     ((tailf, 'inherit-set-hook'), '?'),
                     ((tailf, 'display-when'), '?'),
                     ((tailf, 'sort-priority'), '?'),
                     ((tailf, 'hidden'), '*'),
                     ((tailf, 'cli-boolean-no'), '?'),
                     ((tailf, 'cli-show-no'), '?'),
                     ((tailf, 'cli-disallow-value'), '?'),
                     ((tailf, 'cli-autowizard'), '?'),
                     ((tailf, 'cli-show-config'), '?'),
                     ((tailf, 'cli-diff-dependency'), '?'),
                     ((tailf, 'cli-display-empty-config'), '?'),
                     ((tailf, 'cli-mode-name'), '?'),
                     ((tailf, 'cli-show-order-taglist'), '?'),
                     ((tailf, 'cli-show-order-tag'), '?'),
                     ((tailf, 'cli-show-mode-name-actionpoint'), '?'),
                     ((tailf, 'cli-add-mode'), '?'),
                     ((tailf, 'cli-flatten-container'), '?'),
                     ((tailf, 'cli-suppress-mode'), '?'),
                     ((tailf, 'cli-remove-before-change'), '?'),
                     ((tailf, 'cli-no-value-on-delete'), '?'),
                     ((tailf, 'cli-no-name-on-delete'), '?'),
                     ((tailf, 'cli-allow-join-with-key'), '?'),
                     ((tailf, 'cli-exit-command'), '?'),
                     ((tailf, 'cli-key-format'), '?'),
                     ((tailf, 'cli-suppress-key-sort'), '?'),
                     ((tailf, 'cli-suppress-validation-warning-prompt'), '?'),
                     ((tailf, 'cli-suppress-key-abbreviation'), '?'),
                     ((tailf, 'cli-allow-key-abbreviation'), '?'),
                     ((tailf, 'cli-table-legend'), '?'),
                     ((tailf, 'cli-table-footer'), '?'),
                     ((tailf, 'cli-completion-actionpoint'), '?'),
                     ((tailf, 'cli-multi-word-key'), '?'),
                     ((tailf, 'cli-allow-range'), '?'),
                     ((tailf, 'cli-suppress-range'), '?'),
                     ((tailf, 'cli-custom-range'), '?'),
                     ((tailf, 'cli-custom-range-actionpoint'), '?'),
                     ((tailf, 'cli-custom-range-enumerator'), '?'),
                     ((tailf, 'cli-allow-wildcard'), '?'),
                     ((tailf, 'cli-suppress-wildcard'), '?'),
                     ((tailf, 'cli-delayed-auto-commit'), '?'),
                     ((tailf, 'cli-preformatted'), '?'),
                     ((tailf, 'cli-trim-default'), '?'),
                     ((tailf, 'cli-expose-key-name'), '?'),
                     ((tailf, 'cli-enforce-table'), '?'),
                     ((tailf, 'cli-drop-node-name'), '?'),
                     ((tailf, 'cli-compact-syntax'), '?'),
                     ((tailf, 'cli-compact-stats'), '?'),
                     ((tailf, 'cli-column-stats'), '?'),
                     ((tailf, 'cli-column-width'), '?'),
                     ((tailf, 'cli-min-column-width'), '?'),
                     ((tailf, 'cli-column-align'), '?'),
                     ((tailf, 'cli-list-syntax'), '?'),
                     ((tailf, 'cli-flat-list-syntax'), '?'),
                     ((tailf, 'cli-range-list-syntax'), '?'),
                     ((tailf, 'cli-incomplete-command'), '?'),
                     ((tailf, 'cli-full-command'), '?'),
                     ((tailf, 'cli-sequence-command'), '?'),
                     ((tailf, 'cli-reset-container'), '?'),
                     ((tailf, 'cli-break-sequence-commands'), '?'),
                     ((tailf, 'cli-optional-in-sequence'), '?'),
                     ((tailf, 'cli-incomplete-show-path'), '?'),
                     ((tailf, 'cli-hide-in-submode'), '?'),
                     ((tailf, 'cli-prefix-key'), '?'),
                     ((tailf, 'cli-show-with-default'), '?'),
                     ((tailf, 'cli-full-show-path'), '?'),
                     ((tailf, 'cli-suppress-show-path'), '?'),
                     ((tailf, 'cli-suppress-show-match'), '?'),
                     ((tailf, 'cli-suppress-list-no'), '?'),
                     ((tailf, 'cli-suppress-no'), '?'),
                     ((tailf, 'cli-suppress-silent-no'), '?'),
                     ((tailf, 'cli-full-no'), '?'),
                     ((tailf, 'cli-incomplete-no'), '?'),
                     ((tailf, 'cli-no-match-completion'), '?'),
                     ((tailf, 'cli-suppress-show-conf-path'), '?'),
                     ((tailf, 'cli-no-key-completion'), '?'),
                     ((tailf, 'cli-instance-info-leafs'), '?'),
                     ((tailf, 'cli-multi-value'), '?'),
                     ((tailf, 'cli-value-display-template'), '?'),
                     ((tailf, 'cli-show-template'), '?'),
                     ((tailf, 'cli-show-template-legend'), '?'),
                     ((tailf, 'cli-show-template-footer'), '?'),
                     ((tailf, 'cli-show-template-enter'), '?'),
                     ((tailf, 'cli-run-template'), '?'),
                     ((tailf, 'cli-run-template-legend'), '?'),
                     ((tailf, 'cli-run-template-enter'), '?'),
                     ((tailf, 'cli-run-template-footer'), '?'),
                     ((tailf, 'info'), '?'),
                     ((tailf, 'info-html'), '?'),
                     ((tailf, 'cli-oper-info'), '?'),
                     ((tailf, 'cli-custom-error'), '?'),
                     ((tailf, 'snmp-oid'), '?'),
                     ((tailf, 'snmp-exclude-object'), '?'),
                     ((tailf, 'snmp-name'), '*'), 
                     ((tailf, 'cli-range-delimiters'), '?'),
                     ('must', '*')]),
     ['module', 'submodule', 'list', 'container', 'augment', 'case']),
      
    ('path', '1',
     ('tailf-path', []),
     []),

    ('inherit-set-hook', '1',
     ('boolean', []),
     []),
    
    ('secondary-index', '*',
     ('string', [((tailf, 'index-leafs'), '1'),
                 ((tailf, 'sort-order'), '?'),
                 ((tailf, 'display-default-order'), '?')]),
     ['list']),

    ('index-leafs', '1',
     ('key-arg', []),
     []),

    ('typepoint', '*',
     ('tailf-identifier', []),
     ['typedef', 'leaf', 'leaf-list']),

    ('validate', '*',
     ('tailf-identifier', [('description', '?'),
                           ((tailf, 'call-once'), '?'),
                           ((tailf, 'internal'), '?'),
                           ((tailf, 'opaque'), '?'),
                           ((tailf, 'priority'), '?'),
                           ((tailf, 'dependency'), '*')]),
     ['leaf', 'leaf-list', 'list', 'container', 'grouping',
      'refine', 'must']),

    ('call-once', '?',
     ('boolean', []),
     []),

    ('priority', '?',
     ('integer', []),
     []),

    ('dependency', '*',
     ('string', []),
     ['must', 'when']),

    ('value-length', '?',
     ('length-arg', [('error-app-tag', '?'),
                     ('error-message', '?')]),
     ['type']),
      
    ('path-filters', '?',
     ('string', [((tailf, 'no-subtree-match'), '?')]),
     ['type']),
      
    ('step', '?',
     ('string', []),
     ['range']),

    ('no-subtree-match', '?',
     (None, []),
     []),
      
    ('suppress-echo', '?',
     ('boolean', []),
     ['typedef', 'leaf', 'leaf-list']),

    ('hidden', '*',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine', 'rpc']),

    ('display-when', '?',
     ('string', [((tailf, 'xpath-root'), '?')]),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('display-groups', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('display-hint', '?',
     ('string', []),
     ['leaf', 'typedef']),

    ('alt-name', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('info', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine',
      'identity', 'typedef', 'rpc', 'type',
      'enum', 'length', 'pattern', 'range']),

    ('info-html', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine',
      'identity', 'rpc']),

    ('cli-oper-info', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine',
      'identity', 'rpc']),

    ('cli-custom-error', '?',
     ('string', []),
     ['leaf', 'refine']),

    ('display-status-name', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('display-column-name', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'refine']),

    ('cli-show-no', '?',
     (None, []),
     ['leaf', 'refine', 'list', 'leaf-list', 'container']),

    ('cli-disallow-value', '?',
     ('tailf-disallow-pattern', []),
     ['leaf', 'leaf-list', 'refine']),

    ('cli-boolean-no', '?',
     (None, [((tailf, 'cli-reversed'), '?')]),
     ['leaf', 'refine']),

    ('cli-reversed', '?',
     (None, []),
     []),

    ('cli-autowizard', '?',
     (None, []),
     ['leaf', 'refine']),

    ('cli-show-config', '?',
     (None, []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('cli-show-order-tag', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('cli-show-order-taglist', '?',
     ('string', []),
     ['list', 'container', 'refine']),

    ('cli-display-empty-config', '?',
     (None, []),
     ['list', 'refine']),

    ('cli-mode-name', '?',
     ('string', []),
     ['container',  'list', 'refine']),

    ('cli-mode-name-actionpoint', '?',
     ('string', []),
     ['container',  'list', 'refine']),
    
    ('cli-add-mode', '?',
     (None, []),
     ['container', 'refine']),

    ('cli-flatten-container', '?',
     (None, []),
     ['container', 'list', 'refine']),
    
    ('cli-suppress-mode', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-remove-before-change', '?',
     (None, []),
     ['list', 'leaf', 'refine']),
    
    ('cli-no-value-on-delete', '?',
     (None, []),
     ['leaf', 'refine']),
    
    ('cli-no-name-on-delete', '?',
     (None, []),
     ['leaf', 'refine']),
    
    ('cli-show-long-obu-diffs', '?',
     (None, []),
     ['list', 'refine']),

    ('cli-allow-join-with-key', '?',
     (None, [((tailf, 'cli-display-joined'), '?')]),
     ['list', 'refine']),
    
    ('cli-display-joined', '?',
     (None, []),
     []),

    ('cli-key-format', '?',
     ('string', []),
     ['list', 'refine']),
    
    ('cli-exit-command', '?',
     ('string', [((tailf, 'info'), '?')]),
     ['list', 'container', 'refine']),
    
    ('cli-suppress-key-sort', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-suppress-table', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-suppress-key-abbreviation', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-allow-key-abbreviation', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-table-legend', '?',
     ('string', []),
     ['list', 'refine']),
    
    ('cli-table-footer', '?',
     ('string', []),
     ['list', 'refine']),
    
    ('cli-completion-actionpoint', '?',
     ('tailf-identifier', [((tailf, 'cli-completion-id'), '?')]),
     ['leaf', 'refine']),

    ('cli-custom-range-actionpoint', '?',
     ('tailf-identifier', [((tailf, 'cli-completion-id'), '?')]),
     ['list', 'refine']),

    ('cli-custom-range-enumerator', '?',
     ('tailf-identifier', [((tailf, 'cli-completion-id'), '?')]),
     ['list', 'refine']),

    ('cli-completion-id', '?',
     ('tailf-identifier', []),
     []),

    ('cli-multi-word-key', '?',
     (None, [((tailf, 'cli-max-words'), '?')]),
     ['leaf', 'refine']),

    ('cli-max-words', '?',
     ('non-negative-integer', []),
     []),

    ('cli-prefix-key', '?',
     (None, [((tailf, 'cli-before-key'), '?')]),
     ['leaf', 'leaf-list', 'refine']),

    ('cli-before-key', '?',
     ('non-negative-integer', []),
     []),

    ('cli-show-with-default', '?',
     (None, []),
     ['leaf', 'refine']),

    ('cli-hide-in-submode', '?',
     (None, []),
     ['leaf', 'refine']),

    ('cli-allow-range', '?',
     (None, []),
     ['leaf', 'refine']),
    
    ('cli-range-delimiters', '?',
     ('string', []),
     ['list', 'refine']),

    ('cli-suppress-range', '?',
     (None, []),
     ['leaf', 'refine']),

    ('cli-allow-wildcard', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-operational-mode', '?',
     (None, []),
     []),
    
    ('cli-configure-mode', '?',
     (None, []),
     []),
    
    ('cli-suppress-wildcard', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-delayed-auto-commit', '?',
     (None, []),
     ['container',  'list', 'refine']),

    ('cli-preformatted', '?',
     (None, []),
     ['leaf', 'refine']),

    ('cli-trim-default', '?',
     (None, []),
     ['leaf', 'refine']),

    ('cli-expose-key-name', '?',
     (None, []),
     ['leaf', 'refine']),

    ('cli-enforce-table', '?',
     (None, []),
     ['list', 'refine']),
    
    ('cli-drop-node-name', '?',
     (None, []),
     ['list', 'leaf', 'container', 'leaf-list', 'refine']),

    ('cli-suppress-validation-warning-prompt', '?',
     (None, []),
     ['list', 'leaf', 'container', 'leaf-list', 'refine']),

    ('cli-compact-syntax', '?',
     (None, []),
     ['list',  'container', 'refine']),

    ('cli-column-stats', '?',
     (None, []),
     ['container', 'refine']),

    ('cli-column-width', '?',
     ('non-negative-integer', []),
     ['leaf', 'leaf-list', 'refine']),

    ('cli-min-column-width', '?',
     ('non-negative-integer', []),
     ['leaf', 'leaf-list', 'refine']),

    ('cli-column-align', '?',
     ('tailf-cli-column-align-type', []),
     ['leaf', 'leaf-list', 'refine']),

    ('cli-list-syntax', '?',
     (None, [((tailf, 'cli-multi-word'), '?')]),
     ['leaf-list', 'refine']),

    ('cli-multi-word', '?',
     (None, [((tailf, 'cli-max-words'), '?')]),
     []),

    ('cli-flat-list-syntax', '?',
     (None, [((tailf, 'cli-replace-all'), '?')]),
     ['leaf-list', 'refine']),

    ('cli-replace-all', '?',
     (None, []),
     []),

    ('cli-range-list-syntax', '?',
     (None, []),
     ['leaf-list', 'list', 'refine']),

    ('cli-incomplete-command', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-full-command', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-sequence-commands', '?',
     (None, [((tailf, 'cli-reset-siblings'), '?'),
             ((tailf, 'cli-reset-all-siblings'), '?')]),
     ['list',  'container', 'refine']),

    ('cli-reset-siblings', '?',
     (None, []),
     []),

    ('cli-reset-all-siblings', '?',
     (None, []),
     []),

    ('cli-reset-container', '?',
     (None, []),
     ['leaf','list', 'container','refine']),

    ('cli-break-sequence-commands', '?',
     (None, []),
     ['leaf', 'leaf-list', 'list',  'container', 'refine']),

    ('cli-optional-in-sequence', '?',
     (None, []),
     ['leaf', 'leaf-list', 'list',  'container', 'refine']),

    ('cli-incomplete-show-path', '?',
     (None, [((tailf, 'cli-min-keys'), '?')]),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-min-keys', '?',
     ('non-negative-integer', []),
     []),

    ('cli-full-show-path', '?',
     (None, [((tailf, 'cli-max-keys'), '?')]),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-max-keys', '?',
     ('non-negative-integer', []),
     []),

    ('cli-suppress-show-path', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-suppress-show-conf-path', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-suppress-show-match', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-suppress-list-no', '?',
     (None, []),
     ['leaf-list',  'list', 'refine']),

    ('cli-suppress-no', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-suppress-silent-no', '?',
     ('string', []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-full-no', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-incomplete-no', '?',
     (None, []),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-multi-value', '?',
     (None, [((tailf, 'cli-max-words'), '?')]),
     ['leaf', 'refine']),

    ('cli-no-key-completion', '?',
     (None, []),
     ['list', 'refine']),

    ('cli-no-match-completion', '?',
     (None, []),
     ['list', 'refine']),

    ('cli-compact-stats', '?',
     (None, [((tailf, 'cli-wrap'), '?'),
             ((tailf, 'cli-width'), '?'),
             ((tailf, 'cli-delimiter'), '?'),
             ((tailf, 'cli-prettify'), '?'),
             ((tailf, 'cli-spacer'), '?')]),
     ['list', 'container', 'refine']),

    ('cli-wrap', '?',
     (None, []),
     []),

    ('cli-width', '?',
     ('non-negative-integer', []),
     []),

    ('cli-delimiter', '?',
     ('string', []),
     []),

    ('cli-prettify', '?',
     (None, []),
     []),

    ('cli-spacer', '?',
     ('string', []),
     []),

    ('cli-custom-range', '?',
     (None, [((tailf, 'cli-range-type'), '1')]),
     ['leaf']),

    ('cli-range-type', '?',
     ('string', []),
     []),

    ('cli-show-template', '?',
     ('string', [((tailf, 'cli-auto-legend'), '?')]),
     ['leaf',  'leaf-list',  'list',  'container', 'refine']),

    ('cli-auto-legend', '?',
     (None, []),
     []),

    ('cli-value-display-template', '?',
     ('string', []),
     ['leaf',  'refine']),

    ('cli-show-template-legend', '?',
     ('string', []),
     ['list', 'refine']),

    ('cli-show-template-enter', '?',
     ('string', []),
     ['list', 'refine']),

    ('cli-show-template-footer', '?',
     ('string', []),
     ['list', 'refine']),

    ('cli-run-template', '?',
     ('string', []),
     ['leaf',  'leaf-list', 'refine']),

    ('cli-run-template-legend', '?',
     ('string', []),
     ['list', 'refine']),

    ('cli-run-template-enter', '?',
     ('string', []),
     ['list', 'refine']),

    ('cli-run-template-footer', '?',
     ('string', []),
     ['list', 'refine']),

    ('cli-instance-info-leafs', '?',
     ('string', []),
     ['list', 'refine']),

    ('junos-val-as-xml-tag', '?',
     (None, []),
     ['leaf']),

    ('junos-val-with-prev-xml-tag', '?',
     (None, []),
     ['leaf']),

    ('display-default-order', '?',
     (None, []),
     []),

    ('id', '?',
     ('string', []),
     ['module']),

    ('snmp-oid', '?',
     ('tailf-oid', []),
     ['leaf', 'leaf-list', 'list', 'container', 'module', 'refine']),
    
    ('snmp-exclude-object', '?',
     (None, []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('snmp-name', '*',
     ('tailf-snmp-identifier', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),
    
    ('snmp-mib-module-name', '?',
     ('identifier', []),
     ['leaf', 'leaf-list', 'list', 'container', 'module', 'refine']),
    
    ('snmp-row-status-column', '?',
     ('positive-decimal', []),
     ['list', 'refine']),
    
    ('snmp-lax-type-check', '?',
     ('boolean', []),
     ['leaf']),

    ('snmp-delete-value', '?',
     ('string', [((tailf, 'snmp-send-delete-value'), '?')]),
     ['leaf']),

    ('snmp-send-delete-value', '?',
     (None, []),
     []),

    ('snmp-ned-set-before-row-modification', '?',
     ('string', []),
     ['leaf']),

    ('snmp-ned-modification-dependent', '?',
     (None, []),
     ['leaf']),

    ('snmp-ned-accessible-column', '?',
     ('string', []),
     ['list']),

    ('snmp-ned-num-instances', '?',
     ('string', []),
     ['list']),

    ('java-class-name', '?',
     ('tailf-identifier', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),
      
    ('code-name', '?',
     ('tailf-identifier', []),
     ['enum']),

    ('action', '*',
     ('identifier', [('description', '?'),
                     ('input', '?'),
                     ('output', '?'),
                     ((tailf, 'display-when'), '?'),
                     ((tailf, 'exec'), '?'),
                     ((tailf, 'actionpoint'), '?'),
                     ((tailf, 'hidden'), '?'),
                     ((tailf, 'confirm-text'), '?'),
                     ((tailf, 'cli-configure-mode'), '?'),
                     ((tailf, 'cli-operational-mode'), '?'),
                     ((tailf, 'info'), '?'),
                     ((tailf, 'info-html'), '?'),
                     ((tailf, 'cli-oper-info'), '?')]),
     ['list', 'container', 'grouping', 'augment']),
                      
    ('actionpoint', '?',
     ('tailf-identifier', [((tailf, 'internal'), '?'),
                           ((tailf, 'opaque'), '?')]),
     ['rpc']),
    
    ('confirm-text', '?',
     ('string', [((tailf, 'confirm-default'), '?')]),
     []),
    
    ('confirm-default', '?',
     ('boolean', []),
     []),
    
    ('error-info', '*',
     (None, [('description', '?'),
             ('leaf', '*'),
             ('leaf-list', '*'),
             ('container', '*'),
             ('list', '*'),
             ('choice', '*')]),
     ['module', 'submodule']),
                      
    ('indexed-view', '?',
     (None, []),
     ['list']),

    ('non-strict-leafref', '?',
     (None, [('path', '1')]),
     ['leaf', 'leaf-list']),

    ('key-default', '?',
     ('string', []),
     ['leaf']),

    ('exec', '?',
     ('string', [((tailf, 'args'), '?'),
                 ((tailf, 'uid'), '?'),
                 ((tailf, 'gid'), '?'),
                 ((tailf, 'wd'), '?'),
                 ((tailf, 'global-no-duplicate'), '?'),
                 ((tailf, 'batch'), '?'),
                 ((tailf, 'raw-xml'), '?'),
                 ((tailf, 'interruptible'), '?'),
                 ((tailf, 'interrupt'), '?')]),
     ['rpc']),

    ('args', '?',
     ('string', []),
     []),

    ('raw-xml', '?',
     (None, [((tailf, 'batch'), '?')]),
     []),

    ('interruptible', '?',
     ('boolean', []),
     []),

    ('interrupt', '?',
     ('tailf-interrupt-type', []),
     []),

    ('batch', '?',
     (None, []),
     []),

    ('uid', '?',
     ('string', []),
     []),

    ('gid', '?',
     ('string', []),
     []),

    ('wd', '?',
     ('string', []),
     []),

    ('global-no-duplicate', '?',
     ('string', []),
     []),

    ('batch', '?',
     (None, []),
     []),

    ('sort-priority', '?',
     ('integer', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

    ('xpath-root', '?',
     ('integer', []),
     ['when', 'must']),

    ('writable', '?',
     ('boolean', []),
     ['leaf']),

    ('ncs-device-type', '?',
     ('string', []),
     ['leaf', 'leaf-list', 'list', 'container', 'refine']),

]
    
# tailf:annotate can have as substatement any tailf statement
# that can occur in a YANG statement.  this code figures this out.
ann_rules = [((tailf, 'annotate'), '*'),
             ((tailf, 'annotate-statement'), '*'),
             ('must', '*'),
             ('when', '?'),
             ('min-elements', '?'),
             ('max-elements', '?'),
             ('mandatory', '?'),
             ]
ann_add_to_stmts = []
for (stmt, occurance, (arg, rules), add_to_stmts) in tailf_stmts:
    if stmt in ['use-in', 'arg-type', 'substatement',
                'symlink', 'action']:
        continue
    if len(add_to_stmts) > 0:
        ann_rules.append(((tailf, stmt), occurance))
        for g in add_to_stmts:
            if g not in ann_add_to_stmts:
                ann_add_to_stmts.append(g)

tailf_stmts.append(
    ('annotate', '*',
     ('tailf-annotate-arg', ann_rules),
     ['module', 'submodule']))
tailf_stmts.append(
    ('annotate-statement', '*',
     ('tailf-annotate-statement-arg', ann_rules),
     ['module', 'submodule']))

def v_pre_import(ctx, s):
    if s.pos.ref in ctx.opts.tailf_annotate:
        return 'stop'

def v_import(ctx, s):
    pass
#    if s.i_module.pos.ref in ctx.opts.tailf_annotate:
#    if s.i_module.pos.ref in ctx.i_tailf_annotate:
        # don't report this import (from an annotation module) as a
        # circular reference
#        s.i_is_safe_import = True

def v_chk_sort_order(ctx, s):
    """Validate that the sort-order statement is set only for orderd-by
    system lists."""
    x = s.parent.search_one('ordered-by')
    if x is not None and x.arg == 'user':
        err_add(ctx.errors, s.pos, 'TAILF_BAD_SORT_ORDER', ())

def v_chk_default_ref(ctx, s):
    x = s.parent.search_one('mandatory')
    if x is not None and x.arg == 'true':
        err_add(ctx.errors, s.pos, 'TAILF_BAD_DEFAULT_REF', 'is mandatory')
    if s.parent.keyword == 'leaf':
        if s.parent.i_default is not None:
            err_add(ctx.errors, s.pos, 'TAILF_BAD_DEFAULT_REF', 'has default')
        if (hasattr(s.parent.parent, 'i_key') and
            s.parent in s.parent.parent.i_key):
            err_add(ctx.errors, s.pos, 'TAILF_BAD_DEFAULT_REF', 'is key')
    elif s.parent.keyword == 'refine':
        pass
    
    
def v_chk_index_leafs(ctx, s):
    found = []
    for x in s.arg.split():
        if x == '':
            continue
        ptr = attrsearch(x, 'arg', s.parent.parent.i_children)
        if x in found:
            err_add(ctx.errors, s.pos, 'TAILF_DUPLICATE_INDEX_LEAF', x)
            return
        elif ((ptr is None) or (ptr.keyword != 'leaf')):
            err_add(ctx.errors, s.pos, 'TAILF_BAD_INDEX_LEAF', (x, 'not found'))
            return
        mandatory = ptr.search_one('mandatory')
        if ((mandatory is None or mandatory.arg == 'false') and
            (not (hasattr(s.parent.parent, 'i_key') and
                  ptr in s.parent.parent.i_key))):
            err_add(ctx.errors, s.pos, 'TAILF_BAD_INDEX_LEAF',
                    (x, 'must be mandatory'))
            return
            
        found.append(x)

def v_chk_display_default_order(ctx, s):
    """Make sure that only one display-default-order statement is present
    on the list."""
    list_stmt = s.parent.parent
    if hasattr(list_stmt, 'i_tailf_display_default_order'):
        return
    for si in list_stmt.search((tailf, 'secondary-index')):
        x = si.search_one((tailf, 'display-default-order'))
        if x is not None and x != s:
            err_add(ctx.errors, s.pos, 'TAILF_MULTIPLE_DEFAULT_ORDER', x.pos)
            list_stmt.i_tailf_display_default_order = 'error'
            return

def v_chk_cache(ctx, s):
    if s.arg == "true":
        cfg = s.parent.search_one((tailf, 'config'))
        if ((cfg == None or cfg.arg != "false") and
            s.parent.parent.i_config == True):
            err_add(ctx.errors, s.pos, 'TAILF_CACHE_CONFIG', ())

def v_chk_store_in_cdb(ctx, s):
    pass

def v_chk_call_once(ctx, s):
    if (s.parent.parent.keyword == 'list' or
        (s.parent.parent.keyword == (tailf, 'annotate') and
         s.parent.parent.i_annotate_node.keyword == 'list')):
        pass
    else:
        err_add(ctx.errors, s.pos, 'TAILF_BAD_CALL_ONCE', ())

def v_chk_cli_show_no(ctx, s):
    x = s.parent.search_one('mandatory')
    if x is not None and x.arg == 'true':
        err_add(ctx.errors, s.pos, 'TAILF_BAD_CLI_SHOW_NO', ())

def v_chk_cli_autowizard(ctx, s):
    x = s.parent.search_one('mandatory')
    if x is not None and x.arg == 'true':
        err_add(ctx.errors, s.pos, 'TAILF_BAD_CLI_AUTOWIZARD', ())

def v_chk_instance_id(ctx, s):
    if s.parent.arg != 'instance-identifier':
        err_add(ctx.errors, s.pos, 'TAILF_BAD_INSTANCE_IDENTIFIER',
                util.keyword_to_str(s.raw_keyword))
        
tailf_value_length_types = \
    ((yang, 'object-identifier'),
     (yang, 'object-identifier-128'),
     (yang, 'phys-address'),
     (tailf, 'hex-list'),
     (tailf, 'octet-list'),
     (xs, 'hexBinary'))

def v_chk_value_length(ctx, s):
    if (not s.parent.i_is_validated or s.parent.i_typedef is None):
        return
        
    type_name = (s.parent.i_typedef.i_module.arg,
                 s.parent.i_typedef.arg)
    if type_name not in tailf_value_length_types:
        err_add(ctx.errors, s.pos, 'TAILF_BAD_VALUE_LENGTH_TYPE',
                (util.keyword_to_str(s.raw_keyword), s.parent.arg))
    lengths_spec = types.validate_length_expr(ctx.errors, s)
    if lengths_spec is not None:
        # override the length spec on the parent
        # this is ok, since we don't support 'length' on the lexical
        # value for these types.
        s.parent.i_is_derived = True
        s.parent.i_lengths = lengths_spec[0]
        # FIXME: should make a new str_to_val function or a new base
        # type spec, which returns a correct value, which the length
        # then can be verified against.
        s.parent.i_type_spec = types.LengthTypeSpec(s.parent.i_type_spec,
                                                    lengths_spec)
def v_chk_step(ctx, s):
    type_ = s.parent.parent
    if (not type_.i_is_validated or type_.i_type_spec is None):
        return

    # FIXME: we could extend the 'validate' function to take the step
    # into account, in order to validate default values etc
    
    v = type_.i_type_spec.str_to_val(ctx.errors, s.pos, s.arg)
    zero = type_.i_type_spec.str_to_val(ctx.errors, s.pos, "0")
    if v <= zero:
        err_add(ctx.errors, s.pos, 'TAILF_BAD_STEP', s.arg)
    #    type_.i_type_spec = StepTypeSpec(type_.i_type_spec, v)

def v_annotate_module(ctx, s):
    # called for each module/submodule loaded before expansion
    # check if the module/submodule is annotated
    for f in ctx.tailf_ann_mods:
        (m, _validated) = ctx.tailf_ann_mods[f]
        am = m.search_one((tailf, 'annotate-module'), s.arg)
        if am is not None:
            am.i_annotate_node = s
            for substmt in am.substmts:
                if substmt.keyword == (tailf, 'annotate-statement'):
                    v_annotate_statement(ctx, substmt)
                else:
                    s.substmts.append(substmt)
        
def v_annotate_statement(ctx, s):
    if not s.is_grammatically_valid:
        return
    if not hasattr(s.parent, 'i_annotate_node'):
        err_add(ctx.errors, s.pos, 'TAILF_BAD_ANNOTATE', ())
        return None
    node = s.parent.i_annotate_node
    toks = xpath.tokens(s.arg)
    toks = drop_all_ws(toks)
    # expect a statement name
    if toks[0][0] != 'name':
        err_add(ctx.errors, s.pos, 'SYNTAX_ERROR',
                "expected name of statement, got %s" % toks[0][1])
    stmt_name = toks[0][1]
    # if the next argument is not a predicate, it means that we'll annotate
    # the one and only statement of this type
    if len(toks) == 1 or toks[1][0] != '[':
        matches = node.search(stmt_name)
        if len(matches) == 0:
            err_add(ctx.errors, s.pos, 'TAILF_ANNOTATATION_ERROR',
                    "found no '%s' statements as children of %s %s at %s"
                    % (stmt_name, node.raw_keyword, node.arg, node.pos))
            return
        if len(matches) > 1:
            err_add(ctx.errors, s.pos, 'TAILF_ANNOTATATION_ERROR',
                    "found too many '%s' statements as children of %s %s at %s"
                    % (stmt_name, node.raw_keyword, node.arg, node.pos))
            return
        node = matches[0]
    elif len(toks) == 1:
        return
    else:
        toks = toks[1:]
        if toks[0][0] == '[':
            # skip the argument name and equality sign
            toks = toks[3:]
            stmt_arg = toks[0][1]
            if stmt_arg[0] == "'" or stmt_arg[0] == '"':
                stmt_arg = stmt_arg[1:-1]
            else:
                err_add(ctx.errors, s.pos, 'SYNTAX_ERROR',
                        "expected quoted name of statement, got %s" % toks[0][1])
                return

            match = node.search_one(stmt_name, stmt_arg)
            if match is None:
                err_add(ctx.errors, s.pos, 'TAILF_ANNOTATATION_ERROR',
                        "found no '%s %s' statements as children of %s %s at %s"
                        % (stmt_name, stmt_arg, node.raw_keyword,
                           node.arg, node.pos))
                return
            else:
                node = match
        else:
            # cannot end up here
            return


    s.i_annotate_node = node
    for substmt in s.substmts:
        if substmt.keyword == (tailf, 'annotate-statement'):
            v_annotate_statement(ctx, substmt)
        else:
            if (util.is_prefixed(substmt.keyword) and
                substmt.keyword[0] == tailf):
                g = util.keysearch(substmt.keyword[1], 0, tailf_stmts)
                if g is not None:
                    if node.keyword not in g[3]:
                        error.err_add(
                            ctx.errors, substmt.pos,
                            'TAILF_INVALID_ANNOTATE', 
                            (util.keyword_to_str(substmt.raw_keyword),
                             util.keyword_to_str(node.raw_keyword)))
                        return None
                    
            node.substmts.append(substmt)

def drop_all_ws(toks):
    res = []
    for tok in toks:
        if tok[0] != 'whitespace':
            res.append(tok)
    return res
        
def v_chk_annotate(ctx, s):
    if s.arg == '*':
        # special case - apply annotation to all children
        if (hasattr(s.parent, 'i_annotate_node') and
            hasattr(s.parent.i_annotate_node, 'i_children')):
            nodes = s.parent.i_annotate_node.i_children
        else:
            err_add(ctx.errors, s.pos, 'TAILF_BAD_ANNOTATE', ())
            return None
    else:
        node = find_target_node(ctx, s)
        if node is not None:
            nodes = [node]
        else:
            nodes = []
    for node in nodes:
        for substmt in s.substmts:
            if substmt.keyword != (tailf,'annotate'):
                if (util.is_prefixed(substmt.keyword) and
                    substmt.keyword[0] == tailf):
                    g = util.keysearch(substmt.keyword[1], 0, tailf_stmts)
                    if g is not None:
                        if node.keyword not in g[3]:
                            error.err_add(
                                ctx.errors, substmt.pos,
                                'TAILF_INVALID_ANNOTATE', 
                                (util.keyword_to_str(substmt.raw_keyword),
                                 util.keyword_to_str(node.raw_keyword)))
                            return None
                        
                node.substmts.append(substmt)
        
def v_add_annotations(ctx, s):
    for filename in ctx.opts.tailf_annotate:
        try:
            fd = file(filename)
            text = fd.read()
        except IOError, ex:
            sys.stderr.write("error %s: %s\n" % (filename, str(ex)))
            sys.exit(1)
        ctx.add_module(filename, text)
    ctx.i_tailf_annotate = ctx.opts.tailf_annotate
    ctx.opts.tailf_annotate = []

def find_target_node(ctx, stmt):
    if stmt.arg.startswith('/'):
        is_absolute = True
        arg = stmt.arg
    else:
        is_absolute = False
        arg = "/" + stmt.arg
    # parse the path into a list of two-tuples of (prefix,identifier)
    path = [(m[1], m[2]) for m in syntax.re_schema_node_id_part.findall(arg)]
    # find the module of the first node in the path 
    (prefix, identifier) = path[0]
    module = statements.prefix_to_module(stmt.i_module, prefix,
                                         stmt.pos, ctx.errors)
    if module is None:
        # error is reported by prefix_to_module
        return None
    if is_absolute:
        # find the first node
        node = statements.search_data_keyword_child(module.i_children,
                                                    module.i_modulename,
                                                    identifier)
        if node is None: 
            # check all our submodules
            for inc in module.search('include'):
                submod = ctx.get_module(inc.arg)
                if submod is not None:
                    node = statements.search_data_keyword_child(
                        submod.i_children,
                        submod.i_modulename,
                        identifier)
                    if node is not None:
                        break
            if node is None:
                err_add(ctx.errors, stmt.pos, 'NODE_NOT_FOUND',
                        (module.arg, identifier))
                return None
        path = path[1:]
    else:
        if hasattr(stmt.parent, 'i_annotate_node'):
            node = stmt.parent.i_annotate_node
        else:
            err_add(ctx.errors, stmt.pos, 'TAILF_BAD_ANNOTATE', ())
            return None

    # then recurse down the path
    for (prefix, identifier) in path:
        module = statements.prefix_to_module(stmt.i_module, prefix, stmt.pos,
                                             ctx.errors)
        if module is None:
            return None
        child = None
        if hasattr(node, 'i_children'):
            child = statements.search_data_keyword_child(node.i_children,
                                                         module.i_modulename,
                                                         identifier)
            if child is None:
                if hasattr(node, 'i_not_supported'):
                    child = statements.search_data_keyword_child(
                        node.i_not_supported,
                        module.i_modulename,
                        identifier)
        if child is None:
            err_add(ctx.errors, stmt.pos, 'NODE_NOT_FOUND',
                    (module.arg, identifier))
            return None
        node = child

    stmt.i_annotate_node = node
    return node

def v_chk_action(ctx, s):
    s.i_tree_flags_str = "-x"
    if (s.search_one((tailf, 'actionpoint')) is None and
        s.search_one((tailf, 'exec')) is None):
        err_add(ctx.errors, s.pos, 'TAILF_NEED_ACTIONPOINT',
                (util.keyword_to_str(s.keyword), s.arg))
    def chk(stmt):
        for k in ['cli-drop-node-name', 'cli-sequence-commands']:
            if stmt.search_one((tailf, k)) is not None:
                err_add(ctx.errors, s.pos, 'TAILF_NOT_IN_ACTION', k)
    statements.iterate_i_children(s, chk)

def v_chk_raw_xml(ctx, s):
    if s.parent.parent.keyword != 'rpc':
        err_add(ctx.errors, s.pos, 'SYNTAX_ERROR',
                "raw-xml is only allowed in rpc definitions")

def v_chk_path(ctx, s):
    path_spec = types.validate_path_expr(ctx.errors, s)
## See #3680 - maybe the correct solution is a new keyword on the symlink,
## tailf:no-check or a keyword tailf:cs-path...
    if ctx.opts.tailf_sanitize:
        # when we do sanitization, we require symlink paths to point
        # to nodes we know, so that we can copy them
        x = statements.validate_leafref_path(ctx, s.parent, path_spec, s,
                                             accept_non_leaf_target = True)
        if x is None:
            s.tailf_target_node = None
            return
        ptr, expanded_path, path_list = x
        s.tailf_target_node = ptr
    else:
        # for now, just check the prefixes used
        if path_spec is None: # e.g. invalid path
            return
        (up, dn, derefup, derefdn) = path_spec
        for identifier in dn:
            if util.is_prefixed(identifier):
                (prefix, _name) = identifier
                pmodule = statements.prefix_to_module(s.i_module, prefix, s.pos,
                                                      ctx.errors)

def v_chk_typedef(ctx, s):
    if s.i_module.arg == 'tailf-xsd-types':
        if s.arg == 'float':
            t = s.search_one('type')
            t.i_type_spec = FloatTypeSpec(32)
        elif s.arg == 'double':
            t = s.search_one('type')
            t.i_type_spec = FloatTypeSpec(64)
        elif s.arg == 'decimal':
            t = s.search_one('type')
            t.i_type_spec = FloatTypeSpec(64)
    elif s.i_module.arg == 'tailf-common':
        if s.arg == 'md5-digest-string':
            t = s.search_one('type')
            t.i_type_spec = Md5DigestStringTypeSpec()
            
def v_chk_when(ctx, s):
    if ctx.opts.format != 'cs':
        return
    w = s.search_one('when')
    if w is None:
        return
    w.i_tailf_tagpath = xpath_get_tagpath(w.arg)
    if hasattr(s, 'i_config') and s.i_config != False:
        if (w.i_tailf_tagpath is None and
            w.search_one((tailf, 'dependency')) is None):
            err_add(ctx.errors, w.pos, 'TAILF_WHEN_NEED_DEPENDENCY', ())

def v_chk_must(ctx, s):
    if ctx.opts.format != 'cs':
        return
    for m in s.search('must'):
        m.i_tailf_tagpath = xpath_get_tagpath(m.arg)
        if hasattr(s, 'i_config') and s.i_config != False:
            if (m.i_tailf_tagpath is None and
                m.search_one((tailf, 'dependency')) is None):
                err_add(ctx.errors, m.pos, 'TAILF_MUST_NEED_DEPENDENCY', ())

def v_chk_key_default(ctx, s):
    ch = s.search_one((tailf, 'key-default'))
    if ch is not None:
        if (not (hasattr(s.parent, 'i_key') and
                 s in s.parent.i_key)):
            err_add(ctx.errors, ch.pos, 'TAILF_BAD_KEY_DEFAULT', ())
            return
        i = s.parent.i_key.index(s)
        for k in s.parent.i_key[i+1:]:
            if k.search_one((tailf, 'key-default')) is None:
                err_add(ctx.errors, k.pos, 'TAILF_NEED_KEY_DEFAULT', (ch.pos))

def v_chk_info(ctx, s):
    if s.parent.keyword == 'type' and s.parent.arg == 'empty':
        err_add(ctx.errors, s.pos, 'SYNTAX_ERROR',
                "cannot have tailf:info in a type empty leaf.")

def v_chk_info_html(ctx, s):
    if s.parent.search_one((tailf, 'info')) is not None:
        err_add(ctx.errors, s.pos, 'SYNTAX_ERROR',
                "cannot have tailf:info-html and tailf:info at the same time")
    if s.parent.keyword == 'type' and s.parent.arg == 'empty':
        err_add(ctx.errors, s.pos, 'SYNTAX_ERROR',
                "cannot have tailf:info-html in a type empty leaf.")

def v_chk_cli_is_mode(ctx, s):
    if (s.parent.keyword == 'container' and
        s.parent.search_one((tailf, 'cli-add-mode')) == None):
        err_add(ctx.errors, s.pos, 'TAILF_CONTAINER_MUST_BE_MODE', s.parent.arg)
    if (s.parent.keyword == 'list' and
        s.parent.search_one((tailf, 'cli-suppress-mode')) != None):
        err_add(ctx.errors, s.pos, 'TAILF_LIST_MUST_BE_MODE', s.parent.arg)

def v_chk_cli_is_key_leaf(ctx, s):
    kwds = [(tailf, 'cli-allow-range'),
            (tailf, 'cli-suppress-range'),
            (tailf, 'cli-custom-range'),
            (tailf, 'cli-multi-word-key')]
    for kwd in kwds:
        ch = s.search_one(kwd)
        if ch is not None:
            if (s.parent.keyword == 'list' and
                s in s.parent.i_key):
                pass
            else:
                err_add(ctx.errors, ch.pos, 'TAILF_MUST_BE_KEY', ch.keyword[1])
            return

def v_chk_cli_is_config(ctx, s):
    if hasattr(s.parent, 'i_config') and s.parent.i_config == False:
        err_add(ctx.errors, s.pos, 'TAILF_MUST_BE_CONFIG', s.keyword[1])

def v_chk_cli_prefix_key(ctx, s):
    if (s.parent.keyword == 'leaf-list' and
        s.parent.search_one((tailf, 'cli-range-list-syntax')) == None):
        err_add(ctx.errors, s.pos, 'TAILF_LEAF_LIST_MUST_HAVE_RANGE', 
                s.keyword[1])
    else:
        pass
    return
            
def v_chk_cli_range_type(ctx, s):
    s.i_type_namespace = None
    s.i_type_name = None
    name = s.arg
    if name.find(":") == -1:
        prefix = None
    else:
        [prefix, name] = name.split(':', 1)
    if prefix is None or s.i_module.i_prefix == prefix:
        # check local typedefs
        pmodule = s.i_module
        typedef = statements.search_typedef(s, name)
    else:
        # this is a prefixed name, check the imported modules
        pmodule = statements.prefix_to_module(s.i_module, prefix, s.pos,
                                              ctx.errors)
        if pmodule is None:
            return
        typedef = statements.search_typedef(pmodule, name)
    if typedef == None:
        err_add(ctx.errors, s.pos, 'TYPE_NOT_FOUND', (name, pmodule.arg))
        return
    s.i_type_namespace = typedef.i_module.search_one('namespace').arg
    s.i_type_name = name
    
def v_chk_cli_range_list_syntax(ctx, s):
    def chk_type(type_, errcode):
        return
        # if type_ is None or type_.i_type_spec is None:
        #     return
        # if 'range' not in type_.i_type_spec.restrictions():
        #     err_add(ctx.errors, s.pos, errcode, s.keyword[1])

    if s.parent.keyword == 'leaf-list':
        type_ = s.parent.search_one('type')
        chk_type(type_, 'TAILF_BAD_INT_TYPE')
    elif s.parent.keyword == 'list':
        if len(s.parent.i_key) != 1:
            err_add(ctx.errors, s.pos, 'TAILF_BAD_KEY', s.keyword[1])
            return
        type_ = s.parent.i_key[0].search_one('type')
        chk_type(type_, 'TAILF_BAD_KEY')

def v_chk_display_when(ctx, s):
    statements.v_reference_must(ctx, s)

def v_chk_writable(ctx, s):
    if (s.arg == 'true' and
        hasattr(s.parent, 'i_config') and s.parent.i_config == True):
        err_add(ctx.errors, s.pos, 'TAILF_MUST_BE_CONFIG_FALSE', s.keyword[1])
        
def v_chk_snmp_delete_value(ctx, s):
    # make sure it is config
    if hasattr(s.parent, 'i_config') and s.parent.i_config == False:
        err_add(ctx.errors, s.pos, 'TAILF_MUST_BE_CONFIG', s.keyword[1])
    # make sure this leaf is not mandatory
    if (s.parent.search_one('mandatory', 'true') is not None or
        hasattr(s.parent, 'i_is_key')):
        err_add(ctx.errors, s.pos, 'TAILF_MUST_BE_OPTIONAL', s.keyword[1])
    # first convert the given value
    type_ = s.parent.search_one('type')
    if type_.i_type_spec is None:
        return
    if type_.arg == 'enumeration':
        try:
            val = int(s.arg)
            if util.keysearch(val, 1, type_.i_type_spec.enums):
                err_add(ctx.errors, s.pos,
                        'TAILF_BAD_SNMP_DELETE_VALUE_ENUM', ())
        except:
            val = None
            err_add(ctx.errors, s.pos,
                    'TAILF_BAD_SNMP_DELETE_VALUE_ENUM', ())
    else:
        val = type_.i_type_spec.str_to_val(ctx.errors, s.pos, s.arg)
    s.i_tailf_val = val
    if val is not None:
        # then make sure it is not a valid YANG value
        tmp_errors = []
        if type_.i_type_spec.validate(tmp_errors, s.pos, val, '') == True:
            err_add(ctx.errors, s.pos, 'TAILF_BAD_DELETE_VALUE', ())

def v_chk_non_strict_leafref(ctx, s):
    path = s.search_one('path')
    if path.is_grammatically_valid == True:
        path_spec = types.validate_path_expr(ctx.errors, path)
        if path_spec is not None:
            path_type_spec = types.PathTypeSpec(path_spec, path, path.pos)
            x = statements.validate_leafref_path(ctx, s.parent,
                                                 path_type_spec.path_spec,
                                                 path_type_spec.path_,
                                                 accept_non_leaf_target=True,
                                                 accept_non_config_target=True)
            if x is None:
                return
            ptr, expanded_path, path_list = x
            if ptr is None:
                return
            ptr_type = ptr.search_one('type')
            parent_type = s.parent.search_one('type')
            if ptr_type is None or parent_type is None:
                return
            if not((ptr_type.i_typedef is not None and
                    ptr_type.i_typedef == parent_type.i_typedef) or
                   (ptr_type.arg == parent_type.arg)):
                err_add(ctx.errors, s.pos, 'TAILF_BAD_LEAFREF_TYPE',
                        (ptr_type.arg, parent_type.arg))
                
            path_type_spec.i_target_node = ptr
            path_type_spec.i_expanded_path = expanded_path
            path_type_spec.i_path_list = path_list
            s.parent.i_leafref_ptr = (ptr, path_type_spec.pos)
            s.parent.tailf_non_strict_leafref = path_type_spec


class FloatTypeSpec(types.TypeSpec):
    def __init__(self, bits):
        types.TypeSpec.__init__(self)
        self.bits = bits

    def str_to_val(self, errors, pos, str):
        try:
            if str in ['min', 'max']:
                return str
            return float(str)
        except ValueError:
            err_add(errors, pos, 'TYPE_VALUE',
                    (str, self.definition, 'not a float'))
            return None

    # FIXME: validate 32/64 bit floats

    def restrictions(self):
        return ['range']


class Md5DigestStringTypeSpec(types.TypeSpec):
    def __init__(self):
        types.TypeSpec.__init__(self)

    def str_to_val(self, errors, pos, str):
        if len(str) > 0 and re_tailf_md5_digest.search(str) is None:
            err_add(errors, pos, 'TYPE_VALUE',
                    (str, self.definition,
                     'not a valid md5-digest-string default value'))
            return None
        else:
            return str

class StepTypeSpec(types.TypeSpec):
    def __init__(self, base, step):
        types.TypeSpec.__init__(self)
        self.base = base
        self.step = step

    def str_to_val(self, errors, pos, str):
        return self.base.str_to_val(errors, pos, str)

    def validate(self, errors, pos, val, errstr=''):
        if self.base.validate(errors, pos, val, errstr) == False:
            return False
        if (val - self.base.min) % self.step != 0:
            err_add(errors, pos, 'TYPE_VALUE',
                    (val, self.definition, 'does not match the tailf:step'))
            
        return True

    def restrictions(self):
        return self.base.restrictions()

def xpath_is_tagpath(s):
    oktoks = ('/', 'name', '..', '.')
    for (tokname, x) in xpath.tokens(s):
        if tokname not in oktoks:
            return False
    return True

# ad-hoc code to do auto-dependencies; try to figure out
# if the xpath refers to a single tagpath.  handles:
#   /x/y/z
#   ../../x/y/z
#   ../x/y = 2
#   ../../x/y/z != 'foo'
# if we had a xpath parser we could do more here...
def xpath_get_tagpath(s):
    pathtoks = ('/', 'name', '..', '.')
    toks = xpath.tokens(s)
    i = 0
    is_path = True
    while is_path and i < len(toks):
        (tokname, x) = toks[i]
        if tokname not in pathtoks:
            is_path = False
        else:
            i += 1
    if i == 0 and is_path == False:
        return None
    # ok, we have a path in the beginning
    pathstr = ''.join([x for (_, x) in toks[:i]])
    # check if what follows is a simple boolean comparison
    oktoks = ('whitespace', 'literal', 'number',
              '=','!=', '>', '>=', '<', '<=')
    for (tokname, x) in toks[i:]:
        if tokname not in oktoks:
            return None
    return pathstr

# Expand tailf:symlink
# Inline types from tailf-common (NYI)
# Remove anything marked as tailf:hidden "full"
# Optionally keep tailf:action
# Optionally keep tailf:info
# Remove all other tailf: statements.
# Remove the import of tailf-common, if all refereneces to it has
#   been removed.

# FIXME: keep comments!!
def sanitize(ctx, m):
    keep_import = sanitize_tree(ctx, m, m)
    if not keep_import:
        # remove the import of tailf-common,
        i = m.search_one('import', 'tailf-common')
        if i is not None:
            idx = m.substmts.index(i)
            del m.substmts[idx]

_body_stmts = ['container', 'list', 'leaf', 'leaf-list',
               'augment',
               'rpc', 'notification']

def sanitize_tree(ctx, module, stmt, keep_import=False):
    def sanitize_node(s, keep_import):
        if s.keyword in ('leaf', 'leaflist', 'typedef'):
            type_ = s.search_one('type')
            if (type_ is not None and type_.i_typedef is not None and
                type_.i_typedef.i_module.arg == 'tailf-common'):
                if ctx.opts.tailf_keep_typedefs:
                    keep_import = True
                else:
                    type_.arg = type_.i_typedef.arg
                    if module.search_one('typedef', type_.i_typedef.arg) is None:
                        copy_typedef(module, type_.i_typedef)
                        keep_import = False
                        
        keep_import = sanitize_tree(ctx, module, s, keep_import)
        return keep_import
        
    def tr(stmt, i_orig_module, no_default=True):
        if hasattr(stmt, 'tailf_tr'):
            return stmt.arg
        else:
            stmt.tailf_tr = True
            if no_default:
                defprefix = module.i_prefix
            else:
                defprefix = module.search_one('import', i_orig_module.arg).search_one('prefix').arg
            return translate_prefixes(stmt.arg,
                                      i_orig_module.i_prefixes,
                                      i_orig_module.i_prefix,
                                      i_orig_module,
                                      module.i_prefixes,
                                      defprefix,
                                      module,
                                      no_default)

    def patch_node(stmt, symlink_target):

        def patch_prefixes(stmt):
            for ch in stmt.substmts:
                if ch.keyword == 'when' and not ctx.opts.tailf_keep_when:
                    stmt.substmts.remove(ch)
                elif ch.keyword == 'must' and not ctx.opts.tailf_keep_must:
                    stmt.substmts.remove(ch)
                elif ch.keyword == 'path':
                    (ref, _pos) = ch.parent.parent.i_leafref_ptr
                    if (is_child(ref, symlink_target) and
                        not is_hidden_full(ref)):
                        ch.arg = tr(ch, ch.i_orig_module)
                    else:
                        t_arg = tr(ref.search_one('type'), ch.i_orig_module)
                        ch.parent.arg = t_arg
                        ch.parent.substmts.remove(ch)
                elif ch.keyword == 'type':
                    if ch.i_typedef is not None:
                        # FIXME: tr is wrong here.  if it doesn't have
                        # a prefix, we should add a prefix to the 
                        # imported module (san2)
                        ch.arg = tr(ch, ch.i_orig_module, no_default=False)
                patch_prefixes(ch)

        # copy nodes from i_children into substmts, if they are not
        # already present
        if hasattr(stmt, 'i_children'):
            i = 0
            for ch in stmt.i_children:
                patch_node(ch, symlink_target)
                if stmt.search_one(ch.keyword, ch.arg) is None:
                    stmt.substmts.insert(i, ch)
                    i += 1
                i += 1
            patch_prefixes(stmt)

    # copy the list so that we can delete from the original
    list_ = []
    for s in stmt.substmts:
        list_.append(s)

    for s in list_:
        if util.is_prefixed(s.keyword) and s.keyword[0] == 'tailf-common':
            if s.keyword[1] == 'symlink':
                ptr = s.search_one((tailf, 'path')).tailf_target_node
                if ptr is None:
                    # error in symlink; we just remove it
                    stmt.substmts.remove(s)
                else:
                    # copy the target.  we also need to make sure
                    # all augmented nodes are present in the substms
                    # list.  further, we must fix prefixes and possibly
                    # add new imports to our module
                    new = ptr.copy()
                    new.arg = s.arg
                    patch_node(new, ptr)
                    keep_import = sanitize_node(new, keep_import)
                    # replace the symlink statement with the target
                    idx = stmt.substmts.index(s)
                    stmt.substmts[idx] = new
            elif (s.keyword[1] in ['info', 'info-html'] and
                  ctx.opts.tailf_keep_info):
                m = module.search_one('import', 'tailf-common')
                tailf_prefix = m.search_one('prefix').arg
                s.raw_keyword = (tailf_prefix, s.keyword[1])
                keep_import = True
            elif s.keyword[1] == 'action' and ctx.opts.tailf_keep_actions:
                m = module.search_one('import', 'tailf-common')
                tailf_prefix = m.search_one('prefix').arg
                s.raw_keyword = (tailf_prefix, s.keyword[1])
                sanitize_node(s, keep_import)
                keep_import = True
            else:
                stmt.substmts.remove(s)
        elif s.search_one((tailf, 'hidden'), 'full') is not None:
            stmt.substmts.remove(s)
        elif (ctx.opts.tailf_remove_body and
              s.parent == module and s.keyword in _body_stmts):
            stmt.substmts.remove(s)
        else:
            keep_import = sanitize_node(s, keep_import)
    return keep_import

def copy_typedef(module, typedef):
    module.substmts.append(typedef)

def translate_prefixes(s, oldmap, oldmodprefix, oldmod,
                       newmap0, newmodprefix, newmod,
                       no_default):
    newmodname = newmod.arg
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

    def gen_new_import(modname, revision):
        i = 0
        pre = "p" + str(i)
        while pre in newmod.i_prefixes:
                i = i + 1
                pre = "p" + str(i)
        newmod.i_prefixes[pre] = (modname, revision)
        imp = statements.Statement(newmod, newmod, None, 'import', modname)
        prefix = statements.Statement(newmod, imp, None, 'prefix', pre)
        imp.substmts.append(prefix)
        if revision is not None:
            rev = statements.Statement(newmod, imp, None, 'revision-date',
                                       revision)
            imp.substmts.append(rev)
        # we know that there is at least one import (for the symlink target)
        first_import = newmod.search_one('import')
        idx = newmod.substmts.index(first_import)
        newmod.substmts.insert(idx, imp)
        return pre

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
                    if oldmap[prefix] in newmap:
                        newprefix = newmap[oldmap[prefix]]
                    else:
                        # add an import
                        (modname, revision) = oldmap[prefix]
                        newprefix = gen_new_import(modname, revision)
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

def is_child(s, p):
    if s.parent is None:
        return False
    if s.parent == p:
        return True
    return is_child(s.parent, p)

def is_hidden_full(s):
    if s.search_one((tailf, 'hidden'), 'full') is not None:
        return True
    elif s.parent is not None:
        return is_hidden_full(s.parent)
    else:
        return False

def v_chk_dependency(ctx, stmt):
    statements.v_xpath(ctx, stmt)

def v_chk_snmp_ned_accessible_column(ctx, s):
    if (s.parent.keyword == (tailf, 'annotate') and
        s.parent.i_annotate_node.keyword == 'list'):
        list_node = s.parent.i_annotate_node
    else:
        list_node = s.parent
    x = list_node.search_one('leaf', s.arg)
    if x is None:
        try:
            s.i_tailf_accessible_column = int(s.arg)
        except:
            err_add(ctx.errors, s.pos, 'TAILF_BAD_COLUMN',
                    (s.arg, list_node.arg))
    else:
        oid = x.search_one((tailf, 'snmp-oid'))
        if oid is not None:
            s.i_tailf_accessible_column = int(oid.arg.split('.')[-1])
        elif hasattr(x, 'i_smi_oid'):
            s.i_tailf_accessible_column = x.i_smi_oid[-1]
        else:
            err_add(ctx.errors, s.pos, 'TAILF_NO_OID', s.arg)

def v_chk_snmp_ned_num_instances(ctx, s):
    path_spec = types.validate_path_expr(ctx.errors, s)
    x = statements.validate_leafref_path(ctx, s.parent, path_spec, s,
                                         accept_non_leaf_target = False)
    if x is None:
        s.tailf_target_node = None
        return
    ptr, expanded_path, path_list = x
    s.tailf_target_node = ptr

def v_chk_snmp_ned_set_before_row_modification(ctx, s):
    # first convert the given value
    type_ = s.parent.search_one('type')
    if type_ is None or type_.i_type_spec is None:
        return
    else:
        val = type_.i_type_spec.str_to_val(ctx.errors, s.pos, s.arg)
    if val is not None:
        # then check if it is a valid YANG value
        tmp_errors = []
        if type_.i_type_spec.validate(tmp_errors, s.pos, val, '') != True:
            err_add(ctx.errors, s.pos, 'TAILF_BAD_VALUE', ())
    # we need to store a string as a quoted string, an integer as an integer,
    # and an enum as an integer
    if hasattr(type_.i_type_spec, 'enums'):
        s.i_tailf_val = type_.i_type_spec.get_value(s.arg)
    elif type(val) == type(''):
        s.i_tailf_val = '"' + val + '"'

def v_chk_snmp_ned_modification_dependent(ctx, s):
    # first, make sure this is set on a column in a table
    if (s.parent == None or s.parent.parent == None or
        s.parent.parent.keyword != 'list'):
        # FIXME: report error
        return
    # make sure there is a column, in this table, or in the table this
    # table (sparsely) augments, which has
    # tailf:snmp-ned-set-before-row-modification.

    def table_set_before_row_mod(tab):
        for col in tab.search('leaf'):
            x = col.search_one((tailf, 'snmp-ned-set-before-row-modification'))
            if x is not None:
                return x
        return None

    def search_set_before_row_mod(tab):
        x = table_set_before_row_mod(tab)
        if x is not None:
            return x, len(tab.i_key)
        else:
            # figure out if this is a sparse augment
            first_key = tab.i_key[0]
            if (hasattr(first_key, 'i_leafref_ptr') and
                first_key.i_leafref_ptr is not None):
                (ptr, x) = first_key.i_leafref_ptr
                if hasattr(ptr, 'i_key'):
                    return search_set_before_row_mod(ptr.parent)
            else:
                return None, None
    x, nkeys = search_set_before_row_mod(s.parent.parent)
    s.i_tailf_set_before_leaf = x
    s.i_tailf_set_before_nkeys = nkeys

package com.tailf.jnc;

import java.util.ArrayList;

/**
 * A path expression. This is a small subset of the W3C recommendations of
 * XPath 1.0 expression evaluator and parser.
 * <p>
 * The result of evaluating an expression is a set of {@link Element} nodes
 * that fulfill the location steps, with node tests, and predicates. The path
 * expression operates on an element tree.
 * <p>
 * Example:
 * 
 * <pre>
 * Path expr = new Path(&quot;/hosts/host[name='kalle']/ip&quot;);
 * NodeSet s = path.eval(element_tree);
 * </pre>
 * 
 **/

public class Path {

    /**
     * Constructor for a Path from a path expression string.
     */
    public Path(String pathStr) throws JNCException {
        create = false;
        this.pathStr = pathStr;
        locationSteps = parse(tokenize(pathStr));
    }

    /**
     * Dummy constructor
     */
    Path() {
    }

    /**
     * Evaluates the Path given a contextNode.
     * <p>
     * Makes a selection by traversing the locationSteps, return an ArrayList
     * (NodeSet) of Element nodes.
     * 
     * @param contextNode The context node to evaluate expressions on
     * @return A nodeSet of elements
     */
    public NodeSet eval(Element contextNode) throws JNCException {
        trace("eval(): " + this);
        NodeSet nodeSet = new NodeSet();
        nodeSet.add(contextNode);
        for (final LocationStep step : locationSteps) {
            nodeSet = step.step(nodeSet);
        }
        return nodeSet;
    }

    /**
     * Evaluate the Path given a NodeSet.
     * <p>
     * Makes a selection by traversing ONE locationStep. Returns an updated
     * NodeSet of Element nodes.
     */
    NodeSet evalStep(NodeSet nodeSet, int step) throws JNCException {
        if (step < 0 || step >= locationSteps.size()) {
            throw new JNCException(JNCException.PATH_ERROR,
                    "cannot eval location step: " + step + " in path");
        }
        final LocationStep locStep = locationSteps.get(step);
        trace("evalStep(): step=" + step + ", " + locStep);
        nodeSet = locStep.step(nodeSet);
        return nodeSet;
    }

    /**
     * Return the number of LocationSteps in this Path. Steps are numbered from
     * 0 to NumberOfSteps-1. Use with evalStep above.
     */
    int steps() {
        return locationSteps.size();
    }

    /* Internal section */

    /** AXIS in LocationStep */
    static final int AXIS_CHILD = 1;
    static final int AXIS_SELF = 2;
    static final int AXIS_PARENT = 3;
    static final int AXIS_ROOT = 4;

    /**
     * Flag says if it's a 'path' or a 'create path' expression.
     */
    boolean create = false;

    /**
     * The list of location steps (LocationStep).
     */
    ArrayList<LocationStep> locationSteps;

    /**
     * The original path string.
     */
    String pathStr;

    /**
     * Location step.
     * <p>
     * A location step has three parts:
     * <ul>
     * <li>an axis, which specifies the tree relationship between the nodes
     * selected by the location step and the context node,
     * <li>
     * a node test, which specifies the node type and expanded-name of the
     * nodes selected by the location step, and
     * <li>
     * zero or more predicates, which use arbitrary expressions to further
     * refine the set of nodes selected by the location step.
     * </ul>
     */
    class LocationStep {

        int axis;
        String name;
        String prefix;
        ArrayList<Expr> predicates; // list of Expr (Predicates)

        LocationStep(int axis) {
            this.axis = axis;
        }

        LocationStep(int axis, String name) {
            this.axis = axis;
            this.name = name;
        }

        LocationStep(int axis, String prefix, String name) {
            this.axis = axis;
            this.prefix = prefix;
            this.name = name;
        }

        /**
         * Step down
         */
        NodeSet step(NodeSet nodes) throws JNCException {
            final NodeSet result = new NodeSet();
            for (int i = 0; i < nodes.size(); i++) {
                final Element node = nodes.getElement(i);
                /** select axis */
                switch (axis) {
                case AXIS_CHILD:
                    if (node.children != null) {
                        result.addAll(nodeTest(node.children));
                    }
                    break;
                case AXIS_PARENT:
                    if (node.parent != null) {
                        result.addAll(nodeTest(new NodeSet(node.parent)));
                    }
                    break;
                case AXIS_SELF:
                    result.addAll(nodeTest(new NodeSet(node)));
                    break;
                }
                // FIXME: What about AXIS_ROOT and other cases?
            }
            return result;
        }

        /**
         * perform nodeTest on nodeSet. (only NameTest) since all nodes are
         * simplified to be Elements
         */
        private NodeSet nodeTest(NodeSet nodeSet) throws JNCException {
            NodeSet result = new NodeSet();
            /**
             * A simple "NameTest" Filter away those with wrong name
             */
            for (int i = 0; i < nodeSet.size(); i++) {
                final Element node = nodeSet.getElement(i);
                if (node.name.equals(name)) {
                    /* check namespace also, if prefix is given */
                    String namespace;
                    if (prefix != null) {
                        namespace = node.lookupContextPrefix(prefix);
                        if (node.namespace.equals(namespace)) {
                            result.add(node);
                        }
                    } else {
                        // skip namespace test when prefix is not given
                        result.add(node);
                    }
                }
            }
            if (result.size() == 0) {
                return result;
            }

            /* Apply predicates on nodeSet in order, if any. Rebuild new
             * nodeSet, for each predicate level, since contextSet is needed as
             * an argument we do not want to change it. */
            NodeSet contextSet;
            if (predicates != null) {
                for (int i = 0; i < predicates.size(); i++) {
                    final Expr p = predicates.get(i);
                    contextSet = result;
                    result = new NodeSet();
                    for (int j = 0; j < contextSet.size(); j++) {
                        final Element node = contextSet.getElement(j);
                        if (p.eval(node, contextSet).booleanValue()) {
                            result.add(node);
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Creates a Element node from this LocationStep. Used from
         * PathCreate.eval() to build a structure of elements.
         */
        Element createElem(PrefixMap prefixMap, Element parent)
                throws JNCException {
            trace("createElem() from " + this);
            switch (axis) {
            case AXIS_ROOT:
                return null;
            case AXIS_CHILD:
                String ns;
                if (prefix == null) {
                    if (parent != null) {
                        ns = parent.namespace;
                    } else {
                        ns = prefixMap.prefixToNs("");
                    }
                } else {
                    ns = prefixMap.prefixToNs(prefix);
                }
                if (ns == null) {
                    throw new JNCException(JNCException.PATH_CREATE_ERROR,
                            "missing namespace for prefix: \"" + prefix
                                    + "\"");
                }
                final Element elem = new Element(ns, name);
                // need to do createElem on predicates as well
                if (predicates != null) {
                    for (int i = 0; i < predicates.size(); i++) {
                        final Expr expr = predicates.get(i);
                        expr.evalCreate(elem);
                    }
                }
                return elem;
            case AXIS_SELF:

            case AXIS_PARENT:
            default:
                throw new JNCException(JNCException.PATH_CREATE_ERROR,
                        "unknown axis in create path");
            }
        }

        /**
         * Return the string representation of this LocationStep.
         */
        @Override
        public String toString() {
            String s = "LocationStep{";
            switch (axis) {
            case AXIS_CHILD:
                s = s + "AXIS_CHILD";
                break;
            case AXIS_PARENT:
                s = s + "AXIS_PARENT";
                break;
            case AXIS_SELF:
                s = s + "AXIS_SELF";
                break;
            case AXIS_ROOT:
                s = s + "AXIS_ROOT";
                break;
            default:
                s = s + "AXIS_UNKNOWN(" + axis + ")";
            }
            if (prefix != null) {
                s = s + ",prefix=" + prefix;
            }
            if (name != null) {
                s = s + ",name=" + name;
            }
            if (predicates != null && predicates.size() > 0) {
                s = s + "," + predicates;
            }
            s = s + "}";
            return s;
        }

        /**
         *
         */
        private void trace(String s) {
            if (Element.debugLevel >= Element.DEBUG_LEVEL_LOCATIONSTEP) {
                System.err.println("*LocationStep: " + s);
            }
        }

    }

    /**
     * Predicate expression Examples: [@AttrName=Value, ...] [@AttrName>Value,
     * ...] [@AttrName, ...] ; value=null denotes that AttrName exists
     * 
     */
    class Expr {

        int op; // See OP codes above
        Object lvalue; // Expr|Boolean|String|Integer|NodeSet
        Object rvalue; // Expr|Boolean|String|Integer|NodeSet

        /** constructor */
        Expr(int op, Object lvalue) {
            this.op = op;
            this.lvalue = lvalue;
        }

        Expr(int op, Object lvalue, Object rvalue) {
            this.op = op;
            this.lvalue = lvalue;
            this.rvalue = rvalue;
        }

        public Boolean eval(Element node, NodeSet contextSet)
                throws JNCException {
            return f_boolean(eval2(node, contextSet));
        }

        private Object eval2(Element node, NodeSet contextSet)
                throws JNCException {
            Object lval, rval; // results
            lval = lvalue;
            rval = rvalue;

            // eval sub expressions
            if (lval instanceof Expr) {
                lval = ((Expr) lval).eval2(node, contextSet);
            }
            if (rval instanceof Expr) {
                rval = ((Expr) rval).eval(node, contextSet);
            }

            // the type of lval and rval is now : String | Boolean | Integer
            switch (op) {
            case ATTR_VALUE:
                return node.getAttrValue((String) lval);
            case CHILD_VALUE:
                return node.getValueOfChild((String) lval);
            case OR:
                // rvalue not evaluated if lval is 'true'
                if (f_boolean(lval).booleanValue()) {
                    return true;
                } else {
                    return f_boolean(rval);
                }
            case AND:
                // rvalue not evaluated if lval is 'false'
                if (!f_boolean(lval).booleanValue()) {
                    return false;
                } else {
                    return f_boolean(rval);
                }
            case EQ: // '='
            case NEQ: // '!='
                if (isBoolean(lval)) {
                    rval = f_boolean(rval);
                } else if (isBoolean(rval)) {
                    lval = f_boolean(lval);
                } else if (isNumber(lval)) {
                    rval = f_number(rval);
                } else if (isNumber(rval)) {
                    lval = f_number(lval);
                } else {
                    lval = f_string(lval);
                    rval = f_string(rval);
                }
                if (op == EQ) {
                    return compare(lval, rval) == 0;
                } else // op==NEQ
                    return compare(lval, rval) != 0;
            case GT: // '>'
                return compare(f_number(lval), f_number(rval)) > 0;
                case GTE: // '>='
                    return compare(f_number(lval), f_number(rval)) >= 0;
                case LT: // '<'
                    return compare(f_number(lval), f_number(rval)) < 0;
                case LTE: // '<='
                    return compare(f_number(lval), f_number(rval)) <= 0;

                // FUNCTIONS
            case FUN_STRING:
                return f_string(lval);
            case FUN_NUMBER:
                return f_number(lval);
            case FUN_BOOLEAN:
                return f_boolean(lval);
            case FUN_POSITION:
                return Integer.valueOf(contextSet.indexOf(node) + 1);
            case FUN_LAST:
                return Integer.valueOf(contextSet.size());
            case FUN_COUNT:
                return Integer.valueOf(f_nodeSet(lval).size());

                // STRING FUNCTIONS:
            case FUN_CONCAT:
                return f_string(lval) + f_string(rval);

                // BOOLEAN FUNCTIONS:
            case FUN_NOT:
                return !f_boolean(lval).booleanValue();
            case FUN_TRUE:
                return true;
            case FUN_FALSE:
                return false;

                // NUMBER FUNCTIONS:
            case FUN_NEG: // '- x' (UNARY)
                return neg(f_number(lval));
            case MINUS: // 'x - y'
                return minus(f_number(lval), f_number(rval));
            case PLUS: // 'x + y'
                return plus(f_number(lval), f_number(rval));

            default:
                throw new JNCException(JNCException.PATH_ERROR,
                        "illegal operator: " + op);
            }
        }

        /** compare */
        private int compare(Object x, Object y) throws JNCException {
            if ((x instanceof Boolean) && (y instanceof Boolean)) {
                if (((Boolean) x).booleanValue() == ((Boolean) y)
                        .booleanValue()) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if ((x instanceof Integer) && (y instanceof Integer)) {
                return ((Integer) x).compareTo((Integer) y);
            }
            if (x instanceof Number) {
                return ((Float) x).compareTo(f_float(y));
            }
            if ((x instanceof String) && (y instanceof String)) {
                if (((String) x).equals(y)) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if (x == null) {
                return -1;
            }
            if (y == null) {
                return +1;
            }
            throw new JNCException(JNCException.PATH_ERROR,
                    "badarg to compare (not of same type): " + x + ", " + y);
        }

        private boolean isBoolean(Object x) {
            return (x instanceof Boolean);
        }

        private boolean isNumber(Object x) {
            return (x instanceof Float) || (x instanceof Integer);
        }

        /** boolean() function */
        private Boolean f_boolean(Object x) throws JNCException {
            if (x instanceof Float) {
                final float f = (Float) x;
                return Boolean.valueOf(f > 0.000001 || f < 0.000001);
            } else if (x instanceof Integer) {
                final int n = (Integer) x;
                return Boolean.valueOf(n != 0);
            } else if (x instanceof String) {
                return Boolean.valueOf(((String) x).length() > 0);
            } else if (x instanceof Boolean) {
                return (Boolean) x;
            } else if (x == null) {
                return null;
            }
            throw new JNCException(JNCException.PATH_ERROR,
                    "badarg to function boolean(): " + x);
        }

        /** number() function. */
        private Number f_number(Object x) throws JNCException {
            if (x instanceof Float) {
                return (Float) x;
            } else if (x instanceof Integer) {
                return (Integer) x;
            } else if (x instanceof Boolean) {
                return Integer.valueOf((((Boolean) x).booleanValue()) ? 1 : 0);
            } else if (x instanceof String) {
                final String s = (String) x;
                try {
                    return Integer.valueOf(s);
                } catch (final NumberFormatException e1) {
                    try {
                        return new Float(s);
                    } catch (final NumberFormatException e2) {
                        return new Float(Float.NaN);
                    }
                }
            } else if (x instanceof NodeSet) {
                return f_number(f_string(x));
            } else if (x == null) {
                return null;
            }
            throw new JNCException(JNCException.PATH_ERROR,
                    "badarg to function number(): " + x);
        }

        /** nodeset() function */
        private NodeSet f_nodeSet(Object x) throws JNCException {
            if (x instanceof NodeSet) {
                return (NodeSet) x;
            } else if (x == null) {
                return null;
            }
            throw new JNCException(JNCException.PATH_ERROR,
                    "badarg to function nodeset(): " + x);
        }

        /** neg. Unary minus "-x" */
        private Number neg(Object x) throws JNCException {
            if (x instanceof Integer) {
                return Integer.valueOf(-((Integer) x).intValue());
            } else if (x instanceof Float) {
                return new Float(-((Float) x).floatValue());
            }
            throw new JNCException(JNCException.PATH_ERROR,
                    "badarg to function neg(): " + x);
        }

        /** minus "x - y" */
        private Number minus(Number x, Number y) throws JNCException {
            if ((x instanceof Integer) && (y instanceof Integer)) {
                return Integer.valueOf(((Integer) x).intValue()
                        - ((Integer) y).intValue());
            }
            final Float xf = f_float(x);
            final Float yf = f_float(y);
            return new Float(xf.floatValue() - yf.floatValue());
        }

        /** plus "x + y" */
        private Number plus(Number x, Number y) throws JNCException {
            if ((x instanceof Integer) && (y instanceof Integer)) {
                return Integer.valueOf(((Integer) x).intValue()
                        + ((Integer) y).intValue());
            }
            final Float xf = f_float(x);
            final Float yf = f_float(y);
            return new Float(xf.floatValue() + yf.floatValue());
        }

        /** the float() function. */
        private Float f_float(Object x) throws JNCException {
            if (x instanceof Float) {
                return (Float) x;
            } else if (x instanceof Integer) {
                return new Float(((Integer) x).floatValue());
            } else if (x == null) {
                return null;
            }
            throw new JNCException(JNCException.PATH_ERROR,
                    "badarg to function float(): " + x);
        }

        /** the string() function. */
        private String f_string(Object x) throws JNCException {
            if (x instanceof Integer) {
                return ((Integer) x).toString();
            } else if (x instanceof Float) {
                return ((Float) x).toString();
            } else if (x instanceof String) {
                return (String) x;
            } else if (x instanceof Boolean) {
                return ((Boolean) x) ? "true" : "false";
            } else if (x == null) {
                return null;
            }
            return x.toString();
        }

        /**
         * evalCreate will create attributes and values on the Elementent that
         * is being created.
         */
        Object evalCreate(Element node) throws JNCException {
            trace("evalCreate(): Expr= " + this);
            Object lval, rval; // results
            lval = lvalue;
            rval = rvalue;

            // eval sub expressions
            if (lval instanceof Expr) {
                lval = ((Expr) lval).evalCreate(node);
                // if (rval instanceof Expr)
                // rval = ((Expr)rval).eval(node);
            }

            // the type of lval and rval is now : String | Boolean | Integer
            switch (op) {
            case CHILD_VALUE:
                // create child value, inherit namnespace from parent
                // TODO: match out prefix possible here?
                String name = (String) lval;
                final String ns = node.namespace;
                Element elem = new Element(ns, name);
                node.addChild(elem);
                return elem;
            case ATTR_VALUE:
                name = (String) lval;
                return node.setAttr(name, "");
            case EQ: // '='
                if (lval instanceof Element) {
                    elem = (Element) lval;
                    elem.setValue(f_string(rval));
                    return elem;
                }
                if (lval instanceof Attribute) {
                    final Attribute attr = (Attribute) lval;
                    attr.setValue(f_string(rval));
                    return attr;
                }
                throw new JNCException(JNCException.PATH_CREATE_ERROR,
                        "illegal path create expr: " + this);
            default:
                throw new JNCException(JNCException.PATH_CREATE_ERROR,
                        "illegal path create expr:" + this);
            }
        }

        /**
         * Returns a string representation of this Expr.
         */
        @Override
        public String toString() {
            switch (op) {
            case AND:
                return "AND(" + lvalue + "," + rvalue + ")";
            case OR:
                return "OR(" + lvalue + "," + rvalue + ")";
            case EQ:
                return "EQ(" + lvalue + "," + rvalue + ")";
            case NEQ:
                return "NEQ(" + lvalue + "," + rvalue + ")";
            case GT:
                return "GT(" + lvalue + "," + rvalue + ")";
            case GTE:
                return "GTE(" + lvalue + "," + rvalue + ")";
            case LT:
                return "LT(" + lvalue + "," + rvalue + ")";
            case LTE:
                return "LTE(" + lvalue + "," + rvalue + ")";
            case PLUS:
                return "PLUS(" + lvalue + "," + rvalue + ")";
            case MINUS:
                return "MINUS(" + lvalue + "," + rvalue + ")";
            case FUN_BOOLEAN:
                return "FUN_BOOLEAN(" + lvalue + ")";
            case FUN_NUMBER:
                return "FUN_NUMBER(" + lvalue + ")";
            case FUN_STRING:
                return "FUN_STRING(" + lvalue + ")";
            case FUN_POSITION:
                return "FUN_POSITION()";
            case FUN_LAST:
                return "FUN_LAST()";
            case FUN_COUNT:
                return "FUN_COUNT(" + lvalue + ")";
            case FUN_CONCAT:
                return "FUN_COUNT(" + lvalue + "," + rvalue + ")";
            case FUN_NOT:
                return "FUN_NOT(" + lvalue + ")";
            case FUN_TRUE:
                return "FUN_TRUE()";
            case FUN_FALSE:
                return "FUN_FALSE()";
            case FUN_NEG:
                return "FUN_NEG(" + lvalue + ")";
            case ATTR_VALUE:
                return "ATTR_VALUE(" + lvalue + ")";
            case CHILD_VALUE:
                return "CHILD_VALUE(" + lvalue + ")";
            default:
                return "Expr{op=" + op + "," + lvalue + "," + rvalue + "}";
            }
        }

        /**
         *
         */
        private void trace(String s) {
            if (Element.debugLevel >= Element.DEBUG_LEVEL_EXPR) {
                System.err.println("*Expr: " + s);
            }
        }

    }

    /* Parser */

    /** OP codes in Expr (Predicate) */
    static final int AND = 1;
    static final int OR = 2;
    static final int EQ = 3;
    static final int NEQ = 4;
    static final int GT = 5;
    static final int GTE = 6;
    static final int LT = 7;
    static final int LTE = 8;
    static final int PLUS = 9;
    static final int MINUS = 10;
    static final int FUN_BOOLEAN = 11;
    static final int FUN_NUMBER = 12;
    static final int FUN_STRING = 13;
    static final int FUN_POSITION = 14;
    static final int FUN_LAST = 15;
    static final int FUN_COUNT = 16;
    static final int FUN_CONCAT = 17;
    static final int FUN_NOT = 18;
    static final int FUN_TRUE = 19;
    static final int FUN_FALSE = 20;
    static final int FUN_NEG = 21;
    static final int ATTR_VALUE = 22;
    static final int CHILD_VALUE = 23;

    /**
     * Returns a list of LocationSteps for a path expression.
     * 
     */
    ArrayList<LocationStep> parse(TokenList tokens) throws JNCException {
        final ArrayList<LocationStep> steps = new ArrayList<LocationStep>();
        try {
            Token tok1, tok2, tok3, tok4, tok5;
            LocationStep step;

            int sz = tokens.size();
            while (sz > 0) {
                trace("parse(): " + tokens);
                /* peek at tokens */
                if (sz >= 1) {
                    tok1 = tokens.getToken(0);
                } else {
                    tok1 = new Token();
                }
                if (sz >= 2) {
                    tok2 = tokens.getToken(1);
                } else {
                    tok2 = new Token();
                }
                if (sz >= 3) {
                    tok3 = tokens.getToken(2);
                } else {
                    tok3 = new Token();
                }
                if (sz >= 4) {
                    tok4 = tokens.getToken(3);
                } else {
                    tok4 = new Token();
                }
                if (sz >= 5) {
                    tok5 = tokens.getToken(4);
                } else {
                    tok5 = new Token();
                }

                /* "/" (root) */
                if (tok1.type == SLASH) {
                    step = new LocationStep(AXIS_ROOT);
                    steps.add(step);
                    tokens.remove(0);
                }
                /* AXIS::PREFIX:TAG */
                else if (tok1.type == ATOM && tok2.type == COLONCOLON
                        && tok3.type == ATOM && tok4.type == COLON
                        && tok5.type == ATOM) {
                    step = new LocationStep(parseAxis(tok1.value),
                            tok3.value, tok5.value);
                    steps.add(step);
                    tokens.removeRange(0, 5);
                    parsePredicates(tokens, step);
                }
                /* AXIS::TAG */
                else if (tok1.type == ATOM && tok2.type == COLONCOLON
                        && tok3.type == ATOM) {
                    step = new LocationStep(parseAxis(tok1.value), tok3.value);
                    steps.add(step);
                    tokens.removeRange(0, 3);
                    parsePredicates(tokens, step);
                }
                /* PREFIX:TAG */
                else if (tok1.type == ATOM && tok2.type == COLON
                        && tok3.type == ATOM) {
                    step = new LocationStep(AXIS_CHILD, tok1.value,
                            tok3.value);
                    steps.add(step);
                    tokens.removeRange(0, 3);
                    parsePredicates(tokens, step);
                }
                /* TAG */
                else if (tok1.type == ATOM) {
                    step = new LocationStep(AXIS_CHILD, tok1.value);
                    steps.add(step);
                    tokens.remove(0);
                    parsePredicates(tokens, step);
                }
                /* '//' (SLASHSLASH) */
                else if (tok1.type == SLASHSLASH) {
                    /* expand into '/descendant-or-self::node()/' */
                    tokens.add(0, new Token(SLASH, "/"));
                    tokens.add(1, new Token(ATOM, "descendant-or-self"));
                    tokens.add(2, new Token(COLONCOLON, "::"));
                    tokens.add(3, new Token(ATOM, "node"));
                    tokens.add(4, new Token(LBRACER, "("));
                    tokens.add(5, new Token(RBRACER, ")"));
                    tokens.add(6, new Token(SLASH, "/"));
                }
                /* parse error */
                else {
                    parseError(tokens);
                }
                // trace("parse(): sz= "+tokens.size());
                sz = tokens.size();
            }
        } catch (final Exception e) {
            final int errorCode = JNCException.PATH_ERROR;
            throw new JNCException(errorCode, "parse error: " + e);
        }
        trace("parse() -> " + steps);
        return steps;
    }

    /**
     * check axis
     */
    int parseAxis(String str) throws JNCException {
        if (str.equals("child")) {
            return AXIS_CHILD;
        }
        if (str.equals("self")) {
            return AXIS_SELF;
        }
        throw new JNCException(JNCException.PATH_ERROR,
                "unsupported or unknown axis: " + str);
    }

    /**
     * parse out predicates
     */
    void parsePredicates(TokenList tokens, LocationStep step)
            throws JNCException {
        trace("parsePredicates(): " + tokens);
        final int sz = tokens.size();
        if (sz >= 1) {
            Token tok1 = tokens.getToken(0);
            if (tok1.type == LPRED) {
                /* search for matching RPRED */
                int i = 1;
                try {
                    while ((tokens.getToken(i)).type != RPRED) {
                        i++;
                    }
                } catch (final Exception e) {
                    throw new JNCException(JNCException.PATH_ERROR,
                            "unmatched '[' in expression");
                }
                if (step.predicates == null) {
                    step.predicates = new ArrayList<Expr>();
                }
                /* search for sequence */
                int start = 1;
                for (int j = 1; (j + 1) < i; j++) {
                    tok1 = tokens.getToken(j);
                    final Token tok2 = tokens.getToken(j + 1);
                    if (tok1.type == COMMA && tok2.type != COMMA) {
                        final Expr pred = parsePredicate(tokens, start, j);
                        step.predicates.add(pred);
                        start = j + 1;
                    }
                }
                final Expr pred = parsePredicate(tokens, start, i);
                step.predicates.add(pred);
                tokens.removeRange(0, i + 1);
                parsePredicates(tokens, step);
            } else if (tok1.type == SLASH) {
                tokens.remove(0);
            } else if (tok1.type != SLASHSLASH) {
                parseError(tokens);
            }
        }
    }

    /**
     * Parses a predicate expression. must consume all tokens from 'from' to
     * 'to'.
     */
    Expr parsePredicate(TokenList tokens, int from, int to)
            throws JNCException {
        Token tok1, tok2, tok3;
        final int i = from;
        while (i < to) {
            /* peek at tokens */
            tok1 = tokens.getToken(i);
            if ((i + 1) < to) {
                tok2 = tokens.getToken(i + 1);
            } else {
                tok2 = null;
            }
            if ((i + 2) < to) {
                tok3 = tokens.getToken(i + 2);
            } else {
                tok3 = null;
            }

            trace("parsePredicate(): from=" + from + " to=" + to + " ["
                    + tok1 + "," + tok2 + "," + tok3 + ", ...]");

            /* ATOM = */
            if (tok1.type == ATOM && (tok2 != null ? tok2.type : 0) == COMPARE && tok3 != null) {
                final Object rexpr = parsePredicate_rvalue(tokens, i + 2, to);
                return new Expr(tok2.op, new Expr(CHILD_VALUE, tok1.value),
                        rexpr);
            }
            /* ATTR = */
            else if (tok1.type == ATTR && (tok2 != null ? tok2.type : 0) == COMPARE
                    && tok3 != null) {
                final Object rexpr = parsePredicate_rvalue(tokens, i + 2, to);
                return new Expr(tok2.op, new Expr(ATTR_VALUE, tok1.value),
                        rexpr);
            }
            /* It's a parse error */else {
                parseError(tokens, from, to);
            }
        }
        return null;
    }

    /**
     * Parses an rvalue of the predicate expression. must consume all tokens
     * from 'from' to 'to'.
     */
    Object parsePredicate_rvalue(TokenList tokens, int from, int to)
            throws JNCException {
        Token tok1, tok2, tok3;
        final int i = from;
        while (i < to) {
            /* peek at tokens */
            tok1 = tokens.getToken(i);
            if ((i + 1) < to) {
                tok2 = tokens.getToken(i + 1);
            } else {
                tok2 = null;
            }
            if ((i + 2) < to) {
                tok3 = tokens.getToken(i + 2);
            } else {
                tok3 = null;
            }

            trace("parsePredicate_rvalue(): from=" + from + " to=" + to
                    + " [" + tok1 + "," + tok2 + "," + tok3 + ", ...]");

            if (tok1.type == ATOM && tok2 == null) {
                return new Expr(CHILD_VALUE, tok1.value);
            } else if (tok1.type == ATTR && tok2 == null) {
                return new Expr(ATTR_VALUE, tok1.value);
            } else if (tok1.type == STRING && tok2 == null) {
                return tok1.value;
            } else if (tok1.type == NUMBER && tok2 == null) {
                return tok1.value;
            }
            else {
                parseError(tokens, from, to);
            }
        }
        return null;
    }

    /**
     * Throws a parse error exception.
     */
    void parseError(TokenList tokens) throws JNCException {
        /* show 5 next tokens */
        parseError(tokens, 0, 5);
    }

    void parseError(TokenList tokens, int from, int to) throws JNCException {
        String errStr = "parse error: \"";
        final int sz = tokens.size();
        for (int i = from; i < to; i++) {
            if (i < sz) {
                errStr = errStr + tokens.getToken(i).value;
            } else {
                break;
            }
        }
        errStr = errStr + "...\"";
        throw new JNCException(JNCException.PATH_ERROR, errStr);
    }

    /* Tokenizer */

    /** Tokens */
    static final int SLASH = 1;
    static final int SLASHSLASH = 2;
    static final int DOT = 3;
    static final int DOTDOT = 4;
    static final int COLON = 5;
    static final int COLONCOLON = 6;
    static final int OP = 7;
    static final int COMPARE = 8;
    static final int ATOM = 9;
    static final int ATTR = 10;
    static final int NUMBER = 11;
    static final int STRING = 12;
    static final int LBRACER = 13;
    static final int RBRACER = 14;
    static final int LPRED = 15;
    static final int RPRED = 16;
    static final int COMMA = 17;

    /**
     * A list of Tokens
     */
    class TokenList extends ArrayList<Token> {

        private static final long serialVersionUID = 1L;

        public Token getToken(int i) {
            return super.get(i);
        }

        @Override
        public void removeRange(int from, int to) {
            super.removeRange(from, to);
        }

        @Override
        public String toString() {
            String s = "TokenList[";
            boolean comma = false;
            for (int i = 0; i < size(); i++) {
                if (comma) {
                    s = s + ",";
                }
                s = s + getToken(i);
                comma = true;
            }
            s = s + "]";
            return s;
        }
    }

    /**
     * A Token
     */
    class Token {

        Token() {
        }

        Token(int type, String value) {
            this.type = type;
            this.value = value;
        }

        Token(int type, String value, Number number) {
            this.type = type;
            this.value = value;
            this.number = number;
        }

        Token(int type, int op, String value) {
            this.type = type;
            this.op = op;
            this.value = value;
        }

        int type;
        int op;
        String value;
        Number number;

        @Override
        public String toString() {
            switch (type) {
            case SLASH:
                return "SLASH";
            case SLASHSLASH:
                return "SLASHSLASH";
            case DOT:
                return "DOT";
            case DOTDOT:
                return "DOTDOT";
            case COLON:
                return "COLON";
            case COLONCOLON:
                return "COLONCOLON";
            case OP:
                return "OP(" + value + ")";
            case COMPARE:
                return "COMPARE(" + value + ")";
            case ATOM:
                return "ATOM(" + value + ")";
            case ATTR:
                return "ATTR(" + value + ")";
            case NUMBER:
                return "NUMBER(" + value + ")";
            case STRING:
                return "STRING(" + value + ")";
            case LBRACER:
                return "LBRACER";
            case RBRACER:
                return "RBRACER";
            case LPRED:
                return "LPRED";
            case RPRED:
                return "RPRED";
            case COMMA:
                return "COMMA";
            default:
                return "UNKOWN(type=" + type + ",value=" + value + ")";
            }
        }
    }

    /**
     * Returns a TokenList (ArrayList) of Tokens.
     */
    TokenList tokenize(String s) throws JNCException {
        final TokenList tokens = new TokenList();
        final byte[] buf = s.getBytes();
        byte curr, next;
        int i = 0, j;

        while (i < s.length()) {

            if ((i + 1) < buf.length) {
                next = buf[i + 1];
            } else {
                next = 0;
            }
            curr = buf[i];
            if (curr == ' ' || curr == '\t' || curr == '\n') {
                // whitespace - ignore
                i++;
            } else if ((curr >= 'a' && curr <= 'z')
                    || (curr >= 'A' && curr <= 'Z') || (curr == '\\')) {
                boolean escape = (curr == '\\');
                j = i + 1;
                while (j < buf.length
                        && ((buf[j] >= 'a' && buf[j] <= 'z')
                                || (buf[j] >= 'A' && buf[j] <= 'Z')
                                || (buf[j] >= '0' && buf[j] <= '9')
                                || (buf[j] == '-') || (buf[j] == '_')
                                || (buf[j] == '\\') || escape)) {
                    if (buf[j] == '\\') {
                        escape = true;
                    } else if (escape) {
                        escape = false;
                    }
                    j++;
                }
                final String newToken = new String(buf, i, j - i);
                tokens.add(new Token(ATOM, newToken.replaceAll("\\\\", "")));
                i = j;
            } else if (curr == '@'
                    && ((next >= 'a' && next <= 'z') || next >= 'A'
                            && next <= 'Z')) {
                i++;
                j = i + 1;
                while (j < buf.length
                        && ((buf[j] >= 'a' && buf[j] <= 'z')
                                || (buf[j] >= 'A' && buf[j] <= 'Z')
                                || (buf[j] >= '0' && buf[j] <= '9')
                                || (buf[j] == '-') || (buf[j] == '_'))) {
                    j++;
                }
                tokens.add(new Token(ATTR, new String(buf, i, j - i)));
                i = j;
            } else if (curr == '\'') {
                i++;
                j = i;
                while (j < buf.length && buf[j] != '\'') {
                    j++;
                }
                if (j == buf.length) {
                    throw new JNCException(JNCException.PATH_ERROR,
                            "unterminated value: "
                                    + new String(buf, i, buf.length - i));
                }
                tokens.add(new Token(STRING, new String(buf, i, j - i)));
                i = j + 1;
            } else if (curr == '\"') {
                i++;
                j = i;
                while (j < buf.length && buf[j] != '\"') {
                    j++;
                }
                if (j == buf.length) {
                    throw new JNCException(JNCException.PATH_ERROR,
                            "unterminated value: "
                                    + new String(buf, i, buf.length - i));
                }
                tokens.add(new Token(STRING, new String(buf, i, j - i)));
                i = j + 1;
            } else if (curr >= '0' && curr <= '9') {
                j = i + 1;
                while (j < buf.length && (buf[j] >= '0' && buf[j] <= '9')) {
                    j++;
                }
                String value;
                Number number;
                if ((j + 1) < buf.length && buf[j] == '.'
                        && (buf[j + 1] >= '0' && buf[j + 1] <= '9')) { // float
                    j++;
                    while (j < buf.length && (buf[j] >= '0' && buf[j] <= '9')) {
                        j++;
                    }
                    value = new String(buf, i, j - i);
                    number = new Float(value);
                } else {
                    value = new String(buf, i, j - i);
                    number = Integer.valueOf(value);
                }
                tokens.add(new Token(NUMBER, value, number));
                i = j;
            } else if (curr == '!' && next == '=') {
                tokens.add(new Token(COMPARE, "!="));
                i = i + 2;
            } else {
                switch (curr) {
                case '[':
                    tokens.add(new Token(LPRED, "["));
                    break;
                case ']':
                    tokens.add(new Token(RPRED, "]"));
                    break;
                case '/':
                    if (next == '/') {
                        tokens.add(new Token(SLASHSLASH, "//"));
                        i++;
                    } else {
                        tokens.add(new Token(SLASH, "/"));
                    }
                    break;
                case ':':
                    if (next == ':') {
                        tokens.add(new Token(COLONCOLON, "::"));
                        i++;
                    } else {
                        tokens.add(new Token(COLON, ":"));
                    }
                    break;
                case '.':
                    if (next == '.') {
                        tokens.add(new Token(DOTDOT, ".."));
                        i++;
                    } else {
                        tokens.add(new Token(DOT, "."));
                    }
                    break;
                case ',':
                    tokens.add(new Token(COMMA, ","));
                    break;
                case '+':
                case '-':
                case '*':
                    tokens.add(new Token(OP, new String(new byte[] { curr })));
                    break;
                case '=':
                    if (next == '>') {
                        tokens.add(new Token(COMPARE, GTE, ">="));
                        i++;
                    } else if (next == '<') {
                        tokens.add(new Token(COMPARE, LTE, "<="));
                        i++;
                    } else {
                        tokens.add(new Token(COMPARE, EQ, "="));
                    }
                    break;
                case '>':
                    if (next == '=') {
                        tokens.add(new Token(COMPARE, GTE, ">="));
                        i++;
                    } else {
                        tokens.add(new Token(COMPARE, GT, ">"));
                    }
                    break;
                case '<':
                    if (next == '=') {
                        tokens.add(new Token(COMPARE, LTE, "<="));
                        i++;
                    } else {
                        tokens.add(new Token(COMPARE, LT, "="));
                    }
                    break;
                default:
                    throw new JNCException(JNCException.PATH_ERROR,
                            "illegal character in expression: " + curr);
                }
                i++;
            }
        }
        trace("tokenize() -> " + tokens);
        return tokens;
    }

    /* help functions */

    /**
     * Returns a string representation of this Path. It's a parse tree.
     */
    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Path[");
        for (LocationStep lstep : locationSteps) {
            s.append(lstep);
        }
        s.append("]");
        return s.toString();
    }

    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_PATH) {
            System.err.println("*Path: " + s);
        }
    }

}

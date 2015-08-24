package com.tailf.jnc;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

final class Utils {

    /* ---------- YangType utilities ---------- */

    /**
     * The precision of Yang Decimal64 values with max (18) fraction digits
     */
    public static final double EPSILON = 1E-17;

    /**
     * Converts a number into its BigDecimal equivalent. Useful for comparisons
     * between Numbers.
     * 
     * @param n an instance of java.lang.Number to be converted.
     * @return BigDecimal equivalent of n.
     */
    public static BigDecimal bigDecimalValueOf(Number n) {
        if (n instanceof BigDecimal) {
            return (BigDecimal) n;
        } else if (n instanceof BigInteger) {
            return new BigDecimal((BigInteger) n);
        } else if (n instanceof Double) {
            return new BigDecimal((Double) n);
        } else if (n instanceof Float) {
            return new BigDecimal((Float) n);
        } else {
            return n == null ? null : new BigDecimal(n.longValue());
        }
    }

    /**
     * Boolean operators.
     * 
     * @author emil@tail-f.com
     */
    public static enum Operator {

        /**
         * Equality operator. EQ.cmp(a, b) is equivalent to a == b.
         */
        EQ {
            @Override
            public boolean cmp(BigDecimal x1, BigDecimal x2) {
                return x1.subtract(x2).abs().doubleValue() < EPSILON;
            }
        },

        /**
         * Greater than operator. GR.cmp(a, b) is equivalent to a &gt; b.
         */
        GR {
            @Override
            public boolean cmp(BigDecimal x1, BigDecimal x2) {
                return x1.subtract(x2).doubleValue() > 0;
            }
        },

        /**
         * Greater than or equal. GE.cmp(a, b) is equivalent to a &gt;= b.
         */
        GE {
            @Override
            public boolean cmp(BigDecimal x1, BigDecimal x2) {
                return x1.subtract(x2).doubleValue() > -EPSILON;
            }
        },

        /**
         * Less than operator. LT.cmp(a, b) is equivalent to a &lt; b.
         */
        LT {
            @Override
            public boolean cmp(BigDecimal x1, BigDecimal x2) {
                return x1.subtract(x2).doubleValue() < 0;
            }
        },

        /**
         * Less than or equal. LE.cmp(a, b) is equivalent to a &lt;= b.
         */
        LE {
            @Override
            public boolean cmp(BigDecimal x1, BigDecimal x2) {
                return x1.subtract(x2).doubleValue() < EPSILON;
            }
        };

        /**
         * Comparison function for a Boolean operator.
         * 
         * @param x1 First operand
         * @param x2 Second operand
         * @return The result of the comparison
         */
        public abstract boolean cmp(BigDecimal x1, BigDecimal x2);
    }

    /**
     * Checks that a comparison between v and arg, or between the length of v
     * and arg if applicable, evaluates to true.
     * 
     * @param v A Number or String value to be compared.
     * @param arg The Number value to compare against.
     * @param op The operator to use (EQ: ==, GR: &gt;, LT: &lt;).
     * @throws YangException If the comparison does not evaluate to true, or if
     *             v is not a Number or a String.
     */
    public static void restrict(Object v, Number arg, Operator op)
            throws YangException {
        if (v instanceof Number) {
            restrict((Number) v, bigDecimalValueOf(arg), op);
        } else if (v instanceof String) {
            restrict(((String) v).length(), bigDecimalValueOf(arg), op);
        } else {
            YangException.throwException(true, v);
        }
    }

    /**
     * Checks that a comparison between v and arg, or between the length of v
     * and arg if applicable, evaluates to true.
     * 
     * @param v A Number value to be compared.
     * @param arg The BigDecimal value to compare against.
     * @param op The operator to use (EQ: ==, GR: &gt;, LT: &lt;).
     * @throws YangException If the comparison does not evaluate to true, or if
     *             v is not a Number or a String.
     */
    public static void restrict(Number v, BigDecimal arg, Operator op)
            throws YangException {
        YangException.throwException(!op.cmp(bigDecimalValueOf(v), arg), v);
    }

    /* ---------- String utilities ---------- */

    /**
     * Whitespace collapse. Contiguous sequences of 0x20 are collapsed into a
     * single #x20, and initial and/or final #x20s are deleted.
     * <p>
     * This method is used by most other data types to collapse Strings from
     * the XML parser.
     * 
     * @param value The string to collapse.
     * @return The collapsed string.
     */
    public static String wsCollapse(String value) {
        if (value == null) {
            return null;
        }

        // Collapse multiple spaces into single spaces
        final String res = value.replaceAll(" +", " ");

        // Remove any leading and/or trailing space
        final int startOffset = res.startsWith(" ") ? 1 : 0;
        final int stopOffset = res.length() > 1 && res.endsWith(" ") ? -1 : 0;
        return res.substring(startOffset, res.length() + stopOffset);
    }

    /**
     * Whitespace replace. Replaces whitespaces with spaces.
     * 
     * @param value The String to replace whitespaces in.
     * @return a copy of value with all characters matching "[\t\n\r]" replaced
     *         by " " (a blank).
     */
    public static String wsReplace(String value) {
        return value == null ? null : value.replaceAll("[\t\n\r]", " ");
    }

    public static boolean matches(String value, String[] regexes)
            throws YangException {
        boolean matches = true;
        try {
            for (int i = 0; i < regexes.length; i++) {
                if (!(matches = Pattern.matches(regexes[i], value))) {
                    break;
                }
            }
        } catch (final PatternSyntaxException e) {
            YangException.throwException(true, e);
        }
        return matches;
    }

    // Thanks Dimitris Kolovos for the code below
    public static String escapeXml(String original) {
        String escaped = null;
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Text text = document.createTextNode(original);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(text);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(source, result);
            escaped = writer.toString();
        } catch (Exception ignored) {
        }
        return (escaped != null) ? escaped : original ;
    }

}
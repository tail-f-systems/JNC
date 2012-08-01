package com.tailf.confm.yang;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tailf.confm.ConfMException;

final class TypeUtil {

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
            @Override public boolean cmp(int x1, int x2) {
                return x1 == x2;
            }
        },
        /**
         * Greater than operator. GR.cmp(a, b) is equivalent to a &gt; b.
         */
        GR {
            @Override public boolean cmp(int x1, int x2) {
                return x1 > x2;
            }
        },
        /**
         * Less than operator. LT.cmp(a, b) is equivalent to a &lt; b.
         */
        LT {
            @Override public boolean cmp(int x1, int x2) {
                return x1 < x2;
            }
        };
        /**
         * Comparison function for a Boolean operator.
         * 
         * @param x1 First operand
         * @param x2 Second operand
         * @return The result of the comparison
         */
        public abstract boolean cmp(int x1, int x2);
    }

    /**
     * Checks that a comparison between arg and the value of this object, or
     * its length if applicable, evaluates to true.
     * 
     * @param arg The integer value to compare against.
     * @param op The operator to use (EQ: ==, GR: &gt;, LT: &lt;).
     * @throws ConfMException If the comparison does not evaluate to true.
     * @throws ClassCastException If type of arg prevents it from being
     *                            compared to the arg of this object.
     */
    public static void restrict(Object value, int arg, Operator op) throws ConfMException {
        boolean fail = true;
        if (value instanceof Number) {
            fail = !op.cmp(((Number) value).intValue(), arg);
        } else if (value instanceof String) {
            fail = !op.cmp(((String) value).length(), arg);
        }
        ConfMException.throwException(fail, value);
    }

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
        String res = value.replaceAll(" +", " ");
        
        // Remove any leading and/or trailing space
        int startOffset = res.startsWith(" ") ? 1 : 0;
        int stopOffset = res.length() > 1 && res.endsWith(" ") ? -1 : 0;
        return res.substring(startOffset, res.length() + stopOffset);
    }

    /**
     * Whitespace replace. Replaces whitespaces with spaces.
     *
     * @param value The String to replace whitespaces in.
     * @return a copy of value with all characters matching "[\t\n\r]"
     *         replaced by " " (a blank).
     */
    public static String wsReplace(String value) {
        return value == null ? null : value.replaceAll("[\t\n\r]", " ");
    }

}
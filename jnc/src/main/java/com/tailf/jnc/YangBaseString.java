package com.tailf.jnc;

/**
 * A String wrapper extended by built in String based types.
 * <p>
 * White space collapse and replace methods, regexp pattern matchers, and
 * length assertion methods are provided.
 * 
 * @author emil@tail-f.com
 */
public class YangBaseString extends YangBaseType<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangBaseString object from a java.lang.String.
     * 
     * @param value The Java String.
     * @throws YangException If an invariant was broken during assignment.
     */
    public YangBaseString(String value) throws YangException {
        setValue(value);
    }

    /**
     * Sets the value of this object using a java.lang.String.
     * 
     * @param value The Java String.
     * @throws YangException If an invariant was broken during assignment.
     */
    @Override
    public void setValue(String value) throws YangException {
        YangException.throwException(value == null,
                new NullPointerException());
        this.value = value;
        check();
    }

    /**
     * Identity method provided because this class extends the YangBaseType
     * class.
     * 
     * @param s A string.
     * @return s.
     */
    @Override
    protected String fromString(String s) {
        return s;
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangBaseString or
     *         java.lang.String; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangBaseString;
    }

    @Override
    protected YangBaseString cloneShallow() throws YangException {
        return new YangBaseString(value);
    }

    /* ---------- Restrictions ---------- */

    /**
     * Checks that a regular expression matches the value of this object.
     * 
     * @param regex The regular expression.
     * @throws YangException If regexp has a syntax error or does not match.
     */
    protected void pattern(String regex) throws YangException {
        pattern(new String[] { regex });
    }

    /**
     * Checks that a set of regular expressions match the value of this object.
     * 
     * @param regexes The regular expressions.
     * @throws YangException If any regexp in regexes has a syntax error or
     *             does not match.
     */
    protected void pattern(String[] regexes) throws YangException {
        YangException.throwException(!Utils.matches(value, regexes), value);
    }

    /**
     * Whitespace replace. Replaces all occurrences of #x9 (tab), #xA (line
     * feed), and #xD (CR) with #x20 (space).
     */
    protected void wsReplace() {
        value = Utils.wsReplace(value);
    }

    /**
     * Whitespace replace. Contiguous sequences of 0x20 are collapsed into a
     * single 0x20, and initial and/or final 0x20s are deleted.
     */
    protected void wsCollapse() {
        value = Utils.wsCollapse(value);
    }

    /** ---------- Restrictions ---------- */

    /**
     * Checks that the value of this object has the specified length.
     * 
     * @param length The required length of this value
     * @throws YangException If the comparison does not evaluate to true.
     */
    protected void exact(int length) throws YangException {
        Utils.restrict(value, length, Utils.Operator.EQ);
    }

    /**
     * Checks that the value of this object has at least the specified length.
     * 
     * @param length The lower limit of the length of this value
     * @throws YangException if value is shorter than length.
     */
    protected void min(int length) throws YangException {
        Utils.restrict(value, length, Utils.Operator.GE);
    }

    /**
     * Checks that the value of this object has at most the specified length.
     * 
     * @param length The upper limit of the length of this value.
     * @throws YangException if value is longer than length.
     */
    protected void max(int length) throws YangException {
        Utils.restrict(value, length, Utils.Operator.LE);
    }

}
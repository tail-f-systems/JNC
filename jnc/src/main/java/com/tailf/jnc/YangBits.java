package com.tailf.jnc;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Implements the built-in YANG data type "bits".
 * 
 * @author emil@tail-f.com
 */
public class YangBits extends YangBaseInt<BigInteger> {

    private static final long serialVersionUID = 1L;

    private String[] smap;

    private int[] imap;

    private BigInteger mask;

    /**
     * Constructor with value as a String.
     * 
     * @param value The value to initialize the object with, as a Number.
     * @param mask The bit mask as a string.
     * @param smap mapping from flag names to position values
     * @param smap mapping from position values to flag names
     * @throws YangException If value does not match mask.
     * @throws NumberFormatException If value is not valid as a number.
     */
    public YangBits(String value, Number mask, String[] smap, int[] imap)
            throws YangException {
        super(value);
        YangException.throwException(smap.length != imap.length, value);
        this.mask = Utils.bigDecimalValueOf(mask).toBigIntegerExact();
        this.smap = smap;
        this.imap = imap;
        setValue(value);
    }

    /**
     * Constructor with value as a Number.
     * 
     * @param value The value to initialize the object with, as a Number.
     * @param mask The bit mask to initialize the object with.
     * @param smap mapping from flag names to position values
     * @param smap mapping from position values to flag names
     * @throws YangException If value does not match mask.
     */
    public YangBits(Number value, Number mask, String[] smap, int[] imap)
            throws YangException {
        super(Utils.bigDecimalValueOf(value).toBigIntegerExact());
        YangException.throwException(smap.length != imap.length, value);
        this.mask = Utils.bigDecimalValueOf(mask).toBigIntegerExact();
        this.smap = smap;
        this.imap = imap;
        check();
    }

    /**
     * Checks that the value matches mask.
     * 
     * @throws YangException if value space does not match mask.
     */
    @Override
    public void check() throws YangException {
        if (mask == null) {
            return;
        }
        super.check();
        final boolean fail = mask.or(value).compareTo(mask) != 0;
        YangException.throwException(fail, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#toString()
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < imap.length; i++) {
            BigInteger flag = BigInteger.ONE.shiftLeft(imap[i]);
            if (!value.and(flag).equals(BigInteger.ZERO)) {
                res.append(smap[i]);
                if (i < imap.length - 1) {
                    res.append(" ");
                }
            }
        }
        return Utils.wsCollapse(res.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseInt#canEqual(java.lang.Object)
     */
    @Override
    public boolean canEqual(Object obj) {
        return (obj instanceof YangBits);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#hashCode()
     */
    @Override
    public int hashCode() {
        int mapHashCodes = Arrays.hashCode(smap) + Arrays.hashCode(imap);
        return super.hashCode() + mask.hashCode() + mapHashCodes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        YangBits other = (YangBits) obj;
        if (!other.mask.equals(mask) || other.smap.length != smap.length
                || other.imap.length != imap.length) {
            return false;
        }
        for (int i = 0; i < smap.length; i++) {
            if (!smap[i].equals(other.smap[i]) || imap[i] != other.imap[i]) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseInt#decode(java.lang.String)
     */
    @Override
    protected BigInteger decode(String s) throws NumberFormatException {
        if (smap == null || imap == null) {
            return BigInteger.ONE.negate(); // Bogus value
        } else {
            StringTokenizer st = new StringTokenizer(s);
            BigInteger res = BigInteger.ZERO;
            while (st.hasMoreTokens()) {
                boolean found = false;
                String tok = st.nextToken();
                for (int i = 0; i < smap.length; i++) {
                    if (tok.compareTo(smap[i]) == 0) {
                        found = true;
                        res = res.add(BigInteger.ONE.shiftLeft(imap[i]));
                    }
                }
                if (!found) {
                    throw new NumberFormatException(tok + " not found");
                }
            }
            return res;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangBits cloneShallow() throws YangException {
        return new YangBits(value.toString(), mask, smap, imap);
    }

    /**
     * Performs arithmetic or assignment: this.value &= v.value.
     * 
     * @param v YangBits instance to fetch value from.
     */
    public void AND(YangBits v) {
        value = value.and(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value |= v.value.
     * 
     * @param v YangBits instance to fetch value from.
     */
    public void OR(YangBits v) {
        value = value.or(v.getValue());
    }

    /**
     * Performs arithmetic or assignment: this.value ^= v.value.
     * 
     * @param v YangBits instance to fetch value from.
     */
    public void XOR(YangBits v) {
        value = value.xor(v.getValue());
    }

}

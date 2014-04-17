package com.tailf.jnc;

/**
 * Implements the built-in YANG data type "binary".
 * 
 * @author emil@tail-f.com
 */
public class YangBinary extends YangBaseType<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a YangBinary object from a String. The string is whitespace
     * collapsed before it is encoded.
     * 
     * @param value the String
     * @throws YangException if setValue(value) does
     */
    public YangBinary(String value) throws YangException {
        setValue(value);
    }

    /**
     * Creates a YangBinary object from a base 64 encoded byte buffer. No
     * checks are performed to confirm that the buffer is actually encoded in
     * base 64.
     * 
     * @param buffer The base 64 encoded byte buffer
     */
    public YangBinary(byte[] buffer) {
        setValue(buffer);
    }

    /**
     * Sets the value of this object using a String. The string is whitespace
     * collapsed before it is encoded.
     * 
     * @param value The string
     * @throws YangException If the input is not valid Base64 encoded data.
     */
    @Override
    public void setValue(String value) throws YangException {
        value = Utils.wsCollapse(value);
        try {
            this.value = Base64Coder.encodeString(value);
        } catch (IllegalArgumentException e) {
            YangException.throwException(true, e);
        }
    }

    /**
     * Sets the value of this object by copying a base 64 encoded byte buffer.
     * No checks are performed to confirm that the buffer is actually encoded
     * in base 64.
     * 
     * @param buffer The base 64 encoded byte buffer.
     */
    public void setValue(byte[] buffer) {
        value = new String(buffer);
    }

    /**
     * Nop method provided because this class extends the YangBaseType class.
     */
    @Override
    public void check() throws YangException {
    }

    /**
     * @return The decoded value of this object, as a String.
     */
    @Override
    public String toString() {
        return Base64Coder.decodeString(value);
    }

    /**
     * Identity method provided because this class extends the YangBaseType
     * class.
     * 
     * @param s A string.
     * @return s.
     */
    @Override
    protected String fromString(String s) throws YangException {
        return Base64Coder.encodeString(s);
    }

    /**
     * Compares type of obj with this object to see if they can be equal.
     * 
     * @param obj Object to compare type with.
     * @return true if obj is an instance of YangBinary; false otherwise.
     */
    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof YangBinary;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tailf.jnc.YangBaseType#cloneShallow()
     */
    @Override
    protected YangBinary cloneShallow() throws YangException {
        return new YangBinary(toString());
    }

    /* ---------- Restrictions ---------- */

    /**
     * Checks that the value buffer of this object has the specified number of
     * octets/bytes.
     * 
     * @param len The number of octets/bytes to check for.
     * @throws YangException If value buffer does not have len number of
     *             octets/bytes.
     */
    protected void exact(int numberOfBytes) throws YangException {
        Utils.restrict(value, numberOfBytes, Utils.Operator.EQ);
    }

    /**
     * Checks that the value buffer of this object has less than the specified
     * number of octets/bytes.
     * 
     * @param len The number of octets/bytes to compare against.
     * @throws YangException If value buffer does not have less than len number
     *             of octets/bytes.
     */
    protected void min(int numberOfBytes) throws YangException {
        Utils.restrict(value, numberOfBytes, Utils.Operator.GE);
    }

    /**
     * Checks that the value buffer of this object has more than the specified
     * number of octets/bytes.
     * 
     * @param len The number of octets/bytes to compare against.
     * @throws YangException If value buffer does not have more than len number
     *             of octets/bytes.
     */
    protected void max(int numberOfBytes) throws YangException {
        Utils.restrict(value, numberOfBytes, Utils.Operator.LE);
    }

    /* ---------- YangBinary encoder/decoder class ---------- */

    /**
     * A Base64 Encoder/Decoder used to encode and decode data in Base64 format
     * as described in RFC 4648.
     * <p>
     * This is "Open Source" software and released under the <a
     * href="http://www.gnu.org/licenses/lgpl.html">GNU/LGPL</a> license.<br>
     * It is provided "as is" without warranty of any kind.<br>
     * Copyright 2003: Christian d'Heureuse, Inventec Informatik AG,
     * Switzerland.<br>
     * Home page: <a href="http://www.source-code.biz">www.source-code.biz</a>
     * <br>
     * 
     * @author Christian d'Heureuse
     */
    private static class Base64Coder {

        /**
         * Mapping table from 6-bit nibbles to Base64 characters.
         */
        private static char[] map1 = new char[64];
        static {
            int i = 0;
            for (char c = 'A'; c <= 'Z'; c++) {
                map1[i++] = c;
            }
            for (char c = 'a'; c <= 'z'; c++) {
                map1[i++] = c;
            }
            for (char c = '0'; c <= '9'; c++) {
                map1[i++] = c;
            }
            map1[i++] = '+';
            map1[i] = '/';
        }

        /**
         * Mapping table from Base64 characters to 6-bit nibbles.
         */
        private static byte[] map2 = new byte[128];
        static {
            for (int i = 0; i < map2.length; i++) {
                map2[i] = -1;
            }
            for (int i = 0; i < 64; i++) {
                map2[map1[i]] = (byte) i;
            }
        }

        /**
         * Encodes a string into Base64 format. No blanks or line breaks are
         * inserted.
         * 
         * @param s a String to be encoded.
         * @return A String with the Base64 encoded data.
         */
        public static String encodeString(String s) {
            return new String(encode(s.getBytes()));
        }

        /**
         * Encodes a byte array into Base64 format. No blanks or line breaks
         * are inserted.
         * 
         * @param in an array containing the data bytes to be encoded.
         * @return A character array with the Base64 encoded data.
         */
        public static char[] encode(byte[] in) {
            return encode(in, in.length);
        }

        /**
         * Encodes a byte array into Base64 format. No blanks or line breaks
         * are inserted.
         * 
         * @param in an array containing the data bytes to be encoded.
         * @param iLen number of bytes to process in <code>in</code>.
         * @return A character array with the Base64 encoded data.
         */
        public static char[] encode(byte[] in, int iLen) {
            final int oDataLen = (iLen * 4 + 2) / 3; // output length without
                                                     // padding
            final int oLen = ((iLen + 2) / 3) * 4; // output length including
                                                   // padding
            final char[] out = new char[oLen];
            int ip = 0;
            int op = 0;
            while (ip < iLen) {
                final int i0 = in[ip++] & 0xff;
                final int i1 = ip < iLen ? in[ip++] & 0xff : 0;
                final int i2 = ip < iLen ? in[ip++] & 0xff : 0;
                final int o0 = i0 >>> 2;
                final int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
                final int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
                final int o3 = i2 & 0x3F;
                out[op++] = map1[o0];
                out[op++] = map1[o1];
                out[op] = op < oDataLen ? map1[o2] : '=';
                op++;
                out[op] = op < oDataLen ? map1[o3] : '=';
                op++;
            }
            return out;
        }

        /**
         * Decodes a string from Base64 format.
         * 
         * @param s a Base64 String to be decoded.
         * @return A String containing the decoded data.
         * @throws IllegalArgumentException If the input is not valid Base64
         *             encoded data.
         */
        public static String decodeString(String s) {
            return new String(decode(s));
        }

        /**
         * Decodes a byte array from Base64 format.
         * 
         * @param s a Base64 String to be decoded.
         * @return An array containing the decoded data bytes.
         * @throws IllegalArgumentException If the input is not valid Base64
         *             encoded data.
         */
        public static byte[] decode(String s) {
            return decode(s.toCharArray());
        }

        /**
         * Decodes a byte array from Base64 format. No blanks or line breaks
         * are allowed within the Base64 encoded data.
         * 
         * @param in a character array containing the Base64 encoded data.
         * @return An array containing the decoded data bytes.
         * @throws IllegalArgumentException If the input is not valid Base64
         *             encoded data.
         */
        public static byte[] decode(char[] in) {
            int iLen = in.length;
            if (iLen % 4 != 0) {
                throw new IllegalArgumentException("Length of Base64 encoded" +
                        " input string is not a multiple of 4.");
            }
            while (iLen > 0 && in[iLen - 1] == '=') {
                iLen--;
            }
            final int oLen = (iLen * 3) / 4;
            final byte[] out = new byte[oLen];
            int ip = 0;
            int op = 0;
            while (ip < iLen) {
                final int i0 = in[ip++];
                final int i1 = in[ip++];
                final int i2 = ip < iLen ? in[ip++] : 'A';
                final int i3 = ip < iLen ? in[ip++] : 'A';
                if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                    throw new IllegalArgumentException(
                            "Illegal character in Base64 encoded data.");
                }
                final int b0 = map2[i0];
                final int b1 = map2[i1];
                final int b2 = map2[i2];
                final int b3 = map2[i3];
                if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                    throw new IllegalArgumentException(
                            "Illegal character in Base64 encoded data.");
                }
                final int o0 = (b0 << 2) | (b1 >>> 4);
                final int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
                final int o2 = ((b2 & 3) << 6) | b3;
                out[op++] = (byte) o0;
                if (op < oLen) {
                    out[op++] = (byte) o1;
                }
                if (op < oLen) {
                    out[op++] = (byte) o2;
                }
            }
            return out;
        }

        /**
         * Dummy constructor.
         */
        private Base64Coder() {
        }

    }

}
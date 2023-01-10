package com.tailf.jnc.framing;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Common interface to InputStream and Reader capabilities.
 *
 * They deal with char[] or byte[], respectively, so implementing
 * classes that serve as a façades over InputStream/Reader instances
 * need to provide some access methods to such objects.
 */
public interface BaseReader<B> {
    int read(B buf) throws IOException;
    int read(B buf, int offset, int length) throws IOException;
    int bufSize(B buf);
    ByteBuffer encode(B buf, int offset, int length);
    boolean ready() throws IOException;
}

class ByteReader implements BaseReader<byte[]> {
    InputStream in;

    ByteReader(InputStream in) {
        this.in = in;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return in.read(buf);
    }

    @Override
    public int read(byte[] buf, int offset, int length) throws IOException {
        return in.read(buf, offset, length);
    }

    @Override
    public int bufSize(byte[] buf) {
        return buf.length;
    }

    @Override
    public ByteBuffer encode(byte[] buf, int offset, int length) {
        return ByteBuffer.wrap(buf, offset, length);
    }

    @Override
    public boolean ready() throws IOException {
        return in.available() > 0;
    }
}

class CharReader implements BaseReader<char[]> {
    Reader in;

    CharReader(Reader in) {
        this.in = in;
    }

    @Override
    public int read(char[] buf) throws IOException {
        return in.read(buf);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return in.read(buf, offset, length);
    }

    @Override
    public int bufSize(char[] buf) {
        return buf.length;
    }

    @Override
    public ByteBuffer encode(char[] buf, int offset, int length) {
        return StandardCharsets.UTF_8.encode(CharBuffer.wrap(buf, offset, length));
    }

    @Override
    public boolean ready() throws IOException {
        return in.ready();
    }
}

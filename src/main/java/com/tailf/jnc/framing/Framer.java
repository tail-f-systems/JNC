package com.tailf.jnc.framing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;



/**
 * Frame formatting and parsing.
 */

public interface Framer {
    /**
     * Write the frame and the corresponding markers to the output.
     */
    void sendFrame(String frame) throws IOException;
    /**
     * Parse the input and format a frame.
     */
    String parseFrame() throws IOException;
}

abstract class BaseFramer implements Framer {
    DataReader rdr;

    BaseFramer(DataReader rdr) {
        this.rdr = rdr;
    }
}

@SuppressWarnings("PMD.ClassNamingConventions")
class NC1_0_Framer extends BaseFramer {
    private static final String endmarker = "]]>]]>";
    Reader in;
    Writer out;
    BaseReader<char[]> inFacade;
    static final int BUFSIZ = 8 * 1024;
    char[] buf = new char[BUFSIZ];

    public NC1_0_Framer(DataReader rdr, InputStream in, OutputStream out) {
        super(rdr);
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = new PrintWriter(out);
        inFacade = new CharReader(this.in);
    }

    @Override
    public void sendFrame(String frame) throws IOException {
        out.write(frame);
        out.write(endmarker);
        out.flush();
    }

    @Override
    public String parseFrame() throws IOException {
        StringBuilder frame = new StringBuilder();
        while (true) {
            in.mark(BUFSIZ + 1);
            int read = rdr.readData(inFacade, buf);
            frame.append(buf, 0, read);
            int offset = frame.length() - read;
            int start = Integer.max(0, offset - endmarker.length() + 1);
            int markerpos = frame.indexOf(endmarker, start);
            if (markerpos != -1) {
                // need to rewind the stream
                in.reset();
                in.skip(read - (frame.length() - markerpos - endmarker.length()));
                return frame.substring(0, markerpos);
            }
        }
    }
}

@SuppressWarnings("PMD.ClassNamingConventions")
class NC1_1_Framer extends BaseFramer {
    static final int MAX_HEADER_SIZE = 13;
    byte[] chunkHdr = new byte[MAX_HEADER_SIZE];

    static final int LF = '\n';
    static final int HASH = '#';

    InputStream in;
    OutputStream out;
    BaseReader<byte[]> inFacade;

    public NC1_1_Framer(DataReader rdr, InputStream in, OutputStream out) {
        super(rdr);
        this.in = new BufferedInputStream(in);
        this.out = out;
        inFacade = new ByteReader(this.in);
    }

    @Override
    public void sendFrame(String frame) throws IOException {
        byte[] data = frame.getBytes(StandardCharsets.UTF_8);
        out.write(String
                  .format("%n#%1$d%n", data.length)
                  .getBytes(StandardCharsets.UTF_8));
        out.write(data);
        out.write("\n##\n".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Index of the LF byte at the end of chunk header, -1 if not found.
     */
    int hdrEnd(int hdrSize) {
        if (hdrSize < 4) {
            // at least LF HASH DIGIT/HASH LF
            return -1;
        }
        for (int i = 3; i < hdrSize; i++) {
            if (chunkHdr[i] == LF) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Read the chunk header or frame terminator.  Assumes that the
     * stream is positioned at the initial LF HASH sequence.  When
     * completed, the stream points at the first byte after the final
     * LF.
     *
     * @return length of the header
     */
    private int readChunkHeader() throws IOException {
        in.mark(MAX_HEADER_SIZE + 1);
        int hdrRead = 0;
        while (hdrEnd(hdrRead) == -1) {
            if (hdrRead == MAX_HEADER_SIZE) {
                throw new IOException("Invalid chunk header");
            }
            hdrRead += rdr.readData(inFacade, chunkHdr, hdrRead, MAX_HEADER_SIZE - hdrRead);
        }
        if (chunkHdr[0] != LF || chunkHdr[1] != HASH) {
            throw new IOException(String.format("Expected LF HASH, got %s",
                                                Arrays.copyOf(chunkHdr, 2)));
        }
        int realHdrSize = hdrEnd(hdrRead) + 1;
        // skip the header, the rest is the actual chunk or belongs to another frame
        in.reset();
        if (in.skip(realHdrSize) != realHdrSize) {
            throw new IOException("Error while reading from the stream");
        }
        return realHdrSize;
    }

    @Override
    public String parseFrame() throws IOException {
        StringBuilder frame = new StringBuilder();
        byte[] chunk;
        while (true) {
            int hdrSize = readChunkHeader();
            if (hdrSize == 4 && chunkHdr[2] == HASH) {
                // end of frame found - we're done
                return frame.toString();
            }
            // we know we are looking at new chunk - parse its length
            String chunkSizeStr = new String(chunkHdr, 2, hdrSize - 3, StandardCharsets.UTF_8);
            int chunkSize;
            try {
                chunkSize = Integer.parseUnsignedInt(chunkSizeStr);
            } catch (NumberFormatException e) {
                String errmsg = String.format("Expected new chunk size, received <<%s>>",
                                              chunkSizeStr);
                throw new IOException(errmsg, e);
            }
            // read the chunk now
            chunk = new byte[chunkSize];
            int chunkRead = 0;
            while (chunkRead < chunkSize) {
                chunkRead += rdr.readData(inFacade, chunk, chunkRead, chunkSize - chunkRead);
            }
            frame.append(new String(chunk, StandardCharsets.UTF_8));
        }
    }
}

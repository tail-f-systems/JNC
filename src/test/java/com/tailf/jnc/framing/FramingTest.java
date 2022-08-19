package com.tailf.jnc.framing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import static org.junit.Assert.assertEquals;

public class FramingTest {
    private final static String ENDMARK = "]]>]]>";
    private final static String FRAME = "a frame";
    private final static String CHUNKED_FRAME =
        String.format("\n#%d\n%s\n##\n", FRAME.length(), FRAME);
    private final static String LONGER_FRAME = "a longer frame";
    private final static int splitpoint = 6;
    private final static String CHUNKED_LONGER_FRAME =
        String.format("\n#%d\n%s\n#%d\n%s\n##\n",
                      splitpoint, LONGER_FRAME.substring(0, splitpoint),
                      LONGER_FRAME.length() - splitpoint, LONGER_FRAME.substring(splitpoint));
    private final static String INTL_FRAME = "私のホバークラフトは鰻でいっぱいです";
    private final static String CHUNKED_INTL_FRAME =
        String.format("\n#%d\n%s\n##\n", INTL_FRAME.getBytes(StandardCharsets.UTF_8).length, INTL_FRAME);

    @Rule
    public Timeout globalTimeout= new Timeout(500, TimeUnit.MILLISECONDS);

    @Test
    public void separatedFraming() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Framer framer =
            Framing.END_OF_MESSAGE.newSessionFramer(null, InputStream.nullInputStream(), out);
        framer.sendFrame(FRAME);
        assertEquals("formatted frame should have separator",
                     FRAME + ENDMARK, out.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void chunkedFraming() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Framer framer =
            Framing.CHUNKED.newSessionFramer(null, InputStream.nullInputStream(), out);
        framer.sendFrame(FRAME);
        assertEquals("formatted frame should be chunked",
                     CHUNKED_FRAME, out.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void chunkedIntlFraming() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Framer framer =
            Framing.CHUNKED.newSessionFramer(null, InputStream.nullInputStream(), out);
        framer.sendFrame(INTL_FRAME);
        assertEquals("formatted frame with non-latin chars should be chunked",
                     CHUNKED_INTL_FRAME, out.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void baseAccept() throws IOException {
        InputStream in = new ByteArrayInputStream((FRAME + ENDMARK).getBytes(StandardCharsets.UTF_8));
        Framer framer = Framing.END_OF_MESSAGE.newSessionFramer(in, OutputStream.nullOutputStream());
        assertEquals("should accept the 1.0 frame", FRAME, framer.parseFrame());
    }

    @Test
    public void chunkedAccept() throws IOException {
        InputStream in = new ByteArrayInputStream(CHUNKED_FRAME.getBytes(StandardCharsets.UTF_8));
        Framer framer = Framing.CHUNKED.newSessionFramer(in, OutputStream.nullOutputStream());
        assertEquals("should accept the 1.1 frame", FRAME, framer.parseFrame());
    }

    @Test
    public void chunkedIntlAccept() throws IOException {
        InputStream in = new ByteArrayInputStream(CHUNKED_INTL_FRAME.getBytes(StandardCharsets.UTF_8));
        Framer framer = Framing.CHUNKED.newSessionFramer(in, OutputStream.nullOutputStream());
        assertEquals("should accept the 1.1 frame with multibyte chars",
                     INTL_FRAME, framer.parseFrame());
    }

    @Test
    public void twoChunks() throws IOException {
        InputStream in = new ByteArrayInputStream(CHUNKED_LONGER_FRAME.getBytes(StandardCharsets.UTF_8));
        Framer framer = Framing.CHUNKED.newSessionFramer(in, OutputStream.nullOutputStream());
        assertEquals("should accept the chunked frame", LONGER_FRAME, framer.parseFrame());
    }

    /**
     * Extension of the {@link DataReader} class that makes sure that
     * the data come in two parts separated at the given boundary.
     */
    private static class TestDataReader implements DataReader {
        OutputStream source;
        Iterator<byte[]> dataIterator;

        TestDataReader(OutputStream source, byte[] data, int boundary) {
            this.source = source;
            dataIterator = Arrays.asList(Arrays.copyOfRange(data, 0, boundary),
                                         Arrays.copyOfRange(data, boundary, data.length))
                .iterator();
        }

        @Override
        public <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf)
            throws IOException {
            return readData(rdr, buf, 0, rdr.bufSize(buf));
        }

        @Override
        public <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf,
                                          int offset, int length) throws IOException {
            if (! rdr.ready()) {
                if (dataIterator.hasNext()) {
                    byte[] data = dataIterator.next();
                    source.write(data, 0, data.length);
                    source.flush();
                } else {
                    // readMoreData would block - no need to wait for that
                    throw new IOException("No data available");
                }
            }
            return rdr.read(buf, offset, length);
        }
    }

    void partialChunksTest(String frame, String chunkedFrame) throws IOException {
        PipedOutputStream po = new PipedOutputStream();
        PipedInputStream pi = new PipedInputStream(po);
        byte[] data = chunkedFrame.getBytes(StandardCharsets.UTF_8);
        for (int i = 2; i < data.length - 1; i++) {
            DataReader rdr = new TestDataReader(po, data, i);
            Framer framer = Framing.CHUNKED.newSessionFramer(rdr, pi,
                                                             OutputStream.nullOutputStream());
            assertEquals("should restore the frame from two parts", frame, framer.parseFrame());
        }
    }

    @Test
    public void partialChunks() throws IOException {
        partialChunksTest(LONGER_FRAME, CHUNKED_LONGER_FRAME);
    }

    @Test
    public void partialMultibyteChunks() throws IOException {
        partialChunksTest(INTL_FRAME, CHUNKED_INTL_FRAME);
    }

    @Test
    public void twoSeparatedFrames() throws IOException {
        String frames = INTL_FRAME + ENDMARK + LONGER_FRAME + ENDMARK;
        byte[] data = frames.getBytes(StandardCharsets.UTF_8);
        PipedOutputStream po = new PipedOutputStream();
        PipedInputStream pi = new PipedInputStream(po);
        for (int i = 1; i < frames.length(); i++) {
            int bytesplit = frames.substring(0, i).getBytes(StandardCharsets.UTF_8).length;
            DataReader rdr = new TestDataReader(po, data, bytesplit);
            Framer framer = Framing.END_OF_MESSAGE.newSessionFramer(rdr, pi,
                                                             OutputStream.nullOutputStream());
            assertEquals("should restore both frames from parts",
                         INTL_FRAME + LONGER_FRAME,
                         framer.parseFrame() + framer.parseFrame());
        }
    }

    @Test
    public void twoChunkedFrames() throws IOException {
        byte[] data = (CHUNKED_INTL_FRAME + CHUNKED_LONGER_FRAME).getBytes(StandardCharsets.UTF_8);
        PipedOutputStream po = new PipedOutputStream();
        PipedInputStream pi = new PipedInputStream(po);
        for (int i = 1; i < data.length; i++) {
            DataReader rdr = new TestDataReader(po, data, i);
            Framer framer = Framing.CHUNKED.newSessionFramer(rdr, pi,
                                                             OutputStream.nullOutputStream());
            assertEquals("should restore both frames from parts",
                         INTL_FRAME + LONGER_FRAME,
                         framer.parseFrame() + framer.parseFrame());
        }
    }
}

package com.tailf.jnc.framing;

import java.io.IOException;

/**
 * Wrapper around reading routine.  Implementation can do any
 * stuff that is needed while reading.
 */
public interface DataReader {
    <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf)
        throws IOException;
    <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf, int offset, int length)
        throws IOException;
}

class BasicDataReader implements DataReader {
    public <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf)
        throws IOException {
        return rdr.read(buf);
    }
    public <DataBufType> int readData(BaseReader<DataBufType> rdr, DataBufType buf, int offset, int length)
        throws IOException {
        return rdr.read(buf, offset, length);
    }
}

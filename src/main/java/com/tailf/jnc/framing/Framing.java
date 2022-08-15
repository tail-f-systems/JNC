package com.tailf.jnc.framing;

import java.lang.reflect.Constructor;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Enum defining the framing types
 */
public enum Framing {
    END_OF_MESSAGE(NC1_0_Framer.class),
    CHUNKED(NC1_1_Framer.class);

    Class<? extends BaseFramer> clazz;

    Framing(Class<? extends BaseFramer> clazz) {
        this.clazz = clazz;
    }

    public Framer newSessionFramer(InputStream in, OutputStream out) {
        return newSessionFramer(new BasicDataReader(), in, out);
    }

    public Framer newSessionFramer(DataReader rdr, InputStream in, OutputStream out) {
        try {
            Constructor<? extends BaseFramer> cstr = clazz.getConstructor(DataReader.class,
                                                                          InputStream.class,
                                                                          OutputStream.class);
            return cstr.newInstance(rdr, in, out);
        } catch (ReflectiveOperationException e) {
            System.err.println("Failed to instantiate a framer.");
            return null;
        }
    }
}

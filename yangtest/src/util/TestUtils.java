package util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

public class TestUtils {
    
    private static util.Environment env = new util.Environment();

    public static void startConfD(String confdPath) 
            throws InterruptedException, IOException {
        boolean success = util.ConfD.startConfD(confdPath, env);
        assertTrue("Failed to launch ConfD.\nDid you set the CONFD_DIR env "
                + "variable correctly?", success);
    }

    public static void makeStopClean(String confdPath) 
            throws InterruptedException, IOException {
        util.ConfD.makeStop(confdPath, env);
        boolean success = util.ConfD.makeClean(confdPath, env);
        assertTrue("Failed to clean up after.\nDid you set the CONFD_DIR env "
                + "variable correctly?", success);
    }
}

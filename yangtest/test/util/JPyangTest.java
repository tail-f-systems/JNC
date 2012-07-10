package util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import util.JPyang;

public class JPyangTest {

    @Test
    public void testRun() throws IOException, InterruptedException {
        JPyang.run("src/cont", "container-test.yang");
    }

}

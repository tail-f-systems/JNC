package app;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.tailf.inm.INMException;

public class MainTest2 {

    @Test
    public void testGetConfigDevice() throws IOException, INMException {
        Main main = new Main();
        main.getConfig();
    }

    @Test
    public void testGetConfig() {
        fail("Not yet implemented");
    }

}

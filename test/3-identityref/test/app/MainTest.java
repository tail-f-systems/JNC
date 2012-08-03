package app;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import gen.Idref;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tailf.jnc.*;

public class MainTest {
    
    Main main;
    
    private static void flush(InputStream is, String msg) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = "", tmp;
        if(msg != null && msg.length() > 0 && br.ready()) {
            System.err.println(msg);
        }
        if (msg != null) {
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }
        } else {
            while ((tmp = br.readLine()) != null) {
                line = tmp;
            }
            System.out.println("Confd started: "+line);
        }
    }
    
    private static void startConfD() throws InterruptedException, IOException {
        File confdDir = new File("../../confd");
        String[] args = {"make", "clean", "all", "start"};
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(confdDir);
        Process p = builder.start();
        flush(p.getErrorStream(), "Test error: unable to run makefile");
        flush(p.getInputStream(), null);
        p.waitFor();
    }
    
    @BeforeClass
    public static void oneTimeSetUp() throws InterruptedException, IOException,
            JNCException {
        Idref.enable();
        startConfD();
    }
    
    @Before
    public void setUp() {
        main = new Main();
    }
    
    @Test
    public void testInitialConfig() throws JNCException, IOException {
        Iterator<Element> iter = main.getConfig().listIterator();
        while (iter.hasNext()) {
            Element config = (Element)iter.next();
            if (config instanceof gen.C) {
                gen.C l = (gen.C) config;
                System.out.println(l.getRefLeafValue());
            }
        }
    }

}

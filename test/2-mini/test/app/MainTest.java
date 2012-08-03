package app;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import gen.Mini;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;

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
        Mini.enable();
        startConfD();
    }
    
    @Before
    public void setUp() {
        main = new Main();
    }
    
    @Test
    public void testInitialConfig() throws JNCException, IOException {
        Iterator<Element> iter = main.getConfig().listIterator();
        boolean test3Found = false, test4Found = false;
        while (iter.hasNext()) {
            Element config = (Element)iter.next();
            if (config instanceof gen.L) {
                gen.L l = (gen.L) config;
                test3Found |= l.getKValue().equals("test3");
                test4Found |= l.getKValue().equals("test4");
            }
        }
        assertTrue("Test3 not found", test3Found);
        assertFalse("Test4 found", test4Found);
    }
    
    @Test
    public void testUpdateConfig() throws JNCException, IOException {
        gen.L l = null;
        String msg = "test2 already in configuration!";
        Iterator<Element> iter = main.getConfig().listIterator();
        while (iter.hasNext()) {
            Element next = (Element)iter.next();
            if (next instanceof gen.L) {
                l = (gen.L) next;
                if (l.getKValue().equals("test2")) {
                    fail(msg);
                }
            }
        }
        
        if (l == null) {
            fail("No instance of list l was found in configuration");
        }
        l.setKValue("test2");
        main.editConfig(l);
        iter = main.getConfig().listIterator();
        boolean test2Found = false;
        while (iter.hasNext()) {
            Element config = (Element)iter.next();
            if (config instanceof gen.L) {
                l = (gen.L) config;
                test2Found |= l.getKValue().equals("test2");
            }
        }
        assertTrue("test2 set but not found in configuration", test2Found);
    }

}

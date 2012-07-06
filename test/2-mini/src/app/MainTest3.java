package app;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class MainTest3 {
    
    private static void flush(InputStream is, String msg) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        if(msg != null && msg.length() > 0 && br.ready()) {
            System.err.println(msg);
        }
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }
    
    private static void startConfD() throws InterruptedException, IOException {
        File confdDir = new File("confd");
        String[] args = {"make", "clean", "all", "start"};
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(confdDir);
        Process p = builder.start();
        flush(p.getErrorStream(), "Test error: unable to run makefile");
        flush(p.getInputStream(), null);
        p.waitFor();
    }

    private static boolean daemonIsRunning() throws IOException,
            InterruptedException {
//        String confd = System.getenv("CONFD_DIR");
//        File confdDir = new File(confd+"/bin/");
//        String[] args = {"confd", "--status"};
//        ProcessBuilder builder = new ProcessBuilder(args);
//        builder.directory(confdDir);
//        Process p = builder.start();
//        flush(p.getErrorStream(), "Test error: unable to run makefile");
//        flush(p.getInputStream(), null);
//        p.waitFor();
        return false;
    }

    @Test
    public void test() {
        try {
            if (!daemonIsRunning()) {
                startConfD();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

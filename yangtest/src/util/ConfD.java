package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class ConfD {
    
    /**
     * Reads input from is and outputs to standard out or err
     * 
     * @param is Input stream to read from
     * @return false if msg not null and is not empty, else true
     * @param msg 
     *          Message: Printed if is has data.
     *          if msg is null, output the last line of is to std out.
     *          If msg is not null, print message and output to std err.
     * @throws IOException
     *          If unable to read from is
     */
    public static boolean flush(InputStream is, String msg) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = "", tmp;
        if(msg != null && msg.length() > 0 && br.ready()) {
            System.err.println(msg);
        }
        if (msg != null) {
            line = br.readLine();
            boolean empty = line == null;
            while (line != null) {
                System.err.println(line);
                line = br.readLine();
            }
            return empty;
        } else {
            while ((tmp = br.readLine()) != null) {
                line = tmp;
            }
            System.out.println(line);
        }
        return true;
    }
    
    /**
     * Spawns a make process that (re)starts confd.
     * 
     * @param confdLocation Location of makefile to start confd with
     * @return true on success
     * @throws InterruptedException
     *          if unable to start make process
     * @throws IOException
     *          if unable to read from process input streams
     */
    public static boolean startConfD(String confdLocation, Environment env)
            throws InterruptedException, IOException {
        File confdDir = new File(confdLocation);
        String[] args = {"make", "clean", "all", "start"};
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(confdDir);
        builder.environment().put("CONFD_DIR", env.getVariable("CONFD_DIR"));
        Process p = builder.start();
        String errmsg = "ConfD error: unable to run makefile";
        boolean err = flush(p.getErrorStream(), errmsg);
        System.out.print("Confd started: ");
        flush(p.getInputStream(), null);
        p.waitFor();
        return err;
    }
    
}

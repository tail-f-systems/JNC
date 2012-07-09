package util;

import java.io.IOException;

public class JPyang {
    
    public static void run(String outDir, String yangFile) 
            throws IOException, InterruptedException {
        String[] arguments = {
                "/home/emil/Dropbox/tail-f/confd/bin/pyang",
                "-p", outDir,
                "-f", "jpyang",
                "-d", outDir,
                "--jpyang-no-schema",
                outDir+"/"+yangFile};
        ProcessBuilder builder = new ProcessBuilder(arguments);
        // builder.directory(confdDir);
        Process p = builder.start();
        ConfD.flush(p.getErrorStream(), "Test error: pyang encountered " +
        		"an error.");
        ConfD.flush(p.getInputStream(), null);
        p.waitFor();
    }

    /**
     * @param args ignored
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) 
            throws IOException, InterruptedException {
        run("src/cont", "container-test.yang");
    }

}

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

//https://tomgregory.com/gradle-task-inputs-and-outputs/
class JncPyangTask extends DefaultTask {
    @OutputDirectory
    File outputDir;

    @Optional
    @Input
    String yangPath = null

    @Optional
    @Input
    String pluginDir = null

    @InputFiles
    File[] inputFiles;

    @TaskAction
    def generate() {
        // TODO check pyang is installed
        outputs.getPreviousOutputFiles().each {file ->
            if (file.isFile()) {
                logger.info("Deleting previous file ${file.getPath()}")
                file.delete()
            }
        }

        inputFiles.each { yang ->
            def script = "pyang${yangPath == null ? "" : " -p $yangPath"}${pluginDir == null ? "" : " --plugindir $pluginDir"} -f jnc --jnc-output ${outputDir} ${yang}".execute()
            script.waitForProcessOutput(System.out, System.err)
            logger.info "Generated ${yang}"
        }
        logger.info "Generated ${inputFiles.size()} to ${outputDir.getPath()} path ${yangPath}"
    }
}

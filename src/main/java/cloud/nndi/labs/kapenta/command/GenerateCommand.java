package cloud.nndi.labs.kapenta.command;

import cloud.nndi.labs.kapenta.autogen.KapentaApiGenerator;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Paths;

import static picocli.CommandLine.Option;

public class GenerateCommand implements Runnable {

    @Option(names = {"-d", "--source-directory"}, required = true)
    private String directory;

    @Option(names = {"-o", "--output"}, required = true)
    private String outputFile;

    @Override
    public void run() {
        KapentaApiGenerator generator = new KapentaApiGenerator(
            Paths.get(FilenameUtils.getName(directory)),
            Paths.get(FilenameUtils.getName(outputFile))
        );
        try {
            generator.generate();
        } catch (Exception e) {
            System.err.println("Failed to generate YAML configuration. Got: " + e.getMessage());
        }
    }
}

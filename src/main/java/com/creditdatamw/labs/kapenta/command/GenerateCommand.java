package com.creditdatamw.labs.kapenta.command;

import com.creditdatamw.labs.kapenta.autogen.PentahoApiGenerator;

import java.nio.file.Paths;

import static picocli.CommandLine.Option;

public class GenerateCommand implements Runnable {

    @Option(names = {"-d", "--source-directory"}, required = true)
    private String directory;

    @Option(names = {"-o", "--output"}, required = true)
    private String outputFile;

    @Override
    public void run() {
        PentahoApiGenerator generator = new PentahoApiGenerator(
                Paths.get(directory), Paths.get(outputFile));
        try {
            generator.generate();
        } catch (Exception e) {
            System.err.println("Failed to generate YAML configuration. Got: " + e.getMessage());
        }
    }
}

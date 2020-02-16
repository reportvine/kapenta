package com.creditdatamw.labs.kapenta.reportdefinition;

import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportDefinitionDirectoryWalker {
    private Stream.Builder<ReportDefinition> reportDefinitions;
    private int maximumDepth;
    private final Path directory;
    private ReportDefinitionFileReader parser;
    private boolean directoryWalked = false;

    /**
     *
     * @param directory
     * @param maximumDepth The depth of recursion on the directory in case of
     *                     directory with report definitions in subdirectories
     * @param resourceManager
     */
    public ReportDefinitionDirectoryWalker(Path directory, int maximumDepth, ResourceManager resourceManager) {
        assert Files.isDirectory(directory);
        this.directory = directory;
        this.maximumDepth = maximumDepth;
        this.parser = new ReportDefinitionFileReader(resourceManager);
        this.reportDefinitions = Stream.builder();
    }

    /**
     * Search for pentaho report files in the given directory
     *

     * @return a list of the absolute paths to the Pentaho files
     */
    private void walkDirectory() {
        try {
            Files.walkFileTree(this.directory, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return innerVisitFile(file, attrs);
                }
            });
            this.directoryWalked = true;
        } catch(IOException ioe) {
            LoggerFactory.getLogger(getClass())
                .error("Failed to search for prpts in : {}", directory, ioe);
            throw new RuntimeException(ioe);
        }
    }

    private FileVisitResult innerVisitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file == null)
            return FileVisitResult.CONTINUE;

        Path filePath = file.getFileName();
        
        if (filePath == null)
            return FileVisitResult.CONTINUE;

        String fileName = filePath.toString();
        if (fileName.endsWith(".prpt")) {
            try {
                ReportDefinition reportDefinition = parser.createFromFile(
                    this.directory,
                    file,
                    true
                );
                reportDefinitions.add(reportDefinition);
            } catch(Exception ex) {
                LoggerFactory.getLogger(getClass())
                        .error("Failed to parse report file: " + file, ex);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    public int getMaximumDepth() {
        return this.maximumDepth;
    }

    /**
     * Get the report definitions as a Stream
     * @return
     */
    public Stream<ReportDefinition> getReportDefinitionStream() {
        return reportDefinitions.build();
    }

    /**
     * Get Report Defintions as a List
     * @return
     */
    public List<ReportDefinition> getReportDefinitions() {
        if (!directoryWalked) {
            this.walkDirectory();
        }
        return getReportDefinitionStream().collect(Collectors.toList());
    }
}

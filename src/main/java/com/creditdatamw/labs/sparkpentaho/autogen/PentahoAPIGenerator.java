package com.creditdatamw.labs.sparkpentaho.autogen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.pentaho.reporting.engine.classic.core.MasterReport;


import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.creditdatamw.labs.sparkpentaho.SparkPentahoAPI;


import com.creditdatamw.labs.sparkpentaho.config.ApiConfiguration;
import com.creditdatamw.labs.sparkpentaho.config.Method;
import com.creditdatamw.labs.sparkpentaho.config.ReportConfiguration;
import com.creditdatamw.labs.sparkpentaho.reports.OutputType;

import com.creditdatamw.labs.sparkpentaho.reports.ParameterDefinition;

import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Pentaho API Generator
 * <br/>
 * Given a a directory where you have pentaho reports and an output file
 * will generate and API configuration for the reports in that directory
 * with all the parameters defined.
 *
 */
public class PentahoAPIGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PentahoAPIGenerator.class);
    private static ObjectMapper objectMapper = SparkPentahoAPI.OBJECT_MAPPER;

    private final Path sourceDir;
    private final Path outputFile;

    public PentahoAPIGenerator(Path sourceDir, Path outputFile) {
        this.sourceDir = sourceDir;
        this.outputFile = outputFile;
    }

    public void generate() throws IOException {
        List<ReportDefinition> defs = searchForPrpts(sourceDir);
        ApiConfiguration apiConfiguration = buildConfiguration(defs);
        writeConfiguratotionToFile(apiConfiguration);
    }

    public void writeConfiguratotionToFile(ApiConfiguration apiConfiguration) throws IOException {
        Objects.requireNonNull(apiConfiguration);
        
        ObjectWriter writer = objectMapper.writerFor(ApiConfiguration.class);
        
        writer.writeValues(outputFile.toFile());
        // TODO: Figure out how to write YAML out to the file
        objectMapper.writeValue(writer, apiConfiguration);
    }
    private ApiConfiguration buildConfiguration(List<ReportDefinition> reportDefinitions) {
        ApiConfiguration apiConfiguration = new ApiConfiguration();

        apiConfiguration.setApiRoot("/api");
        apiConfiguration.setHost("localhost");
        apiConfiguration.setPort(4567);
        apiConfiguration.setReports(reportDefinitions.stream()
                .map(reportDefinition -> {
                    ReportConfiguration reportConfiguration = new ReportConfiguration();

                    String reportPath = reportDefinition.getReportName().toLowerCase().replace(" ", "_");
                    
                    reportConfiguration.setPath(reportPath);

                    reportConfiguration.setReportName(reportDefinition.getReportName());
                    reportConfiguration.setDescription(reportDefinition.getDescription());
                    reportConfiguration.setVersion(reportDefinition.getVersion());
                    reportConfiguration.setParameters(reportDefinition.getParameters());
                    reportConfiguration.setReportFilePath(reportDefinition.getReportFilePath());
                    
                    // Support both GET and POST by default
                    reportConfiguration.setMethods(Method.ALL);

                    // Add all the output types for this report
                    reportConfiguration.setExtensions(Arrays.asList(OutputType.values())
                        .stream()
                        .filter(o -> o.equals(OutputType.NONE))
                        .map(OutputType::name)
                        .collect(Collectors.toList()));

                    return reportConfiguration;
                })
                .collect(Collectors.toList()));

        return apiConfiguration;
    }

    /**
     * Search for pentaho report files in the given directory
     * 
     * @return a list of the absolute paths to the pentaho files
     */
    private List<ReportDefinition> searchForPrpts(Path filePath) {
        try {
            final List<ReportDefinition> prptFiles = new ArrayList<>();
            Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".prpt")) {
                        try {
                            ReportDefinition reportDefinition = parseReportDefinition(file);
                            prptFiles.add(reportDefinition);
                        } catch(Exception ex) {
                            LOGGER.error("Failed to parse report file: " + file, ex);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return prptFiles;
        } catch(IOException ioe) {
            LOGGER.error("Failed to search for prpts in : " + filePath, ioe);
        }
        return Collections.emptyList();
    }

    private final PentahoMetadataScanner scanner = new PentahoMetadataScanner();
    private ReportDefinition parseReportDefinition(Path reportFilePath) throws Exception {
        
        org.pentaho.reporting.engine.classic.core.ReportDefinition pentahoReport = scanner.reportDefinition(reportFilePath);

        MasterReport masterReport = (MasterReport) pentahoReport.getParentSection().getMasterReport();
        
        List<ParameterDefinitionEntry> pentahoParams = Arrays.asList(masterReport.getParameterDefinition().getParameterDefinitions());

        List<ParameterDefinition> params = pentahoParams.stream()
                .map(param -> {
                    try {
                        if (param.isMandatory()) {
                            return ReportDefinition.requiredParameter(param.getName(),
                                                                    param.getDefaultValue(null),
                                                                    param.getValueType());
                        }
                        return ReportDefinition.optionalParameter(param.getName(),
                                                                param.getDefaultValue(null),
                                                                param.getValueType());
                    } catch(Exception ex) {
                        LOGGER.error("Failed to extract parameter", ex);
                    }
                    return null;
                })
                .filter(Objects::isNull)
                .collect(Collectors.toList());

        // Ensure that all the parameters are extracted
        if (pentahoParams.size() != params.size()) {
            throw new Exception("Parameters in report definition and extracted parameters do not match");
        }

        String reportName = pentahoReport.getName();
        String version = null;
        String description = null;
        
        return new ReportDefinition(reportName, reportFilePath.toAbsolutePath().toString(), params, version, description);
    }


    /**
     * Obtains pentaho metadata
     */
    private final class PentahoMetadataScanner {

        public org.pentaho.reporting.engine.classic.core.ReportDefinition reportDefinition(Path filePath) throws Exception {
            // TODO: extract pentaho report definition from the given path
            return null;
        }

    }
}
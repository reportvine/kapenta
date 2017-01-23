package com.creditdatamw.labs.sparkpentaho.autogen;

import com.creditdatamw.labs.sparkpentaho.SparkPentahoAPI;
import com.creditdatamw.labs.sparkpentaho.config.ApiConfiguration;
import com.creditdatamw.labs.sparkpentaho.config.Method;
import com.creditdatamw.labs.sparkpentaho.config.ReportConfiguration;
import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.reports.ParameterDefinition;
import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.parameters.DefaultParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final YAMLMapper yamlMapper = new YAMLMapper();
    private static final ObjectMapper objectMapper = SparkPentahoAPI.OBJECT_MAPPER;
    private final ResourceManager resourceManager;

    private final Path sourceDir;
    private final Path outputFile;

    /**
     * Creates an ApiConfiguration generator
     *
     * @param sourceDir The directory to search for reports from
     * @param outputFile The file to write the YAML configuration to
     */
    public PentahoAPIGenerator(Path sourceDir, Path outputFile) {
        this.sourceDir = sourceDir;
        this.outputFile = outputFile;
        ClassicEngineBoot.getInstance().start();
        this.resourceManager = new ResourceManager();
        resourceManager.registerDefaults();
    }

    public void generate() throws IOException {
        List<ReportDefinition> defs = searchForPrpts(sourceDir);
        ApiConfiguration apiConfiguration = buildConfiguration(defs);

        Objects.requireNonNull(apiConfiguration);

        try (OutputStream fos = Files.newOutputStream(outputFile)) {
            yamlMapper.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
            yamlMapper.writeValue(fos, apiConfiguration);
            LOGGER.debug("Wrote output YAML to: {}", outputFile);
        } catch(Exception e) {
            LOGGER.error("Failed to write output YAML", e);
            throw new RuntimeException("Failed to write YAML to: " + outputFile);
        }
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
                    
                    reportConfiguration.setPath(reportPath.startsWith("/") ? reportPath : "/".concat(reportPath));

                    reportConfiguration.setReportName(reportDefinition.getReportName());
                    reportConfiguration.setDescription(reportDefinition.getDescription());
                    reportConfiguration.setVersion(reportDefinition.getVersion());
                    reportConfiguration.setParameters(reportDefinition.getParameters());
                    reportConfiguration.setReportFilePath(reportDefinition.getReportFilePath());
                    
                    // Support both GET and POST by default
                    reportConfiguration.setMethods(Method.ALL);

                    // Add all the output types for this report
                    reportConfiguration.setExtensions(Arrays.stream(OutputType.values())
                        .filter(o -> !o.equals(OutputType.NONE))
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

    private ReportDefinition parseReportDefinition(Path reportFilePath) throws Exception {

        final Resource resource = resourceManager.createDirectly(reportFilePath.toFile(), MasterReport.class);

        final MasterReport pentahoReport = (MasterReport) resource.getResource();

        List<ParameterDefinitionEntry> pentahoParams = Arrays.asList(pentahoReport.getParameterDefinition()
                                                                                  .getParameterDefinitions());
        List<ParameterDefinition> params = pentahoParams.stream()
                .map(param -> mapPentahoParameterToParameterDefinition(param, pentahoReport))
                .filter(p -> !Objects.isNull(p))
                .collect(Collectors.toList());

        // Ensure that all the parameters are extracted
        if (pentahoParams.size() != params.size()) {
            String realParams = objectMapper.writeValueAsString(pentahoParams);
            String extractedParams = objectMapper.writeValueAsString(params);
            String comparison = String.format("Expected: %s, Actual: %s", realParams, extractedParams);
            throw new Exception("Parameters in report definition and extracted parameters do not match. " + comparison);
        }

        String reportName = reportFilePath.getFileName().toString();
        // Remove the extension
        reportName = reportName.replace(".prpt", "");
        // TODO: Get version and description from report metadata
        String version = pentahoReport.getId();
        String description = pentahoReport.getTitle();
        
        return new ReportDefinition(reportName, reportFilePath.toAbsolutePath().toString(), params, version, description);
    }

    /**
     * Map a pentaho report parameter to the ParameterDefinition class
     *
     * @param param
     * @param pentahoReport
     * @return
     */
    private ParameterDefinition mapPentahoParameterToParameterDefinition(ParameterDefinitionEntry param, MasterReport pentahoReport) {
        Object defaultValue = null;
        try {
            /**
             * Pentaho requires us to provide a {@link org.pentaho.reporting.engine.classic.core.parameters.ParameterContext}
             * which may fail since it needs to connect to the DataFactory (database)
             * for some other kinds of parameters so we allow the call to fail in which case
             * the default will remain a <code>null</code>
             */
            DefaultParameterContext context = new DefaultParameterContext(pentahoReport);
            defaultValue = param.getDefaultValue(context);
        } catch(Exception ex) {
            LOGGER.error("Failed to get default value for parameter", ex);
        }

        if (param.isMandatory()) {
            return ReportDefinition.requiredParameter(param.getName(),
                    defaultValue,
                    param.getValueType());
        }
        return ReportDefinition.optionalParameter(param.getName(),
                defaultValue,
                param.getValueType());
    }
}
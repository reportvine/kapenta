package com.creditdatamw.labs.kapenta.reportdefinition;

import com.creditdatamw.labs.kapenta.Server;
import com.creditdatamw.labs.kapenta.parameters.PentahoToParameterDefinitionMapper;
import com.creditdatamw.labs.kapenta.parameters.ParameterDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportDefinitionFileReader {
    private static final ObjectMapper objectMapper = Server.OBJECT_MAPPER;
    private final ResourceManager resourceManager;
    private final PentahoToParameterDefinitionMapper parameterMapper =
        new PentahoToParameterDefinitionMapper();

    ReportDefinitionFileReader(ResourceManager resourceManager) {
        this.resourceManager = Objects.requireNonNull(resourceManager);
    }

    public ReportDefinition createFromFile(final Path parentDir,
                                           final Path reportFilePath,
                                           boolean relativizeApiPath) throws Exception {

        Objects.requireNonNull(reportFilePath, "reportFilePath cannot be null");

        final Resource resource = resourceManager.createDirectly(reportFilePath.toFile(), MasterReport.class);

        final MasterReport pentahoReport = (MasterReport) resource.getResource();

        List<ParameterDefinitionEntry> pentahoParams = Arrays.asList(pentahoReport.getParameterDefinition()
                .getParameterDefinitions());
        List<ParameterDefinition> params = pentahoParams.stream()
                .map(param -> parameterMapper.map(param, pentahoReport))
                .filter(p -> !Objects.isNull(p))
                .collect(Collectors.toList());

        // Ensure that all the parameters are extracted
        if (pentahoParams.size() != params.size()) {
            String realParams = objectMapper.writeValueAsString(pentahoParams);
            String extractedParams = objectMapper.writeValueAsString(params);
            String comparison = String.format("Expected: %s, Actual: %s", realParams, extractedParams);
            throw new Exception("Parameters in report definition and extracted parameters do not match. " + comparison);
        }

        Path fileName = reportFilePath.getFileName();
        if (Objects.isNull(fileName)) {
            throw new Exception("Failed to read fileName from reportFilePath");
        }

        String reportName = fileName.toString();
        // Remove the extension
        reportName = reportName.replace(".prpt", "");
        // TODO: Get version and description from report metadata
        String version = pentahoReport.getId();
        String description = pentahoReport.getTitle();

        return new ReportDefinition(reportName, reportFilePath.toAbsolutePath().toString(), params, version, description);
    }
}

package com.creditdatamw.labs.sparkpentaho.reports;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Report definition
 */
public class ReportDefinition {

    @JsonProperty("name")
    private String reportName;

    @JsonProperty("version")
    private String version = "1.0.0";

    @JsonProperty("description")
    private String description;

    @JsonProperty("path")
    private String reportFilePath;

    @JsonProperty("parameters")
    private final List<ParameterDefinition> parameters;

    public ReportDefinition() {
        this.parameters = Collections.emptyList();
    }

    public ReportDefinition(String reportName, String reportFilePath, List<ParameterDefinition> parameters) {
        this.reportName = reportName;
        this.reportFilePath = reportFilePath;
        Set<ParameterDefinition> paramSet = new HashSet<>();
        paramSet.addAll(parameters);
        this.parameters = new ArrayList<>();
        paramSet.forEach(this.parameters::add);
    }

    public ReportDefinition(String reportName,
                            String reportFilePath,
                            List<ParameterDefinition> parameters,
                            String version,
                            String description) {
        this.reportName = reportName;
        this.reportFilePath = reportFilePath;
        this.version = version;
        this.description = description;
        Set<ParameterDefinition> paramSet = new HashSet<>();
        paramSet.addAll(parameters);
        this.parameters = new ArrayList<>();
        paramSet.forEach(this.parameters::add);
    }


    public String getReportName() {
        return reportName;
    }


    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getReportFilePath() {
        return reportFilePath;
    }

    public List<ParameterDefinition> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public static ParameterDefinition requiredParameter(String name, String type) {
        return new ParameterDefinition(name, true, type);
    }

    public static ParameterDefinition requiredParameter(String name, Class clazz) {
        return new ParameterDefinition(name, true, clazz);
    }

    public static <T> ParameterDefinition requiredParameter(String name, T defaultValue, Class clazz) {
        return new ParameterDefinition(name, true, clazz, defaultValue);
    }

    public static ParameterDefinition optionalParameter(String name, Class clazz) {
        return new ParameterDefinition(name, clazz);
    }

    public static <T> ParameterDefinition optionalParameter(String name, T defaultValue, Class clazz) {
        return new ParameterDefinition(name, false, clazz, defaultValue);
    }

    /**
     * Validate parameters to ensure they are valid
     *
     */
    public void validate() {

        if (reportName.isEmpty()) {
            throw new IllegalArgumentException("ReportDefinion must have a valid name");
        }

        if (! Files.exists(Paths.get(reportFilePath))) {
            throw new RuntimeException("ReportFile not found: " + reportFilePath);
        }
    }

    public Class<?> parameterType(String queryParam) {
        Optional<ParameterDefinition> optional = parameters.stream()
            .filter(p -> p.getName().equalsIgnoreCase(queryParam))
            .findFirst();

        if (! optional.isPresent()) {
            return null;
        }
        return optional.get().getType();
    }

}

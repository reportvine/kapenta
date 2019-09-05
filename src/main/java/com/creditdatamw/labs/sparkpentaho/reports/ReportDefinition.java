package com.creditdatamw.labs.sparkpentaho.reports;

import com.creditdatamw.labs.sparkpentaho.parameters.ParameterDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Report definition for a report without parameters
     *
     * @param reportName
     * @param reportFilePath
     */
    public ReportDefinition(String reportName, String reportFilePath) {
        this.reportName = reportName;
        this.reportFilePath = reportFilePath;
        this.parameters = Collections.emptyList();
    }

    /**
     * Report definition for a report with parameters
     *
     * @param reportName
     * @param reportFilePath
     * @param parameters
     */
    public ReportDefinition(String reportName, String reportFilePath, List<ParameterDefinition> parameters) {
        this.reportName = reportName;
        this.reportFilePath = reportFilePath;
        this.parameters = new ArrayList<>();
        Set<ParameterDefinition> paramSet = new HashSet<>();
        paramSet.addAll(parameters);
        paramSet.forEach(this.parameters::add);
    }

    /**
     * Report definition for a report with parameters and addition information like version and description
     * @param reportName
     * @param reportFilePath
     * @param parameters
     * @param version
     * @param description
     */
    public ReportDefinition(String reportName,
                            String reportFilePath,
                            List<ParameterDefinition> parameters,
                            String version,
                            String description) {
        this(reportName, reportFilePath, parameters);
        this.version = version;
        this.description = description;
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

    public boolean hasParameters() {
        return !this.parameters.isEmpty();
    }
    /**
     * Checks that the set contains all the <em>required</em> parameter names
     * for this report definition. The set may not contain the optional parameters.</br>
     * This is a utility method to validate for required parameters before attempting to
     * generate a report.
     *
     * @param params
     * @return
     */
    public boolean hasRequiredParameterNamesIn(Set<String> params) {
        Optional<Boolean> result = parameters
                .stream()
                .filter(ParameterDefinition::isMandatory)
                .map(param -> params.contains(param.getName()))
                .reduce(Boolean::logicalOr);

        return result.isPresent() && result.get();
    }
    public boolean hasRequiredParameters() {
        boolean hasRequired = false;
        for (ParameterDefinition p: parameters) {
            hasRequired |= p.isMandatory();
            if (hasRequired) break;
        }
        return hasRequired;
    }

    public String extractMissingRequiredParams(Set<String> params) {
        return parameters
            .stream()
            .filter(ParameterDefinition::isMandatory)
            .filter(param -> !params.contains(param.getName()))
            .map(ParameterDefinition::getName)
            .sorted()
            .collect(Collectors.joining(","));
    }
}

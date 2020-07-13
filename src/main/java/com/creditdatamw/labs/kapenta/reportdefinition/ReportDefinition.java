package com.creditdatamw.labs.kapenta.reportdefinition;

import com.creditdatamw.labs.kapenta.config.ReportConfiguration;
import com.creditdatamw.labs.kapenta.parameters.ParameterDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    /**
     * Two Report Definitions are equal if their names and paths are the same
     * 
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportDefinition that = (ReportDefinition) o;
        return Objects.equals(reportName, that.reportName) &&
                Objects.equals(reportFilePath, that.reportFilePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportName, version, description, reportFilePath, parameters);
    }

    public static ReportDefinition readFromConfiguration(Optional<Path> baseDir, ReportConfiguration rptCfg) {
        // if the report configuration uses a relative path to the report
        // resolve the report to the base directory
        String reportFullPath = rptCfg.getReportFilePath();

        if (! Files.exists(Paths.get(rptCfg.getReportFilePath()))) {
            if (reportFullPath.startsWith("./") || reportFullPath.startsWith(".\\")) {
                String cleaned = reportFullPath.replace("./", "")
                    .replace(".\\","");

                if (baseDir.isPresent()) {
                    reportFullPath = baseDir.get()
                        .resolve(cleaned)
                        .toAbsolutePath()
                        .toString();
                }
            }
        }
        if (Objects.isNull(rptCfg.getParameters())) {
            return new ReportDefinition(rptCfg.getReportName(), reportFullPath);
        }
        return new ReportDefinition(rptCfg.getReportName(), reportFullPath, rptCfg.getParameters());
    }
}

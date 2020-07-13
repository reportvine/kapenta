package com.creditdatamw.labs.kapenta.config;

import com.creditdatamw.labs.kapenta.OutputType;
import com.creditdatamw.labs.kapenta.parameters.ParameterDefinition;
import com.creditdatamw.labs.kapenta.reportdefinition.ReportDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ReportConfiguration {

    @JsonProperty("path")
    private String path;

    @JsonProperty("methods")
    private Method methods;

    @JsonProperty("ext")
    private List<String> extensions;

    @JsonProperty("name")
    private String reportName;

    @JsonProperty("version")
    private String version = "1.0.0";

    @JsonProperty("description")
    private String description;

    @JsonProperty("file")
    private String reportFilePath;

    @JsonProperty("parameters")
    private List<ParameterDefinition> parameters;

    public ReportConfiguration() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Method getMethods() {
        return methods;
    }

    public void setMethods(Method methods) {
        this.methods = methods;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReportFilePath() {
        return reportFilePath;
    }

    public void setReportFilePath(String reportFilePath) {
        this.reportFilePath = reportFilePath;
    }

    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDefinition> parameters) {
        this.parameters = parameters;
    }


    public EnumSet<OutputType> outputTypes() {
        final EnumSet<OutputType> set = EnumSet.noneOf(OutputType.class);

        this.extensions.forEach(ext -> {
            if (ext.toLowerCase().equalsIgnoreCase("pdf")) {
                set.add(OutputType.PDF);
            }
            if (ext.toLowerCase().equalsIgnoreCase("html")) {
                set.add(OutputType.HTML);
            }
            if (ext.toLowerCase().equalsIgnoreCase("txt")) {
                set.add(OutputType.TXT);
            }
        });
        return set;
    }

    public ReportDefinition toReportDefinition(Optional<Path> baseDir) {
       return ReportDefinition.readFromConfiguration(baseDir, this);
    }
}

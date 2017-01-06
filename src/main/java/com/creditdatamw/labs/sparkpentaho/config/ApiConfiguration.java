package com.creditdatamw.labs.sparkpentaho.config;

import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Configuration class for an API generated from a YAML file
 */
public class ApiConfiguration {

    @JsonProperty(required=true)
    private String apiRoot;

    @JsonProperty(required=true, defaultValue = "0.0.0.0")
    private String host;

    @JsonProperty(required=true)
    private int port;

    @JsonProperty
    private List<ReportConfiguration> reports;

    public ApiConfiguration() {
    }

    public String getApiRoot() {
        return apiRoot;
    }

    public void setApiRoot(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<ReportConfiguration> getReports() {
        return reports;
    }

    public void setReports(List<ReportConfiguration> reports) {
        this.reports = reports;
    }
}
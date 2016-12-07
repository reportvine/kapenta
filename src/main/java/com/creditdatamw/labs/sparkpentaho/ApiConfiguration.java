package com.creditdatamw.labs.sparkpentaho;

import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;

import java.util.List;

/**
 * Configuration class for an API generated from a YAML file
 */
public class ApiConfiguration {

    private String apiRoot;

    private String host;

    private int port;

    private List<ReportDefinition> reports;

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

    public List<ReportDefinition> getReports() {
        return reports;
    }

    public void setReports(List<ReportDefinition> reports) {
        this.reports = reports;
    }
}

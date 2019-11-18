package com.creditdatamw.labs.kapenta.config;

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
    BasicAuth basicAuth;

    @JsonProperty
    LoggingConfiguration logging;

    @JsonProperty
    Backup backup;

    @JsonProperty
    Database database;

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

    public BasicAuth getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(BasicAuth basicAuth) {
        this.basicAuth = basicAuth;
    }

    public LoggingConfiguration getLogging() {
        return logging;
    }

    public void setLogging(LoggingConfiguration logging) {
        this.logging = logging;
    }

    public Backup getBackup() {
        return backup;
    }

    public void setBackup(Backup backup) {
        this.backup = backup;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}

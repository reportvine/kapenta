package com.creditdatamw.labs.sparkpentaho.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for Backup of generated reports
 *
 */
public class Backup {

    @JsonProperty
    private String directory;

    @JsonProperty(defaultValue = "false")
    private boolean rollingBackup = false;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public boolean isRollingBackup() {
        return rollingBackup;
    }

    public void setRollingBackup(boolean rollingBackup) {
        this.rollingBackup = rollingBackup;
    }
}

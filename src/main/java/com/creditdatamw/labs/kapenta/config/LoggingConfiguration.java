package com.creditdatamw.labs.kapenta.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class LoggingConfiguration {
    @JsonProperty("rootLevel")
    private String level;

    @JsonProperty
    private String directory;

    public LoggingConfiguration() {
    }

    public LoggingConfiguration(String directory, String level) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.level = Objects.requireNonNull(level, "level");
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}

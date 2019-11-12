package com.creditdatamw.labs.kapenta.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Configuration for Backup of generated generator
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

    public String getFullDirectory() {
        if (isRollingBackup()) {
            LocalDate date = LocalDate.now();
            String currentYmd = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            return Paths.get(directory, currentYmd).toAbsolutePath().toString();
        }

        return directory;
    }
}

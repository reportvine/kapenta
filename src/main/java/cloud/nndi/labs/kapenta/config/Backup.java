package cloud.nndi.labs.kapenta.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
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

    public Path makeBackupPath(String fileName) {
        var directoryName = FilenameUtils.getName(directory);
        if (isRollingBackup()) {
            LocalDate date = LocalDate.now();
            String currentYmd = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            // findbugs sec-bugs filter makes us do things...
            return Paths.get(directoryName, FilenameUtils.getName(currentYmd), FilenameUtils.getName(fileName));
        }

        return Paths.get(directoryName, FilenameUtils.getName(fileName));
    }
}

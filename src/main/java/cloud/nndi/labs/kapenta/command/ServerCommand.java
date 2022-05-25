package cloud.nndi.labs.kapenta.command;

import cloud.nndi.labs.kapenta.Server;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static cloud.nndi.labs.kapenta.Server.kapenta;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * Runs the server given a path to a YAML configuration file
 *
 */
@Command(name="server", version = "0.3.1-SNAPSHOT")
public class ServerCommand implements Runnable{

    @Option(names = { "-c", "--config" }, required = true, description = "Configuration file")
    private String configurationFile;

    @Option(names = { "--host" }, description = "Address to bind on")
    private String ipAddress;

    @Option(names = { "--port" }, description = "Port to bind on")
    private int port;

    @Override
    public void run() {
        if (Objects.isNull(configurationFile)) {
            throw new RuntimeException("Kapenta server requires path to yaml configuration file to run!");
        }

        final String fileName = FilenameUtils.getName(configurationFile);
        if (!Files.exists(Paths.get(fileName))) {
            throw new RuntimeException("Kapenta server requires path to yaml configuration file to run!");
        }

        Server.kapenta(ipAddress, port, fileName);
    }
}

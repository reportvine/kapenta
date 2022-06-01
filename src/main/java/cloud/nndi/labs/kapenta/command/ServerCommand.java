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
    private String ipAddress = "0.0.0.0";

    @Option(names = { "--port" }, description = "Port to bind on")
    private int port = 4567;

    @Override
    public void run() {
        final String fileName = Paths.get(configurationFile).toAbsolutePath().toString();
        //FilenameUtils.getFullPath(configurationFile);
        if (!Files.exists(Paths.get(configurationFile))) {
            throw new RuntimeException(String.format("File %s does not exist. Kapenta server requires path to yaml configuration file to run!", fileName));
        }

        Server.kapenta(ipAddress, port, fileName);
    }
}

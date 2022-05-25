package cloud.nndi.labs.kapenta;

import cloud.nndi.labs.kapenta.command.GenerateCommand;
import cloud.nndi.labs.kapenta.command.KapentaCommand;
import cloud.nndi.labs.kapenta.command.ServerCommand;
import picocli.CommandLine;

/**
 * Run the application to get an auto-generated Server API for Pentaho Reports
 *
 * @author Zikani
 */
public class Main {

    /**
     * Main method - runs the server
     *
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        int exitCode = new CommandLine(new KapentaCommand())
                .addSubcommand("server", new ServerCommand())
                .addSubcommand("generate", new GenerateCommand())
                .execute(args);
        System.exit(exitCode);
    }
}

package com.creditdatamw.labs.sparkpentaho;

import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.creditdatamw.labs.sparkpentaho.resources.ReportResourceImpl;
import com.google.common.collect.ImmutableList;
import spark.Spark;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Objects;

import static com.creditdatamw.labs.sparkpentaho.SparkPentahoAPI.sparkPentaho;
import static spark.Spark.halt;

/**
 * Run the application to get an auto-generated Server API for Pentaho Reports
 *
 * @author Zikani
 */
public class Application {

    /**
     * Main method - runs the server
     *
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        if (args.length < 1) {
            showHelpAndExit();
        }

        String yamlFile = args[0];

        if (Objects.isNull(yamlFile)) {
            showHelpAndExit();
        }

        if (!Files.exists(Paths.get(yamlFile))) {
            showHelpAndExit();
        }

        // Run the spark pentaho report server
        sparkPentaho(yamlFile);
    }

    /**
     * Show help and exit the application
     */
    public static void showHelpAndExit() {
        throw new RuntimeException("Spark Pentaho Reports Server requires path to yaml configuration file to run!");
        // System.exit(-1);
    }
}

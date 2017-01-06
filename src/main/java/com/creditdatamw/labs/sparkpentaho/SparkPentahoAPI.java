package com.creditdatamw.labs.sparkpentaho;

import com.creditdatamw.labs.sparkpentaho.config.ApiConfiguration;
import com.creditdatamw.labs.sparkpentaho.config.Method;
import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.resources.ReportResource;
import com.creditdatamw.labs.sparkpentaho.resources.ReportResourceImpl;
import com.creditdatamw.labs.sparkpentaho.resources.Reports;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.constructor.Constructor;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main API for creating APIs out of Pentaho .prpt reports via SparkJava
 *
 */
public class SparkPentahoAPI {

    private final Reports reports;

    SparkPentahoAPI(String apiRoot, List<ReportResource> availableReports) {
        Objects.requireNonNull(apiRoot);
        Objects.requireNonNull(availableReports);
        reports = new Reports(apiRoot, availableReports);
    }

    SparkPentahoAPI(String resourceDefinitionYaml) {
        Objects.requireNonNull(resourceDefinitionYaml);
        reports = createFromYaml(resourceDefinitionYaml);
    }

    public Reports getReports() {
        return reports;
    }

    public void start() {
        ClassicEngineBoot.getInstance().start();
        reports.registerResources();
    }

    /**
     * Create a new Spark Pentaho API
     * @param apiRoot
     * @param reportResources
     */
    public static final void sparkPentaho(String apiRoot, List<ReportResource> reportResources) {
        new SparkPentahoAPI(apiRoot, reportResources).start();
    }

    /**
     * Create a new Spark Pentaho API
     * @param yamlFile
     */
    public static final void sparkPentaho(String yamlFile) {
        new SparkPentahoAPI(yamlFile).start();
    }

    private static Reports createFromYaml(String yamlFile) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ApiConfiguration configuration = null;

        Path yamlFileDir = Paths.get(yamlFile).getParent();

        try {
            configuration = mapper.readValue(new File(yamlFile), ApiConfiguration.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(SparkPentahoAPI.class).error("Failed to read yaml file", e);
            throw new RuntimeException("Failed to parse configuration from Yaml file", e);
        }

        List<ReportResource> reportResources = new ArrayList<>();

        final String[] defaultMethods = new String[] { "GET", "POST" };

        configuration.getReports().forEach(reportConfiguration -> {
            String reportName = reportConfiguration.getReportName();
            String reportRoute = reportName.toLowerCase().replace(" ", "_");
            String path = Optional.ofNullable(reportConfiguration.getPath()).orElse(reportRoute);

            Method methods = reportConfiguration.getMethods();

            if (! methods.isGet() && ! methods.isPost()) {
                throw new RuntimeException("Specify at least one HTTP method between GET or POST");
            }

            reportResources.add(
                new ReportResourceImpl(
                    path.startsWith("/") ? path : "/".concat(path),
                    methods.toArray().length < 1 ? defaultMethods : methods.toArray(),
                    reportConfiguration.extensions(),
                    reportConfiguration.toReportDefinition(Optional.of(yamlFileDir))));
        });

        String host = Optional.ofNullable(configuration.getHost()).orElse("0.0.0.0");
        Spark.ipAddress(host);

        Spark.port(configuration.getPort());

        return new Reports(configuration.getApiRoot(), Collections.unmodifiableList(reportResources));
    }
}

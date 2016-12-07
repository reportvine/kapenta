package com.creditdatamw.labs.sparkpentaho;

import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.creditdatamw.labs.sparkpentaho.resources.ReportResource;
import com.creditdatamw.labs.sparkpentaho.resources.ReportResourceImpl;
import com.creditdatamw.labs.sparkpentaho.resources.Reports;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
        try {
            configuration = mapper.readValue(new File(yamlFile), ApiConfiguration.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(SparkPentahoAPI.class).error("Failed to read yaml file", e);
            throw new RuntimeException("Failed to parse configuration from Yaml file", e);
        }

        List<ReportResource> reportResources = new ArrayList<>();

        configuration.getReports().forEach(reportDefinition -> {
            String reportName = reportDefinition.getReportName();
            String reportRoute = reportName.toLowerCase().replace(" ", "_");
            reportResources.add(
                new ReportResourceImpl(
                    reportRoute.startsWith("/") ? reportRoute : "/".concat(reportRoute),
                    new String[] { "GET", "POST"},
                    EnumSet.of(OutputType.PDF, OutputType.HTML),
                    reportDefinition));
        });

        return new Reports(configuration.getApiRoot(), Collections.unmodifiableList(reportResources));
    }

    private static class MapConstructor extends Constructor {
        @Override
        protected Map<Object, Object> createDefaultMap() {
            return new HashMap<>();
        }
    }
}

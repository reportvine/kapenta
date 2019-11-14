package com.creditdatamw.labs.kapenta;

import com.creditdatamw.labs.kapenta.config.ApiConfiguration;
import com.creditdatamw.labs.kapenta.config.BasicAuth;
import com.creditdatamw.labs.kapenta.config.Method;
import com.creditdatamw.labs.kapenta.filter.BasicAuthenticationFilter;
import com.creditdatamw.labs.kapenta.http.ReportResource;
import com.creditdatamw.labs.kapenta.http.ReportResourceImpl;
import com.creditdatamw.labs.kapenta.http.Reports;
import com.creditdatamw.labs.kapenta.http.ReportsRoute;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.slf4j.LoggerFactory;
import spark.Service;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main API for creating APIs out of Pentaho .prpt generator via SparkJava
 *
 */
public class Server {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ApiConfiguration configuration;
    private final Reports reports;

    Server(String apiRoot, List<ReportResource> availableReports) {
        Objects.requireNonNull(apiRoot);
        Objects.requireNonNull(availableReports);
        reports = new Reports(apiRoot, availableReports);
    }

    Server(String resourceDefinitionYaml) {
        Objects.requireNonNull(resourceDefinitionYaml);
        configuration = createFromYaml(resourceDefinitionYaml);
        Path yamlFileDir = Paths.get(resourceDefinitionYaml).getParent();
        reports = createReportsFromConfiguration(yamlFileDir, configuration);
    }

    public Reports getReports() {
        return reports;
    }

    public void start() {
        ClassicEngineBoot.getInstance().start();
        createHttpServer();
    }

    public void stop() {
        Spark.stop();
    }

    /**
     * Create a new Spark Pentaho API
     * @param apiRoot
     * @param reportResources
     */
    public static final void kapenta(String apiRoot, List<ReportResource> reportResources) {
        new Server(apiRoot, reportResources).start();
    }

    /**
     * Create a new Spark Pentaho API
     * @param yamlFile
     */
    public static final void kapenta(String yamlFile) {
        new Server(yamlFile).start();
    }

    private static ApiConfiguration createFromYaml(String yamlFile) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ApiConfiguration configuration = null;

        try {
            configuration = mapper.readValue(new File(yamlFile), ApiConfiguration.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(Server.class).error("Failed to read yaml file", e);
            throw new RuntimeException("Failed to parse configuration from Yaml file", e);
        }
        return configuration;
    }

    private Reports createReportsFromConfiguration(Path yamlFileDir, ApiConfiguration configuraiton) {
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

        if (Optional.ofNullable(configuration.getBasicAuth()).isPresent()) {
            configureBasicAuth(configuration);
        }
        final Reports reports = new Reports(configuration.getApiRoot(),
                           Collections.unmodifiableList(reportResources),
                           configuration.getBackup(),
                           configuration.getDatabase());


        return reports;
    }

    private void createHttpServer() {
        final String rootPath = configuration.getApiRoot();

        String host = Optional.ofNullable(configuration.getHost()).orElse("0.0.0.0");

        spark.Service server = Service.ignite();

        server.ipAddress(host);
        server.port(configuration.getPort());
        server.get(rootPath.concat("/").concat("reports.json"),new ReportsRoute(reports));

        reports.setHttpServer(server);
        // Registers Spark Routes for the Reports
        reports.registerResources();
    }

    private static void configureBasicAuth(ApiConfiguration configuration) {
        BasicAuth basicAuth = configuration.getBasicAuth();
        boolean multipleUsers = false;

        BasicAuth.User user = basicAuth.getUser();
        List<BasicAuth.User> userList = basicAuth.getUsers();

        if (! Objects.isNull(userList) && !userList.isEmpty()) {
            multipleUsers = true;
        }

        if (multipleUsers) {
            // Configure for multiple users
            Spark.before(new BasicAuthenticationFilter(userList));
        } else {
            // Configure auth for the single user
            Spark.before(new BasicAuthenticationFilter(user));
        }
    }
}

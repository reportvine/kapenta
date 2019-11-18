package com.creditdatamw.labs.kapenta;

import com.creditdatamw.labs.kapenta.config.ApiConfiguration;
import com.creditdatamw.labs.kapenta.config.BasicAuth;
import com.creditdatamw.labs.kapenta.config.LoggingConfiguration;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Main API for creating APIs out of Pentaho .prpt generator via SparkJava
 *
 */
public class Server {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ApiConfiguration configuration;
    private final Reports reports;
    private spark.Service httpServer;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    Server(String apiRoot, List<ReportResource> availableReports) {
        Objects.requireNonNull(apiRoot);
        Objects.requireNonNull(availableReports);
        reports = new Reports(apiRoot, availableReports);
    }

    private Server(String resourceDefinitionYaml) {
        Objects.requireNonNull(resourceDefinitionYaml);
        configuration = createFromYaml(resourceDefinitionYaml);
        this.configureLogging(configuration);
        Path yamlFileDir = Paths.get(resourceDefinitionYaml).getParent();
        this.httpServer = createHttpServer();
        reports = createReportsFromConfiguration(yamlFileDir, configuration);
    }

    public Reports getReports() {
        return reports;
    }

    public void start() {
        ClassicEngineBoot.getInstance().start();

        final String rootPath = configuration.getApiRoot();
        httpServer.get(rootPath.concat("/").concat("reports.json"),
                new ReportsRoute(reports));

        reports.setHttpServer(httpServer);

        // Registers Spark Routes for the Reports
        reports.registerResources();

        try {
            httpServer.awaitStop();
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        httpServer.stop();
        countDownLatch.countDown();
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
        Objects.requireNonNull(configuraiton, "configuration");
        Objects.requireNonNull(httpServer, "Server.httpServer");

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

    private spark.Service createHttpServer() {
        String host = Optional.ofNullable(configuration.getHost()).orElse("0.0.0.0");
        httpServer = Service.ignite();
        httpServer.ipAddress(host);
        httpServer.port(configuration.getPort());
        return httpServer;
    }

    private void configureBasicAuth(ApiConfiguration configuration) {
        final List<BasicAuth.User> userList = new ArrayList<>();

        BasicAuth basicAuth = configuration.getBasicAuth();

        Optional.ofNullable(basicAuth.getUser())
                .ifPresent(userList::add);

        Optional.ofNullable(basicAuth.getUsers())
                .ifPresent(userList::addAll);

        if (!userList.isEmpty()) {
            httpServer.before(new BasicAuthenticationFilter(userList));
        }
    }

    private void configureLogging(ApiConfiguration apiConfiguration) {

        LoggingConfiguration log = Optional.ofNullable(apiConfiguration.getLogging())
                .orElse(new LoggingConfiguration());

        System.setProperty("logging.directory",
                Optional.ofNullable(log.getDirectory()).orElse("./logs"));

        System.setProperty("logging.rootLevel",
                Optional.ofNullable(log.getLevel()).orElse("INFO"));
    }
}

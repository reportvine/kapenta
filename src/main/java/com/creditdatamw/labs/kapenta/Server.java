package com.creditdatamw.labs.kapenta;

import com.creditdatamw.labs.kapenta.config.*;
import com.creditdatamw.labs.kapenta.http.*;
import com.creditdatamw.labs.kapenta.http.filter.BasicAuthenticationFilter;
import com.creditdatamw.labs.kapenta.http.filter.CorsFilter;
import com.creditdatamw.labs.kapenta.openapi.OpenAPISchemaGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.creditdatamw.labs.kapenta.http.Utils.isValidResourcePath;

/**
 * Main API for creating APIs out of Pentaho .prpt generator via SparkJava
 *
 */
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ApiConfiguration configuration;
    private final Path yamlFileDir;
    private final Reports reports;
    private spark.Service httpServer;
    private PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String[] DEFAULT_METHODS = new String[] { "GET", "POST" };
    private static final String REPORTS_JSON_ENDPOINT = "/reports.json";

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
    public static final void kapenta(String ipAddress, int port, String yamlFile) {
        new Server(ipAddress, port, yamlFile).start();
    }

    /**
     * Create Server instance with an apiRoot and availableReports
     *
     * @param apiRoot Root of the api
     * @param availableReports reports available on the API
     */
    Server(String apiRoot, List<ReportResource> availableReports) {
        Objects.requireNonNull(apiRoot);
        Objects.requireNonNull(availableReports);
        this.yamlFileDir = Paths.get("."); // For unspecified path, default to cwd
        this.reports = new Reports(apiRoot, availableReports);
    }

    private Server(String ipAddress, int port, String resourceDefinitionYaml) {
        this.yamlFileDir = Paths.get(Objects.requireNonNull(resourceDefinitionYaml)).getParent();
        this.configuration = createFromYaml(resourceDefinitionYaml);
        this.configureLogging(configuration);
        this.httpServer = createHttpServer(ipAddress, port);
        this.configureOpenAPIEndpoint();
        this.configurePrometheusMetricsEndpoint();
        this.reports = createReportsFromConfiguration(configuration);
    }


    /**
     * Gets the configured Reports
     *
     * @return get configured reports
     */
    public Reports getReports() {
        return reports;
    }

    /**
     * Start the server
     */
    public void start() {
        ClassicEngineBoot.getInstance().start();

        final String rootPath = configuration.getApiRoot();
        // Registers the `/reports.json` endpoint
        httpServer.get(rootPath.concat(REPORTS_JSON_ENDPOINT),
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

    /**
     * Stop the server
     */
    public void stop() {
        httpServer.stop();
        countDownLatch.countDown();
    }

    /**
     * Parses and reads the yamlFile to an ApiConfiguration instance
     * @param yamlFile path to the yaml configuration file
     * @return api configuration instance
     */
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

    private boolean isValidReportPath(String path) {
        if (! isValidResourcePath(path)) return false;

        String apiRoot = this.configuration.getApiRoot();
        if (apiRoot.equalsIgnoreCase(path)) {
            return false;
        }

        if (apiRoot.concat(REPORTS_JSON_ENDPOINT).equalsIgnoreCase(path)) {
            return false;
        }

        return true;
    }

    private Reports createReportsFromConfiguration(ApiConfiguration configuraiton) {
        Objects.requireNonNull(configuraiton, "configuration");
        Objects.requireNonNull(httpServer, "Server.httpServer");

        List<ReportResource> reportResources = configuration.getReports()
            .stream()
            .map(this::mapReportResourceFromConfiguration)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        if (Optional.ofNullable(configuration.getBasicAuth()).isPresent()) {
            LOGGER.info("Configuring HTTP Basic Auth from configuration file");
            configureBasicAuth(configuration);
        }
        if (reportResources.isEmpty()) {
            throw new RuntimeException("Server cannot boot without Reports correctly configured. Please review the configuration");
        }
        LOGGER.info("Registered {} Report Resource endpoints", reportResources.size());
        return new Reports(configuration.getApiRoot(),
            reportResources,
            configuration.getBackup(),
            configuration.getDatabase());
    }

    private Optional<ReportResource> mapReportResourceFromConfiguration(ReportConfiguration reportConfiguration) {
        String reportName = reportConfiguration.getReportName();
        String reportRoute = reportName.toLowerCase().replace(" ", "_");
        String path = Optional.ofNullable(reportConfiguration.getPath()).orElse(reportRoute);

        Method methods = reportConfiguration.getMethods();

        if (! methods.isGet() && ! methods.isPost()) {
            throw new RuntimeException("Specify at least one HTTP method between GET or POST");
        }

        String reportResourcePath = path.startsWith("/") ? path : "/".concat(path);

        if (! isValidReportPath(reportResourcePath)) {
            return Optional.empty();
        }

        return Optional.of(new ReportResourceImpl(
            reportResourcePath,
            methods.toArray().length < 1 ? DEFAULT_METHODS : methods.toArray(),
            reportConfiguration.outputTypes(),
            reportConfiguration.toReportDefinition(Optional.of(yamlFileDir))));
    }

    /**
     * Creates a Spark HttpService
     * @return
     */
    private spark.Service createHttpServer(String ipAddress, int port) {
        String host = Optional.ofNullable(ipAddress).orElse(configuration.getHost());
        httpServer = Service.ignite();
        httpServer.ipAddress(host);
        httpServer.port(Optional.ofNullable(port).orElse(configuration.getPort()));

        httpServer.staticFileLocation("public");

        httpServer.after(new CorsFilter());

        return httpServer;
    }

    private void configurePrometheusMetricsEndpoint() {
        httpServer.get("/metrics", (req, res) -> prometheusRegistry.scrape());
    }

    private void configureOpenAPIEndpoint() {
        OpenApi3 openApi3 = new OpenAPISchemaGenerator(configuration).getGeneratedSchema();
        httpServer.get("/openapi",  new OpenAPIRoute(objectMapper, openApi3));
    }

    /**
     * Configures basic authentication if provided in the configuration
     * @param configuration the configuration file
     */
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

    /**
     * Configure logging
     * @param apiConfiguration the API configuration object
     */
    private void configureLogging(ApiConfiguration apiConfiguration) {

        LoggingConfiguration log = Optional.ofNullable(apiConfiguration.getLogging())
                .orElse(new LoggingConfiguration());
        try {
            System.setProperty("logging.directory", Optional.ofNullable(log.getDirectory())
                .orElse(Files.createTempDirectory("kapenta").toAbsolutePath().toString()));
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }

        System.setProperty("logging.rootLevel",
                Optional.ofNullable(log.getLevel()).orElse("INFO"));
    }
}

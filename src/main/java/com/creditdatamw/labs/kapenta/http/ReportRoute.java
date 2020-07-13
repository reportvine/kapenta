package com.creditdatamw.labs.kapenta.http;

import com.creditdatamw.labs.kapenta.config.Backup;
import com.creditdatamw.labs.kapenta.config.Database;
import com.creditdatamw.labs.kapenta.io.MultiplexOutputStream;
import com.creditdatamw.labs.kapenta.pentaho.Generator;
import com.creditdatamw.labs.kapenta.pentaho.GeneratorException;
import com.creditdatamw.labs.kapenta.OutputType;
import com.creditdatamw.labs.kapenta.reportdefinition.ReportDefinition;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.creditdatamw.labs.kapenta.Server.OBJECT_MAPPER;

/**
 * Route Handler for a single report resource
 *
 */
final class ReportRoute implements Route {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportRoute.class);

    public static final String APPLICATION_JSON = "application/json;charset=utf-8";

    private final ReportResource reportResource;

    private final Optional<Backup> backup;

    private final Optional<Database> database;

    public ReportRoute(ReportResource reportResource) {
        Objects.requireNonNull(reportResource);
        this.reportResource = reportResource;
        this.backup = Optional.empty();
        this.database = Optional.empty();
    }

    public ReportRoute(ReportResource reportResource, Backup backup) {
        Objects.requireNonNull(reportResource);
        Objects.requireNonNull(backup);
        this.reportResource = reportResource;
        this.backup = Optional.of(backup);
        this.database = Optional.empty();
    }

    public ReportRoute(ReportResource reportResource, Backup backup, Optional<Database> database) {
        Objects.requireNonNull(reportResource);
        Objects.requireNonNull(backup);
        Objects.requireNonNull(database);
        this.reportResource = reportResource;
        this.backup = Optional.of(backup);
        this.database = database;
    }

    @Override
    public Object handle(Request request, Response response) {
        final ReportDefinition reportDefinition = reportResource.reportDefinition();
        final OutputType outputType = determineOutputTypeAndContentType(request, response);
        final StringJoiner sj = new StringJoiner(",", "[", "]");

        reportResource.outputTypes().forEach(val -> sj.add(val.name()));

        if (!reportResource.outputTypes().contains(outputType)) {
            LOGGER.error("Failed to generate report. Unsupported output type: " + request.headers("Accept"));
            response.type(APPLICATION_JSON);
            response.status(HttpStatus.BAD_REQUEST_400);
            return errorJson("Unsupported output type. This report only supports: " + sj.toString());
        }

        final Map<String, Object> reportParameters = new HashMap<>();;
        if (reportDefinition.hasParameters()) {

            if (reportDefinition.hasRequiredParameters() &&
                !reportDefinition.hasRequiredParameterNamesIn(request.queryParams())) {

                String missingRequireds = String.format(
                    "The following required parameters not provided: %s",
                    reportDefinition.extractMissingRequiredParams(request.queryParams()));

                LOGGER.error("Failed to generate report. {}", missingRequireds);
                response.type(APPLICATION_JSON);
                response.status(HttpStatus.BAD_REQUEST_400);
                return errorJson("Please provide all required parameters. " + missingRequireds);
            }
            // Map query string key=values to report parameters
            request.queryParams()
                .forEach(queryParam ->
                    reportParameters.put(
                        queryParam,
                        parseQueryParamToReportParamValue(request, reportDefinition, queryParam))
                );
        }

        try {
            // A MultiplexOutputStream allows us to write to more than one output stream at a time
            MultiplexOutputStream outputStream = new MultiplexOutputStream(response.raw().getOutputStream());

            // We will write to the backup output stream if the backup configuration is present
            if (backup.isPresent()) {
                outputStream = new MultiplexOutputStream(
                        response.raw().getOutputStream(),
                        getBackupOutput(reportDefinition.getReportName(), outputType)
                );
            }

            // Some generator may require that a database configuration is present
            // To enable them to point to another database configuration (e.g. DEV, QA, PROD)
            if (database.isPresent()) {
                Generator.generateReport(
                        reportDefinition.getReportFilePath(),
                        reportDefinition.hasParameters() ? reportParameters : Collections.emptyMap(),
                        outputType,
                        outputStream,
                        database.get()
                );
            } else {
                Generator.generateReport(
                        reportDefinition.getReportFilePath(),
                        reportDefinition.hasParameters() ? reportParameters : Collections.emptyMap(),
                        outputType,
                        outputStream
                );
            }

            LOGGER.info("Generated report: {} for {}", reportDefinition.getReportName(), reportResource.path());
        } catch (GeneratorException | IOException e) {
            LOGGER.error("Failed to generate report. Error: " + e.getMessage(), e.getCause());
            response.type(APPLICATION_JSON);
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return errorJson("Failed to generate report. Please contact the System Administrator.");
        }

        return response;
    }

    private OutputStream getBackupOutput(String reportName, OutputType outputType) throws IOException {
        String nameSanitized = reportName.replace(" ", "");

        String backupFileName = String.format("%s-%s.%s", System.currentTimeMillis(), nameSanitized, outputType.name());
        Backup backupConfig = backup.get();
        Path outputPath = Paths.get(backupConfig.getFullDirectory(), backupFileName.toLowerCase());

        return Files.newOutputStream(outputPath, StandardOpenOption.CREATE_NEW);
    }

    /**
     * Determine the content type to be returned when generating a response and
     * set the appropriate type on the response object.
     *
     * @param request
     * @param response
     */
    private static OutputType determineOutputTypeAndContentType(Request request, Response response) {
        // Yea, I know a method shouldn't do too much but a foolish consistency ...
        String accept = Objects.isNull(request.headers("Accept")) ? "" : request.headers("Accept");
        String requestUri = Objects.isNull(request.uri()) ?  "" : request.uri().toLowerCase();

        if (accept.toLowerCase().contains("pdf") || requestUri.contains(".pdf")) {
            response.type(OutputType.PDF.getContentTypeUtf8());
            return OutputType.PDF;
        } else if (accept.toLowerCase().contains("text/plain") || requestUri.contains(".txt")) {
            response.type(OutputType.TXT.getContentTypeUtf8());
            return OutputType.TXT;
        } else if (accept.toLowerCase().contains("html") || requestUri.contains(".html")) {
            // Check for html last since most browsers will send accept with html  or */* ...
            response.type(OutputType.HTML.getContentTypeUtf8());
            return OutputType.HTML;
        } else if (accept.toLowerCase().equalsIgnoreCase("*/*")) {
            // If you are cool with anything, you get back html - you're probably a dumb http client
            response.type(OutputType.HTML.getContentTypeUtf8());
            return OutputType.HTML;
        }

        // If clients don't specify a valid type - we assume they don't want anything
        return OutputType.NONE;
    }

    /**
     * Parse a query parameter from the request to a report parameter required in the report
     * @param request
     * @param reportDefinition
     * @param queryParam
     * @return
     */
    public Object parseQueryParamToReportParamValue(final Request request,
                                                     final ReportDefinition reportDefinition,
                                                     String queryParam) {

        Object val = request.queryMap(queryParam).value();
        if (reportDefinition.parameterType(queryParam) == Long.class) {
            val = request.queryMap(queryParam).longValue();
        } else if (reportDefinition.parameterType(queryParam) == Integer.class) {
            val = request.queryMap(queryParam).integerValue();
        } else if (reportDefinition.parameterType(queryParam) == Number.class) {
            val = request.queryMap(queryParam).integerValue();
        } else if (reportDefinition.parameterType(queryParam) == Boolean.class) {
            val = request.queryMap(queryParam).booleanValue();
        } else if (reportDefinition.parameterType(queryParam) == Date.class) {
            LocalDate d = LocalDate.parse(request.queryMap(queryParam).value());
            LocalDateTime dt = d.atStartOfDay();
            // TODO: enabled configuration of zone offset via parameter
            val = Date.from(dt.toInstant(ZoneOffset.UTC));
        } else if (reportDefinition.parameterType(queryParam) == Timestamp.class) {
            LocalDate d = LocalDate.parse(request.queryMap(queryParam).value());
            LocalDateTime dt = d.atStartOfDay();
            // TODO: enabled configuration of zone offset via parameter
            val = Timestamp.from(dt.toInstant(ZoneOffset.UTC));
        }
        return val;
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch(Exception e) {
            LOGGER.error("Failed to serialize object to json", e);
        }
        return null;
    }

    public static String errorJson(String message) {
        return toJson(new MessageResponse(message, true));
    }
}

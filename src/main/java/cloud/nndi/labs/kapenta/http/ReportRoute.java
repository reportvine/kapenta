package cloud.nndi.labs.kapenta.http;

import cloud.nndi.labs.kapenta.OutputType;
import cloud.nndi.labs.kapenta.config.Backup;
import cloud.nndi.labs.kapenta.config.Database;
import cloud.nndi.labs.kapenta.io.MultiplexOutputStream;
import cloud.nndi.labs.kapenta.pentaho.Generator;
import cloud.nndi.labs.kapenta.pentaho.GeneratorException;
import cloud.nndi.labs.kapenta.reportdefinition.ReportDefinition;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static cloud.nndi.labs.kapenta.Server.OBJECT_MAPPER;

/**
 * Route Handler for a single report resource
 *
 */
final class ReportRoute implements Handler {
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
    public void handle(@NotNull Context context) throws Exception {
        final ReportDefinition reportDefinition = reportResource.reportDefinition();
        final OutputType outputType = determineOutputTypeAndContentType(context);
        final StringJoiner sj = new StringJoiner(",", "[", "]");

        reportResource.outputTypes().forEach(val -> sj.add(val.name()));

        if (!reportResource.outputTypes().contains(outputType)) {
            LOGGER.error("Failed to generate report. Unsupported output type: " + context.header("Accept"));
            context.json(errorJson("Unsupported output type. This report only supports: " + sj.toString()));
            return;
        }

        final Map<String, Object> reportParameters = new HashMap<>();;
        if (reportDefinition.hasParameters()) {

            if (reportDefinition.hasRequiredParameters() &&
                !reportDefinition.hasRequiredParameterNamesIn(context.queryParamMap().keySet())) {

                String missingRequireds = String.format(
                    "The following required parameters not provided: %s",
                    reportDefinition.extractMissingRequiredParams(context.queryParamMap().keySet()));

                LOGGER.error("Failed to generate report. {}", missingRequireds);
                context.json(errorJson("Please provide all required parameters. " + missingRequireds));
                return;
            }
            // Map query string key=values to report parameters
            context.queryParamMap().keySet()
                .forEach(queryParam ->
                    reportParameters.put(
                        queryParam,
                        parseQueryParamToReportParamValue(context, reportDefinition, queryParam))
                );
        }

        try {
            // A MultiplexOutputStream allows us to write to more than one output stream at a time
            MultiplexOutputStream outputStream = new MultiplexOutputStream(context.outputStream());

            // We will write to the backup output stream if the backup configuration is present
            if (backup.isPresent()) {
                outputStream = new MultiplexOutputStream(
                        context.outputStream(),
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
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            context.json(errorJson("Failed to generate report. Please contact the System Administrator."));
        }

    }

    private OutputStream getBackupOutput(String reportName, OutputType outputType) throws IOException {
        String nameSanitized = FilenameUtils.getName(reportName.replace(" ", ""));

        String backupFileName = String.format("%s-%s.%s", System.currentTimeMillis(), nameSanitized, outputType.name());
        Backup backupConfig = backup.get();
        Path outputPath = backupConfig.makeBackupPath(backupFileName.toLowerCase());

        return Files.newOutputStream(outputPath, StandardOpenOption.CREATE_NEW);
    }

    /**
     * Determine the content type to be returned when generating a response and
     * set the appropriate type on the response object.
     *
     * @param context
     */
    private static OutputType determineOutputTypeAndContentType(Context context) {
        // Yea, I know a method shouldn't do too much but a foolish consistency ...
        String accept = Objects.isNull(context.header("Accept")) ? "" : context.header("Accept");
        String requestUri = Objects.isNull(context.url()) ?  "" : context.url().toLowerCase();

        if (accept.toLowerCase().contains("pdf") || requestUri.contains(".pdf")) {
            context.contentType(OutputType.PDF.getContentTypeUtf8());
            return OutputType.PDF;
        } else if (accept.toLowerCase().contains("text/plain") || requestUri.contains(".txt")) {
            context.contentType(OutputType.TXT.getContentTypeUtf8());
            return OutputType.TXT;
        } else if (accept.toLowerCase().contains("html") || requestUri.contains(".html")) {
            // Check for html last since most browsers will send accept with html  or */* ...
            context.contentType(OutputType.HTML.getContentTypeUtf8());
            return OutputType.HTML;
        } else if (accept.toLowerCase().equalsIgnoreCase("*/*")) {
            // If you are cool with anything, you get back html - you're probably a dumb http client
            context.contentType(OutputType.HTML.getContentTypeUtf8());
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
    public Object parseQueryParamToReportParamValue(final Context request,
                                                     final ReportDefinition reportDefinition,
                                                     String queryParam) {

        Object val = request.queryParam(queryParam);
        if (reportDefinition.parameterType(queryParam) == Long.class) {
            val = request.queryParamAsClass(queryParam, Long.class);
        } else if (reportDefinition.parameterType(queryParam) == Integer.class) {
            val = request.queryParamAsClass(queryParam, Integer.class);
        } else if (reportDefinition.parameterType(queryParam) == Number.class) {
            val = request.queryParamAsClass(queryParam, Integer.class);
        } else if (reportDefinition.parameterType(queryParam) == Boolean.class) {
            val = request.queryParamAsClass(queryParam, Boolean.class);
        } else if (reportDefinition.parameterType(queryParam) == Date.class) {
            LocalDate d = LocalDate.parse(request.queryParam(queryParam));
            LocalDateTime dt = d.atStartOfDay();
            // TODO: enabled configuration of zone offset via parameter
            val = Date.from(dt.toInstant(ZoneOffset.UTC));
        } else if (reportDefinition.parameterType(queryParam) == Timestamp.class) {
            LocalDate d = LocalDate.parse(request.queryParam(queryParam));
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

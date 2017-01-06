package com.creditdatamw.labs.sparkpentaho.resources;

import com.creditdatamw.labs.sparkpentaho.reports.Generator;
import com.creditdatamw.labs.sparkpentaho.reports.GeneratorException;
import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.reports.ParameterDefinition;
import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.creditdatamw.labs.sparkpentaho.SparkPentahoAPI.OBJECT_MAPPER;

/**
 * Route Handler for a single report resource
 *
 */
final class ReportRoute implements Route {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportRoute.class);

    public static final String APPLICATION_JSON = "application/json;charset=utf-8";

    private final ReportResource reportResource;

    public ReportRoute(ReportResource reportResource) {
        this.reportResource = reportResource;
    }

    @Override
    public Object handle(Request request, Response response) {
        final ReportDefinition reportDefinition = reportResource.reportDefinition();

        List<ParameterDefinition> params = reportDefinition.getParameters();
        List<ParameterDefinition> requiredParams = params
                .stream()
                .filter(param -> !param.isMandatory())
                .collect(Collectors.toList());

        final OutputType outputType = determineOutputTypeAndContentType(request, response);
        final StringJoiner sj = new StringJoiner(",", "[", "]");

        reportResource.outputTypes().forEach(val -> sj.add(val.name()));

        if (!reportResource.outputTypes().contains(outputType)) {
            LOGGER.error("Failed to generate report. Unsupported output type: " + request.headers("Accept"));
            response.type(APPLICATION_JSON);
            response.status(HttpStatus.BAD_REQUEST_400);
            return errorJson("Unsupported output type. This report only supports: " + sj.toString());
        }

        List<ParameterDefinition> requiredButMissing = requiredParams
                .stream()
                .filter(param -> !request.queryMap(param.getName().toLowerCase()).hasValue())
                .collect(Collectors.toList());

        if (! requiredButMissing.isEmpty()) {
            LOGGER.error("Failed to generate report. Unsupported parameters given");
            response.type(APPLICATION_JSON);
            response.status(HttpStatus.BAD_REQUEST_400);
            return errorJson("Please provide all required parameters");
        }

        final Map<String, Object> reportParameters = new HashMap<>();

        // Map query string key=values to report parameters
        request.queryParams().forEach(queryParam -> {
            Object val = request.queryMap(queryParam).value();
            if (reportDefinition.parameterType(queryParam) == Long.class) {
                val = request.queryMap(queryParam).longValue();
            } else if (reportDefinition.parameterType(queryParam) == Integer.class) {
                val = request.queryMap(queryParam).integerValue();
            } else if (reportDefinition.parameterType(queryParam) == Boolean.class) {
                val = request.queryMap(queryParam).booleanValue();
            }
            reportParameters.put(queryParam, val);
        });

        try {
            Generator.generateReport(
                reportDefinition.getReportFilePath(),
                reportParameters,
                outputType,
                response.raw().getOutputStream()
            );
            LOGGER.info("Generated report: {} for {}", reportDefinition.getReportName(), reportResource.path());
        } catch (GeneratorException | IOException e) {
            LOGGER.error("Failed to generate report", e);
            response.type(APPLICATION_JSON);
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return errorJson("Failed to generate report. Please contact the System Administrator.");
        }

        return response;
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
            response.type("application/pdf;charset=utf-8");
            return OutputType.PDF;
        } else if (accept.toLowerCase().contains("text/plain") || requestUri.contains(".txt")) {
            response.type("text/plain;charset=utf-8");
            return OutputType.TXT;
        } else if (accept.toLowerCase().contains("html") || requestUri.contains(".html")) {
            // Check for html last since most browsers will send accept with html  or */* ...
            response.type("text/html;charset=utf-8");
            return OutputType.HTML;
        } else if (accept.toLowerCase().equalsIgnoreCase("*/*")) {
            // If you are cool with anything, you get back html - you're probably a dumb http client
            response.type("text/html;charset=utf-8");
            return OutputType.HTML;
        }

        // If clients don't specify a valid type - we assume they don't want anything
        return OutputType.NONE;
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

package com.creditdatamw.labs.sparkpentaho.resources;

import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.creditdatamw.labs.sparkpentaho.resources.ReportRoute.APPLICATION_JSON;
import static com.creditdatamw.labs.sparkpentaho.resources.ReportRoute.toJson;

/**
 * Report Definition Route
 *
 * Generates a route for presenting the Report Definition
 */
public class ReportDefinitionRoute implements Route {

    public final ReportResource reportResource;

    public ReportDefinitionRoute(ReportResource reportResource) {
        this.reportResource = reportResource;
    }

    @Override
    public String handle(Request request, Response response) throws Exception {
        response.status(HttpStatus.OK_200);
        response.type(APPLICATION_JSON);
        // We send the report definition without the reportFilePath to avoid
        // exposing how and where reports are stored on the server #security
        final ReportDefinition original = reportResource.reportDefinition(),
            reportDefinition = new ReportDefinition(
                original.getReportName(),
                null,
                original.getParameters(),
                original.getVersion(),
                original.getDescription()
            );
        return toJson(reportDefinition);
    }
}

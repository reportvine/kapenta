package com.creditdatamw.labs.sparkpentaho.http;

import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;

import static com.creditdatamw.labs.sparkpentaho.http.ReportRoute.APPLICATION_JSON;
import static com.creditdatamw.labs.sparkpentaho.http.ReportRoute.toJson;

public class ReportsRoute implements Route {
    private final Reports reports;
    public ReportsRoute(final Reports reports) {
        this.reports = reports;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        LoggerFactory.getLogger(getClass()).debug("Processing ReportsRoute request");
        List<ReportResource> reportResources = new ArrayList<>(reports.resources());
        response.status(200);
        response.type(APPLICATION_JSON);
        return toJson(reportResources);
    }
}

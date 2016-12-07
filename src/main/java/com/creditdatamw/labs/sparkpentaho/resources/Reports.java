package com.creditdatamw.labs.sparkpentaho.resources;

import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;

import java.util.*;

import static spark.Spark.halt;

/**
 * Creates and registers the API endpoints for report resources
 *
 */
public class Reports {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reports.class);

    private final String rootPath;

    private final List<ReportResource> resources;

    public Reports(String rootPath, List<ReportResource> resourceList) {
        Objects.requireNonNull(rootPath);
        Objects.requireNonNull(resourceList);
        assert(! resourceList.isEmpty());
        this.rootPath = rootPath;
        this.resources = resourceList;
    }

    protected void validateResources() {
        resources.forEach(reportResource ->  {
            ReportDefinition reportDefinition = reportResource.reportDefinition();

            reportDefinition.validate();
        });
    }

    public void registerResources() {
        validateResources();
        resources.stream()
            .peek(this::registerParameterRoute)
            .forEach(this::registerRoute);
    }

    private String withRootPath(String path) {
        return rootPath.concat(path);
    }

    private void registerParameterRoute(ReportResource reportResource) {
    }

    private void registerRoute(ReportResource reportResource) {
        final ReportRoute reportRoute = new ReportRoute(reportResource);

        for(String method: reportResource.methods()) {
            if (method.equalsIgnoreCase("GET")) {
                Spark.get(withRootPath(reportResource.path()), reportRoute);
            }

            if (method.equalsIgnoreCase("POST")) {
                Spark.post(withRootPath(reportResource.path()),reportRoute);
            }
        }
        LoggerFactory.getLogger(getClass()).debug("Registered Route: {}", withRootPath(reportResource.path()));
    }

    public List<ReportResource> resources() {
        return Collections.unmodifiableList(resources);
    }

}

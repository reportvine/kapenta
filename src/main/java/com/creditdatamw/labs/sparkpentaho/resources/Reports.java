package com.creditdatamw.labs.sparkpentaho.resources;

import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;

import java.util.*;

import static java.util.stream.Collectors.toList;
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

    /**
     * Returns the Report definition
     *
     * @param reportResource
     */
    private void registerParameterRoute(ReportResource reportResource) {
        String reportInfoPath = withRootPath(reportResource.path().concat("/info"));
        Spark.get(reportInfoPath, new ReportDefinitionRoute(reportResource));
    }

    /**
     * Registers a reportResource to the spark application.
     * The report is mapped to the {@linkplain ReportResource#path()} of the resource
     * as <code>/path</code> and depending on the output types the resource
     * is mapped to routes with extensions e.g. <code>/path.html</code>
     *
     * If the report resource has a GET or POST defined as {@linkplain ReportResource#methods()}
     * the routes are mapped to those HTTP methods
     *
     * @param reportResource
     */
    private void registerRoute(ReportResource reportResource) {
        final ReportRoute reportRoute = new ReportRoute(reportResource);
        final String reportPath = withRootPath(reportResource.path());

        List<String> extensionList = reportResource.outputTypes()
            .stream()
            .map(ot -> ".".concat(ot.name().toLowerCase()))
            .collect(toList());

        for(String method: reportResource.methods()) {
            if (method.equalsIgnoreCase("GET")) {
                Spark.get(reportPath, reportRoute);
                // We want to map the path to the route /path and /path.ext for each output in the resource
                extensionList.forEach(extension -> Spark.get(reportPath.concat(extension), reportRoute));
            }

            if (method.equalsIgnoreCase("POST")) {
                Spark.post(withRootPath(reportResource.path()),reportRoute);
                // We want to map the path to the route /path and /path.ext for each output in the resource
                extensionList.forEach(extension -> Spark.post(reportPath.concat(extension), reportRoute));
            }
        }
        LoggerFactory.getLogger(getClass()).debug("Registered Route: {}", withRootPath(reportResource.path()));
    }

    public List<ReportResource> resources() {
        return Collections.unmodifiableList(resources);
    }

}

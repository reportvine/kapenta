package com.creditdatamw.labs.kapenta.http;

import com.creditdatamw.labs.kapenta.config.Backup;
import com.creditdatamw.labs.kapenta.config.Database;
import com.creditdatamw.labs.kapenta.reportdefinition.ReportDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Creates and registers the API endpoints for report resources
 *
 */
public class Reports {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reports.class);

    private final String rootPath;

    private final List<ReportResource> resources;

    private Service httpServer;
    
    private Backup backup;

    private Database database;

    public Reports(String rootPath, List<ReportResource> resourceList) {
        Objects.requireNonNull(rootPath);
        Objects.requireNonNull(resourceList);
        assert(! resourceList.isEmpty());
        this.rootPath = rootPath;
        this.resources = resourceList;
    }

    public Reports(String rootPath, List<ReportResource> resourceList, Backup backup) {
        this(rootPath, resourceList);
        this.backup = backup;
    }

    public Reports(String rootPath, List<ReportResource> resourceList, Backup backup, Database database) {
        this(rootPath, resourceList, backup);
        this.database = database;
    }

    protected void validateResources() {
        resources.forEach(reportResource ->  {
            ReportDefinition reportDefinition = reportResource.reportDefinition();

            reportDefinition.validate();
        });
    }

    public void setHttpServer(Service httpServer) {
        this.httpServer = httpServer;
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
        httpServer.get(reportInfoPath, new ReportDefinitionRoute(reportResource));
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
        final ReportRoute reportRoute = Objects.isNull(backup)
                                        ? new ReportRoute(reportResource)
                                        : new ReportRoute(reportResource, backup, Optional.ofNullable(database));

        final String reportPath = withRootPath(reportResource.path());

        List<String> extensionList = reportResource.outputTypes()
            .stream()
            .map(ot -> ".".concat(ot.name().toLowerCase()))
            .collect(toList());

        for(String method: reportResource.methods()) {
            if (method.equalsIgnoreCase("GET")) {
                httpServer.get(reportPath, reportRoute);
                // We want to map the path to the route /path and /path.ext for each output in the resource
                extensionList.forEach(extension -> httpServer.get(reportPath.concat(extension), reportRoute));
            }

            if (method.equalsIgnoreCase("POST")) {
                httpServer.post(withRootPath(reportResource.path()),reportRoute);
                // We want to map the path to the route /path and /path.ext for each output in the resource
                extensionList.forEach(extension -> httpServer.post(reportPath.concat(extension), reportRoute));
            }
        }
        LoggerFactory.getLogger(getClass()).debug("Registered Route: {}", withRootPath(reportResource.path()));
    }

    public List<ReportResource> resources() {
        return Collections.unmodifiableList(resources);
    }

}

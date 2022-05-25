package cloud.nndi.labs.kapenta.http;

import cloud.nndi.labs.kapenta.reportdefinition.ReportDefinition;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

import static cloud.nndi.labs.kapenta.http.ReportRoute.APPLICATION_JSON;
import static cloud.nndi.labs.kapenta.http.ReportRoute.toJson;

public class ReportsRoute implements Route {
    private final String reportResources;
    public ReportsRoute(final Reports reports) {
        this.reportResources = toJson(removePaths(reports.resources()));
    }

    @Override
    public Object handle(Request request, Response response) {
        LoggerFactory.getLogger(getClass()).debug("Processing ReportsRoute request");
        response.status(200);
        response.type(APPLICATION_JSON);
        return reportResources;
    }

    private List<ReportResource> removePaths(List<ReportResource> reports) {
        return reports.stream()
            .map(r -> {
                return new ReportResourceImpl(
                    r.path(),
                    r.methods(),
                    r.outputTypes(),
                    new ReportDefinition(
                        r.reportDefinition().getReportName(),
                        null,
                        r.reportDefinition().getParameters()
                    )
                );
            })
            .collect(Collectors.toList());
    }
}

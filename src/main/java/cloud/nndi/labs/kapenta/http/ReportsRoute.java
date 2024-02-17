package cloud.nndi.labs.kapenta.http;

import cloud.nndi.labs.kapenta.reportdefinition.ReportDefinition;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static cloud.nndi.labs.kapenta.http.ReportRoute.toJson;

public class ReportsRoute implements Handler {
    private final String reportResources;
    public ReportsRoute(final Reports reports) {
        this.reportResources = toJson(removePaths(reports.resources()));
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        LoggerFactory.getLogger(getClass()).debug("Processing ReportsRoute request");
        context.json(reportResources);
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

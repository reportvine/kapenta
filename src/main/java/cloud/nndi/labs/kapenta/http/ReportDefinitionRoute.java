package cloud.nndi.labs.kapenta.http;

import cloud.nndi.labs.kapenta.reportdefinition.ReportDefinition;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * Report Definition Route
 *
 * Generates a route for presenting the Report Definition
 */
public class ReportDefinitionRoute implements Handler {

    public final ReportResource reportResource;

    public ReportDefinitionRoute(ReportResource reportResource) {
        this.reportResource = reportResource;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {

        // We send the report definition without the reportFilePath to avoid
        // exposing how and where generator are stored on the server #security
        final ReportDefinition original = reportResource.reportDefinition(),
            reportDefinition = new ReportDefinition(
                original.getReportName(),
                null,
                original.getParameters(),
                original.getVersion(),
                original.getDescription()
            );

        context.json(reportDefinition);
    }
}

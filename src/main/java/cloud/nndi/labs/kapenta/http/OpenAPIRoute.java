package cloud.nndi.labs.kapenta.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapi4j.parser.model.v3.OpenApi3;
import spark.Request;
import spark.Response;
import spark.Route;

public class OpenAPIRoute implements Route {
    private final OpenApi3 openApiSchema;
    private volatile String cachedJson;
    private final ObjectMapper objectMapper;

    public OpenAPIRoute(final ObjectMapper objectMapper, final OpenApi3 schema) {
        this.objectMapper = objectMapper;
        this.openApiSchema = schema;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.status(200);
        response.type(ReportRoute.APPLICATION_JSON);
        if (cachedJson == null) {
            cachedJson = objectMapper.writeValueAsString(openApiSchema.toNode());
        }
        return cachedJson;
    }
}

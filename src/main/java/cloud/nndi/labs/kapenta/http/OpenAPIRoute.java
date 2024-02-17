package cloud.nndi.labs.kapenta.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.openapi4j.parser.model.v3.OpenApi3;

public class OpenAPIRoute implements Handler {
    private final OpenApi3 openApiSchema;
    private volatile Object cachedJson;
    private final ObjectMapper objectMapper;

    public OpenAPIRoute(final ObjectMapper objectMapper, final OpenApi3 schema) {
        this.objectMapper = objectMapper;
        this.openApiSchema = schema;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        if (cachedJson == null) {
            cachedJson = openApiSchema.toNode();
        }
        context.json(cachedJson);
    }
}

package com.creditdatamw.labs.kapenta.http;

import com.creditdatamw.labs.kapenta.config.ApiConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Schema;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.creditdatamw.labs.kapenta.http.ReportRoute.APPLICATION_JSON;

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
        response.type(APPLICATION_JSON);
        if (cachedJson == null) {
            cachedJson = objectMapper.writeValueAsString(openApiSchema.toNode());
        }
        return cachedJson;
    }
}

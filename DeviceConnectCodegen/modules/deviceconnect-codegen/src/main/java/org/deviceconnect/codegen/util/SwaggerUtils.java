package org.deviceconnect.codegen.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Swagger;
import io.swagger.util.Json;

import java.io.IOException;

public final class SwaggerUtils {

    private static JsonNode convertToNode(final Swagger swagger) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonAsString = mapper.writeValueAsString(swagger);
        return mapper.readTree(jsonAsString);
    }

    public static Swagger cloneSwagger(final Swagger swagger) throws IOException {
        JsonNode json = convertToNode(swagger);
        return Json.mapper().convertValue(json, Swagger.class);
    }

    private SwaggerUtils() {
    }
}

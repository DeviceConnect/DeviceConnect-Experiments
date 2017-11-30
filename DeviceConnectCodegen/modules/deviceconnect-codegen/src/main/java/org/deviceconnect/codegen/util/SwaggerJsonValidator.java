package org.deviceconnect.codegen.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SwaggerJsonValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerJsonValidator.class);

    private final JsonSchema jsonSchema;

    public SwaggerJsonValidator() {
        URL schemaURL = ClassLoader.getSystemResource("v2.schema.json");
        LOGGER.info("Swagger 2.0 Schema: " + schemaURL);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode schema = mapper.readTree(schemaURL);

            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            jsonSchema = factory.getJsonSchema(schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Result validate(final JsonNode swagger) throws ProcessingException {
        ProcessingReport report = this.jsonSchema.validate(swagger, true);
        List<Error> errors = new ArrayList<>();
        for (Iterator<ProcessingMessage> it = report.iterator(); it.hasNext(); ) {
            ProcessingMessage msg = it.next();
            Error error = parseError(msg);
            if (error != null) {
                errors.add(error);
            }
        }
        return new Result(errors);
    }

    private Error parseError(final ProcessingMessage report) {
        JsonNode root = report.asJson();
        JsonNode domainNode = root.get("domain");
        if (domainNode == null || !domainNode.asText().equals("validation")) {
            return null;
        }
        JsonNode levelNode = root.get("level");
        if (levelNode == null || !levelNode.asText().equals("error")) {
            return null;
        }
        JsonNode instanceNode = root.get("instance");
        if (instanceNode == null) {
            return null;
        }
        JsonNode pointerNode = instanceNode.get("pointer");
        if (pointerNode == null) {
            return null;
        }
        String pointer = pointerNode.asText();
        if (pointer.equals("")) {
            pointer = "/";
        }
        JsonNode messageNode = root.get("message");
        String message = messageNode != null ? messageNode.asText() : null;
        return new Error(pointer, message);
    }

    public static class Result {
        private final List<Error> errors;

        Result(final List<Error> errors) {
            this.errors = errors;
        }

        public boolean isSuccess() {
            return errors.size() <= 0;
        }

        public List<Error> getErrors() {
            return new ArrayList<>(errors);
        }
    }

    public static class Error {
        private final String message;
        private final String jsonPointer;

        Error(final String jsonPointer, final String message) {
            this.jsonPointer = jsonPointer;
            this.message = message;
        }

        public String getJsonPointer() {
            return jsonPointer;
        }

        public String getMessage() {
            return message;
        }
    }

    public static void main(final String[] args) throws Exception {
        String json = "{\"swagger\": \"2.0\", \"info\":{}";
        JsonNode instance = new ObjectMapper().readTree(json);

        SwaggerJsonValidator validator = new SwaggerJsonValidator();
        validator.validate(instance);
    }
}

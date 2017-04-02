package org.deviceconnect.codegen.models;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.*;

import java.util.List;
import java.util.Map;

public class DConnectOperation {

    private static final String TYPE = "x-type";
    private static final String PREFIX_DEFINITION_REF = "#/definitions/";

    private final Operation entity;
    private final Type type;
    private final Response eventModel;

    private DConnectOperation(final Operation entity, final Type type, final Response eventModel) {
        this.entity = entity;
        this.type = type;
        this.eventModel = eventModel;
    }

    public Type getType() {
        return type;
    }

    public List<Parameter> getParameters() {
        return entity.getParameters();
    }

    public Map<String, Response> getResponses() {
        return entity.getResponses();
    }

    public Response getEventModel() {
        return eventModel;
    }

    public static DConnectOperation parse(final Swagger swagger, final Operation entity) {
        Map<String, Object> extensions = entity.getVendorExtensions();
        if (extensions != null) {
            Object tmp = extensions.get(TYPE);
            if (tmp != null && tmp instanceof String) {
                if ("event".equals(tmp)) {
                    Response eventModel = parseEventModel(entity);
                    return new DConnectOperation(entity, Type.EVENT, eventModel);
                } else if ("streaming".equals(tmp)) {
                    return new DConnectOperation(entity, Type.STREAMING, null);
                }
            }
        }
        return new DConnectOperation(entity, Type.ONE_SHOT, null);
    }

    static Response parseEventModel(final Operation parent) {
        Map<String, Object> extensions = parent.getVendorExtensions();
        if (extensions == null) {
            return null;
        }
        Object root = extensions.get("x-event");
        if (root == null) {
            return null;
        }
        if (!(root instanceof ObjectNode)) {
            return null;
        }
        ObjectNode rootNode = (ObjectNode) root;
        JsonNode schema = rootNode.get("schema");
        if (schema == null) {
            return null;
        }
        if (!(schema instanceof ObjectNode)) {
            return null;
        }
        ObjectNode schemaNode = (ObjectNode) schema;
        JsonNode refNode = schemaNode.get("$ref");
        if (refNode != null) {
            if (!refNode.isTextual()) {
                return null;
            }
            String text = refNode.textValue();
            if (!(text.startsWith(PREFIX_DEFINITION_REF))) {
                return null;
            }

            Map<String, Object> examples = null;
            JsonNode examplesNode = rootNode.get("examples");
            if (examplesNode != null) {
                ObjectMapper mapper = new ObjectMapper();
                examples = mapper.convertValue(examplesNode, Map.class);
            }

            Response eventModel = new Response();
            RefProperty refSchema = new RefProperty();
            refSchema.set$ref(text);
            eventModel.setSchema(refSchema);
            eventModel.setExamples(examples);
            return eventModel;
        } else {
            return null; // TODO
        }
    }

    public enum Type {
        ONE_SHOT,
        EVENT,
        STREAMING
    }
}

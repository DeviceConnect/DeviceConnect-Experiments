/*
 DConnectOperation.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.models;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.RefProperty;

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

    public boolean hasIntervalForEvent() {
        if (getType() != Type.EVENT) {
            return false;
        }
        List<Parameter> params = entity.getParameters();
        if (params == null) {
            return false;
        }
        for (Parameter param : params) {
            if ("interval".equals(param.getName())) {
                if (param instanceof SerializableParameter) {
                    if ("integer".equals(((SerializableParameter) param).getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static DConnectOperation parse(final Operation entity) {
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
        if (root instanceof Map) {
            return parseEventModelAsMap((Map) root);
        }
        if (root instanceof ObjectNode) {
            return parseEventModelAsNode((ObjectNode) root);
        }
        return null;
    }

    // For swagger-codegen 2.2.3
    static Response parseEventModelAsMap(final Map root) {
        Object schemaObj = root.get("schema");
        Object examplesObj = root.get("examples");
        if (!(schemaObj instanceof Map)) {
            return null;
        }
        Map schema = (Map) schemaObj;
        Object refObj = schema.get("$ref");
        if (refObj != null) {
            if (!(refObj instanceof String)) {
                return null;
            }
            String ref = (String) refObj;
            if (!ref.startsWith(PREFIX_DEFINITION_REF)) {
                return null;
            }
            if (!(examplesObj instanceof Map)) {
                return null;
            }
            Map examples= (Map) examplesObj;

            Response eventModel = new Response();
            RefProperty refSchema = new RefProperty();
            refSchema.set$ref(ref);
            eventModel.setSchema(refSchema);
            eventModel.setExamples(examples);
            return eventModel;
        } else {
            return null;
        }
    }

    // For swagger-codegen 2.2.1
    static Response parseEventModelAsNode(final ObjectNode rootNode) {
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
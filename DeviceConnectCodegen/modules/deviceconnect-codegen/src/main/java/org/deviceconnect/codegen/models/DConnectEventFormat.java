package org.deviceconnect.codegen.models;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.*;
import io.swagger.models.properties.Property;

import java.util.Map;


public class DConnectEventFormat implements Model {

    private static final String PREFIX_DEFINITION_REF = "#/definitions/";

    private final Model entity;

    private DConnectEventFormat(final Model entity) {
        this.entity = entity;
    }

    public static DConnectEventFormat parse(final Swagger swagger, final Operation parent) {
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
        JsonNode refNode = rootNode.get("$ref");
        if (refNode != null) {
            if (!refNode.isTextual()) {
                return null;
            }
            String text = refNode.textValue();
            if (!(text.startsWith(PREFIX_DEFINITION_REF))) {
                return null;
            }
            String modelName = text.substring(PREFIX_DEFINITION_REF.length());
            if (swagger.getDefinitions() == null) {
                return null;
            }
            Model model = swagger.getDefinitions().get(modelName);
            if (model == null) {
                return null;
            }
            return new DConnectEventFormat(model);
        } else {
            return null; // TODO
        }
    }

    @Override
    public String getTitle() {
        return this.getTitle();
    }

    @Override
    public void setTitle(final String s) {
        this.entity.setTitle(s);
    }

    @Override
    public String getDescription() {
        return this.getDescription();
    }

    @Override
    public void setDescription(final String s) {
        this.entity.setDescription(s);
    }

    @Override
    public Map<String, Property> getProperties() {
        return this.entity.getProperties();
    }

    @Override
    public void setProperties(final Map<String, Property> map) {
        this.entity.setProperties(map);
    }

    @Override
    public Object getExample() {
        return this.entity.getExample();
    }

    @Override
    public void setExample(final Object o) {
        this.entity.setExample(o);
    }

    @Override
    public ExternalDocs getExternalDocs() {
        return this.entity.getExternalDocs();
    }

    @Override
    public String getReference() {
        return this.entity.getReference();
    }

    @Override
    public void setReference(final String s) {
        this.entity.setReference(s);
    }

    @Override
    public Map<String, Object> getVendorExtensions() {
        return this.entity.getVendorExtensions();
    }

    @Override
    public Object clone() {
        return this.entity.clone();
    }
}

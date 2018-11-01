/*
 AbstractDocsCodegenConfig.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.docs;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenType;
import io.swagger.models.*;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.apache.commons.cli.CommandLine;
import org.deviceconnect.codegen.AbstractCodegenConfig;
import org.deviceconnect.codegen.ProfileTemplate;
import org.deviceconnect.codegen.ValidationResultSet;
import org.deviceconnect.codegen.models.DConnectOperation;

import java.io.IOException;
import java.util.*;

public abstract class AbstractDocsCodegenConfig extends AbstractCodegenConfig {

    private ArrayList<OperationListDocs> swaggerList = new ArrayList<>();

    protected List<OperationListDocs> getSwaggerList() {
        return this.swaggerList;
    }

    @Override
    public ValidationResultSet validateOptions(final CommandLine cmd, final ClientOpts clientOpts) {
        return new ValidationResultSet();
    }

    @Override
    public String getDefaultDisplayName() {
        return "Device Connect API Specifications";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        embeddedTemplateDir = getName();

        for (Map.Entry<String, Swagger> specEntry : profileSpecs.entrySet()) {
            final String profileKey = specEntry.getKey();
            final String profileName;
            final Swagger profileSpec = specEntry.getValue();
            String basePath = profileSpec.getBasePath();
            if (basePath == null) {
                basePath = "/gotapi/" + profileKey;
                profileName = profileKey;
            } else {
                System.out.println("basePath: " + basePath);
                profileName = basePath.split("/")[2];
            }

            final List<Object> operationList = new ArrayList<>();
            for (Map.Entry<String, Path> pathEntry : profileSpec.getPaths().entrySet()) {
                String pathName = pathEntry.getKey();
                Path path = pathEntry.getValue();
                if ("/".equals(pathName)) {
                    pathName = "";
                }
                final String fullPathName = basePath + pathName;

                HttpMethod[] httpMethods = {
                        HttpMethod.GET,
                        HttpMethod.POST,
                        HttpMethod.PUT,
                        HttpMethod.DELETE
                };
                for (HttpMethod key : httpMethods) {
                    final String method = key.name().toUpperCase();
                    final Operation op = path.getOperationMap().get(key);
                    if (op == null) {
                        continue;
                    }
                    final List<Object> paramList = new ArrayList<>();
                    for (final Parameter param : op.getParameters()) {
                        paramList.add(new Object() {
                            String name = param.getName();
                            String type() {
                                String type;
                                String format;
                                Property items;
                                if (param instanceof QueryParameter) {
                                    type = ((QueryParameter) param).getType();
                                    format = ((QueryParameter) param).getFormat();
                                    items = ((QueryParameter) param).getItems();
                                } else if (param instanceof FormParameter) {
                                    type = ((FormParameter) param).getType();
                                    format = ((FormParameter) param).getFormat();
                                    items = ((FormParameter) param).getItems();
                                } else {
                                    return null;
                                }

                                if ("array".equals(type)) {
                                    if (items != null) {
                                        return type + "(" + convertPropertyToCommonName(items) + ")";
                                    } else {
                                        return type;
                                    }
                                } else if ("object".equals(type)) {
                                    return type;
                                } else {
                                    return convertPrimitiveProperty(type, format);
                                }
                            }
                            String required = param.getRequired() ? "Yes" : "No";
                            String description = param.getDescription();
                        });
                    }

                    operationList.add(new Object() {
                        String id() {
                            return op.getOperationId();
                        }
                        String name = method + " " + fullPathName;
                        String type = (String) op.getVendorExtensions().get("x-type");
                        String summary = op.getSummary();
                        String description() {
                            String description = op.getDescription();
                            if ("".equals(description)) {
                                return null;
                            }
                            return description;
                        }
                        List<Object> paramList() {
                            return paramList;
                        }
                        Object response = createResponseDocument(profileSpec, op);
                        Object event = createEventDocument(profileSpec, op);
                        String tag() {
                            String tag = this.name;
                            tag = tag.toLowerCase();
                            tag = tag.replaceAll(" ", "-");  // 半角スペース -> 半角ハイフン
                            tag = tag.replaceAll("/", "");  // 半角スラッシュ -> 空文字
                            return tag;
                        }
                    });
                }
            }

            OperationListDocs swaggerObj = new OperationListDocs() {
                public String profileName() { return profileName; }
                String profileNameCamelCase() { return toUpperCapital(profileName, false); }
                String version = profileSpec.getInfo().getVersion();
                String title = profileSpec.getInfo().getTitle();
                String description = profileSpec.getInfo().getDescription();
                public List<Object> operationList() { return operationList; }
            };
            swaggerList.add(swaggerObj);
        }
        Collections.sort(swaggerList, new Comparator<ProfileDocs>() {
            @Override
            public int compare(ProfileDocs o1, ProfileDocs o2) {
                return o1.profileName().compareTo(o2.profileName());
            }
        });
        additionalProperties.put("swaggerList", swaggerList);

        filterProfiles();
    }

    protected String[] getIgnoredProfiles() {
        return new String[] {"files"};
    }

    private void filterProfiles() {
        for (String ignored : getIgnoredProfiles()) {
            for(Iterator<OperationListDocs> it = swaggerList.iterator(); it.hasNext(); ) {
                OperationListDocs swagger = it.next();
                if (swagger.profileName().equalsIgnoreCase(ignored)) {
                    it.remove();
                }
            }
            profileSpecs.remove(ignored);
        }
    }

    private OperationListDocs findSwagger(final String profileName) {
        for (final OperationListDocs doc : swaggerList) {
            if (doc.profileName().equals(profileName)) {
                return doc;
            }
        }
        return null;
    }

    @Override
    public void processSwagger(final Swagger swagger) {
        try {
            for (Map.Entry<String, Swagger> specEntry : profileSpecs.entrySet()) {
                final String profileName = specEntry.getKey();
                List<ProfileTemplate> templates = new ArrayList<>();
                Map<String, Object> properties = new LinkedHashMap<>();
                properties.put("profileSpec", findSwagger(profileName));

                prepareTemplates(profileName, templates);
                if (templates.size() > 0) {
                    prepareProperties(profileName, properties);
                    for (ProfileTemplate template : templates) {
                        generateProfile(template, properties);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void prepareTemplates(final String profileName, final List<ProfileTemplate> templates) {
        // To be override by child class.
    }

    protected void prepareProperties(final String profileName, final Map<String, Object> properties) {
        // To be override by child class.
    }

    private String convertPropertyToCommonName(final Property prop) {
        String type = prop.getType();
        String format = prop.getFormat();
        if ("array".equals(type)) {
            ArrayProperty arrayProp = (ArrayProperty) prop;
            return type + "(" + convertPropertyToCommonName(arrayProp.getItems()) + ")";
        } else if ("array".equals(type)) {
            return type;
        } else  {
            return convertPrimitiveProperty(type, format);
        }
    }

    private String convertPrimitiveProperty(final String type, final String format) {
        if ("integer".equals(type)) {
            if ("int64".equals(format)) {
                return "long";
            }
            return "integer";
        } else if ("number".equals(type)) {
            if ("double".equals(format)) {
                return format;
            }
            return "float";
        } else if ("string".equals(type)) {
            if ("byte".equals(format) || "binary".equals(format) || "date".equals(format) || "password".equals(format)) {
                return format;
            } else if ("date-time".equals(format)) {
                return "dateTime";
            } else {
                return type; // string
            }
        } else {
            return type;
        }
    }

    private Object createResponseDocument(final Swagger swagger, final Operation operation) {
        final Map<String, Response> responses = operation.getResponses();
        if (responses != null) {
            Response response = responses.get("200");
            if (response != null) {
                return createMessageDocument(swagger, response);
            }
        }
        return null;
    }

    private Object createEventDocument(final Swagger swagger, final Operation operation) {
        DConnectOperation dConnectOperation = DConnectOperation.parse(swagger, operation);
        if (dConnectOperation == null) {
            return null;
        }
        Response eventModel = dConnectOperation.getEventModel();
        if (eventModel == null) {
            return null;
        }
        return createMessageDocument(swagger, eventModel);
    }

    private Object createMessageDocument(final Swagger swagger, final Response message) {
        Property schema = message.getSchema();
        if (schema == null) {
            return null;
        }
        ObjectProperty root;
        if (schema instanceof ObjectProperty) {
            root = (ObjectProperty) schema;
        } else if (schema instanceof RefProperty) {
            RefProperty ref = (RefProperty) schema;
            Model model = findDefinition(swagger, ref.getSimpleRef());
            Map<String, Property> properties;
            if (model instanceof ComposedModel) {
                properties = getProperties(swagger, (ComposedModel) model);
            } else if (model instanceof ModelImpl) {
                properties = model.getProperties();
            } else {
                return null;
            }
            if (properties == null) {
                return null;
            }
            root =  new ObjectProperty();
            root.setProperties(properties);
        } else {
            return null;
        }

        final List<ResponseParamDoc> paramDocList = new ArrayList<>();
        Map<String, Property> props = root.getProperties();
        if (props != null && props.size() > 0) {
            createResponseParameterDocument(swagger, root, paramDocList, 1);
        }

        final int maxNestLevel = getMaxNestLevel(paramDocList);
        for (ResponseParamDoc paramDoc : paramDocList) {
            paramDoc.setMaxNestLevel(maxNestLevel);
        }

        return new Object() {
            List<ResponseParamDoc> paramList() { return paramDocList; }

            int maxNestLevel() { return maxNestLevel; }

            String example() {
                String exampleJson = null;
                Map<String, Object> examples = message.getExamples();
                if (examples != null) {
                    Map<String, Object> example = (Map<String, Object>) examples.get("application/json");
                    if (example != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                        try {
                            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
                            exampleJson = writer.writeValueAsString(example);
                        } catch (JsonProcessingException e) {
                            // NOP.
                        }
                    }
                }
                return exampleJson;
            }
        };
    }

    private int getMaxNestLevel(final List<ResponseParamDoc> paramDocList) {
        int level = 1;
        for (ResponseParamDoc paramDoc : paramDocList) {
            if (level < paramDoc.nestLevel) {
                level = paramDoc.nestLevel;
            }
        }
        return level;
    }

    private void createResponseParameterDocument(final Swagger swagger,
                                                 final ObjectProperty root,
                                                 final List<ResponseParamDoc> paramDocList,
                                                 final int nestLevel) {
        Map<String, Property> props = root.getProperties();
        if (props == null) {
            return;
        }
        for (Map.Entry<String, Property> propEntry : props.entrySet()) {
            String propName = propEntry.getKey();
            Property prop = propEntry.getValue();

            ObjectProperty childProp = null;
            if (prop instanceof ArrayProperty) {
                ArrayProperty arrayProp = (ArrayProperty) prop;
                Property itemsProp = arrayProp.getItems();
                if (itemsProp instanceof ObjectProperty) {
                    childProp = (ObjectProperty) itemsProp;
                } else if (itemsProp instanceof RefProperty) {
                    String simpleRef = ((RefProperty) itemsProp).getSimpleRef();
                    Model m = findDefinition(swagger, simpleRef);
                    if (m != null) {
                        ObjectProperty objProp = createObjectProperty(m);
                        arrayProp.setItems(objProp);
                        childProp = objProp;
                    }
                }
            } else if (prop instanceof ObjectProperty) {
                childProp = (ObjectProperty) prop;
            } else if (prop instanceof RefProperty) {
                String simpleRef = ((RefProperty) prop).getSimpleRef();
                Model m = findDefinition(swagger, simpleRef);
                if (m != null) {
                    ObjectProperty objProp = createObjectProperty(m);
                    prop = objProp;
                    childProp = objProp;
                }
            }

            ResponseParamDoc paramDoc = new ResponseParamDoc(propName, prop, nestLevel);
            paramDocList.add(paramDoc);
            if (childProp != null) {
                createResponseParameterDocument(swagger, childProp, paramDocList, nestLevel + 1);
            }
        }
    }

    private ObjectProperty createObjectProperty(final Model m) {
        ObjectProperty p = new ObjectProperty();
        p.setProperties(m.getProperties());
        p.setTitle(m.getTitle());
        p.setDescription(m.getDescription());
        p.setVendorExtensionMap(m.getVendorExtensions());
        return p;
    }

    private Model findDefinition(final Swagger swagger, final String simpleRef) {
        Map<String, Model> definitions = swagger.getDefinitions();
        if (definitions == null) {
            return null;
        }
        return definitions.get(simpleRef);
    }

    private Map<String, Property> getProperties(final Swagger swagger, final ComposedModel parent) {
        Map<String, Property> result = new LinkedHashMap<>();
        Stack<ComposedModel> stack = new Stack<>();
        stack.push(parent);
        do {
            ComposedModel model = stack.pop();
            List<Model> children = model.getAllOf();
            for (Model child : children) {
                if (child instanceof ModelImpl) {
                    if (child.getProperties() != null) {
                        result.putAll(child.getProperties());
                    }
                } else if (child instanceof ComposedModel) {
                    stack.push((ComposedModel) child);
                } else if (child instanceof RefModel) {
                    String refName = ((RefModel) child).getSimpleRef();
                    Model m = findDefinition(swagger, refName);
                    if (m == null) {
                        continue;
                    }
                    if (m.getProperties() != null) {
                        result.putAll(m.getProperties());
                    }
                }
            }
        } while (!stack.empty());
        return result;
    }

    interface ProfileDocs {
        String profileName();
    }

    interface OperationListDocs extends ProfileDocs {
        List<Object> operationList();
    }

    class ResponseParamDoc {
        final String name;
        final String type;
        final String dataType;
        final String format;
        final String title;
        final String description;
        final boolean isRequired;
        final int nestLevel;
        int maxNestLevel;

        ResponseParamDoc(final String name,
                         final Property prop,
                         final int nestLevel) {
            this.dataType = convertPropertyToCommonName(prop);
            this.name = name;
            this.type = prop.getType();
            this.format = prop.getFormat();
            this.title = prop.getTitle();
            this.description = prop.getDescription();
            this.isRequired = prop.getRequired();
            this.nestLevel = nestLevel;
        }

        String required() {
            return this.isRequired ? "Yes" : "No";
        }

        void setMaxNestLevel(final int max) {
            this.maxNestLevel = max;
        }

        int colSpan() {
            return this.maxNestLevel - this.nestLevel + 1;
        }

        List<Object> indents() {
            List<Object> indents = new ArrayList<>();
            for (int cnt = 0; cnt < this.nestLevel - 1; cnt++) {
                indents.add("");
            }
            return indents;
        }
    }
}
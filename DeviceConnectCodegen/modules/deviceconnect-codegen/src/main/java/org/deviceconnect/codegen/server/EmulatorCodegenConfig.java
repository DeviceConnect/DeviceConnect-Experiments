/*
 EmulatorCodegenConfig.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.server;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.swagger.codegen.*;
import io.swagger.models.*;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.cli.CommandLine;
import org.deviceconnect.codegen.*;
import org.deviceconnect.codegen.models.DConnectOperation;
import org.deviceconnect.codegen.util.SwaggerUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Codegen Config for Device Connect System.
 * <p>
 * This class is implemented by modification of NodeJSServerCodegen class.
 * </p>
 */
public class EmulatorCodegenConfig extends AbstractCodegenConfig implements DConnectCodegenConfig {

    private Map<String, Swagger> profileSpecs;

    protected String apiVersion = "1.0.0";
    protected int serverPort = 4035;
    protected String projectName = "swagger-server";

    @Override
    protected String profileFileFolder() {
        return null; // Not be used.
    }

    public EmulatorCodegenConfig() {
        super();
        modelTemplateFiles.clear();
        apiTemplateFiles.put(
                "controller.mustache",   // the template to use
                ".js");       // the extension for each file to write
        embeddedTemplateDir = templateDir = getName();

        setReservedWordsLowerCase(
                Arrays.asList(
                        "break", "case", "class", "catch", "const", "continue", "debugger",
                        "default", "delete", "do", "else", "export", "extends", "finally",
                        "for", "function", "if", "import", "in", "instanceof", "let", "new",
                        "return", "super", "switch", "this", "throw", "try", "typeof", "var",
                        "void", "while", "with", "yield")
        );

        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put("serverPort", serverPort);

        supportingFiles.add(new SupportingFile("swagger.mustache",
                "api",
                "swagger.yaml")
        );
        writeOptional(outputFolder, new SupportingFile("index.mustache", "", "index.js"));
        writeOptional(outputFolder, new SupportingFile("package.mustache", "", "package.json"));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
        if (System.getProperty("noservice") == null) {
            apiTemplateFiles.put(
                    "service.mustache",   // the template to use
                    "Service.js");       // the extension for each file to write
        }
    }

    @Override
    public ValidationResultSet validateOptions(final CommandLine cmd, final ClientOpts clientOpts) {
        return new ValidationResultSet();
    }

    @Override
    public String apiPackage() {
        return "controllers";
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultController";
        }
        return initialCaps(name);
    }

    @Override
    public String toApiFilename(String name) {
        return toApiName(name);
    }

    @Override
    public String escapeReservedWord(String name) {
        return "_" + name;  // add an underscore to the name
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        for (CodegenOperation operation : operations) {
            operation.httpMethod = operation.httpMethod.toLowerCase();

            List<CodegenParameter> params = operation.allParams;
            if (params != null && params.size() == 0) {
                operation.allParams = null;
            }
            List<CodegenResponse> responses = operation.responses;
            if (responses != null) {
                for (CodegenResponse resp : responses) {
                    if ("0".equals(resp.code)) {
                        resp.code = "default";
                    }
                }
            }
            if (operation.examples != null && !operation.examples.isEmpty()) {
                // Leave application/json* items only
                for (Iterator<Map<String, String>> it = operation.examples.iterator(); it.hasNext(); ) {
                    final Map<String, String> example = it.next();
                    final String contentType = example.get("contentType");
                    if (contentType == null || !contentType.startsWith("application/json")) {
                        it.remove();
                    }
                }
            }
        }
        return objs;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getOperations(Map<String, Object> objs) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> apiInfo = (Map<String, Object>) objs.get("apiInfo");
        List<Map<String, Object>> apis = (List<Map<String, Object>>) apiInfo.get("apis");
        for (Map<String, Object> api : apis) {
            result.add((Map<String, Object>) api.get("operations"));
        }
        return result;
    }

    private static List<Map<String, Object>> sortOperationsByPath(List<CodegenOperation> ops) {
        Multimap<String, CodegenOperation> opsByPath = ArrayListMultimap.create();

        for (CodegenOperation op : ops) {
            opsByPath.put(op.path, op);
        }

        List<Map<String, Object>> opsByPathList = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Collection<CodegenOperation>> entry : opsByPath.asMap().entrySet()) {
            Map<String, Object> opsByPathEntry = new HashMap<String, Object>();
            opsByPathList.add(opsByPathEntry);
            opsByPathEntry.put("path", entry.getKey());
            opsByPathEntry.put("operation", entry.getValue());
            List<CodegenOperation> operationsForThisPath = Lists.newArrayList(entry.getValue());
            operationsForThisPath.get(operationsForThisPath.size() - 1).hasMore = false;
            if (opsByPathList.size() < opsByPath.asMap().size()) {
                opsByPathEntry.put("hasMore", "true");
            }
        }

        return opsByPathList;
    }

    @Override
    public void preprocessSwagger(Swagger swagger) {
        String host = swagger.getHost();
        String port = "4035";
        if (host != null) {
            String[] parts = host.split(":");
            if (parts.length > 1) {
                port = parts[1];
            }
        }
        this.additionalProperties.put("serverPort", port);

        if (swagger.getInfo() != null) {
            Info info = swagger.getInfo();
            if (info.getTitle() != null) {
                // when info.title is defined, use it for projectName
                // used in package.json
                projectName = dashize(info.getTitle());
                this.additionalProperties.put("projectName", projectName);
            }
        }

        modifySwagger(swagger);
    }

    private void modifySwagger(Swagger swagger) {
        appendAvailability(swagger);
        appendAuthorization(swagger);
        appendServiceDiscovery(swagger);
        appendServiceInformation(swagger);
        checkPaths(swagger);
        checkEvents(swagger);
        appendControllers(swagger);
    }

    private void appendControllers(Swagger swagger) {
        // need vendor extensions for x-swagger-router-controller
        Map<String, Path> paths = swagger.getPaths();
        if(paths != null) {
            for(String pathname : paths.keySet()) {
                Path path = paths.get(pathname);
                Map<HttpMethod, Operation> operationMap = path.getOperationMap();
                if(operationMap != null) {
                    for(HttpMethod method : operationMap.keySet()) {
                        Operation operation = operationMap.get(method);
                        String tag = "default";
                        if(operation.getTags() != null && operation.getTags().size() > 0) {
                            tag = toApiName(operation.getTags().get(0));
                        }
                        if(operation.getOperationId() == null) {
                            operation.setOperationId(getOrGenerateOperationId(operation, pathname, method.toString()));
                        }
                        if(operation.getVendorExtensions().get("x-swagger-router-controller") == null) {
                            operation.getVendorExtensions().put("x-swagger-router-controller", sanitizeTag(tag));
                        }
                    }
                }
            }
        }
    }

    private void checkPaths(final Swagger swagger) {
        List<Object> pathList = new ArrayList<>();
        final String basePath = swagger.getBasePath();
        Map<String, Path> paths = swagger.getPaths();
        if (paths != null) {
            for (final Iterator<String> it = paths.keySet().iterator(); it.hasNext(); ) {
                try {
                    final String path = it.next();
                    final DConnectPath dConnectPath = DConnectPath.parsePath(basePath, path);
                    final boolean hasNext = it.hasNext();
                    pathList.add(new Object() {
                        String path() {
                            return dConnectPath.getPath();
                        }
                        boolean hasNext() { return hasNext; }
                    });
                } catch (IllegalPathFormatException e) {
                    // Fix bug.
                    throw new RuntimeException(e);
                }
            }
        }
        additionalProperties.put("pathList", pathList);
    }

    private void checkEvents(final Swagger swagger) {
        List<Object> eventList = new ArrayList<>();

        // Collect event examples from swagger.json
        final String basePath = swagger.getBasePath();
        Map<String, Path> paths = swagger.getPaths();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        if(paths != null) {
            for(final String pathname : paths.keySet()) {
                Path path = paths.get(pathname);
                Map<HttpMethod, Operation> operationMap = path.getOperationMap();
                if(operationMap != null) {
                    for(HttpMethod method : operationMap.keySet()) {
                        if (method == HttpMethod.PUT) {
                            try {
                                Operation operation = operationMap.get(method);
                                final DConnectOperation dConnectOperation = DConnectOperation.parse(operation);
                                if (dConnectOperation != null) {
                                    Response event = dConnectOperation.getEventModel();
                                    if (event != null) {
                                        Map<String, Object> examples = event.getExamples();
                                        if (examples != null) {
                                            Object json = examples.get("application/json");
                                            if (json != null) {
                                                try {
                                                    final String eventJson = mapper.writeValueAsString(json);
                                                    final DConnectPath dConnectPath = DConnectPath.parsePath(basePath, pathname);
                                                    eventList.add(new Object() {
                                                        String key = dConnectPath.getPath().toLowerCase();
                                                        String json = eventJson;
                                                    });
                                                } catch (IllegalPathFormatException e) {
                                                    // Fix bug
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        additionalProperties.put("eventList", eventList);
    }

    private void appendAvailability(final Swagger allSpecs) {
        try {
            String resPath = getResoucePath("availability.json");
            Swagger authorizationSpec = getProfileSpec(resPath);
            Path path = authorizationSpec.getPath("/");

            Map<String, Path> paths = allSpecs.getPaths();
            paths.put("/gotapi/availability", path);
            allSpecs.setPaths(paths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendAuthorization(final Swagger allSpecs) {
        try {
            String resPath = getResoucePath("authorization.json");
            Swagger authorizationSpec = getProfileSpec(resPath);
            Path grantPath = authorizationSpec.getPath("/grant");
            Path accessTokenPath = authorizationSpec.getPath("/accessToken");

            Map<String, Path> paths = allSpecs.getPaths();
            paths.put("/gotapi/authorization/grant", grantPath);
            paths.put("/gotapi/authorization/accessToken", accessTokenPath);
            allSpecs.setPaths(paths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendServiceDiscovery(final Swagger allSpecs) {
        try {
            String resPath = getResoucePath("servicediscovery.json");
            Swagger serviceDiscoverySpec = getProfileSpec(resPath);
            Path path = serviceDiscoverySpec.getPath("/");
            Map<String, Object> example = findExample(path);

            // scopesプロパティの初期化
            List<String> scopes = new ArrayList<>();
            for (String key : profileSpecs.keySet()) {
                scopes.add(key);
            }
            scopes.add("availability");
            scopes.add("authorization");
            scopes.add("serviceDiscovery");
            scopes.add("serviceInformation");
            Collections.sort(scopes);
            Map<String, Object> service = (Map<String, Object>) ((List<Object>) example.get("services")).get(0);
            service.put("scopes", scopes);

            Map<String, Path> paths = allSpecs.getPaths();
            paths.put("/gotapi/serviceDiscovery", path);
            allSpecs.setPaths(paths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendServiceInformation(final Swagger allSpecs) {
        try {
            String resPath = getResoucePath("serviceinformation.json");
            Swagger serviceInformationSpec = getProfileSpec(resPath);
            Path path = serviceInformationSpec.getPath("/");
            Map<String, Object> example = findExample(path);

            // supportsプロパティの初期化
            List<String> supports = new ArrayList<>();
            for (String key : profileSpecs.keySet()) {
                supports.add(key);
            }
            supports.add("availability");
            supports.add("authorization");
            supports.add("serviceDiscovery");
            supports.add("serviceInformation");
            Collections.sort(supports);
            example.put("supports", supports);

            // supportsApiプロパティの初期化
            Map<String, Object> supportApis = new LinkedHashMap<>();
            for (String profileName : supports) {
                Swagger filtered = filterSwaggerWithProfileName(allSpecs, profileName);
                supportApis.put(profileName, convertToMap(filtered));
            }
            example.put("supportApis", supportApis);

            Map<String, Path> paths = allSpecs.getPaths();
            paths.put("/gotapi/serviceInformation", path);
            allSpecs.setPaths(paths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Swagger filterSwaggerWithProfileName(final Swagger swagger, final String profileName) throws IOException {
        try {
            LOGGER.debug("filterSwaggerWithProfileName: " + profileName);

            Swagger filtered = SwaggerUtils.cloneSwagger(swagger);
            Map<String, Path> paths = filtered.getPaths();
            String basePath = swagger.getBasePath();

            // 不要なAPI定義を削除する.
            for (Iterator<Map.Entry<String, Path>> it = paths.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Path> entry = it.next();
                LOGGER.debug("Filter API: basePath=" + basePath + ", path=" + entry.getKey());
                DConnectPath otherPath = DConnectPath.parsePath(basePath, entry.getKey());
                if (!otherPath.getProfileName().equals(profileName)) {
                    it.remove();
                }
            }

            // 不要なModel定義を削除する.
            deleteUnusedDefinitions(filtered);

            return filtered;
        } catch (IllegalPathFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteUnusedDefinitions(final Swagger swagger) {
        Map<String, Model> definitions = swagger.getDefinitions();
        if (definitions != null) {
            List<String> used = new LinkedList<>();

            Map<String, Path> paths = swagger.getPaths();
            if (paths != null) {
                for (Map.Entry<String, Path> path : paths.entrySet()) {
                    Map<HttpMethod, Operation> operations = path.getValue().getOperationMap();
                    if (operations != null) {
                        for (Map.Entry<HttpMethod, Operation> operation : operations.entrySet()) {
                            Map<String, Response> responses = operation.getValue().getResponses();
                            if (responses != null) {
                                for (Map.Entry<String, Response> response : responses.entrySet()) {
                                    Property schema = response.getValue().getSchema();
                                    // レスポンス定義
                                    collectReferencesFromProperty(schema, used);
                                }
                            }

                            Map<String, Object> extensions = operation.getValue().getVendorExtensions();
                            if (extensions != null) {
                                Object typeObj = extensions.get("x-type");
                                if (typeObj != null && typeObj.equals("event")) {
                                    Object eventObj = extensions.get("x-event");
                                    if (eventObj != null && eventObj instanceof Map) {
                                        Map<String, Object> event = (Map<String, Object>) eventObj;
                                        Object schemaObj = event.get("schema");
                                        if (schemaObj != null && schemaObj instanceof Map) {
                                            // イベント定義
                                            Map<String, Object> schema = (Map<String, Object>) schemaObj;
                                            Object refObj = schema.get("$ref");
                                            if (refObj instanceof String && !used.contains(refObj)) {
                                                String ref = (String) refObj;
                                                String prefix = "#/definitions/";
                                                if (ref.startsWith(prefix)) {
                                                    ref = ref.substring(prefix.length());
                                                    used.add(ref);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            for (int i = 0; i < used.size(); i++) {
                String key = used.get(i);
                collectReferencesFromModel(definitions.get(key), used);
            }

            List<String> unused = new LinkedList<>(definitions.keySet());
            unused.removeAll(used);
            for (String key : unused) {
                LOGGER.debug("Unused: " + key);
                definitions.remove(key);
            }
        }
    }

    private static void collectReferencesFromProperty(final Property property,
                                                      final List<String> jsonPointers) {
        LOGGER.debug("Property: Class=" + property.getClass());
        if (property instanceof RefProperty) {
            RefProperty ref = (RefProperty) property;
            String jsonPointer = ref.getSimpleRef();
            if (!jsonPointers.contains(jsonPointer)) {
                jsonPointers.add(jsonPointer);
            }
        } else {
            // TODO オブジェクトプロパティもトレースする.
        }
    }

    private static void collectReferencesFromModel(final Model model,
                                                   final List<String> jsonPointers) {
        LOGGER.debug("Model: Title=" + model.getTitle() + ", Class=" + model.getClass());
        if (model instanceof ComposedModel) {
            ComposedModel composed = (ComposedModel) model;
            for (Model m : composed.getAllOf()) {
                collectReferencesFromModel(m, jsonPointers);
            }
        } else if (model instanceof RefModel) {
            RefModel ref = (RefModel) model;
            String jsonPointer = ref.getSimpleRef();
            if (!jsonPointers.contains(jsonPointer)) {
                jsonPointers.add(jsonPointer);
            }
        } else {
            // TODO オブジェクトプロパティもトレースする.
        }
    }

    private Map<String, Object> convertToMap(final Swagger swagger) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonAsString = mapper.writeValueAsString(swagger);
        JsonNode json = mapper.readTree(jsonAsString);
        return mapper.convertValue(json, Map.class);
    }

    private Map<String, Object> findExample(final Path root) {
        return (Map<String, Object>) root.getGet().getResponses().get("200").getExamples().get("application/json");
    }

    private String getResoucePath(final String name) {
        return "/" + getName() + "/" + name;
    }

    private Swagger getProfileSpec(final String resPath) throws IOException {
        InputStream in = getClass().getResourceAsStream(resPath);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(in);
        return new SwaggerParser().read(root);
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        Swagger swagger = getOriginalSwagger();
        if (swagger != null) {
            modifySwagger(swagger);

            try {
                SimpleModule module = new SimpleModule();
                module.addSerializer(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public void serialize(Double val, JsonGenerator jgen,
                                          SerializerProvider provider) throws IOException, JsonProcessingException {
                        jgen.writeNumber(new BigDecimal(val));
                    }
                });
                objs.put("swagger-yaml", Yaml.mapper().registerModule(module).writeValueAsString(swagger));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        for (Map<String, Object> operations : getOperations(objs)) {
            @SuppressWarnings("unchecked")
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");

            List<Map<String, Object>> opsByPathList = sortOperationsByPath(ops);
            operations.put("operationsByPath", opsByPathList);
        }
        return super.postProcessSupportingFileData(objs);
    }

    @Override
    public String removeNonNameElementToCamelCase(String name) {
        return removeNonNameElementToCamelCase(name, "[-:;#]");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public String getDefaultDisplayName() {
        return "DeviceConnectEmulator";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "deviceConnectEmulator";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public Map<String, Swagger> getProfileSpecs() {
        return this.profileSpecs;
    }

    @Override
    public void setProfileSpecs(final Map<String, Swagger> profileSpecs) {
        this.profileSpecs = profileSpecs;
    }
}
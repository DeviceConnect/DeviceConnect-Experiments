package org.deviceconnect.codegen.server;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.sun.org.apache.xml.internal.utils.Hashtree2Node;
import io.swagger.codegen.*;
import io.swagger.models.*;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Yaml;
import org.deviceconnect.codegen.DConnectCodegenConfig;

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
public class EmulatorCodegenConfig extends DefaultCodegen implements DConnectCodegenConfig {

    private Map<String, Swagger> profileSpecs;

    protected String apiVersion = "1.0.0";
    protected int serverPort = 4035;
    protected String projectName = "swagger-server";


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
            operationsForThisPath.get(operationsForThisPath.size() - 1).hasMore = null;
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

        appendAvailability(swagger);
        appendAuthorization(swagger);
        appendServiceDiscovery(swagger);
        appendServiceInformation(swagger);
        checkEvents(swagger);

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
                                JsonNode event = (JsonNode) operation.getVendorExtensions().get("x-event");
                                if (event != null) {
                                    JsonNode examples = event.get("examples");
                                    if (examples != null) {
                                        JsonNode json = examples.get("application/json");
                                        if (json != null) {
                                            final String eventJson = mapper.writeValueAsString(json);
                                            eventList.add(new Object() {
                                                String key = (basePath + pathname).toLowerCase();
                                                String json = eventJson;
                                            });
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
            paths.put("/availability", path);
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
            paths.put("/authorization/grant", grantPath);
            paths.put("/authorization/accessToken", accessTokenPath);
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
            Collections.sort(scopes);
            Map<String, Object> service = (Map<String, Object>) ((List<Object>) example.get("services")).get(0);
            service.put("scopes", scopes);

            Map<String, Path> paths = allSpecs.getPaths();
            paths.put("/serviceDiscovery", path);
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
            Collections.sort(supports);
            example.put("supports", supports);

            // supportsApiプロパティの初期化
            Map<String, Object> supportApis = new LinkedHashMap<>();
            for (String profileName : supports) {
                supportApis.put(profileName, convertToMap(profileSpecs.get(profileName)));
            }
            example.put("supportApis", supportApis);

            Map<String, Path> paths = allSpecs.getPaths();
            paths.put("/serviceInformation", path);
            allSpecs.setPaths(paths);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        Swagger swagger = (Swagger)objs.get("swagger");
        if(swagger != null) {
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

/*
 AbstractPluginCodegenConfig.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.plugin;


import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.deviceconnect.codegen.AbstractCodegenConfig;
import org.deviceconnect.codegen.DConnectPath;
import org.deviceconnect.codegen.IllegalPathFormatException;
import org.deviceconnect.codegen.ProfileTemplate;
import org.deviceconnect.codegen.models.DConnectOperation;
import org.deviceconnect.codegen.util.JsonStringifyPrettyPrinter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class AbstractPluginCodegenConfig extends AbstractCodegenConfig {

    private final String[] standardProfileClassNames;

    private Map<String, Swagger> profileSpecs;

    protected AbstractPluginCodegenConfig() {
        standardProfileClassNames = loadStandardProfileNames();
        additionalProperties.put("supportedProfileClasses", new ArrayList<>());
    }

    public String getDefaultPackageName() {
        return null;
    }

    @Override
    public String getDefaultDisplayName() {
        return "MyPlugin";
    }

    protected String loadResourceFile(final String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream in = classLoader.getResourceAsStream(fileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) > 0) {
            baos.write(buf, 0, len);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    protected String[] loadStandardProfileNames() {
        try {
            String resource = loadResourceFile("standardProfiles");
            return resource.split("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getStandardClassName(final String profileName) {
        for (String standardName : standardProfileClassNames) {
            if (standardName.equalsIgnoreCase(profileName)) {
                return standardName;
            }
        }
        return null;
    }

    protected String getClassPrefix() {
        return (String) additionalProperties.get("classPrefix");
    }

    @Override
    public Map<String, Swagger> getProfileSpecs() {
        return this.profileSpecs;
    }

    @Override
    public void setProfileSpecs(final Map<String, Swagger> profileSpecs) {
        this.profileSpecs = profileSpecs;
    }

    @Override
    public void preprocessSwagger(final Swagger swagger) {
        Map<String, Map<String, Object>> profiles = new LinkedHashMap<>();

        for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            Path path = pathEntry.getValue();
            DConnectPath dConnectPath;
            try {
                dConnectPath = DConnectPath.parsePath(swagger.getBasePath(), pathEntry.getKey());
            } catch (IllegalPathFormatException e) {
                continue;
            }

            final String profileName = dConnectPath.getProfileName();
            Map<String, Object> profile = profiles.get(profileName);
            if (profile == null) {
                profile = new HashMap<>();
                profile.putAll(additionalProperties);
                profile.put("apiList", new ArrayList<Map<String, Object>>());
                profiles.put(profileName, profile);
            }
            List<Map<String, Object>> apiList = (List<Map<String, Object>>) profile.get("apiList");

            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                HttpMethod method = operationEntry.getKey();
                DConnectOperation operation = DConnectOperation.parse(swagger, operationEntry.getValue());

                final Map<String, Object> api = new HashMap<>();
                final String interfaceName = dConnectPath.getInterfaceName();
                final String attributeName = dConnectPath.getAttributeName();
                final String apiId = createApiIdentifier(method, dConnectPath);

                api.put("method", method.name().toLowerCase());
                api.put("interface", interfaceName);
                api.put("attribute", attributeName);
                api.put("apiPath", dConnectPath.getSubPath()); // プロファイル名以下のパス
                api.put("apiFullPath", dConnectPath.getPath()); // フルパス
                api.put("apiId", apiId);

                switch (method) {
                    case GET:
                        api.put("getApi", true);
                        profile.put("hasGetApi", true);
                        break;
                    case POST:
                        api.put("postApi", true);
                        profile.put("hasPostApi", true);
                        break;
                    case PUT:
                        api.put("putApi", true);
                        profile.put("hasPutApi", true);
                        break;
                    case DELETE:
                        api.put("deleteApi", true);
                        profile.put("hasDeleteApi", true);
                        break;
                }

                switch (operation.getType()) {
                    case ONE_SHOT:
                        api.put("isOneShotApi", true);
                        profile.put("hasOneShotApi", true);

                        // Response data creation
                        for (Map.Entry<String, Response> entity : operation.getResponses().entrySet()) {
                            if ("200".equals(entity.getKey())) { // HTTP Code
                                api.put("responses", getResponseCreation(swagger, entity.getValue()));
                                break;
                            }
                        }
                        break;
                    case EVENT:
                        api.put("isEventApi", true);
                        profile.put("hasEventApi", true);

                        // Event data creation
                        if (method == HttpMethod.PUT) {
                            Response event = operation.getEventModel();
                            if (event != null) {
                                api.put("events", getEventCreation(swagger, event));
                            }
                        }
                        break;
                    case STREAMING:
                        api.put("isStreamingApi", true);
                        profile.put("hasStreamingApi", true);
                        break;
                }

                // Function Name
                String functionName = method.name().toLowerCase()
                        + toUpperCapital(profileName, false);
                if (interfaceName != null) {
                    functionName += toUpperCapital(interfaceName, false);
                }
                if (attributeName != null) {
                    functionName += toUpperCapital(attributeName, false);
                }
                api.put("functionName", functionName);

                // Parameter declarations
                List<Object> paramList = new ArrayList<>();
                for (final Parameter param : operation.getParameters()) {
                    paramList.add(new Object() {
                        String declaration = getDeclaration(param);
                    });
                }
                api.put("paramList", paramList);
                apiList.add(api);

                LOGGER.info("Parsed path: profile = " + profileName + ", interface = " + interfaceName + ", attribute = " + attributeName);
            }
            for (Iterator<Map<String, Object>> it = apiList.iterator(); it.hasNext(); ) {
                it.next().put("hasNext", it.hasNext());
            }
        }


        // 各プロファイルのスケルトンコード生成
        List<Object> supportedProfileNames = new ArrayList<>();
        List<Map<String, Object>> profileList = new ArrayList<>();
        for (final Iterator<Map.Entry<String, Map<String, Object>>> it = profiles.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<String, Map<String, Object>> entry = it.next();
            final String profileName = entry.getKey();
            final Map<String, Object> profile = entry.getValue();

            try {
                List<ProfileTemplate> profileTemplates = prepareProfileTemplates(profileName, profile);
                if (profileTemplates != null) {
                    for (ProfileTemplate template : profileTemplates) {
                        generateProfile(template, profile);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate profile source code: profile = " + profileName, e);
            }

            supportedProfileNames.add(new Object() {
                String name = profileName;
                String id = profileName.toLowerCase();
                boolean hasNext = it.hasNext();
            });
            profileList.add(profile);
        }
        additionalProperties.put("supportedProfileNames", supportedProfileNames);
        additionalProperties.put("profileList", profileList);

        // プロファイル定義ファイルのコピー
        try {
            copyProfileSpecFiles();
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy profile spec file.", e);
        }
    }

    protected abstract String getDeclaration(Parameter p);

    protected abstract List<String> getResponseCreation(Swagger swagger, Response response);

    protected abstract List<String> getEventCreation(Swagger swagger, Response event);

    protected abstract List<ProfileTemplate> prepareProfileTemplates(String profileName, Map<String, Object> properties);

    private static String createApiIdentifier(HttpMethod method, DConnectPath path) {
        return method.name() + " " + path.getPath();
    }

    protected abstract String getProfileSpecFolder();

    private void copyProfileSpecFiles() throws IOException {
        String dirPath = getProfileSpecFolder();
        if (dirPath == null) {
            return;
        }
        File dir = new File(dirPath);
        if (!dir.mkdirs()) {
            throw new IOException("Failed to copy profile spec directory: " + dirPath);
        }
        if (profileSpecs != null) {
            ObjectWriter writer = createJsonWriter();
            for (Map.Entry<String, Swagger> spec : profileSpecs.entrySet()) {
                String fileName = spec.getKey() + ".json";
                String content = writer.writeValueAsString(spec.getValue());
                File destination = new File(getProfileSpecFolder(), fileName);
                LOGGER.info("Output profile spec file: " + destination.getAbsolutePath());
                writeFile(content, destination);
            }
        }
    }

    private static ObjectWriter createJsonWriter() {
        return Json.mapper().writer(new JsonStringifyPrettyPrinter(4));
    }

    protected Model findDefinition(final Swagger swagger, final String simpleRef) {
        Map<String, Model> definitions = swagger.getDefinitions();
        if (definitions == null) {
            return null;
        }
        return definitions.get(simpleRef);
    }

    protected boolean isIgnoredDefinition(final String refName) {
        return "CommonResponse".equals(refName) || "CommonEvent".equals(refName);
    }

    protected Map<String, Property> getProperties(final Swagger swagger, final ComposedModel parent) {
        Map<String, Property> result = new HashMap<>();
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
                    if (isIgnoredDefinition(refName)) {
                        continue;
                    }
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
}
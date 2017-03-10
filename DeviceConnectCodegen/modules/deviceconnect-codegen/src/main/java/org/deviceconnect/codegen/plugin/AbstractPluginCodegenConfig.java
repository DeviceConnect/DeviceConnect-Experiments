package org.deviceconnect.codegen.plugin;


import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.deviceconnect.codegen.DConnectCodegenConfig;
import org.deviceconnect.codegen.models.DConnectOperation;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractPluginCodegenConfig extends DefaultCodegen implements DConnectCodegenConfig {

    private final String[] standardProfileClassNames;

    private Map<String, Swagger> profileSpecs;

    protected AbstractPluginCodegenConfig() {
        standardProfileClassNames = loadStandardProfileNames();
        additionalProperties.put("supportedProfileClasses", new ArrayList<>());
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
            String pathName = pathEntry.getKey();
            Path path = pathEntry.getValue();

            String profileName = getProfileNameFromPath(pathName);
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

                Map<String, Object> api = new HashMap<>();
                String interfaceName = getInterfaceNameFromPath(pathName);
                String attributeName = getAttributeNameFromPath(pathName);
                String apiPath = createApiPath(interfaceName, attributeName);
                String apiFullPath = createApiFullPath(swagger, profileName, interfaceName, attributeName);
                String apiId = createApiIdentifier(swagger, method, profileName, interfaceName, attributeName);

                api.put("method", method.name().toLowerCase());
                api.put("interface", interfaceName);
                api.put("attribute", attributeName);
                api.put("apiPath", apiPath); // プロファイル名以下のパス
                api.put("apiFullPath", apiFullPath); // フルパス
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
        }
        additionalProperties.put("supportedProfileNames", supportedProfileNames);

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

    protected abstract String profileFileFolder();

    protected abstract List<ProfileTemplate> prepareProfileTemplates(String profileName, Map<String, Object> properties);

    private void generateProfile(ProfileTemplate template, Map<String, Object> properties) throws IOException {
        String templateFile = getFullTemplateFile(this, template.templateFile);
        Template tmpl = Mustache.compiler()
                .withLoader(new Mustache.TemplateLoader() {
                    @Override
                    public Reader getTemplate(String name) {
                        return getTemplateReader(getFullTemplateFile(AbstractPluginCodegenConfig.this, name + ".mustache"));
                    }
                })
                .defaultValue("")
                .compile(readTemplate(templateFile));

        String outputFileName = profileFileFolder() + File.separator + template.outputFile;
        writeToFile(outputFileName, tmpl.execute(properties));
    }

    private static String getProfileNameFromPath(String path) {
        String[] array = path.split("/");
        if (array.length < 2) {
            return null;
        }
        return array[1];
    }

    private static String getInterfaceNameFromPath(String path) {
        String[] array = path.split("/");
        if (array.length == 4) {  // '', '<profile>', '<interface>', '<attribute>'
            return array[2];
        }
        return null;
    }

    private static String getAttributeNameFromPath(String path) {
        String[] array = path.split("/");
        if (array.length == 4) { // '', '<profile>', '<interface>', '<attribute>'
            return array[3];
        }
        if (array.length == 3) { // '', '<profile>', '<attribute>'
            return array[2];
        }
        return null;
    }

    private static String createApiPath(String interfaceName, String attributeName) {
        String path = "/";
        if (interfaceName != null) {
            path += interfaceName + "/";
        }
        if (attributeName != null) {
            path += attributeName;
        }
        return path;
    }

    private static String createApiFullPath(Swagger swagger, String profileName,
                                            String interfaceName, String attributeName) {
        String basePath = swagger.getBasePath();
        String path = (basePath == null) ? "" : basePath;
        path += "/" + profileName;
        if (interfaceName != null) {
            path += "/" + interfaceName;
        }
        if (attributeName != null) {
            path += "/" + attributeName;
        }
        return path;
    }

    private static String createApiIdentifier(Swagger swagger, HttpMethod method, String profileName,
                                              String interfaceName, String attributeName) {
        return method.name() + " " + createApiFullPath(swagger, profileName, interfaceName, attributeName);
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
            for (Map.Entry<String, Swagger> spec : profileSpecs.entrySet()) {
                String fileName = spec.getKey() + ".json";
                String content = Json.mapper().writeValueAsString(spec.getValue());
                writeFile(content, new File(getProfileSpecFolder(), fileName));
            }
        }
    }

    private void writeFile(final String source, final File destination) throws IOException {
        if (destination.exists()) {
            throw new IOException("Profile Spec File is already created: " + destination.getAbsolutePath());
        }
        if (!destination.createNewFile()) {
            throw new IOException("Failed to create Profile Spec File: " + destination.getAbsolutePath());
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destination);
            out.write(source.getBytes());
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("static-method")
    private File writeToFile(String filename, String contents) throws IOException {
        LOGGER.info("writing file " + filename);
        File output = new File(filename);

        if (output.getParent() != null && !new File(output.getParent()).exists()) {
            File parent = new File(output.getParent());
            parent.mkdirs();
        }
        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(output), "UTF-8"));

        out.write(contents);
        out.close();
        return output;
    }

    private String readTemplate(String name) {
        try {
            Reader reader = getTemplateReader(name);
            if (reader == null) {
                throw new RuntimeException("no file found");
            }
            Scanner s = new Scanner(reader).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        throw new RuntimeException("can't load template " + name);
    }

    private Reader getTemplateReader(String name) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(getCPResourcePath(name));
            if (is == null) {
                is = new FileInputStream(new File(name)); // May throw but never return a null value
            }
            return new InputStreamReader(is);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        throw new RuntimeException("can't load template " + name);
    }

    /**
     * Get the template file path with template dir prepended, and use the
     * library template if exists.
     *
     * @param config Codegen config
     * @param templateFile Template file
     * @return String Full template file path
     */
    private String getFullTemplateFile(CodegenConfig config, String templateFile) {
        String template = config.templateDir() + File.separator + templateFile;
        if (new File(template).exists()) {
            return template;
        } else {
            String library = config.getLibrary();
            if (library != null && !"".equals(library)) {
                String libTemplateFile = config.embeddedTemplateDir() + File.separator +
                        "libraries" + File.separator + library + File.separator +
                        templateFile;
                if (embeddedTemplateExists(libTemplateFile)) {
                    // Fall back to the template file embedded/packaged in the JAR file...
                    return libTemplateFile;
                }
            }
            // Fall back to the template file embedded/packaged in the JAR file...
            return config.embeddedTemplateDir() + File.separator + templateFile;
        }
    }

    private String readResourceContents(String resourceFilePath) {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(this.getClass().getResourceAsStream(getCPResourcePath(resourceFilePath)), "UTF-8");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private boolean embeddedTemplateExists(String name) {
        return this.getClass().getClassLoader().getResource(getCPResourcePath(name)) != null;
    }

    @SuppressWarnings("static-method")
    private String getCPResourcePath(String name) {
        if (!"/".equals(File.separator)) {
            return name.replaceAll(Pattern.quote(File.separator), "/");
        }
        return name;
    }

    protected static String toUpperCapital(String str) {
        StringBuffer buf = new StringBuffer(str.length());
        buf.append(str.substring(0, 1).toUpperCase());
        buf.append(str.substring(1).toLowerCase());
        return buf.toString();
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

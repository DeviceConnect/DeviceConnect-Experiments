package org.deviceconnect.codegen.languages;


import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractCodegenConfig extends DefaultCodegen implements CodegenConfig {

    @Override
    public void preprocessSwagger(Swagger swagger) {
        Map<String, Map<String, Object>> profiles = new HashMap<>();
        for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            String pathName = pathEntry.getKey();
            Path path = pathEntry.getValue();

            String profileName = getProfileNameFromPath(pathName);
            Map<String, Object> profile = profiles.get(profileName);
            if (profile == null) {
                profile = new HashMap<>();
                profile.put("apiList", new ArrayList<Map<String, Object>>());
                profiles.put(profileName, profile);
            }
            List<Map<String, Object>> apiList = (List<Map<String, Object>>) profile.get("apiList");

            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                HttpMethod method = operationEntry.getKey();

                Map<String, Object> api = new HashMap<>();
                String interfaceName = getInterfaceNameFromPath(pathName);
                String attributeName = getAttributeNameFromPath(pathName);
                api.put("interface", interfaceName);
                api.put("attribute", attributeName);
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
                apiList.add(api);

                LOGGER.info("Parsed path: profile = " + profileName + ", interface = " + interfaceName + ", attribute = " + attributeName);
            }
            postprocessProfiles();
        }

        // 各プロファイルのスケルトンコード生成
        for (Map.Entry<String, Map<String, Object>> entry : profiles.entrySet()) {
            String profileName = entry.getKey();
            Map<String, Object> profile = entry.getValue();
            try {
                preprocessProfile(profileName, profile);
                generateProfile(profileName, profile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate profile source code: profile = " + profileName, e);
            }
        }
    }

    protected abstract String profileTemplateFile(String profileName);

    protected abstract String profileFile(String profileName);

    protected abstract String profileFileFolder();

    protected void preprocessProfile(String profileName, Map<String, Object> properties) {}

    protected void postprocessProfiles() {}

    private void generateProfile(String profileName, Map<String, Object> properties) throws IOException {
        String templateFile = getFullTemplateFile(this, profileTemplateFile(profileName));
        Template tmpl = Mustache.compiler()
                .withLoader(new Mustache.TemplateLoader() {
                    @Override
                    public Reader getTemplate(String name) {
                        return getTemplateReader(getFullTemplateFile(AbstractCodegenConfig.this, name + ".mustache"));
                    }
                })
                .defaultValue("")
                .compile(readTemplate(templateFile));

        String outputFileName = profileFileFolder() + File.separator + profileFile(profileName);
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
}

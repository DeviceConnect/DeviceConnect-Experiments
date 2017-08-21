package org.deviceconnect.codegen;


import config.Config;
import config.ConfigParser;
import io.swagger.codegen.*;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.cli.*;
import org.deviceconnect.codegen.app.HtmlAppCodegenConfig;
import org.deviceconnect.codegen.docs.HtmlDocsCodegenConfig;
import org.deviceconnect.codegen.docs.MarkdownDocsCodegenConfig;
import org.deviceconnect.codegen.plugin.AndroidPluginCodegenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class DConnectCodegen {

    private static final Logger LOGGER = LoggerFactory.getLogger(Codegen.class);
    private static final String[] PROHIBITED_PROFILES = {
            "availability",
            "authorization",
            "serviceDiscovery",
            "serviceInformation",
            "system"
    };
    private static final String[] RESERVED_NAMES = {
            "files",
            "get",
            "put",
            "post",
            "delete"
    };

    static Map<String, DConnectCodegenConfig> configs = new HashMap<String, DConnectCodegenConfig>();
    static String configString;
    static String debugInfoOptions = "\nThe following additional debug options are available for all codegen targets:" +
            "\n -DdebugSwagger prints the swagger specification as interpreted by the codegen" +
            "\n -DdebugModels prints models passed to the template engine" +
            "\n -DdebugOperations prints operations passed to the template engine" +
            "\n -DdebugSupportingFiles prints additional data passed to the template engine";

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("h", "help", false, "shows this message");
        options.addOption("l", "lang", true, "client language to generate.\nAvailable languages include:\n\t[" + configString + "]");
        options.addOption("o", "output", true, "where to write the generated files");
        options.addOption("i", "input-spec", true, "location of the swagger spec, as URL or file");
        options.addOption("s", "input-spec-dir", true, "directory of the swagger specs");
        options.addOption("t", "template-dir", true, "folder containing the template files");
        options.addOption("d", "debug-info", false, "prints additional info for debugging");
        //options.addOption("a", "auth", true, "adds authorization headers when fetching the swagger definitions remotely. Pass in a URL-encoded string of name:header with a comma separating multiple values");
        options.addOption("c", "config", true, "location of the configuration file");
        options.addOption("p", "package-name", true, "package name (for deviceConnectAndroidPlugin only)");
        options.addOption("n", "display-name", true, "display name of the generated project");
        options.addOption("x", "class-prefix", true, "prefix of each generated class that implements a device connect profile");
        options.addOption("b", "connection-type", true, "connection type with device connect manager (for deviceConnectAndroidPlugin only)");

        ClientOptInput clientOptInput = new ClientOptInput();
        ClientOpts clientOpts = new ClientOpts();
        File[] specFiles;

        CommandLine cmd;
        try {
            CommandLineParser parser = new BasicParser();
            DConnectCodegenConfig config;

            cmd = parser.parse(options, args);
            if (cmd.hasOption("d")) {
                usage(options);
                System.out.println(debugInfoOptions);
                return;
            }
            if (cmd.hasOption("h")) {
                config = getConfig(cmd.getOptionValue("l"));
                if (config != null) {
                    options.addOption("h", "help", true, config.getHelp());
                    usage(options);
                    return;
                }
                usage(options);
                return;
            }
            if (cmd.hasOption("l")) {
                config = getConfig(cmd.getOptionValue("l"));
                clientOptInput.setConfig(config);
            } else {
                usage(options);
                return;
            }
            if (cmd.hasOption("o")) {
                config.setOutputDir(cmd.getOptionValue("o"));
            }
            if (cmd.hasOption("i")) {
                String location = cmd.getOptionValue("i");
                Swagger swagger = new SwaggerParser().read(location, clientOptInput.getAuthorizationValues(), true);
                clientOptInput.swagger(swagger);

                String basePath = swagger.getBasePath();
                if (basePath == null || basePath.equals("")) {
                    basePath = "/";
                    swagger.setBasePath(basePath);
                }
                Map<String, Swagger> profiles = new HashMap<>();
                Map<String, Path> paths = swagger.getPaths();
                for (Map.Entry<String, Path> entry : paths.entrySet()) {
                    Path path = entry.getValue();
                    String pathName = entry.getKey();
                    String fullPathName = basePath.equals("/") ? pathName : basePath + pathName;
                    String[] parts = fullPathName.split("/");
                    if (parts.length < 3) {
                        continue;
                    }
                    String apiPart = parts[1];
                    String profilePart = parts[2];
                    String subPath = "/";
                    for (int i = 3; i < parts.length; i++) {
                        subPath += parts[i];
                        if (i < parts.length - 1) {
                            subPath += "/";
                        }
                    }
                    checkProfileName(config, profilePart);

                    Swagger profile = profiles.get(profilePart);
                    if (profile == null) {
                        profile = createProfileSpec(swagger);
                        profile.setBasePath("/" + apiPart + "/" + profilePart);
                        profiles.put(profilePart, profile);
                    }
                    Map<String, Path> subPaths = profile.getPaths();
                    subPaths.put(subPath, path);
                    profile.setPaths(subPaths);
                }
                config.setProfileSpecs(profiles);
            } else if (cmd.hasOption("s")) {
                File dir = new File(cmd.getOptionValue("s"));
                if (dir.isDirectory()) {
                    specFiles = dir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            return name.endsWith(".json") || name.endsWith(".yaml");
                        }
                    });

                    Map<String, Swagger> profileSpecs = new HashMap<>();
                    for (File file : specFiles) {
                        Swagger swagger = new SwaggerParser().read(file.getAbsolutePath(), clientOptInput.getAuthorizationValues(), true);
                        String profileName = parseProfileName(swagger);
                        if (profileName == null) {
                            profileName = parseProfileNameFromFileName(file.getName());
                        }
                        checkProfileName(config, profileName);
                        profileSpecs.put(profileName, swagger);
                    }
                    config.setProfileSpecs(profileSpecs);
                    clientOptInput.swagger(mergeSwaggers(profileSpecs));
                } else {
                    usage(options);
                    return;
                }
            } else {
                usage(options);
                return;
            }
            if (cmd.hasOption("c")) {
                String configFile = cmd.getOptionValue("c");
                Config genConfig = ConfigParser.read(configFile);
                if (null != genConfig && null != config) {
                    for (CliOption langCliOption : config.cliOptions()) {
                        if (genConfig.hasOption(langCliOption.getOpt())) {
                            config.additionalProperties().put(langCliOption.getOpt(), genConfig.getOption(langCliOption.getOpt()));
                        }
                    }
                }
            }
            if (cmd.hasOption("t")) {
                clientOpts.getProperties().put(CodegenConstants.TEMPLATE_DIR, String.valueOf(cmd.getOptionValue("t")));
            }

            String displayName;
            if (cmd.hasOption("n")) {
                displayName = cmd.getOptionValue("n");
            } else {
                displayName = config.getDefaultDisplayName();
            }
            clientOpts.getProperties().put("displayName", displayName);

            String classPrefix;
            if (cmd.hasOption("x")) {
                classPrefix = cmd.getOptionValue("x");
            } else {
                classPrefix = "My";
            }
            clientOpts.getProperties().put("classPrefix", classPrefix);

            ValidationResultSet resultSet = config.validateOptions(cmd, clientOpts);
            if (!resultSet.isValid()) {
                for (ValidationResult result : resultSet.getResults().values()) {
                    if (!result.isValid()) {
                        LOGGER.error(result.getParamName() + " is invalid; " + result.getErrorMessage());
                    }
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            usage(options);
            return;
        }
        try {
            new Codegen().opts(clientOptInput.opts(clientOpts)).generate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static Swagger createProfileSpec(final Swagger swagger) {
        Swagger profile = new Swagger();
        profile.setSwagger(swagger.getSwagger());
        Info info = new Info();
        info.setTitle(swagger.getInfo().getTitle());
        info.setVersion(swagger.getInfo().getVersion());
        profile.setInfo(info);
        profile.setConsumes(swagger.getConsumes());
        profile.setExternalDocs(swagger.getExternalDocs());
        profile.setHost(swagger.getHost());
        profile.setParameters(swagger.getParameters());
        profile.setResponses(swagger.getResponses());
        profile.setProduces(swagger.getProduces());
        profile.setSecurity(swagger.getSecurity());
        profile.setSecurityDefinitions(swagger.getSecurityDefinitions());
        profile.setSchemes(swagger.getSchemes());
        profile.setTags(swagger.getTags());
        Map<String, Object> extensions = swagger.getVendorExtensions();
        if (extensions != null) {
            for (Map.Entry<String, Object> entry : extensions.entrySet()) {
                profile.setVendorExtension(entry.getKey(), entry.getValue());
            }
        }
        profile.setDefinitions(swagger.getDefinitions());
        profile.setPaths(new HashMap<String, Path>());
        return profile;
    }

    private static void checkProfileName(final DConnectCodegenConfig config, final String profileName) {
        if (config instanceof HtmlAppCodegenConfig || config instanceof HtmlDocsCodegenConfig ||
            config instanceof MarkdownDocsCodegenConfig) {
            return;
        }

        // プロファイル名が予約語の場合は異常終了
        if (isReservedName(profileName)) {
            exitOnError("次の名前は予約語のためプロファイル名として使用できません: " + concat(RESERVED_NAMES));
        }
        // プロファイル名が基本プロファイル名の場合は異常終了
        if (isProhibitedProfile(profileName)) {
            exitOnError("次のプロファイルは基本プロファイルのため入力できません: " + concat(PROHIBITED_PROFILES));
        }
    }

    private static void exitOnError(final String message) {
        System.err.println(message);
        System.exit(1);
    }

    private static String concat(final String[] array) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(array[i]);
        }
        return result.toString();
    }

    private static boolean isProhibitedProfile(final String profileName) {
        for (String prohibited : PROHIBITED_PROFILES) {
            if (profileName.equalsIgnoreCase(prohibited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReservedName(final String name) {
        for (String reserved : RESERVED_NAMES) {
            if (name.equalsIgnoreCase(reserved)) {
                return true;
            }
        }
        return false;
    }

    private static String parseProfileName(final Swagger swagger) {
        String basePath = swagger.getBasePath();
        if (basePath == null) {
            return null;
        }
        String[] array = basePath.split("/");
        if (array.length < 3) {
            return null;
        }
        return array[2];
    }

    private static String parseProfileNameFromFileName(final String fileName) {
        if (!fileName.endsWith(".json")) {
            throw new IllegalArgumentException("JSON file is required.");
        }
        return fileName.substring(0, fileName.length() - ".json".length());
    }

    private static Swagger mergeSwaggers(Map<String, Swagger> swaggerMap) {
        Swagger merged = new Swagger();

        // info
        Info info = new Info();
        info.setTitle("Device Connect");
        info.setVersion("1.0.0");
        merged.setInfo(info);

        // paths
        Map<String, Path> paths = new HashMap<>();
        for (Map.Entry<String, Swagger> swagger : swaggerMap.entrySet()) {
            String profileName = swagger.getKey();
            for (Map.Entry<String, Path> subPath : swagger.getValue().getPaths().entrySet()) {
                paths.put("/" + profileName + subPath.getKey(), subPath.getValue());
            }
        }
        merged.paths(paths);

        // definitions
        Map<String, Model> definitions = new HashMap<>();
        for (Map.Entry<String, Swagger> swagger : swaggerMap.entrySet()) {
            if (swagger.getValue().getDefinitions() != null) {
                definitions.putAll(swagger.getValue().getDefinitions());
            }
        }
        merged.setDefinitions(definitions);

        return merged;
    }

    public static List<DConnectCodegenConfig> getExtensions() {
        ServiceLoader<DConnectCodegenConfig> loader = ServiceLoader.load(DConnectCodegenConfig.class);
        List<DConnectCodegenConfig> output = new ArrayList<DConnectCodegenConfig>();
        for (DConnectCodegenConfig aLoader : loader) {
            output.add(aLoader);
        }
        return output;
    }

    static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DConnectCodegen", options);
    }

    public static DConnectCodegenConfig getConfig(String name) {
        if (configs.containsKey(name)) {
            return configs.get(name);
        } else {
            // see if it's a class
            try {
                LOGGER.debug("loading class " + name);
                Class<?> customClass = Class.forName(name);
                LOGGER.debug("loaded");
                return (DConnectCodegenConfig) customClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("can't load class " + name);
            }
        }
    }

    static {
        List<DConnectCodegenConfig> extensions = getExtensions();
        StringBuilder sb = new StringBuilder();

        for (DConnectCodegenConfig config : extensions) {
            if (sb.toString().length() != 0) {
                sb.append(", ");
            }
            sb.append(config.getName());
            configs.put(config.getName(), config);
            configString = sb.toString();
        }
    }

}

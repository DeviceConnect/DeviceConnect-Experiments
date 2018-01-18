package org.deviceconnect.codegen;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
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
import org.deviceconnect.codegen.util.SwaggerJsonValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import static org.deviceconnect.codegen.Const.MESSAGES;

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

    static String debugInfoOptions = "\nThe following additional debug options are available for all codegen targets:" +
            "\n -DdebugSwagger prints the swagger specification as interpreted by the codegen" +
            "\n -DdebugModels prints models passed to the template engine" +
            "\n -DdebugOperations prints operations passed to the template engine" +
            "\n -DdebugSupportingFiles prints additional data passed to the template engine";

    private static final SwaggerJsonValidator JSON_VALIDATOR = new SwaggerJsonValidator();

    private static final MultipleSwaggerConverter SWAGGER_CONVERTER = new MultipleSwaggerConverter();

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        Options options = Const.OPTIONS;

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
                config = Const.getConfig(cmd.getOptionValue("l"));
                if (config != null) {
                    options.addOption("h", "help", true, config.getHelp());
                    usage(options);
                    return;
                }
                usage(options);
                return;
            }
            if (cmd.hasOption("l")) {
                config = Const.getConfig(cmd.getOptionValue("l"));
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
                if(!checkSwagger(new File(location))) {
                    return;
                }
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
                    List<Swagger> swaggerList = new ArrayList<>();
                    for (File file : specFiles) {
                        if(!checkSwagger(file)) {
                            continue;
                        }
                        Swagger swagger = new SwaggerParser().read(file.getAbsolutePath(), clientOptInput.getAuthorizationValues(), true);
                        if (swagger != null) {
                            swaggerList.add(swagger);
                        }
                    }
                    if (swaggerList.size() != specFiles.length) {
                        return;
                    }

                    Map<String, Swagger> profileSpecs = SWAGGER_CONVERTER.convert(swaggerList);
                    for (String profileName : profileSpecs.keySet()) {
                        checkProfileName(config, profileName);
                    }
                    config.setProfileSpecs(profileSpecs);
                    clientOptInput.swagger(mergeSwaggers(profileSpecs));
                } else {
                    // TODO エラーメッセージ詳細化: ディレクトリではなくファイルへのパスが指定されている.
                    usage(options);
                    return;
                }
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

            String gradlePluginVersion;
            if (cmd.hasOption("r")) {
                gradlePluginVersion = cmd.getOptionValue("r");
            } else {
                gradlePluginVersion = "3.0.0";
            }
            clientOpts.getProperties().put("gradlePluginVersion", gradlePluginVersion);

            ValidationResultSet resultSet = config.validateOptions(cmd, clientOpts);
            if (!resultSet.isValid()) {
                for (ValidationResult result : resultSet.getResults().values()) {
                    if (!result.isValid()) {
                        LOGGER.error(result.getParamName() + " is invalid; " + result.getErrorMessage());
                    }
                }
                return;
            }
        } catch (MissingOptionException e) {
            printError(Const.ErrorMessages.CommandOption.MISSING_OPTION.getMessage(e.getMissingOptions()));
            return;
        } catch (MissingArgumentException e) {
            printError(Const.ErrorMessages.CommandOption.MISSING_ARGUMENT.getMessage(e.getOption()));
            return;
        } catch (AlreadySelectedException e) {
            printError(Const.ErrorMessages.CommandOption.ALREADY_SELECTED_OPTION.getMessage(e.getOption()));
            return;
        } catch (UnrecognizedOptionException e) {
            printError(Const.ErrorMessages.CommandOption.UNDEFINED_OPTION.getMessage(e.getOption()));
            return;
        } catch (IllegalPathFormatException e) {
            String errorMessage;
            switch (e.getReason()) {
                case TOO_LONG:
                    errorMessage = Const.ErrorMessages.Path.TOO_LONG.getMessage(e.getPath());
                    break;
                case TOO_SHORT:
                    errorMessage = Const.ErrorMessages.Path.TOO_SHORT.getMessage(e.getPath());
                    break;
                case NOT_STARTED_WITH_ROOT:
                    errorMessage = Const.ErrorMessages.Path.NOT_STARTED_WITH_ROOT.getMessage(e.getPath());
                    break;
                default:
                    throw new RuntimeException("Undefined error");
            }
            printError(errorMessage);
            return;
        } catch (DuplicatedPathException e) {
            printDuplicatedPathError(e);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            new Codegen() {
                @Override
                public File writeToFile(final String filename, final String contents) throws IOException {
                    // LICENSE ファイルは出力させない
                    if (filename != null) {
                        if (filename.endsWith("LICENSE") || filename.endsWith(".swagger-codegen-ignore")) {
                            return null;
                        }
                    }
                    return super.writeToFile(filename, contents);
                }
            }.opts(clientOptInput.opts(clientOpts)).generate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static boolean checkSwagger(final File file) throws IOException, ProcessingException {
        String fileName = file.getName();
        ObjectMapper mapper;
        if (fileName.endsWith(".yaml")) {
            mapper = new ObjectMapper(new YAMLFactory());
        } else if (fileName.endsWith(".json")) {
            mapper = new ObjectMapper();
        } else {
            throw new IllegalArgumentException("file must be JSON or YAML.");
        }

        JsonNode jsonNode = mapper.readTree(file);
        SwaggerJsonValidator.Result result = JSON_VALIDATOR.validate(jsonNode);
        if (result.isSuccess()) {
            return true;
        }

        String template = Const.ErrorMessages.CommandOption.INVALID_SWAGGER.getMessage();
        String errorMessage = template.replace("%file%", file.getName());
        String reasons = "";
        for (SwaggerJsonValidator.Error error : result.getErrors()) {
            String pointer = error.getJsonPointer();
            String reason = error.getMessage();
            reasons += " - Pointer = " + pointer + ", Reason = " + reason + "\n";
        }
        printError(errorMessage + ": \n" + reasons);
        return false;
    }

    private static void printError(final String message) {
        System.err.println(message);
    }

    private static void printDuplicatedPathError(final DuplicatedPathException e) {
        String template = MESSAGES.getString("errorProfileSpecDuplicatedPath");
        List<NameDuplication> duplications = e.getDuplications();

        String pathNames = "";
        for (Iterator<NameDuplication> it = duplications.iterator(); it.hasNext(); ) {
            NameDuplication dup = it.next();
            pathNames += dup.getName();
            if (it.hasNext()) {
                pathNames += ", ";
            }
        }

        String errorMessage = template.replace("%paths%", pathNames);
        printError(errorMessage);
    }

    private static Swagger createProfileSpec(final Swagger swagger) {
        Swagger profile = new Swagger();
        profile.setSwagger(swagger.getSwagger());
        Info info = new Info();
        info.setTitle(swagger.getInfo().getTitle());
        info.setVersion(swagger.getInfo().getVersion());
        info.setDescription(swagger.getInfo().getDescription());
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

    static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DConnectCodegen", options);
    }

}

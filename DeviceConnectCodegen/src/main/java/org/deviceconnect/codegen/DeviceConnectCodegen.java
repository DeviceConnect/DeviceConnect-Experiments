package org.deviceconnect.codegen;


import config.Config;
import config.ConfigParser;
import io.swagger.codegen.*;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class DeviceConnectCodegen {

    private static final Logger LOGGER = LoggerFactory.getLogger(Codegen.class);

    static Map<String, CodegenConfig> configs = new HashMap<String, CodegenConfig>();
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
        //options.addOption("i", "input-spec", true, "location of the swagger spec, as URL or file");
        options.addOption("x", "input-spec-dir", true, "directory of the swagger specs");
        options.addOption("t", "template-dir", true, "folder containing the template files");
        options.addOption("d", "debug-info", false, "prints additional info for debugging");
        //options.addOption("a", "auth", true, "adds authorization headers when fetching the swagger definitions remotely. Pass in a URL-encoded string of name:header with a comma separating multiple values");
        options.addOption("c", "config", true, "location of the configuration file");
        options.addOption("n", "name", true, "name of the generated project");

        ClientOptInput clientOptInput = new ClientOptInput();
        ClientOpts clientOpts = new ClientOpts();
        Map<String, Swagger> swaggerMap = new HashMap<>();

        CommandLine cmd = null;
        try {
            CommandLineParser parser = new BasicParser();
            CodegenConfig config = null;

            cmd = parser.parse(options, args);
            if (cmd.hasOption("d")) {
                usage(options);
                System.out.println(debugInfoOptions);
                return;
            }
            if (cmd.hasOption("l")) {
                clientOptInput.setConfig(getConfig(cmd.getOptionValue("l")));
            } else {
                usage(options);
                return;
            }
            if (cmd.hasOption("o")) {
                clientOptInput.getConfig().setOutputDir(cmd.getOptionValue("o"));
            }
            if (cmd.hasOption("h")) {
                if (cmd.hasOption("l")) {
                    config = getConfig(String.valueOf(cmd.getOptionValue("l")));
                    if (config != null) {
                        options.addOption("h", "help", true, config.getHelp());
                        usage(options);
                        return;
                    }
                }
                usage(options);
                return;
            }
            LOGGER.info("--input-spec-dir: " + cmd.getOptionValue("x"));
            if (cmd.hasOption("x")) {
                File dir = new File(cmd.getOptionValue("x"));
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".json");
                        }
                    });
                    for (File file : files) {
                        String profileName = parseProfileNameFromFileName(file.getName());
                        swaggerMap.put(profileName, new SwaggerParser().read(file.getAbsolutePath(), clientOptInput.getAuthorizationValues(), true));
                    }
                }
            }
            if (cmd.hasOption("c")) {
                String configFile = cmd.getOptionValue("c");
                Config genConfig = ConfigParser.read(configFile);
                config = clientOptInput.getConfig();
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
            if (cmd.hasOption("n")) {
                clientOpts.getProperties().put("projectName", cmd.getOptionValue("n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            usage(options);
            return;
        }
        try {
            clientOptInput.opts(clientOpts).swagger(mergeSwaggers(swaggerMap));
            new Codegen().opts(clientOptInput).generate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static String parseProfileNameFromFileName(String fileName) {
        if (!fileName.endsWith(".json")) {
            throw new IllegalArgumentException("JSON file is required.");
        }
        return fileName.substring(0, fileName.length() - ".json".length());
    }

    private static Swagger mergeSwaggers(Map<String, Swagger> swaggerMap) {
        Swagger merged = new Swagger();
        Map<String, Path> paths = new HashMap<>();
        for (Map.Entry<String, Swagger> swagger : swaggerMap.entrySet()) {
            String profileName = swagger.getKey();
            for (Map.Entry<String, Path> subPath : swagger.getValue().getPaths().entrySet()) {
                paths.put("/" + profileName + subPath.getKey(), subPath.getValue());
            }
        }
        merged.paths(paths);
        return merged;
    }

    public static List<CodegenConfig> getExtensions() {
        ServiceLoader<CodegenConfig> loader = ServiceLoader.load(CodegenConfig.class);
        List<CodegenConfig> output = new ArrayList<CodegenConfig>();
        for (CodegenConfig aLoader : loader) {
            output.add(aLoader);
        }
        return output;
    }

    static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DeviceConnectCodegen", options);
    }

    public static CodegenConfig getConfig(String name) {
        if (configs.containsKey(name)) {
            return configs.get(name);
        } else {
            // see if it's a class
            try {
                LOGGER.debug("loading class " + name);
                Class<?> customClass = Class.forName(name);
                LOGGER.debug("loaded");
                return (CodegenConfig) customClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("can't load class " + name);
            }
        }
    }

    static {
        List<CodegenConfig> extensions = getExtensions();
        StringBuilder sb = new StringBuilder();

        for (CodegenConfig config : extensions) {
            if (sb.toString().length() != 0) {
                sb.append(", ");
            }
            sb.append(config.getName());
            configs.put(config.getName(), config);
            configString = sb.toString();
        }
    }

}

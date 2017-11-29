package org.deviceconnect.codegen;


import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.deviceconnect.codegen.util.ResourceBundleWithUtf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

final class Const {

    private static final Logger LOGGER = LoggerFactory.getLogger(Const.class);

    static final ResourceBundle MESSAGES = ResourceBundle.getBundle("Messages", Locale.getDefault(),
            ResourceBundleWithUtf8.UTF8_ENCODING_CONTROL);

    static final Options OPTIONS = createOptions();

    static Map<String, DConnectCodegenConfig> configs = new HashMap<String, DConnectCodegenConfig>();

    static String configString;

    static DConnectCodegenConfig getConfig(String name) {
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

    static List<DConnectCodegenConfig> getExtensions() {
        ServiceLoader<DConnectCodegenConfig> loader = ServiceLoader.load(DConnectCodegenConfig.class);
        List<DConnectCodegenConfig> output = new ArrayList<DConnectCodegenConfig>();
        for (DConnectCodegenConfig aLoader : loader) {
            output.add(aLoader);
        }
        return output;
    }

    private static Options createOptions() {
        Options options = new Options();

        // Required
        options.addOption(option(true, "l", "lang", true, "client language to generate.\nAvailable languages include:\n\t[" + configString + "]"));
        options.addOption(option(true, "o", "output", true, "where to write the generated files"));
        OptionGroup inputSpecOptions = new OptionGroup();
        inputSpecOptions.setRequired(true);
        inputSpecOptions.addOption(new Option("s", "input-spec-dir", true, "directory of the swagger specs"));
        inputSpecOptions.addOption(new Option("i", "input-spec", true, "location of the swagger spec, as URL or file"));
        options.addOptionGroup(inputSpecOptions);

        // Optional
        options.addOption("h", "help", false, "shows this message");
        options.addOption("t", "template-dir", true, "folder containing the template files");
        options.addOption("d", "debug-info", false, "prints additional info for debugging");
        //options.addOption("a", "auth", true, "adds authorization headers when fetching the swagger definitions remotely. Pass in a URL-encoded string of name:header with a comma separating multiple values");
        options.addOption("c", "config", true, "location of the configuration file");
        options.addOption("p", "package-name", true, "package name (for deviceConnectAndroidPlugin only)");
        options.addOption("n", "display-name", true, "display name of the generated project");
        options.addOption("x", "class-prefix", true, "prefix of each generated class that implements a device connect profile");
        options.addOption("b", "connection-type", true, "connection type with device connect manager (for deviceConnectAndroidPlugin only)");

        return options;
    }

    private static Option option(boolean required, String opt, String longOpt, boolean hasArg, String description) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(required);
        return option;
    }

    static class ErrorMessages {

        // 引数指定
        enum CommandOption {

            MISSING_OPTION("errorMissingOption"),
            MISSING_ARGUMENT("errorMissingArgument"),
            ALREADY_SELECTED_OPTION("errorAlreadySelectedOption"),
            UNDEFINED_OPTION("errorUndefinedOption"),
            INVALID_SWAGGER("errorInvalidSwagger");

            private final String key;

            CommandOption(final String key) {
                this.key = key;
            }

            public String getMessage() {
                return MESSAGES.getString(key);
            }

            public String getMessage(final List<Object> options) {
                return getMessage() + ": \n" + concatOptions(options);
            }

            public String getMessage(final Option opt) {
                return getMessage() + ": " + opt.getLongOpt();
            }

            public String getMessage(final String optName) {
                return getMessage() + ": " + optName;
            }

            private static String concatOptions(List<Object> list) {
                String result = "";
                for (int i = 0; i < list.size(); i++) {
                    Object opt = list.get(i);
                    if (opt == null) {
                        continue;
                    }

                    result += " - ";
                    if (opt instanceof OptionGroup) {
                        OptionGroup group = (OptionGroup) opt;
                        String groupStr = "";
                        for (Iterator it = group.getOptions().iterator(); it.hasNext(); ) {
                            Option o = (Option) it.next();
                            groupStr += o.getLongOpt();
                            if (it.hasNext()) {
                                groupStr += " or ";
                            }
                        }
                        result += groupStr + "\n";
                    } else {
                        Option found = OPTIONS.getOption(opt.toString());
                        if (found != null) {
                            result += found.getLongOpt() + "\n";
                        }
                    }
                }
                return result;
            }
        }

        // パス定義
        enum Path {
            TOO_LONG("errorProfileSpecTooLongPath"),
            TOO_SHORT("errorProfileSpecTooShortPath"),
            NOT_STARTED_WITH_ROOT("errorProfileSpecPathNotStartedWithRoot");

            private final String key;

            Path(final String key) {
                this.key = key;
            }

            public String getMessage(final String path) {
                String template = MESSAGES.getString(key);
                return template.replace("%path%", path);
            }
        }
    }
}

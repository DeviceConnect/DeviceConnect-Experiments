package org.deviceconnect.codegen.languages;


import io.swagger.codegen.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AndroidPluginCodegenConfig extends AbstractPluginCodegenConfig {

    private final String pluginModuleFolder = "plugin";
    private final String projectFolder = pluginModuleFolder + "/src/main";
    private final String sourceFolder = projectFolder + "/java";
    private final String resFolder = projectFolder + "/res";
    private String invokerPackage;

    private final String apiDocPath = "docs/";
    private final String modelDocPath = "docs/";

    //----- AbstractPluginCodegenConfig ----//

    @Override
    protected String profileFileFolder() {
        String separator = File.separator;
        return outputFolder + separator + sourceFolder + separator + getProfilePackage().replace('.', File.separatorChar);
    }

    @Override
    protected String getLanguageSpecificClass(final String type, final String format) {
        if ("string".equals(type)) {
            return "String";
        } else if ("number".equals(type)) {
            if ("double".equals(format)) {
                return "Double";
            }
            return "Float";
        } else if ("integer".equals(type)) {
            if ("int64".equals(format)) {
                return "Long";
            }
            return "Integer";
        } else if ("boolean".equals(type)) {
            return "Boolean";
        } else if ("file".equals(type)) {
            return "byte[]";
        }
        return null;
    }

    @Override
    protected List<ProfileTemplate> prepareProfileTemplates(final String profileName, final Map<String, Object> properties) {
        final List<ProfileTemplate> profileTemplates = new ArrayList<>();
        String baseClassNamePrefix = getStandardClassName(profileName);
        final String baseClassName;
        final String profileClassName;
        final boolean isStandardProfile = baseClassNamePrefix != null;
        if (isStandardProfile) {
            baseClassName = baseClassNamePrefix + "Profile";
            profileClassName = getClassPrefix() + baseClassNamePrefix + "Profile";
        } else {
            baseClassName = "DConnectProfile";
            profileClassName = toUpperCapital(profileName) + "Profile";
        }
        properties.put("baseProfileClass", baseClassName);
        properties.put("profileClass", profileClassName);
        properties.put("profileNameDefinition", profileName);
        properties.put("profilePackage", getProfilePackage());
        properties.put("isStandardProfile", isStandardProfile);

        ((List<Object>) additionalProperties.get("supportedProfileNames")).add(new Object() { String name = profileName; });
        ((List<Object>) additionalProperties.get("supportedProfileClasses")).add(new Object() { String name = profileClassName;});

        ProfileTemplate template = new ProfileTemplate();
        template.templateFile = "profile.mustache";
        template.outputFile = profileClassName + ".java";
        profileTemplates.add(template);
        return profileTemplates;
    }

    private String getProfilePackage() {
        return invokerPackage + ".profiles";
    }

    //----- CodegenConfig ----//

    @Override
    public CodegenType getTag() { return CodegenType.OTHER; }

    @Override
    public String getName() { return "deviceConnectAndroidPlugin"; }

    @Override
    public String getHelp() { return "Generates a stub of Device Connect Plug-in for Android."; }

    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace( '/', File.separatorChar );
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelDocFileFolder() {
        return ( outputFolder + "/" + modelDocPath ).replace( '/', File.separatorChar );
    }

    @Override
    public String toModelName(String name) {
        // add prefix, suffix if needed
        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        // camelize the model name
        // phone_number => PhoneNumber
        name = camelize(sanitizeName(name));

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            String modelName = "Model" + name;
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        // model name starts with number
        if (name.matches("^\\d.*")) {
            String modelName = "Model" + name; // e.g. 200Response => Model200Response (after camelize)
            LOGGER.warn(name + " (model name starts with number) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String escapeReservedWord(String name) {
        return "_" + name;
    }

    @Override
    public String getTypeDeclaration(Property p) {
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            return getSwaggerType(p) + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();

            return getSwaggerType(p) + "<String, " + getTypeDeclaration(inner) + ">";
        }
        return super.getTypeDeclaration(p);
    }

    @Override
    public void processOpts() {
        super.processOpts();
        invokerPackage = (String) additionalProperties.get("packageName");
        embeddedTemplateDir = templateDir = getName();
        additionalProperties.put("profilePackage", getProfilePackage());
        additionalProperties.put("pluginSdkVersion", "1.1.0");

        final String classPrefix = getClassPrefix();
        final String messageServiceClass = classPrefix + "MessageService";
        final String messageServiceProviderClass = classPrefix + "MessageServiceProvider";
        additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
        additionalProperties.put("serviceName", classPrefix + " Service");
        additionalProperties.put("serviceId", classPrefix.toLowerCase() + "_service_id");
        additionalProperties.put("messageServiceClass", messageServiceClass);
        additionalProperties.put("messageServiceProviderClass", messageServiceProviderClass);

        // README
        supportingFiles.add(new SupportingFile("README.md.mustache", "", "README.md"));

        // ビルド設定ファイル
        supportingFiles.add(new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));
        supportingFiles.add(new SupportingFile("manifest.mustache", projectFolder, "AndroidManifest.xml"));
        supportingFiles.add(new SupportingFile("root.build.gradle.mustache", "", "build.gradle"));
        supportingFiles.add(new SupportingFile("plugin.build.gradle.mustache", pluginModuleFolder, "build.gradle"));
        supportingFiles.add(new SupportingFile("gradle.properties.mustache", "", "gradle.properties"));
        supportingFiles.add(new SupportingFile("deviceplugin.xml.mustache", resFolder + "/xml", "deviceplugin.xml"));
        supportingFiles.add(new SupportingFile("string.xml.mustache", resFolder + "/values", "string.xml"));

        // アプリアイコン画像
        supportingFiles.add(new SupportingFile("ic_launcher.png", resFolder + "/drawable", "ic_launcher.png"));

        // 実装ファイル
        final String packageFolder = (sourceFolder + File.separator + invokerPackage).replace(".", File.separator);
        supportingFiles.add(new SupportingFile("MessageServiceProvider.java.mustache", packageFolder, messageServiceProviderClass + ".java"));
        supportingFiles.add(new SupportingFile("MessageService.java.mustache", packageFolder, messageServiceClass + ".java"));
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        return super.postProcessOperations(objs);
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public String toApiDocFilename( String name ) {
        return toApiName( name );
    }

    @Override
    public String toModelDocFilename( String name ) {
        return toModelName( name );
    }

    //----- DefaultCodegen ----//

    @Override
    public String getSwaggerType(Property p) {
        String swaggerType = super.getSwaggerType(p);
        String type = null;
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping.get(swaggerType);
            if (languageSpecificPrimitives.contains(type) || type.indexOf(".") >= 0 ||
                    type.equals("Map") || type.equals("List") ||
                    type.equals("File") || type.equals("Date")) {
                return type;
            }
        } else {
            type = swaggerType;
        }
        return toModelName(type);
    }

    @Override
    public String toVarName(String name) {
        // sanitize name
        name = sanitizeName(name); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // if it's all upper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize (lower first character) the variable name
        // pet_id => petId
        name = camelize(name, true);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        String example;

        if (p.defaultValue == null) {
            example = p.example;
        } else {
            example = p.defaultValue;
        }

        String type = p.baseType;
        if (type == null) {
            type = p.dataType;
        }

        if ("String".equals(type)) {
            if (example == null) {
                example = p.paramName + "_example";
            }
            example = "\"" + escapeText(example) + "\"";
        } else if ("Integer".equals(type) || "Short".equals(type)) {
            if (example == null) {
                example = "56";
            }
        } else if ("Long".equals(type)) {
            if (example == null) {
                example = "56";
            }
            example = example + "L";
        } else if ("Float".equals(type)) {
            if (example == null) {
                example = "3.4";
            }
            example = example + "F";
        } else if ("Double".equals(type)) {
            example = "3.4";
            example = example + "D";
        } else if ("Boolean".equals(type)) {
            if (example == null) {
                example = "true";
            }
        } else if ("File".equals(type)) {
            if (example == null) {
                example = "/path/to/file";
            }
            example = "new File(\"" + escapeText(example) + "\")";
        } else if ("Date".equals(type)) {
            example = "new Date()";
        } else if (!languageSpecificPrimitives.contains(type)) {
            // type is a model class, e.g. User
            example = "new " + type + "()";
        }

        if (example == null) {
            example = "null";
        } else if (Boolean.TRUE.equals(p.isListContainer)) {
            example = "Arrays.asList(" + example + ")";
        } else if (Boolean.TRUE.equals(p.isMapContainer)) {
            example = "new HashMap()";
        }

        p.example = example;
    }

    @Override
    public String toOperationId(String operationId) {
        // throw exception if method name is empty
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method name (operationId) not allowed");
        }

        operationId = camelize(sanitizeName(operationId), true);

        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            String newOperationId = camelize("call_" + operationId, true);
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + newOperationId);
            return newOperationId;
        }

        return operationId;
    }

}
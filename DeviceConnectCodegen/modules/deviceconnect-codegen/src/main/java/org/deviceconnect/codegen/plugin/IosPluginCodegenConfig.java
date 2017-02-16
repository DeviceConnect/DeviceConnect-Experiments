package org.deviceconnect.codegen.plugin;


import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IosPluginCodegenConfig extends AbstractPluginCodegenConfig {

    @Override
    public void processOpts() {
        super.processOpts();
        embeddedTemplateDir = templateDir = getName();

        // README
        supportingFiles.add(new SupportingFile("README.md.mustache", "", "README.md"));

        String classPrefix = getClassPrefix();
        additionalProperties.put("serviceId", classPrefix.toLowerCase() + "_service_id");
        String pluginClass = classPrefix + "Plugin";
        additionalProperties.put("pluginClass", pluginClass);
        additionalProperties.put("basePluginClass", "DConnectDevicePlugin");

        String classesDir = "Classes";
        supportingFiles.add(new SupportingFile("plugin.h.mustache", classesDir, pluginClass + ".h"));
        supportingFiles.add(new SupportingFile("plugin.m.mustache", classesDir, pluginClass + ".m"));
    }

    @Override
    protected String profileFileFolder() {
        return outputFolder + File.separator + "Classes" + File.separator + "Profiles";
    }

    @Override
    protected String getProfileSpecFolder() {
        return null;
    }

    @Override
    protected String getDeclaration(final Parameter p) {
        String type;
        String format;
        if (p instanceof QueryParameter) {
            type = ((QueryParameter) p).getType();
            format = ((QueryParameter) p).getFormat();
        } else if (p instanceof FormParameter) {
            type = ((FormParameter) p).getType();
            format = ((FormParameter) p).getFormat();
        } else {
            return null;
        }

        String varName = p.getName();
        String typeName;
        String parserPrefix;
        boolean usesPointer = false;
        if ("number".equals(type)) {
            if ("double".equals(format)) {
                typeName = "double";
                parserPrefix = "double";
            } else {
                typeName = "float";
                parserPrefix = "float";
            }
        } else if ("integer".equals(type)) {
            if ("int64".equals(format)) {
                typeName = "long long";
                parserPrefix = "longLong";
            } else {
                typeName = "int";
                parserPrefix = "integer";
            }
        } else if ("boolean".equals(type)) {
            typeName = "BOOL";
            parserPrefix = "bool";
        } else if ("string".equals(type) || "array".equals(type)) {
            typeName = "NSString";
            parserPrefix = "string";
            usesPointer = true;
        } else if ("file".equals(type)) {
            typeName = "NSData";
            parserPrefix = "data";
            usesPointer = true;
        } else {
            typeName = "id";
            parserPrefix = "object";
        }
        String leftOperand = usesPointer ? (typeName + " *" + varName) : (typeName + " " + varName);
        String rightOperand = "[request " + parserPrefix + "ForKey:@\"" + varName + "\"]";
        return leftOperand + " = " + rightOperand + ";";
    }

    @Override
    protected List<String> getResponseCreation(final Swagger swagger, final Response response) {
        return null;
    }

    @Override
    protected List<String> getEventCreation(final Swagger swagger, final Response event) {
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
            baseClassName = "DConnect" + baseClassNamePrefix + "Profile";
            profileClassName = getClassPrefix() + baseClassNamePrefix + "Profile";
        } else {
            baseClassName = "DConnectProfile";
            profileClassName = toUpperCapital(profileName) + "Profile";
        }
        properties.put("baseProfileClass", baseClassName);
        properties.put("profileClass", profileClassName);
        properties.put("profileNameDefinition", profileName);
        properties.put("isStandardProfile", isStandardProfile);

        ProfileTemplate header = new ProfileTemplate();
        header.templateFile = "profile.h.mustache";
        header.outputFile = profileClassName + ".h";
        profileTemplates.add(header);
        ProfileTemplate impl = new ProfileTemplate();
        impl.templateFile = "profile.m.mustache";
        impl.outputFile = profileClassName + ".m";
        profileTemplates.add(impl);

        ((List<Object>) additionalProperties.get("supportedProfileClasses")).add(new Object() { String name = profileClassName; });

        return profileTemplates;
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.OTHER;
    }

    @Override
    public String getName() {
        return "deviceConnectIosPlugin";
    }

    @Override
    public String getHelp() {
        return "Generates a stub of Device Connect Plug-in for iOS.";
    }
}

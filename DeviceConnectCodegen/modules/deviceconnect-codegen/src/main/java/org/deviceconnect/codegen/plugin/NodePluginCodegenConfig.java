package org.deviceconnect.codegen.plugin;


import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodePluginCodegenConfig extends AbstractPluginCodegenConfig {

    @Override
    protected String getDeclaration(final Parameter p) {
        return null;
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
    protected String profileFileFolder() {
        return outputFolder + File.separator + "profiles";
    }

    @Override
    protected List<ProfileTemplate> prepareProfileTemplates(final String profileName, final Map<String, Object> properties) {
//        final List<ProfileTemplate> profileTemplates = new ArrayList<>();
//
//        ProfileTemplate template = new ProfileTemplate();
//        template.templateFile = "profile.mustache";
//        template.outputFile = profileClassName + ".java";
//        profileTemplates.add(template);
//        return profileTemplates;

        return null;
    }

    @Override
    public void processOpts() {
        super.processOpts();
        embeddedTemplateDir = templateDir = getName();

        final String classPrefix = getClassPrefix();
        additionalProperties.put("serviceId", classPrefix.toLowerCase() + "_service_id");

        // package.json
        supportingFiles.add(new SupportingFile("package.json.mustache", "", "package.json"));

        // index.js (= プラグイン本体の実装ファイル)
        supportingFiles.add(new SupportingFile("index.js.mustache", "", "index.js"));
    }

    @Override
    protected String getProfileSpecFolder() {
        return null; // 不要
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.OTHER;
    }

    @Override
    public String getName() {
        return "gotapiNodePlugin";
    }

    @Override
    public String getHelp() {
        return "";
    }
}

package org.deviceconnect.codegen.app;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.deviceconnect.codegen.DConnectCodegenConfig;

import java.util.Map;

public class HtmlAppCodegenConfig extends DefaultCodegen implements DConnectCodegenConfig {

    private Map<String, Swagger> profileSpecs;

    @Override
    public String getDefaultDisplayName() {
        return "MyApp";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "deviceConnectHtmlApp";
    }

    @Override
    public String getHelp() {
        return "";
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
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String swaggerJson = mapper.writeValueAsString(swagger);
            additionalProperties.put("resolvedSwagger", swaggerJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize the resolved swagger object.", e);
        }
    }

    @Override
    public void processOpts() {
        super.processOpts();
        embeddedTemplateDir = templateDir = getName();
        supportingFiles.add(new SupportingFile("index.html", "", "index.html"));
        supportingFiles.add(new SupportingFile("resource.html", "", "resource.html"));
        supportingFiles.add(new SupportingFile("css/accordion.css", "css", "accordion.css"));
        supportingFiles.add(new SupportingFile("css/checker.css", "css", "checker.css"));
        supportingFiles.add(new SupportingFile("css/resource.css", "css", "resource.css"));
        supportingFiles.add(new SupportingFile("images/icon_minus.png", "images", "icon_minus.png"));
        supportingFiles.add(new SupportingFile("images/icon_plus.png", "images", "icon_plus.png"));
        supportingFiles.add(new SupportingFile("js/checker.js", "js", "checker.js"));
        supportingFiles.add(new SupportingFile("js/dconnectsdk-2.2.0.js", "js", "dconnectsdk-2.2.0.js"));
        supportingFiles.add(new SupportingFile("js/resource.js", "js", "resource.js"));
        supportingFiles.add(new SupportingFile("js/util.js", "js", "util.js"));
        supportingFiles.add(new SupportingFile("specs/swagger.js.mustache", "specs", "swagger.js"));
    }
}

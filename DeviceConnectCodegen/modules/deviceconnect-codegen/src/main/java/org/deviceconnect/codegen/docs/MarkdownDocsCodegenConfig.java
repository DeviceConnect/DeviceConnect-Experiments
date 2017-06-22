package org.deviceconnect.codegen.docs;


import io.swagger.codegen.SupportingFile;
import org.deviceconnect.codegen.ProfileTemplate;

import java.io.File;
import java.util.List;

public class MarkdownDocsCodegenConfig extends AbstractDocsCodegenConfig {

    @Override
    public String getName() {
        return "deviceConnectMarkdownDocs";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected String profileFileFolder() {
        return outputFolder() + File.separator + "Profiles";
    }

    @Override
    protected void prepareTemplates(final String profileName, final List<ProfileTemplate> templates) {
        ProfileTemplate template = new ProfileTemplate();
        template.templateFile = "profile.md.mustache";
        template.outputFile = toUpperCapital(profileName, false) + ".md";
        templates.add(template);
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.add(new SupportingFile("index.md.mustache", "Device-Connect-API-Reference.md"));
        supportingFiles.add(new SupportingFile("sidebar.md.mustache", "_Sidebar.md"));
    }
}

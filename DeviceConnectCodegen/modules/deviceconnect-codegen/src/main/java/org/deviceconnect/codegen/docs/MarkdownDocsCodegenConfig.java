package org.deviceconnect.codegen.docs;


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
        template.outputFile = profileName + ".md";
        templates.add(template);
    }

}

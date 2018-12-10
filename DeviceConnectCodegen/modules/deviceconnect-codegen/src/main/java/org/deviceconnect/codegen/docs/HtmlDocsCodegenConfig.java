/*
 HtmlDocsCodegenConfig.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.docs;


import io.swagger.codegen.SupportingFile;
import io.swagger.models.Swagger;

import java.util.*;

public class HtmlDocsCodegenConfig extends AbstractDocsCodegenConfig {

    @Override
    public String getName() {
        return "deviceConnectHtmlDocs";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected String profileFileFolder() {
        return null;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        List<ProfileDocs> profileHtmlList = new ArrayList<>();
        for (Map.Entry<String, Swagger> specEntry : profileSpecs.entrySet()) {
            final String profileName = specEntry.getKey();
            profileHtmlList.add(new ProfileDocs() {
                public String profileName() { return profileName; }
            });
        }
        Collections.sort(profileHtmlList, new Comparator<ProfileDocs>() {
            @Override
            public int compare(ProfileDocs o1, ProfileDocs o2) {
                return o1.profileName().compareTo(o2.profileName());
            }
        });
        additionalProperties.put("profileHtmlList", profileHtmlList);

        supportingFiles.add(new SupportingFile("index.html.mustache", "", "index.html"));
        supportingFiles.add(new SupportingFile("css/profile.css", "css", "profile.css"));
        supportingFiles.add(new SupportingFile("css/operation-list.css", "css", "operation-list.css"));
        supportingFiles.add(new SupportingFile("html/profile-list.html.mustache", "html", "profile-list.html"));
        supportingFiles.add(new SupportingFile("html/operation-list.html.mustache", "html", "operation-list.html"));
        supportingFiles.add(new SupportingFile("html/all-operations.html.mustache", "html", "all-operations.html"));
    }

}
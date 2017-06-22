package org.deviceconnect.codegen.docs;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.*;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.deviceconnect.codegen.DConnectCodegenConfig;
import org.deviceconnect.codegen.models.DConnectOperation;

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

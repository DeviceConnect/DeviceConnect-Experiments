package org.deviceconnect.codegen.docs;


import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import org.deviceconnect.codegen.DConnectCodegenConfig;

import java.util.*;

public class HtmlDocsCodegenConfig extends DefaultCodegen implements DConnectCodegenConfig {

    private Map<String, Swagger> profileSpecs;

    @Override
    public String getDefaultDisplayName() {
        return "Device Connect API Specifications";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public String getName() {
        return "deviceConnectHtmlDocs";
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

        ArrayList<ProfileDocs> swaggerList = new ArrayList<>();
        for (Map.Entry<String, Swagger> specEntry : profileSpecs.entrySet()) {
            final String profileName = specEntry.getKey();
            final Swagger profileSpec = specEntry.getValue();
            String basePath = profileSpec.getBasePath();
            if (basePath == null) {
                basePath = "/gotapi/" + profileName;
            }

            final List<Object> operationList = new ArrayList<>();
            for (Map.Entry<String, Path> pathEntry : profileSpec.getPaths().entrySet()) {
                String pathName = pathEntry.getKey();
                Path path = pathEntry.getValue();
                if ("/".equals(pathName)) {
                    pathName = "";
                }
                final String fullPathName = basePath + pathName;

                for (Map.Entry<HttpMethod, Operation> opEntry : path.getOperationMap().entrySet()) {
                    final String method = opEntry.getKey().name().toUpperCase();
                    final Operation op = opEntry.getValue();
                    final List<Object> paramList = new ArrayList<>();
                    for (final Parameter param : op.getParameters()) {
                        paramList.add(new Object() {
                            String name = param.getName();
                            String type() {
                                String type;
                                String format;
                                if (param instanceof QueryParameter) {
                                    type = ((QueryParameter) param).getType();
                                    format = ((QueryParameter) param).getFormat();
                                } else if (param instanceof FormParameter) {
                                    type = ((FormParameter) param).getType();
                                    format = ((FormParameter) param).getFormat();
                                } else {
                                    return null;
                                }
                                if (format == null) {
                                    return type;
                                }
                                return type + " (" + format + ")";
                            }
                            String required = param.getRequired() ? "Yes" : "No";
                            String description = param.getDescription();
                        });
                    }

                    operationList.add(new Object() {
                        String id() {
                            String id = method + "-" + fullPathName.replaceAll("/", "-");
                            return id.toLowerCase();
                        }
                        String name() {
                            return method + " " + fullPathName;
                        }
                        String type = (String) op.getVendorExtensions().get("x-type");
                        String summary = op.getSummary();
                        String description = op.getDescription();
                        List<Object> paramList() {
                            return paramList;
                        }
                    });
                }
            }

            ProfileDocs swaggerObj = new ProfileDocs() {
                public String profileName() { return profileName; }
                String version = profileSpec.getInfo().getVersion();
                String title = profileSpec.getInfo().getTitle();
                String description = profileSpec.getInfo().getDescription();
                List<Object> operationList() { return operationList; }
            };
            swaggerList.add(swaggerObj);
        }
        Collections.sort(swaggerList, new Comparator<ProfileDocs>() {
            @Override
            public int compare(ProfileDocs o1, ProfileDocs o2) {
                return o1.profileName().compareTo(o2.profileName());
            }
        });
        additionalProperties.put("swaggerList", swaggerList);

        embeddedTemplateDir = templateDir = getName();
        supportingFiles.add(new SupportingFile("index.html.mustache", "", "index.html"));
        supportingFiles.add(new SupportingFile("css/profile.css", "css", "profile.css"));
        supportingFiles.add(new SupportingFile("css/operation-list.css", "css", "operation-list.css"));
        supportingFiles.add(new SupportingFile("html/profile-list.html.mustache", "html", "profile-list.html"));
        supportingFiles.add(new SupportingFile("html/operation-list.html.mustache", "html", "operation-list.html"));
        supportingFiles.add(new SupportingFile("html/all-operations.html.mustache", "html", "all-operations.html"));
    }

    private interface ProfileDocs {
        String profileName();
    }
}

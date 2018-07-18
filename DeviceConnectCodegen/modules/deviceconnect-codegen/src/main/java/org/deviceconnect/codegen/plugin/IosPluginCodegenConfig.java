/*
 IosPluginCodegenConfig.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.plugin;


import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.*;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.*;
import org.deviceconnect.codegen.ProfileTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IosPluginCodegenConfig extends AbstractPluginCodegenConfig {

    @Override
    public void processOpts() {
        super.processOpts();
        embeddedTemplateDir = getName();

        // README
        supportingFiles.add(new SupportingFile("README.md.mustache", "", "README.md"));

        String classPrefix = getClassPrefix();
        additionalProperties.put("serviceId", classPrefix.toLowerCase() + "_service_id");
        String pluginClass = classPrefix + "Plugin";
        additionalProperties.put("pluginClass", pluginClass);
        additionalProperties.put("basePluginClass", "DConnectDevicePlugin");

        String classesDir = getClassesDirName();
        supportingFiles.add(new SupportingFile("plugin.h.mustache", classesDir, pluginClass + ".h"));
        supportingFiles.add(new SupportingFile("plugin.m.mustache", classesDir, pluginClass + ".m"));
    }

    private String getClassesDirName() {
        String[] dirs = outputFolder.split("/");
        return dirs[dirs.length - 1];
    }

    @Override
    protected String profileFileFolder() {
        return outputFolder + File.separator + getClassesDirName() + File.separator + "Profiles";
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
        List<String> lines = new ArrayList<>();
        Property schema = response.getSchema();

        ObjectProperty root;
        if (schema instanceof ObjectProperty) {
            root = (ObjectProperty) schema;
        } else if (schema instanceof RefProperty) {
            RefProperty ref = (RefProperty) schema;
            if (isIgnoredDefinition(ref.getName())) {
                return lines;
            }
            Model model = findDefinition(swagger, ref.getSimpleRef());
            Map<String, Property> properties;
            if (model instanceof ComposedModel) {
                properties = getProperties(swagger, (ComposedModel) model);
            } else if (model instanceof ModelImpl) {
                properties = model.getProperties();
            } else {
                lines.add("// WARNING: レスポンスの定義が不正です.");
                return lines;
            }
            if (properties == null) {
                lines.add("// WARNING: レスポンスの定義が見つかりませんでした.");
                return lines;
            }
            root =  new ObjectProperty();
            root.setProperties(properties);
        } else {
            lines.add("// WARNING: レスポンスの定義が不正です.");
            return lines;
        }

        Map<String, Property> props = root.getProperties();
        if (props != null && props.size() > 0) {
            writeExampleMessage(root, "response", lines);
        }
        return lines;
    }

    @Override
    protected List<String> getEventCreation(final Swagger swagger, final Response event) {
        List<String> lines = new ArrayList<>();
        Property schema = event.getSchema();

        ObjectProperty root;
        if (schema instanceof ObjectProperty) {
            root = (ObjectProperty) schema;
        } else if (schema instanceof RefProperty) {
            RefProperty ref = (RefProperty) schema;
            if (isIgnoredDefinition(ref.getName())) {
                return lines;
            }
            Model model = findDefinition(swagger, ref.getSimpleRef());
            Map<String, Property> properties;
            if (model instanceof ComposedModel) {
                properties = getProperties(swagger, (ComposedModel) model);
            } else if (model instanceof ModelImpl) {
                properties = model.getProperties();
            } else {
                lines.add("// WARNING: イベントの定義が不正です.");
                return lines;
            }
            if (properties == null) {
                lines.add("// WARNING: イベントの定義が見つかりませんでした.");
                return lines;
            }
            root = new ObjectProperty();
            root.setProperties(properties);
        } else {
            lines.add("// WARNING: イベントの定義が不正です.");
            return lines;
        }

        Map<String, Property> props = root.getProperties();
        if (props != null && props.size() > 0) {
            writeExampleMessage(root, "message", lines);
        }
        return lines;
    }

    private void writeExampleMessage(final ObjectProperty root, final String rootName,
                                     final List<String> lines) {
        Map<String, Property> props = root.getProperties();
        if (props == null) {
            return;
        }
        for (Map.Entry<String, Property> propEntry : props.entrySet()) {
            String propName = propEntry.getKey();
            Property prop = propEntry.getValue();

            String type = prop.getType();
            String format = prop.getFormat();
            if ("array".equals(type)) {
                ArrayProperty arrayProp;
                if (!(prop instanceof  ArrayProperty)) {
                    continue;
                }
                arrayProp = (ArrayProperty) prop;
                Property itemsProp = arrayProp.getItems();
                lines.add("DConnectArray *" + propName + " = [DConnectArray array];");
                String objPropName = propName + "0";
                if ("object".equals(itemsProp.getType())) {
                    lines.add("DConnectMessage *" + objPropName + " = [DConnectMessage message];");
                    writeExampleMessage((ObjectProperty) itemsProp, objPropName, lines);
                    lines.add("[" + propName + " addMessage:" + objPropName + "];");
                } else {
                    String exampleValue = getExampleValue(itemsProp);
                    String setterName = getSetterNameForArray(itemsProp.getType(), itemsProp.getFormat());
                    lines.add("[" + propName + " " + setterName + ":" + exampleValue + "];");
                }
                lines.add("[" + rootName + " setArray:" + propName + " forKey:@\"" + propName + "\"];");
            } else if ("object".equals(type)) {
                ObjectProperty objectProp;
                if (!(prop instanceof ObjectProperty)) {
                    continue;
                }
                objectProp = (ObjectProperty) prop;
                lines.add("DConnectMessage *" + propName + " = [DConnectMessage message];");
                writeExampleMessage(objectProp, propName, lines);
                lines.add("[" + rootName  + " setMessage:" + propName + " forKey:@\"" + propName +  "\"];");
            } else {
                String setterName = getSetterNameForMessage(type, format);
                if (setterName == null) {
                    continue;
                }
                lines.add("[" + rootName + " " + setterName +  ":"+ getExampleValue(prop) + " forKey:@\"" + propName + "\"];");
            }
        }
    }

    private String getSetterNameForMessage(final String type, final String format) {
        if ("boolean".equals(type)) {
            return "setBool";
        } else if ("string".equals(type)) {
            return "setString";
        } else if ("integer".equals(type)) {
            if ("int64".equals(format)) {
                return "setLongLong";
            } else {
                return "setInt";
            }
        } else if ("number".equals(type)) {
            if ("double".equals(format)) {
                return "setDouble";
            } else {
                return "setFloat";
            }
        } else {
            // 現状のプラグインでは下記のタイプは非対応.
            //  - file
            return null;
        }
    }

    private String getSetterNameForArray(final String type, final String format) {
        if ("boolean".equals(type)) {
            return "addBool";
        } else if ("string".equals(type)) {
            return "addString";
        } else if ("integer".equals(type)) {
            if ("int64".equals(format)) {
                return "addLongLong";
            } else {
                return "addInt";
            }
        } else if ("number".equals(type)) {
            if ("double".equals(format)) {
                return "addDouble";
            } else {
                return "addFloat";
            }
        } else {
            // 現状のプラグインでは下記のタイプは非対応.
            //  - file
            return null;
        }
    }

    // TODO: 他のプラットフォームと共通化する
    private String getExampleValue(final Property prop) {
        final String type = prop.getType();
        final String format = prop.getFormat();
        if ("boolean".equals(type)) {
            return "false";
        } else if ("string".equals(type)) {
            if ("date-time".equals(format)) {
                return "@\"2000-01-01T01:01:0+09:00\"";
            } else if ("date".equals(format)) {
                return "@\"2000-01-01\"";
            } else if ("byte".equals(format)) {
                return "@\"dGVzdA==\"";
            } else if ("binary".equals(format)) {
                return "@\"\""; // TODO: バイナリ形式の文字列表現の仕様を確認
            } else {
                StringProperty strProp = (StringProperty) prop;
                List<String> enumList = strProp.getEnum();
                if (enumList != null && enumList.size() > 0) {
                    return "@\"" + enumList.get(0) + "\"";
                } else {
                    return "@\"test\"";
                }
            }
        } else if ("integer".equals(type)) {
            if ("int64".equals(format)) {
                return "0";
            } else {
                return "0";
            }
        } else if ("number".equals(type)) {
            if ("double".equals(format)) {
                return "0.0d";
            } else {
                return "0.0f";
            }
        } else {
            // 現状のプラグインでは下記のタイプは非対応.
            //  - file
            return null;
        }
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
            profileClassName = getClassPrefix() + toUpperCapital(profileName) + "Profile";
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
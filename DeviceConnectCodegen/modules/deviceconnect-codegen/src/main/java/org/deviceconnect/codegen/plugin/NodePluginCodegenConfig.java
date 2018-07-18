/*
 NodePluginCodegenConfig.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.plugin;


import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.*;
import org.deviceconnect.codegen.AbstractCodegenConfig;
import org.deviceconnect.codegen.ProfileTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodePluginCodegenConfig extends AbstractPluginCodegenConfig {

    @Override
    protected String getDeclaration(final Parameter p) {
        return "let " + p.getName() + " = message.params." + p.getName() + ";";
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
                lines.add("// WARNING: メッセージの定義が不正です.");
                return lines;
            }
            if (properties == null) {
                lines.add("// WARNING: メッセージの定義が見つかりませんでした.");
                return lines;
            }
            root =  new ObjectProperty();
            root.setProperties(properties);
        } else {
            lines.add("// WARNING: メッセージの定義が不正です.");
            return lines;
        }

        Map<String, Property> props = root.getProperties();
        if (props != null && props.size() > 0) {
            writeExampleMessage(root, "message", lines);
        }
        return lines;
    }

    @Override
    protected List<String> getEventCreation(final Swagger swagger, final Response event) {
        return getResponseCreation(swagger, event);
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
            if ("array".equals(type)) {
                ArrayProperty arrayProp;
                if (!(prop instanceof  ArrayProperty)) {
                    continue;
                }
                arrayProp = (ArrayProperty) prop;
                Property itemsProp = arrayProp.getItems();

                lines.add("let " + propName + " = [];");
                if ("object".equals(itemsProp.getType())) {
                    lines.add(propName + "[0] = {};");
                    writeExampleMessage((ObjectProperty) itemsProp, propName + "[0]", lines);
                    lines.add(rootName + "." + propName + " = " + propName + ";");
                } else {
                    lines.add(propName + "[0] = " + getExampleValue(itemsProp) + ";");
                    lines.add(rootName + "." + propName + " = " + propName + ";");
                }
            } else if ("object".equals(type)) {
                ObjectProperty objectProp;
                if (!(prop instanceof ObjectProperty)) {
                    continue;
                }
                objectProp = (ObjectProperty) prop;
                lines.add("let " + propName + " = {};");
                writeExampleMessage(objectProp, propName, lines);
                lines.add(rootName  + "." + propName + " = " + propName + ";");
            } else {
                lines.add(rootName + "." + propName + " = " + getExampleValue(prop) + ";");
            }
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
                return "\"2000-01-01T01:01:0+09:00\"";
            } else if ("date".equals(format)) {
                return "\"2000-01-01\"";
            } else if ("byte".equals(format)) {
                return "\"dGVzdA==\"";
            } else if ("binary".equals(format)) {
                return "\"\""; // TODO: バイナリ形式の文字列表現の仕様を確認
            } else {
                StringProperty strProp = (StringProperty) prop;
                List<String> enumList = strProp.getEnum();
                if (enumList != null && enumList.size() > 0) {
                    return "\"" + enumList.get(0) + "\"";
                } else {
                    return "\"test\"";
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
                return "0.0";
            } else {
                return "0.0";
            }
        } else {
            // 現状のプラグインでは下記のタイプは非対応.
            //  - file
            return null;
        }
    }

    @Override
    protected String profileFileFolder() {
        return outputFolder + File.separator + "profiles";
    }

    @Override
    protected List<ProfileTemplate> prepareProfileTemplates(final String profileName, final Map<String, Object> properties) {
        return null; // 不要
    }

    @Override
    public void processOpts() {
        super.processOpts();
        embeddedTemplateDir = getName();

        final String classPrefix = getClassPrefix();
        additionalProperties.put("serviceId", classPrefix.toLowerCase() + "_service_id");

        // README
        supportingFiles.add(new SupportingFile("README.md.mustache", "", "README.md"));

        // package.json
        supportingFiles.add(new SupportingFile("package.json.mustache", "", "package.json"));

        // index.js (= プラグイン本体の実装ファイル)
        supportingFiles.add(new SupportingFile("index.js.mustache", "", "index.js"));
    }

    @Override
    protected String getProfileSpecFolder() {
        String separator = File.separator;
        return outputFolder + separator + "specs";
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
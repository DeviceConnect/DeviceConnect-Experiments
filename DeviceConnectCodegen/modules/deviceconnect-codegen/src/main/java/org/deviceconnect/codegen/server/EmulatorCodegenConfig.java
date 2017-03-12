package org.deviceconnect.codegen.server;


import io.swagger.codegen.CodegenType;
import io.swagger.codegen.languages.NodeJSServerCodegen;
import io.swagger.models.Swagger;
import org.deviceconnect.codegen.DConnectCodegenConfig;

import java.io.File;
import java.util.Map;

public class EmulatorCodegenConfig extends NodeJSServerCodegen implements DConnectCodegenConfig {

    private Map<String, Swagger> profileSpecs;

    @Override
    public String getDefaultDisplayName() {
        return "MyEmulator";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "deviceConnectEmulator";
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
}

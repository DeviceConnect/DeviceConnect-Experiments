package org.deviceconnect.codegen.server;


import io.swagger.codegen.CodegenType;
import io.swagger.codegen.languages.NodeJSServerCodegen;
import org.deviceconnect.codegen.DConnectCodegenConfig;

import java.io.File;

public class EmulatorCodegenConfig extends NodeJSServerCodegen implements DConnectCodegenConfig {

    private File[] inputSpecFiles;

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
    public File[] getInputSpecFiles() {
        return inputSpecFiles;
    }

    @Override
    public void setInputSpecFiles(final File[] specs) {
        this.inputSpecFiles = specs;
    }
}

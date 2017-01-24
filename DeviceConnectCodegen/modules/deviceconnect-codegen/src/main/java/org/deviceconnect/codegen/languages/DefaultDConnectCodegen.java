package org.deviceconnect.codegen.languages;


import io.swagger.codegen.DefaultCodegen;
import org.deviceconnect.codegen.DConnectCodegenConfig;

import java.io.File;

public abstract class DefaultDConnectCodegen extends DefaultCodegen implements DConnectCodegenConfig {

    private File[] inputSpecFiles;

    @Override
    public File[] getInputSpecFiles() {
        return inputSpecFiles;
    }

    @Override
    public void setInputSpecFiles(File[] specs) {
        this.inputSpecFiles = specs;
    }
}

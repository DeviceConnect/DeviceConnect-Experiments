package org.deviceconnect.codegen;


import io.swagger.codegen.CodegenConfig;

import java.io.File;

public interface DConnectCodegenConfig extends CodegenConfig {

    File[] getInputSpecFiles();

    void setInputSpecFiles(File[] specs);

}

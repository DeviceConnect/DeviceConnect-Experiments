package org.deviceconnect.codegen;


import io.swagger.codegen.CodegenConfig;
import io.swagger.models.Swagger;

import java.io.File;
import java.util.Map;

public interface DConnectCodegenConfig extends CodegenConfig {

    Map<String, Swagger> getProfileSpecs();

    void setProfileSpecs(Map<String, Swagger> profileSpecs);

    String getDefaultDisplayName();

}

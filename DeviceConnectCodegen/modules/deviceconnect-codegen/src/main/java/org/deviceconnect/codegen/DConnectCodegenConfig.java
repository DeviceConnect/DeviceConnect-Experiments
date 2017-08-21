package org.deviceconnect.codegen;


import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.models.Swagger;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.Map;

public interface DConnectCodegenConfig extends CodegenConfig {

    Map<String, Swagger> getProfileSpecs();

    void setProfileSpecs(Map<String, Swagger> profileSpecs);

    String getDefaultDisplayName();

    ValidationResultSet validateOptions(CommandLine cmd, ClientOpts clientOpts);
}

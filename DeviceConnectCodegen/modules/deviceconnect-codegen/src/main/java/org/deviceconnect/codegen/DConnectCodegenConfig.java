/*
 DConnectCodegenConfig.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen;


import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.models.Swagger;
import org.apache.commons.cli.CommandLine;

import java.util.Map;

public interface DConnectCodegenConfig extends CodegenConfig {

    Map<String, Swagger> getProfileSpecs();

    void setProfileSpecs(Map<String, Swagger> profileSpecs);

    Swagger getOriginalSwagger();

    void setOriginalSwagger(Swagger swagger);

    String getDefaultDisplayName();

    ValidationResultSet validateOptions(CommandLine cmd, ClientOpts clientOpts);
}
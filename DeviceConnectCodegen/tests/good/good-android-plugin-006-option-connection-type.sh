#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang            deviceConnectAndroidPlugin \
     --package-name    com.mydomain.testplugin006 \
     --display-name Test006 \
     --input-spec      profile-specs/swagger.json \
     --output          output/android-plugin-006 \
     --connection-type broadcast
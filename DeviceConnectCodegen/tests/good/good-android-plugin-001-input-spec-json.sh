#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang       deviceConnectAndroidPlugin \
     --package-name com.mydomain.testplugin001 \
     --display-name Test001 \
     --input-spec profile-specs/swagger.json \
     --output     output/android-plugin-001
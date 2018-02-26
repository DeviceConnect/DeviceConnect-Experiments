#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang           deviceConnectAndroidPlugin \
     --package-name   com.mydomain.testplugin004 \
     --display-name Test004 \
     --input-spec-dir profile-specs/swagger-files-multiple \
     --output         output/android-plugin-004
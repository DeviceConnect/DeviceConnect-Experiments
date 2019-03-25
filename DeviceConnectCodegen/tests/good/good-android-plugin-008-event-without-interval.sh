#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang                  deviceConnectAndroidPlugin \
     --package-name com.mydomain.testplugin008 \
     --display-name Test008 \
     --input-spec-dir        profile-specs/swagger-files-android-plugin-008/ \
     --output                output/android-plugin-008 \
     --gradle-plugin-version 3.3.2

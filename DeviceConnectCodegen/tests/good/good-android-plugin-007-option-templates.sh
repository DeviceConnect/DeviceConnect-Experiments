#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang                  deviceConnectAndroidPlugin \
     --package-name com.mydomain.testplugin007 \
     --display-name Test007 \
     --input-spec            profile-specs/swagger.json \
     --output                output/android-plugin-007 \
     --template-dir          test-assets/007/templates/ \
     --sdk                   test-assets/007/sdk/ \
     --signing-configs       test-assets/007/signingConfigs/ \
     --gradle-plugin-version 3.0.1
#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang         deviceConnectAndroidPlugin \
     --package-name com.mydomain.testplugin005 \
     --display-name Test005 \
     --input-spec   profile-specs/swagger.json \
     --output       output/android-plugin-005 \
     --class-prefix Your
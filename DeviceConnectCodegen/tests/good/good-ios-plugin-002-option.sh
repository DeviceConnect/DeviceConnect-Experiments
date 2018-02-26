#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang         deviceConnectIosPlugin \
     --input-spec   profile-specs/swagger.json \
     --output       output/ios-plugin-002 \
     --display-name YourPlugin \
     --class-prefix Your
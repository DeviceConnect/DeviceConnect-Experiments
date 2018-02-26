#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang       deviceConnectIosPlugin \
     --input-spec profile-specs/swagger.json \
     --output     output/ios-plugin-001
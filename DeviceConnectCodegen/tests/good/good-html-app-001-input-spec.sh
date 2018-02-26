#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang       deviceConnectHtmlApp \
     --input-spec profile-specs/swagger.json \
     --output     output/html-app-001
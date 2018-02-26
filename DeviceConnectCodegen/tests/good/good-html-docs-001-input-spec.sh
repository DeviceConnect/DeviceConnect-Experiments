#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang       deviceConnectHtmlDocs \
     --input-spec profile-specs/swagger.json \
     --output     output/html-docs-001
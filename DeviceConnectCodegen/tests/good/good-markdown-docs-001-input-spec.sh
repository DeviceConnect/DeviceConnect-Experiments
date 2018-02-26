#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang       deviceConnectMarkdownDocs \
     --input-spec profile-specs/swagger.json \
     --output     output/markdown-docs-001
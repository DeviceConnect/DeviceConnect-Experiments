#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang           deviceConnectMarkdownDocs \
     --input-spec-dir profile-specs/swagger-files \
     --output         output/markdown-docs-001
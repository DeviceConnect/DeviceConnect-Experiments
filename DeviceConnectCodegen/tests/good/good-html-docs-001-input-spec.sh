#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang           deviceConnectHtmlDocs \
     --input-spec-dir profile-specs/swagger-files \
     --output         output/html-docs-001
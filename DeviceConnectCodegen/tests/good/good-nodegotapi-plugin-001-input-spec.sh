#!/bin/sh -x

java -jar ../../bin/deviceconnect-codegen.jar \
     --lang       gotapiNodePlugin \
     --input-spec profile-specs/swagger.json \
     --output     output/nodegotapi-plugin-001
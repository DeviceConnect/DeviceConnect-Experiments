#!/bin/sh -x

LANG="deviceConnectIosPlugin"
SPEC_DIR="./sample-profile-specs"
OUTPUT_DIR="./output/iOS/MyPlugin"
DISPLAY_NAME="MyPlugin"

JAR_FILE="../bin/deviceconnect-codegen.jar"
ARGS="--input-spec-dir $SPEC_DIR  --lang $LANG --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

#!/bin/sh -x

SPEC_DIR="spec"
OUTPUT_DIR="output/Android/MyPlugin"
PACKAGE_NAME="com.mydomain.myplugin"
DISPLAY_NAME="MyPlugin"
JAR_FILE="bin/deviceconnect-codegen-cli.jar"
ARGS="--input-spec-dir $SPEC_DIR  --lang deviceConnectAndroidPlugin --package-name  $PACKAGE_NAME --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

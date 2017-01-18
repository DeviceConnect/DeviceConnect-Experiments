#!/bin/sh -x

executable="bin/deviceconnect-codegen-cli.jar"
args="--input-spec-dir spec  --lang deviceConnectIosPlugin  --display-name MyPlugin  --output output/iOS/MyPlugin"

java -Dfile.encoding=UTF-8 -jar $executable $args

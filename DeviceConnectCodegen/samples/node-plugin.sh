#!/bin/sh -x

# スケルトンコード種別: node-gotapiプラグイン
LANG="gotapiNodePlugin"

# プロファイル定義ファイル
SPEC="./sample-profile-specs/swagger-files"

# スケルトンコード出力先
# NOTE: フォルダ名の先頭に node-gotapi-plugin- を付けること.
OUTPUT_DIR="./output/NodeJS/node-gotapi-plugin-sample"

# node-gotapiプラグインの表示名
DISPLAY_NAME="MyPlugin"

# スケルトンコード生成ツールのバイナリ
JAR_FILE="../bin/deviceconnect-codegen.jar"

ARGS="--input-spec-dir $SPEC  --lang $LANG --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

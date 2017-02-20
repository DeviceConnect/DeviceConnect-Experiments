#!/bin/sh -x

# スケルトンコード種別: node-gotapiプラグイン
LANG="gotapiNodePlugin"

# プロファイル定義ファイルのディレクトリ
SPEC="./sample-profile-specs/swagger.json"

# スケルトンコード出力先
# NOTE: フォルダ名の先頭に node-gotapi-plugin- を付けること.
OUTPUT_DIR="./output/NodeJS/node-gotapi-plugin-sample"

# Androidプラグインのパッケージ名
PACKAGE_NAME="node-gotapi-plugin-sample"

# Androidプラグインの表示名
DISPLAY_NAME="MyPlugin"

# スケルトンコード生成ツールのバイナリ
JAR_FILE="../bin/deviceconnect-codegen.jar"

ARGS="--input-spec $SPEC  --lang $LANG --package-name  $PACKAGE_NAME --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

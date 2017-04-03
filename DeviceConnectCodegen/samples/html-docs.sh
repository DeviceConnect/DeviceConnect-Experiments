#!/bin/sh -x

# スケルトンコード種別: APIリファレンス
LANG="deviceConnectHtmlDocs"

# プロファイル定義ファイル
SPEC="../standard-profile-specs"

# スケルトンコード出力先
OUTPUT_DIR="./output/html/Device_Connect_RESTful_API_Specification"

# API仕様書の表示名
DISPLAY_NAME="Device_Connect_RESTful_API_Specification"

# スケルトンコード生成ツールのバイナリ
JAR_FILE="../bin/deviceconnect-codegen.jar"

ARGS="--input-spec-dir $SPEC  --lang $LANG  --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

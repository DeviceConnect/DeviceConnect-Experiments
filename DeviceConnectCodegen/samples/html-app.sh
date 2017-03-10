#!/bin/sh -x

# スケルトンコード種別: HTMLアプリ
LANG="deviceConnectHtmlApp"

# プロファイル定義ファイル
SPEC="../standard-profile-specs"

# スケルトンコード出力先
OUTPUT_DIR="./output/html/MyApp"

# HTMLアプリの表示名
DISPLAY_NAME="MyApp"

# スケルトンコード生成ツールのバイナリ
JAR_FILE="../bin/deviceconnect-codegen.jar"

ARGS="--input-spec-dir $SPEC  --lang $LANG --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

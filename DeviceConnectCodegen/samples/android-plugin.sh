#!/bin/sh -x

# スケルトンコード種別: Androidプラグイン
LANG="deviceConnectAndroidPlugin"

# プロファイル定義ファイルのディレクトリ
SPEC="./sample-profile-specs/swagger.json"

# スケルトンコード出力先
OUTPUT_DIR="./output/Android/MyPlugin"

# Androidプラグインのパッケージ名
PACKAGE_NAME="com.mydomain.myplugin"

# Androidプラグインの表示名
DISPLAY_NAME="MyPlugin"

# スケルトンコード生成ツールのバイナリ
JAR_FILE="../bin/deviceconnect-codegen.jar"

ARGS="--input-spec $SPEC  --lang $LANG --package-name  $PACKAGE_NAME --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

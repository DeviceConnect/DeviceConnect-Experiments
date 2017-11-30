#!/bin/sh -x

# スケルトンコード種別: Androidプラグイン
LANG="deviceConnectAndroidPlugin"

# プロファイル定義ファイル
SPEC="../samples/sample-profile-specs/swagger-files"

# スケルトンコード出力先
OUTPUT_DIR="./output/Android/MyPlugin"

# Androidプラグインのパッケージ名
PACKAGE_NAME="com.mydomain.myplugin"

# Androidプラグインの連携タイプ
CONNECTION_TYPE="binder"

# Androidプラグインの表示名
DISPLAY_NAME="MyPlugin"

# スケルトンコード生成ツールのバイナリ
JAR_FILE="../bin/deviceconnect-codegen.jar"

ARGS="--undefined-option --input-spec-dir $SPEC --lang $LANG --package-name $PACKAGE_NAME  --connection-type $CONNECTION_TYPE  --display-name $DISPLAY_NAME  --output $OUTPUT_DIR"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

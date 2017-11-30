#!/bin/sh -x

# Androidプラグインのパッケージ名
PACKAGE_NAME="com.mydomain.myplugin"

# Androidプラグインの連携タイプ
CONNECTION_TYPE="binder"

# Androidプラグインの表示名
DISPLAY_NAME="MyPlugin"

# スケルトンコード生成ツールのバイナリ
JAR_FILE="../bin/deviceconnect-codegen.jar"

ARGS="--package-name $PACKAGE_NAME  --connection-type $CONNECTION_TYPE  --display-name $DISPLAY_NAME"

java -Dfile.encoding=UTF-8 -jar $JAR_FILE $ARGS

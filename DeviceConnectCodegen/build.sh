#!/bin/sh

CMDNAME=`basename $0`
PROJECT_DIR=$(cd $(dirname $0) && pwd)

CODEGEN_VERSION_TAG="{{{codegen-version}}}"
CODEGEN_VERSION_VALUE=$1
README_TEMPLATE_FILE=${PROJECT_DIR}/README.md.mustache
README_FILE=${PROJECT_DIR}/README.md

# 引数チェック
if [ $# -ne 1 ]; then
echo "Usage: ${CMDNAME} <codegen_version>" 1>&2
echo "Example: ${CMDNAME} 1.2.3" 1>&2
echo "         ${CMDNAME} 1.2.3-alpha" 1>&2
echo "         ${CMDNAME} 1.2.3-beta.1" 1>&2
exit 1
fi

# 配布用zipへのリンクを更新
echo "DeviceConnectCodegen ver. ${CODEGEN_VERSION_VALUE}"
sed -e "s/${CODEGEN_VERSION_TAG}/${CODEGEN_VERSION_VALUE}/g" ${README_TEMPLATE_FILE} > ${README_FILE}

# 配布用zipをビルド
mvn package

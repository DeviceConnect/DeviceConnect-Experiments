#!/bin/sh

PROJECT_DIR=$(cd $(dirname $0) && pwd)
TEMP_DIR=${PROJECT_DIR}/temp-`date +%s`
REVISION=142f49da15ce351a7e2ab5fad8efe97a27aa474a
SPEC_ZIP_NAME=${REVISION}.zip
SPEC_ZIP_URL=https://github.com/TakayukiHoshi1984/DeviceConnect-Spec/archive/${SPEC_ZIP_NAME}
SPEC_DIR=${PROJECT_DIR}/standard-profile-specs

if [ -e ${SPEC_DIR} ] ; then
    echo "Delete ${SPEC_DIR}"
    rm -rf ${SPEC_DIR}
else
    echo "Create ${SPEC_DIR}"
    mkdir -p ${SPEC_DIR}
fi

mkdir -p ${TEMP_DIR}
cd ${TEMP_DIR}
curl -L -O -k ${SPEC_ZIP_URL}
unzip -d . ${SPEC_ZIP_NAME}
cp ${TEMP_DIR}/DeviceConnect-Spec-${REVISION}/api/*.json ${SPEC_DIR}
rm -rf ${TEMP_DIR}

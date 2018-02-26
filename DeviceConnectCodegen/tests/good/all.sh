#!/bin/sh -x

rm -rf ./output/

./good-android-plugin-001-input-spec-json.sh
./good-android-plugin-002-input-spec-yaml.sh
./good-android-plugin-003-input-specs-single.sh
./good-android-plugin-004-input-specs-multiple.sh
./good-android-plugin-005-option-names.sh
./good-android-plugin-006-option-connection-type.sh
./good-android-plugin-007-option-templates.sh
./good-html-app-001-input-spec.sh
./good-html-app-002-option.sh
./good-html-docs-001-input-spec.sh
./good-ios-plugin-001-input-spec.sh
./good-ios-plugin-002-option.sh
./good-markdown-docs-001-input-spec.sh
./good-nodegotapi-plugin-001-input-spec.sh
./good-nodegotapi-plugin-002-option.sh
./good-nodejs-emulator-001-input-spec.sh
./good-nodejs-emulator-002-option.sh
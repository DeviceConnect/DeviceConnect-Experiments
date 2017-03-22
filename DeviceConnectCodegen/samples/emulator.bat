:: スケルトンコード種別: DeviceConnectプラグイン
set LANG=deviceConnectEmulator

:: プロファイル定義ファイル
set SPEC=.\sample-profile-specs\swagger.json

:: スケルトンコード出力先
set OUTPUT_DIR=.\output\NodeJS\Emulator

:: node-gotapiプラグインの表示名
set DISPLAY_NAME=DeviceConnectEmulator

:: スケルトンコード生成ツールのバイナリ
set JAR_FILE=..\bin\deviceconnect-codegen.jar

java -Dfile.encoding=UTF-8 -jar %JAR_FILE% --input-spec %SPEC%  --lang %LANG%  --display-name %DISPLAY_NAME%  --output %OUTPUT_DIR%
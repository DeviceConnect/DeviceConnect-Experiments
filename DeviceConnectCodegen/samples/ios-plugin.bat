:: スケルトンコード種別: iOSプラグイン
set LANG=deviceConnectIosPlugin

:: プロファイル定義ファイル
set SPEC=.\sample-profile-specs\swagger.json

:: スケルトンコード出力先
set OUTPUT_DIR=.\output\iOS\MyPlugin

:: node-gotapiプラグインの表示名
set DISPLAY_NAME=MyPlugin

:: スケルトンコード生成ツールのバイナリ
set JAR_FILE=..\bin\deviceconnect-codegen.jar

java -Dfile.encoding=UTF-8 -jar %JAR_FILE% --input-spec %SPEC%  --lang %LANG%  --display-name %DISPLAY_NAME%  --output %OUTPUT_DIR%
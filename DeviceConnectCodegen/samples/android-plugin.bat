:: スケルトンコード種別: Androidプラグイン
set LANG=deviceConnectAndroidPlugin

:: プロファイル定義ファイルのディレクトリ
set SPEC_DIR=.\sample-profile-specs

:: スケルトンコード出力先
set OUTPUT_DIR=.\output\Android\MyPlugin

:: Androidプラグインのパッケージ名
set PACKAGE_NAME=com.mydomain.myplugin

:: Androidプラグインの表示名
set DISPLAY_NAME=MyPlugin

:: スケルトンコード生成ツールのバイナリ
set JAR_FILE=..\bin\deviceconnect-codegen.jar

java -Dfile.encoding=UTF-8 -jar %JAR_FILE% --input-spec-dir %SPEC_DIR%  --lang %LANG% --package-name %PACKAGE_NAME%  --display-name %DISPLAY_NAME%  --output %OUTPUT_DIR%
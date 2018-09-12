:: �X�P���g���R�[�h���: node-gotapi�v���O�C��
set LANG=gotapiNodePlugin

:: �v���t�@�C����`�t�@�C��
set SPEC=.\sample-profile-specs\swagger-files

:: �X�P���g���R�[�h�o�͐�
set OUTPUT_DIR=.\output\NodeJS\node-gotapi-plugin-sample

:: node-gotapi�v���O�C���̕\����
set DISPLAY_NAME=MyPlugin

:: �X�P���g���R�[�h�����c�[���̃o�C�i��
set JAR_FILE=..\bin\deviceconnect-codegen.jar

java -Dfile.encoding=UTF-8 -jar %JAR_FILE% --input-spec-dir %SPEC%  --lang %LANG%  --display-name %DISPLAY_NAME%  --output %OUTPUT_DIR%
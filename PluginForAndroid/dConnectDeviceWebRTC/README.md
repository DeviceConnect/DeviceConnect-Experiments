# WebRTCプラグイン

このWebRTCプラグインでは、WebRTCの操作を行うための機能を提供します。

このプラグイン旧Skyway APIを使用しています。
そのため、旧SkywayAPIが行なっていたシグナリングサーバの機能を自作する必要があります。
## 目次
* [開発環境](#section1)
* [サポートするプロファイル](#section2)
* [ビルド手順](#section3)
  * [Android WebRTCプラグインのBuildに必要なパッケージ](#section3.1)<br>
     * [Device Connect SDK](#section3.1.1)
  * [プロジェクトのビルド手順](#section3.2)
     * [Android Studio](#section3.2.1)
       * [dConnectDeviceWebRTCのImport](#section3.2.1.1)
       * [dConnectDeviceWebRTCのBuild](#section3.2.1.2)

## <a name="section1">開発環境</a>
Android Studio 2.2.1以上

## <a name="section2">サポートするプロファイル</a>

* videoChat

## <a name="section3">ビルド手順</a>

#### ※注意1：<br>本Projectの文字コードはUTF-8を使用しています。ビルドエラーが表示される場合は、AndroidStudioの文字コードをUTF-8に設定してください。<br>
<a href="images/utf-8.png" target="_blank">
<img src="images/utf-8.png" border="0" width="352" alt="" />
</a><br>
#### ※注意2：<br>Windowsでは展開するディレクトリ位置によっては「パスが長すぎる」とエラーになりますので、その場合はDeviceConnect-Androidをルートフォルダに展開してください。<br>
※本エラーは、バージョンに関わらず、すべてのWindowsで発生する可能性がありますので、ご注意ください。
詳しくは<a href="http://windows.microsoft.com/ja-jp/windows/file-names-extensions-faq#1TC=windows-7" target="_blank">こちら</a>をご覧ください。


## <a name="section3.1"> Android WebRTCプラグインのBuildに必要なパッケージ</a><br>
Android WebRTCプラグインのBuildに必要なパッケージは以下の通りになります。

### <a name="section3.1.1"> Device Connect SDK</a><br>

|項目|説明|
|:--|:--|
|dConnectDevicePluginSDK|デバイスプラグイン用のSDK。dConnectSDKAndroidをライブラリとして参照。|
|dConnectSDKAndroid|Androidに関連する部分のSDK。|

## <a name="section3.2"> プロジェクトのBuild手順</a><br>
{レポジトリフォルダ}は、githubからプロジェクトをチェックアウトしたフォルダを指します。<br>
また、本書ではAndroidフォルダにプロジェクトをチェックアウトしたことを前提で説明を行います。<br>

### <a name="section3.2.1"> Android Studio</a>
#### <a name="section3.2.1.1">dConnectDeviceWebRTCのImport</a>

まずAndroid Studioを起動してください。<br>
Quick Startの[Open an existing Android Studio project]を選択してください。<br>

<a href="images/WebRTCImage001.png" target="_blank">
<img src="images/WebRTCImage001.png" border="0"
 width="364" height="274" alt="" /></a>

dConnectDeviceWebRTCを選択してください。<br>
dConnectDeviceWebRTCは<br>
{レポジトリフォルダ}/dConnectDevicePlugin/dConnectDeviceWebRTCにあります。<br>
<br>
<a href="images/WebRTCImage002.png" target="_blank">
<img src="images/WebRTCImage002.png" border="0"
 width="443" alt="" /></a>

 dConnectDeviceWebRTCを選択すると、このようなウィンドウが表示されます。<br>
 赤く囲まれたところを押してください。<br>

 <a href="images/WebRTCImage003.png" target="_blank">
 <img src="images/WebRTCImage003.png" border="0"
  width="304" alt="" /></a>

#### <a name="section3.2.1.2">libImageUtils.soのBuild</a>

dConnectDeviceWebRTCをビルドするためには、libImageUtils.soのビルドが必要になります。
libImageUtils.soは、RGB形式のデータをYV12形式のデータに変換を高速に行うためのライブラリです。

libImageUtils.soをビルドするためには、Android NDKが必要になりますので、Android StudioのProject Structure画面からDownload Android NDKをクリックしてインストールを行ってください。<br>
既にインストールされている場合には、スキップしてください。

<a href="images/WebRTCImage004.png" target="_blank">
<img src="images/WebRTCImage004.png" border="0"
 width="316" alt="" /></a>

ビルドするには、Android Studioの下方にあるTerminalをクリックします。
これで、Terminalが開きます。

<a href="images/WebRTCImage005.png" target="_blank">
<img src="images/WebRTCImage005.png" border="0"
 width="316" alt="" /></a>

開いたTerminal上で、以下のコマンドを実行することでlibImageUtils.soをビルドすることができます。

```
$ cd app/src/main
$ ndk-build
```

<a href="images/WebRTCImage006.png" target="_blank">
<img src="images/WebRTCImage006.png" border="0"
 width="316" alt="" /></a>

ビルドに成功するとapp/src/main/libsにsoファイルが生成されます。

#### <a name="section3.2.1.3">dConnectDeviceWebRTCのBuild</a>

libImageUtils.soのビルドが終わりましたら、Android Studioの画面にて、以下の画像にあるように赤く囲まれたところを押してください。<br>

<a href="images/WebRTCImage007.png" target="_blank">
<img src="images/WebRTCImage007.png" border="0"
 width="400" alt="" /></a>

 [Edit Configurations...]を選択してください。<br>

 <a href="images/WebRTCImage008.png" target="_blank">
 <img src="images/WebRTCImage008.png" border="0"
  width="400" alt="" /></a>

新しくウィンドウが出てきます。まず左側のAndroid Application内のappを選択すると右側のような画面が出てきます。
そこで、[Do not launch Activity]を選択してOKを押してください。<br>

<a href="images/WebRTCImage009.png" target="_blank">
<img src="images/WebRTCImage009.png" border="0"
 width="435" alt="" /></a>

そうするとこの画面に戻ります。<br>
この状態になったら赤く囲まれているRunボタンを押してください。<br>

<a href="images/WebRTCImage010.png" target="_blank">
<img src="images/WebRTCImage010.png" border="0"
 width="400" alt="" /></a>

その後、このような画面が出てくるので、[Choose a running device]にチェックを入れて、インストールしたい端末を選んでOKをクリックしてください。<br>

<a href="images/WebRTCImage011.png" target="_blank">
<img src="images/WebRTCImage011.png" border="0"
 width="316" alt="" /></a>


#### ※注意：setting.gradleファイルはレポジトリ上の構成でビルドできるように設定されているため、ディレクトリ構造を変えてビルドするときはsetting.gradleファイルを適宜書き換えてください。<br>



## 関連ページ

* [Skyway](https://webrtc.ecl.ntt.com/)

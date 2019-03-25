# DeviceConnect Codegen

## はじめに
DeviceConnect Codegenは、DeviceConnectシステム上で動作するプラグインおよびアプリケーションのスケルトンコードを生成するためのコマンドラインツールです。（以下、本ツールと呼びます）

本ツールへの入力として、DeviceConnectプロファイルの定義ファイルを自前で用意しておく必要があります。標準プロファイルについては全API仕様を定義したファイルを提供しますので、適宜編集してください。

### 前提知識
- OpenAPI Specification 2.0

### サポート範囲
本ツールで生成可能なスケルトンコードのタイプは下記のとおりです。

- Androidプラグイン
- iOSプラグイン
- [node-gotapi](https://github.com/futomi/node-gotapi)プラグイン
- HTMLアプリケーション
- DeviceConnectエミュレータ
- DeviceConnectAPIリファレンス
  - HTML
  - Markdown

## ファイル構成
本ツールはzipで配布されます。圧縮されているファイルの構成は下記のとおりです。

|ファイル名|説明|
|:--|:--|
|bin/deviceconnect-codegen.jar|本ツールのバイナリ。|
|src/deviceconnect-codegen|本ツールのソースコード。|
|standard-profile-specs/*.json|DeviceConnect標準のプロファイル定義ファイル群。|
|samples/android-plugin.sh|Androidプラグインのスケルトンコードを生成するシェルスクリプトのサンプル。|
|samples/ios-plugin.sh|iOSプラグインのスケルトンコードを生成するシェルスクリプトのサンプル。|
|samples/profiles-specs|シェルスクリプトのサンプルに入力するプロファイル定義ファイル群。|

## Get Started
[deviceconnect-codegen-project-1.8.3-dist.zip](https://github.com/TakayukiHoshi1984/DeviceConnect-Experiments/releases/tag/codegen-v1.8.3) をPC上の任意の場所にダウンロードし、解凍してください。

解凍後、ターミナルを起動し、以下のコマンドによりをサンプルのスケルトンコードを生成してください。

生成後の手順については、各出力先のREADME.mdを参照してください。

### Androidプラグインの場合
```
$ cd deviceconnect-codegen-project-X.Y.Z-dist/samples
$ ./android-plugin.sh
```

出力先: samples/output/Android/MyPlugin

### iOSプラグインの場合
```
$ cd deviceconnect-codegen-project-X.Y.Z-dist/samples
$ ./ios-plugin.sh
```

出力先: samples/output/iOS/MyPlugin

### node-gotapiプラグインの場合
```
$ cd deviceconnect-codegen-project-X.Y.Z-dist/samples
$ ./node-plugin.sh
```

出力先: samples/output/NodeJS/node-gotapi-plugin-sample

### HTMLアプリケーションの場合
```
$ cd deviceconnect-codegen-project-X.Y.Z-dist/samples
$ ./html-app.sh
```

出力先: samples/output/html/MyApp

### DeviceConnectエミュレータの場合
```
$ cd deviceconnect-codegen-project-X.Y.Z-dist/samples
$ ./emulator.sh
```

出力先: samples/output/NodeJS/Emulator

### DeviceConnectAPIリファレンス (HTML) の場合
```
$ cd deviceconnect-codegen-project-X.Y.Z-dist/samples
$ ./html-docs.sh
```

出力先: samples/output/html/Device_Connect_RESTful_API_Specification

### DeviceConnectAPIリファレンス (Markdown) の場合
```
$ cd deviceconnect-codegen-project-X.Y.Z-dist/samples
$ ./md-docs.sh
```

出力先: samples/output/html/Device_Connect_RESTful_API_Specification

## リファレンス

### 実行方法
下記のようなJavaコマンドで実行してください。指定可能なオプションは次節参照。

```
java -jar bin/deviceconnect-codegen.jar [オプション]
```

### オプション一覧

<table>
<thead><tr><th>オプション</th><th>説明</th><th>省略</th></tr></thead>
<tbody>

<tr>
<td valign="top"><pre>--lang</pre></td>
<td valign="top">
スケルトン生成対象の指定。下記のいずれかの値を引数とする。
<ul>
<li>Androidプラグイン: deviceConnectAndroidPlugin</li>
<li>iOSプラグイン: deviceConnectIosPlugin</li>
<li>node-gotapiプラグイン: gotapiNodePlugin</li>
<li>HTMLアプリケーション: deviceConnectHtmlApp</li>
<li>DeviceConnectエミュレータ: deviceConnectEmulator</li>
<li>DeviceConnectAPIリファレンス (HTML): deviceConnectHtmlDocs</li>
<li>DeviceConnectAPIリファレンス (Markdown): deviceConnectMarkdownDocs</li>
</ul>
</td>
<td valign="top">-</td>
</tr>

<tr>
<td valign="top"><pre>--input-spec</pre></td>
<td valign="top">
スケルトンコードでサポートするプロファイル仕様の指定。プロファイル仕様定義ファイルへの絶対パスまたは相対パスを引数とする。ファイルの形式はJSON・YAMLのいずれかとする。指定したファイル内にサポートするすべてのAPIを定義すること。
</td>
<td valign="top">*1</td>
</tr>

<tr>
<td valign="top"><pre>--input-spec-dir</pre></td>
<td valign="top">
スケルトンコードでサポートするプロファイル仕様の指定。プロファイル仕様定義ファイルを格納したディレクトリへの絶対パスまたは相対パスを引数とする。各ファイルの形式はそれぞれJSON・YAMLのいずれかとする。ファイル名は <プロファイル名>.<拡張子>であること。
</td>
<td valign="top">*1</td>
</tr>

<tr>
<td valign="top"><pre>--output</pre></td>
<td valign="top">
スケルトンコードの出力先の指定。PC上の任意のディレクトリへの絶対パスまたは相対パスを引数とする。<br>
<br>
存在しないディレクトリが指定された場合は、そのディレクトリを新規で作成する。存在していた場合は、出力内容を強制的に上書きする。
</td>
<td valign="top">-</td>
</tr>

<tr>
<td valign="top"><pre>--display-name</pre></td>
<td valign="top">
<b>[プラグインまたはアプリケーションのみ有効]</b><br>
スケルトンコードの名前の指定。<br>
<br>
プラグインの場合、System APIによって取得できるデバイスプラグインの名前として使用される。アプリケーションの場合、主にアプリケーションのタイトルとして表示する名前として使用される。<br>
<br>
デフォルト値は、プラグインの場合は "MyPlugin"、アプリケーションの場合は "MyApp"。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top"><pre>--connection-type</pre></td>
<td valign="top">
<b>[Androidプラグインのみ有効]</b><br>
Device Connect Managerとの連携タイプの指定。下記のいずれかの値を引数とする。デフォルト値は、"binder"。
<ul>
<li>broadcast: IntentのブロードキャストによってDevice Connect Managerと通信する。</li>
<li>binder: Device Connect Managerとバインドし、AIDLで定義されたインターフェース経由でIntent形式のメッセージを送受信する。</li>
</ul>
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top"><pre>--package-name</pre></td>
<td valign="top">
<b>[Androidプラグインのみ有効]</b><br>
スケルトンコードのパッケージ名の指定。デフォルト値は、"com.mydomain.myplugin"。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top"><pre>--template-dir</pre></td>
<td valign="top">
<b>[Androidプラグインのみ有効]</b><br>
Androidプラグインのテンプレートを独自テンプレートに差し替えたい場合に、テンプレートをまとめたディレクトリへのパスを指定する。<br>
<br>
本オプションが省略された場合、本ツールにデフォルトのテンプレートが使⽤される。本オプションによって指定したテンプレートとデフォルトのテンプレートの間でファイル名の衝突が発⽣した場合は、本オプションで指定した⽅が優先される。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top"><pre>--sdk</pre></td>
<td valign="top">
<b>[Androidプラグインのみ有効]</b><br>
独自テンプレート向けのオプション。<br>
<br>
ソースコードで提供されたDeviceConnect SDKを使用する場合に、そのフォルダへの絶対パスまたは相対パスを指定する。
<br>
<br>
指定した⽂字列は、テンプレート側から {{{sdkLocation}}} という名前で参照可能。デフォルトのテンプレートでは参照されない。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top"><pre>--signing-configs</pre></td>
<td valign="top">
<b>[Androidプラグインのみ有効]</b><br>
独自テンプレート向けのオプション。<br>
<br>
Androidプラグインのビルド時に使用される署名情報の保存されているフォルダへの絶対パスまたは相対パスを指定する。<br>
<br>
指定した⽂字列は、テンプレート側から {{{signingConfigsLocation}}} という名前で参照可能。デフォルトのテンプレートでは参照されない。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top"><pre>--gradle-plugin-version</pre></td>
<td valign="top">
<b>[Androidプラグインのみ有効]</b><br>
プラグインのビルドツールとして使⽤するAndroidPlugin for Gradle のバージョン名を指定する。省略された場合は 3.0.0 とする。<br>
<br>
指定した⽂字列は、テンプレート側から {{{gradlePluginVersion}}} という名前で参照可能。デフォルトのテンプレートからも参照される。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top"><pre>--class-prefix</pre></td>
<td valign="top">
<b>[Androidプラグイン・iOSプラグインの場合のみ有効]</b><br>
出力されるクラス名のプレフィクスの指定。<br>
<br>
標準プロファイルを実装する場合に、SDK側から提供される既定クラスと名前を区別するために使用される。独自プロファイルの場合は適用されない。<br>
<br>
デフォルト値は、 "My"。 
</td>
<td valign="top">o</td>
</tr>

</tbody>
</table>

*1: `--input-spec` または `--input-spec-dir` のいずれかを必ず指定すること。両方指定された場合は `--input-spec` が優先される。

## 開発環境
### ビルドツール
Apache Maven 3.3.9+

### ビルド方法
DeviceConnectCodegenのルートディレクトリで下記のコマンドを実行すると、本ツールをビルドできます。

```
$ mvn package
```

ビルドを実行すると、本ツールのバイナリと[配布用zip](#ファイル構成)がそれぞれ下記の場所に出力されます。

|項目|出力先|
|:--|:--|
|バイナリ|DeviceConnectCodegen/bin/deviceconnect-codegen.jar|
|配布用zip|DeviceConnectCodegen/target/deviceconnect-codegen-project-1.8.3-dist.zip|

## 参考リンク
- [Swagger](http://swagger.io/)
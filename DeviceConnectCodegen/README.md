DeviceConnect Codegenは、DeviceConnectシステム上で動作するプラグインおよびアプリケーションのスケルトンコードを生成するためのコマンドラインツールです。（以下、本ツールと呼びます）

本ツールへの入力として、DeviceConnectプロファイルの定義ファイルを自前で用意しておく必要があります。標準プロファイルについては全API仕様を定義したファイルを提供しますので、適宜編集してください。

## 前提知識
- OpenAPI Specification 2.0

## サポート範囲
本ツールで生成可能なスケルトンコードのタイプは下記のとおりです。

- Androidプラグイン
- iOSプラグイン
- [未対応] Nodeプラグイン
- [未対応] HTMLアプリケーション
- [未対応] DeviceConnectエミュレータ

## ファイル構成
本ツールはzipで配布されます。圧縮されているファイルの構成は下記のとおりです。

|ファイル名|説明|
|:--|:--|
|bin/deviceconnect-codegen.jar|本ツールのバイナリ。|
|standard-profile-specs/|DeviceConnect標準プロファイルの定義ファイル群。|
|samples/android-plugin.sh|Androidプラグインのスケルトンコードを生成するシェルスクリプトのサンプル。|
|samples/ios-plugin.sh|iOSプラグインのスケルトンコードを生成するシェルスクリプトのサンプル。|
|samples/profiles-specs|シェルスクリプトのサンプルに入力するプロファイル定義ファイル群。|

## 実行方法
下記のようなJavaコマンドで実行してください。指定可能なオプションは次節参照。

```
java -jar bin/deviceconnect-codegen.jar [オプション]
```

## オプション仕様
<table>
<thead><tr><th>オプション</th><th>説明</th><th>省略</th></tr></thead>
<tbody>

<tr>
<td valign="top">--lang</td>
<td valign="top">
スケルトン生成対象の指定。下記のいずれかの値を引数とする。
<ul>
<li>Androidプラグイン: deviceConnectAndroidPlugin</li>
<li>iOSプラグイン: deviceConnectIosPlugin</li>
<li>[未対応] NodeJSプラグイン: deviceConnectNodePlugin</li>
<li>[未対応] HTMLアプリケーション: deviceConnectHtmlApp</li>
<li>[未対応] DeviceConnectエミュレータ: deviceConnectEmulator</li>
</ul>
</td>
<td valign="top">-</td>
</tr>

<tr>
<td valign="top">--input-spec-dir</td>
<td valign="top">
スケルトンコードでサポートするプロファイル仕様の指定。プロファイル仕様定義ファイルを格納したディレクトリへの絶対パスまたは相対パスを引数とする。
</td>
<td valign="top">-</td>
</tr>

<tr>
<td valign="top">--outputdir-dir</td>
<td valign="top">
スケルトンコードの出力先の指定。PC上の任意のディレクトリへの絶対パスまたは相対パスを引数とする。<br>
<br>
存在しないディレクトリが指定された場合は、そのディレクトリを新規で作成する。存在していた場合は、出力内容を強制的に上書きする。
</td>
<td valign="top">-</td>
</tr>

<tr>
<td valign="top">--display-name</td>
<td valign="top">
スケルトンコードの名前の指定。<br>
<br>
プラグインの場合、System APIによって取得できるデバイスプラグインの名前として使用される。アプリケーションの場合、主にアプリケーションのタイトルとして表示する名前として使用される。<br>
<br>
デフォルト値は、プラグインの場合は "MyPlugin"、アプリケーションの場合は "MyApp"。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top">--package-name</td>
<td valign="top">
<b>[Androidプラグインのみ有効]</b><br>
スケルトンコードのパッケージ名の指定。デフォルト値は、"com.mydomain.myplugin"。
</td>
<td valign="top">o</td>
</tr>

<tr>
<td valign="top">--class-name-prefix</td>
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

## Get Started
[DeviceConnectCodegen]()をPC上の任意の場所にダウンロードし、解凍してください。解凍後、ターミナルを起動し、以下のコマンドによりをサンプルのスケルトンコードを生成してください。生成後の手順については、各出力先のREADME.mdを参照してください。

### Androidプラグインの場合
```
$ cd deviceconnect-codegen-project-0.1.0-dist/samples
$ ./android-plugin.sh
```

出力先: samples/output/Android/MyPlugin

### iOSプラグインの場合
```
$ cd deviceconnect-codegen-project-0.1.0-dist/samples
$ ./ios-plugin.sh
```

出力先: samples/output/iOS/MyPlugin

### Nodeプラグインの場合
T.B.D.

### HTMLアプリケーションの場合
T.B.D.

### DeviceConnectエミュレータの場合
T.B.D.

## 推奨開発環境
IntelliJ IDEA

## 参考
- [Swagger](http://swagger.io/)
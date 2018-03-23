# ADBプラグイン

ADB (Android Debug Bridge) コマンドをDevice Connectプロファイルとして提供するプラグインです。

現在は、`adb shell <shell_command>` に相当する機能のみをサポートしています。

## 目次
- <a href="#intro">ご使用の前に</a>
  - <a href="#target">対象OS</a>
  - <a href="#glossary">用語定義</a>
  - <a href="#dependencies">使用しているライブラリ</a>
- <a href="#build-manual">ビルド手順</a>
- <a href="#get-started">使用方法</a>
  - <a href="#device-setting">端末の設定</a>
  - <a href="#connect-to-local">ADB接続 (ホスト端末を操作する場合)</a>
  - <a href="#connect-to-external">ADB接続 (外部端末を操作する場合)</a>
  - <a href="#check">動作確認</a>
- <a href="#adb-profile">ADBプロファイル</a>
  - <a href="#adb-command-api">ADB Command API</a>
- <a href="#known-issues">既知の問題</a>
- <a href="#trouble-shooting">トラブルシューティング</a>

## <a name="intro">ご使用の前に</a>
+ 本プラグインはデバッグまたは研究目的にご使用ください。
+ 不要になった時点でアンインストールしてください。
+ PC上でadbコマンドを使用するための環境が準備されていることを前提とします。（PCへのadbコマンドのインストール手順の説明は省略します。）

### <a name="target">対象OS</a>
- 端末側: Android OS 4.0以上

### <a name="glossary">用語定義</a>
|用語|説明|
|:--|:--|
|ホスト端末|ADBプラグインがインストールされているAndroid端末。|
|外部端末|ホスト端末とは別のAndroid端末。|

### <a name="dependencies">使用しているライブラリ</a>
ADBプラグインの実装では下記のライブラリを使用しています。

|ライブラリ名|備考|
|:--|:--|
|[AdbLib](https://github.com/cgutman/AdbLib)|ADB通信用ライブラリ。本プラグインのプロジェクトにソースコードを同梱しています。本プラグインの要件に合わせて一部改修済です。改修箇所は [MODIFIED] というコメントで明示。|

## <a name="build-manual">ビルド手順</a>
### 1. ソースコードの入手
本リポジトリ(DeviceConnect-Experiments)のmasterブランチをビルド用PC上にチェックアウトします。

以下、チェックアウト先のフォルダへのパスを、[DeviceConnect-Experiments]で表します。

### 2. Android Studioのインストール
Android Studio (Androidアプリケーションの開発環境) を下記のダウンロードページから入手し、所定の手順に従ってビルド用PCにインストールしてください。

https://developer.android.com/studio/index.html

### 3. Android Studioでソースコードを開く
3.1. Android Studioを起動します。

3.2. Android Studioのツールバー上の File メニューから、 Open... を選択します。

3.3. 手順1でチェックアウトしたadbプラグインのソースコードのルートフォルダを選択します。
より具体的には、[DeviceConnect-Experiments]/PluginForAndroid/dConnectDeviceADB を選択します。

3.4. OKを押します。

3.5. Gradle Syncが自動的に開始されるので、終了するまで待機します。
（途中でAndroid Studio上でダイアログが表示された場合は、その案内に従ってください。）

### 4. Android端末とPCを接続
USBデバッグを有効にしたAndroid端末を、ビルド用PCにUSB接続してください。

### 5. ビルド実行
Android Studioのツールバー上の Run メニューの Run 'app' を選択してください。
これによりビルドが開始されます。
ビルドに成功した場合は、自動的に端末にAPKがインストールされます。

## <a name="get-started">使用方法</a>
本プラグインのAPKをホスト端末にインストールした後、下記の手順を実行してください。

### <a name="device-setting">端末の設定</a>
1. <a href="https://developer.android.com/studio/debug/dev-options.html#enable">端末標準の設定画面で「開発者オプション」を有効にする。</a>
1. <a href="https://developer.android.com/studio/debug/dev-options.html#debugging">「開発者オプション」内の「USBデバッグ」をONにする。</a>

### <a name="connect-to-local">ADB接続 (ホスト端末を操作する場合)</a>
ホスト端末を操作するためのDeviceConnectサービスは事前に登録されています。よって、あとは下記の設定をすることで、ホスト端末をADBプロファイルで操作可能になります。

1. Device Connect Managerのサービス一覧画面で「サービス検索」を実行する。
1. ホスト端末とPCをUSBケーブルで接続する。
1. PC側のターミナルで `adb tcpip 5555` を実行。
1. 数秒待機。
1. ホスト端末側に「USBデバッグを許可しますか？」というダイアログが表示されるので、OKを押す。

注: USB許可ダイアログを誤ってキャンセルしてしまった場合は、<a href="#ts1">こちらの手順</a>により復帰可能です。

### <a name="connect-to-external">ADB接続 (外部端末を操作する場合)</a>
外部端末を操作したい場合は、ADBプラグインの設定画面でDeviceConnectサービスを追加します。

1. Device Connect Managerのサービス一覧画面で「サービス検索」を実行する。
1. 外部端末とホスト端末を同じLANに接続する。
1. ADBプラグインの設定画面を開く。
1. 「追加」ボタンを押す。
1. 外部端末側のIPアドレスとポート番号を指定する。
1. 「追加」ボタンを押す。
1. 新しいサービスがオフライン状態で追加されていることを確認。
1. 外部端末をPCにUSB接続する。
1. PC側のターミナルでコマンド `adb tcpip <ポート番号>` を実行。
1. 数秒待機。
1. 外部端末の画面上に「USBデバッグ」の許可ダイアログが表示されるので、OKを押す。
1. 追加したサービスがオンライン状態になっていることを確認。

注: USB許可ダイアログを誤ってキャンセルしてしまった場合は、<a href="#ts1">こちらの手順</a>により復帰可能です。

### <a name="check">動作確認</a>
ADB Command APIを実行できるようになったことを確認します。ここでは例として、`adb shell input swipe 0 0 0 1000`と同じ処理をリクエストします。

1. Device Connect Managerのサービス一覧で、ADBサービスを選択。
1. 「adb」を選択。
1. 「POST /gotapi/adb」を選択。
1. commandパラメータに「adb shell input swipe 0 0 0 1000」と入力。
1. 「Send Request」を押す。
1. 自動スワイプにより、通知領域が引っ張り出されることを確認。
1. レスポンスとして下記と同様のJSONが表示されていることを確認。

> {
>     "result": 0,
>     "output": "",
>      ...
> }

## <a name="adb-profile">ADB プロファイル</a>

### <a name="adb-command-api">ADB Command API</a>

Android端末に対してADBコマンドを送信するワンショットAPIです。

現在は`adb shell <shell_command>`コマンドのみをサポートしています。また、対話形式でshellを実行する機能はサポート外ですので、ご注意ください。

#### <a name="request">リクエスト</a>
```
POST /gotapi/adb?serviceId=<サービスID>&command=<ADBコマンド>
```

例） ホスト端末の画面上で (0, 0) から (0, 1000) までスワイプさせる
```
POST /gotapi/adb?serviceId=127.0.0.1.f3f29547acf295320f1a0fc7485dded.localhost.deviceconnect.org&command=adb+shell+input+swipe+0+0+0+1000
```

#### <a name="response">レスポンス</a>
```
{
    "result": 0,
    "output": "<相手先で標準出力されたメッセージ>",
     ...
}
```

## <a name="known-issues">既知の問題</a>
<table>
<tr>
<th>No.</th><th>問題</th><th>原因</th>
</tr>
<tr>
<td valign="top"><a name="ts1">1</a></td>
<td valign="top">
USBデバッグ許可ダイアログで「このパソコンからのUSBデバッグを常に許可する」と有効にしてADB接続を行なっても、次回再接続時にまた許可ダイアログが表示されてしまう。
</td>
<td>
再接続時に前回とは異なる署名で接続しようとすることが原因。今後のバージョンで修正予定。
</td>
</tr>
</table>

## <a name="trouble-shooting">トラブルシューティング</a>

<table>
<tr>
<th>No.</th><th>内容</th><th>解決方法</th>
</tr>
<tr>
<td valign="top"><a name="ts1">1</a></td>
<td valign="top">
「USBデバッグを許可しますか？」というダイアログでOKを押さずにキャンセルしてしまった。その後、再表示されないので、OKを押せない。
</td>
<td valign="top">
下記の手順により、USBデバッグ許可ダイアログを再表示可能です。<br>
<ol>
<li>ADBプラグインのプラグイン情報画面で「再起動」を押す。</li>
<li>PC側のコンソールで adb tcpip 5555 を実行。</li>
<li>数秒待機。</li>
<li>ホスト端末側にダイアログが再表示されたら、OKを押す。</li>
</ol>
</td>
</tr>
</table>

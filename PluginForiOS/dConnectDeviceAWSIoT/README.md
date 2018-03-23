# AWSIot プラグイン

このAWSIotプラグインでは、AWSIotサーバを経由して、遠隔地にあるDevice Connect Managerを操作するための機能を提供します。

このプラグインを使用するために、AWSIotにアカウントが必要になります。

## 目次
* [開発環境](#section1)
* [サポートするプロファイル](#section2)
* [ビルド手順](#section3)

## <a name="section1">開発環境</a>
Xcode 8以上

## <a name="section2">サポートするプロファイル</a>

* airConditioner
* atmosphericPressure
* battery
* camera
* canvas
* connection
* deviceOrientation
* driveController
* ecg
* echonetLite
* file
* fileDescriptor
* geolocation
* gpio
* health
* humanDetection
* humidity
* illuminance
* keyEvent
* light
* mediaPlayer
* mediaStreamRecording
* messageHook
* notification
* omnidirectionalImage
* phone
* poseEstimation
* power
* powerMeter
* proximity
* remoteController
* serviceDiscovery
* serviceInformation
* setting
* sphero
* stressEstimation
* system
* temperature
* touch
* tv
* vibration
* videoChat
* walkState

# <a name="section3">ビルド手順</a>
1)CocoaPodsコマンドをインストールしてください。<br><br>
2)AWSのFrameworkファイルをダウンロードするために、ターミナルにて以下のディレクトリに移動します。<br>

```
cd {DeviceConnect-iOSが配置されているディレクトリ}/PluginForiOS/dConnectDeviceAWSIoT
```

pod installを実行してください。

```
pod install
```

dConnectDeviceAWSIoT.xcworkspaceというワークスペースができあがります。
その後、できあがったワークスペースでdConnectDeviceAWSIoT_frameworkをビルドしてください。
/binディレクトリにFrameworkとbundleが出来上がりますので、そちらをアプリに組み込んでください。

# Demo Plug-in for Healthcare Device
## Introduction
This is an experimental plug-in to demo functionalities with healthcare devices. The plug-in can retrieve data of "Heart Rate" / "Thermometer" / "Blood Pressure" / "Weight Scale" of healthcare devices which base on "Continua", the healthcare standardization alliance.

## License
This is licensed under MIT license as following,

Copyright (c) 2016 NTT DOCOMO, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Requirements

 - Wireless Connection Device with Android OS (Smartphone, STB etc...)
 - BLE Healthcare Devices. Following devices are just for your reference which were once checked their connectivity.
 
 | Category | Device Name | Device Vendor | Note |
 |------------|-----------------|------------------|-------|
 | Hearr Rate | MIO Alpha | MIO Global | |
 | Heart Rate | MIO FUSE | MIO Global | |
 | Heart Rate| PULSENSE PS-100 | EPSON | |
 | Heart Rate| PULSENSE PS-500 | EPSON | |
 | Thermometer | UT-201BLE | A&D | |
 | Blood Pressure | UA-651BLE | A&D | |
 | Weight Scale | UC-352-BLE | A&D | |

## How To Use
To demonstrate functionalities of healthcare plug-in, it is necessary to make preparation by setting up the environment in advance.

You can refer to the following procedures,

### Install Device Connect Manager / Healthcare Plug-in
 1. Download [dConnectManager.apk](https://github.com/DeviceConnect/DeviceConnect-Docs/blob/master/Bin/Android/dConnectManager.apk) and then install it into your using Android Device, *adb install -r dConnectManager.apk* .
 2. Download [dConnectDeviceHealth.apk](apk/dConnectDeviceHealth.apk) and then install it into the same device, *adb install -r dConnectDeviceHealth.apk* .

### Preparation Setting
1. Tap "Device Connect Manager" in your Android Device.
2. After setting page shown, check "Allow External IP". No further check for other options.
3. Tap "List of device plug-in" in the same page.
4. After list of plug-ins, tap "HealthCare(BLE) device plug-in".
5. Tap "Open settings"
6. Overview of the plug-in is shown, slide pages to [3/3].
  ("Eco-mode" is to save battery by advertising scanning interval from 10sec to 20sec. Do not check it if not necessary.)
7. Make sure if "Bluetooth" was already ON. (See upper right corner)
8. If Bluetooth is ON, tap "start scan".
9. Connectable devices shown in a list, tap "Register" for connection on right side.
10. If connected successfully, the button will be colored pink from blue, and also labeled "Unregister".
11. Go back to the setting page of Device Connect Manager, then turn on the Manager by toggling the switch of "Device Connect Manager".
12. Turn on the Web Server as well.

### Connect to device from Web App
1. Download "healthcareDemo" directory into your local and upload it onto the same Android Device, *adb push healthcareDemo/ /sdcard/org.deviceconnect.android.manager*
2. Access the contents of the app with below address via web browser like Google Chrome, Mozilla Firefox.
http://*"your devices's ip address"*:8080/index.html
3. 4 menus are shown for measuring "Heart Rate" / "Thermometer" / "Blood Pressure" / "Weight Scale" on the top page.
4. After chose one of the menus, select a target device to which you already paired.
5. After selected the device, tap "start" button. Then finally you can see the measured value on the app!


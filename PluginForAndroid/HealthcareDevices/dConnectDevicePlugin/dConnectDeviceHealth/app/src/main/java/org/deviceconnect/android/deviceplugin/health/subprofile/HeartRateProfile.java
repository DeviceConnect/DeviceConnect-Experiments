/*
 HeartRateProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.subprofile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.health.HealthCareDeviceService;
import org.deviceconnect.android.deviceplugin.health.HealthCareManager;
import org.deviceconnect.android.deviceplugin.health.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.health.data.HealthCareDevice;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.HealthConnectProfile;
import org.deviceconnect.android.util.LogUtil;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

/**
 * Implement HealthConnectProfile.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateProfile extends HealthConnectProfile.HealthConnectSubProfile {

    /**
     * 属性: {@value} .
     */
    public static String ATTRIBUTE_HEART_RATE = "heartrate";

    /**
     * パラメータ: {@value} .
     */
    public static String PARAM_HEART_RATE = "heartRate";
    public static String PARAM_HEART_RATE_APP = "heartrate";

    /**
     * HealthCareDeviceServiceインスタンス.
     */
    private HealthCareDeviceService mService;

    /**
     * HealthCareManagerインスタンス
     */
    private HealthCareManager mManager;

    /**
     * HeartRateDataの最新情報を保持
     */
    private final Map<HealthCareDevice, HeartRateData> mHRData = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param mgr instance of {@link HealthCareManager}
     * @param service instance of {@link HealthCareDeviceService}
     */
    public HeartRateProfile(final HealthCareManager mgr, HealthCareDeviceService service) {
        mgr.addOnBluetoothGattListener(HealthCareDevice.PROFILE_TYPE_HEART_RATE, new GattEventListener());
        mManager = mgr;
        mService = service;
    }

    /**
     * 心拍計 GattEvent 実装
     */
    private class GattEventListener extends HealthCareManager.OnGattEventListener {

        @Override
        public boolean onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                HeartRateData data = getHeartRateData(gatt.getDevice().getAddress());
                if (data != null) {
                    HealthCareDevice hr = mManager.getRegisteredDevice(gatt.getDevice().getAddress());
                    if (hr == null) {
                        return true;
                    }
                    data.setLocation(HeartRateData.DeviceState.GET_LOCATION);
                    mHRData.put(hr, data);
                }
            }
            return true;
        }

        @Override
        public synchronized boolean onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                next(gatt);
            }
            return true;
        }
        @Override
        public synchronized boolean onCharacteristicRead(final BluetoothGatt gatt,final BluetoothGattCharacteristic characteristic, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (isBodySensorLocation(characteristic)) {
                    Integer location = characteristic.getIntValue(FORMAT_UINT8, 0);
                    if (location != null) {
                        onReadSensorLocation(gatt.getDevice(), location);
                    }
                    HeartRateData data = getHeartRateData(gatt.getDevice().getAddress());
                    data.setLocation(HeartRateData.DeviceState.REGISTER_NOTIFY);
                }
            }
            LogUtil.d("gatt.discoverServices()");
            gatt.discoverServices();
            return true;
        }
        @Override
        public synchronized boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            notifyHeartRateMeasurement(gatt, characteristic);
            return true;
        }
    }

    @Override
    /**
     * heartreate属性取得リクエストハンドラー.<br/>
     * スマートフォンまたは周辺機器上のテキストや画像、音声、動画（リソースも含む）のデータを提供し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    public synchronized boolean onGetRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        HeartRateData data = getHeartRateData(serviceId);
        if (data == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        setHeartRate(response, data.getHeartRate());
        return true;
    }

    @Override
    /**
     * heartrateコールバック登録リクエストハンドラー.<br/>
     * heartrateコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    public synchronized boolean onPutRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setNotFoundServiceError(response, "serviceID is null.");
            return true;
        }
        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "sessionKey is null.");
            return true;
        }
        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error != EventError.NONE) {
            MessageUtils.setUnknownError(response);
            return true;
        }

        boolean isConnected = mManager.isConnectedDevice(serviceId);
        if (!isConnected) {
            mManager.connectBleDevice(serviceId);
        }
        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    /**
     * heartrateコールバック解除リクエストハンドラー.<br/>
     * heartrateコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    public synchronized boolean onDeleteRequest(final Intent request, final Intent response,
                                     final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "There is no sessionKey.");
            return true;
        }

        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else if (error == EventError.FAILED) {
            MessageUtils.setUnknownError(response, "Failed to delete event.");
        } else if (error == EventError.NOT_FOUND) {
            MessageUtils.setUnknownError(response, "Not found event.");
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    /**
     * 測定データの解析・通知
     * @param gatt GATT Service
     * @param characteristic BluetoothGattCharacteristic
     */
    private void notifyHeartRateMeasurement(final BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
        int heartRate = 0;
        int energyExpended = 0;
        double rrInterval = 0;
        int offset = 1;

        byte[] buf = characteristic.getValue();
        if (buf.length > 1) {
            // Heart Rate Value Format bit
            if ((buf[0] & 0x80) != 0) {
                Integer v = characteristic.getIntValue(FORMAT_UINT16, offset);
                if (v != null) {
                    heartRate = v;
                }
                offset += 2;
            } else {
                Integer v = characteristic.getIntValue(FORMAT_UINT8, offset);
                if (v != null) {
                    heartRate = v;
                }
                offset += 1;
            }

            // Sensor Contact Status bits
            if ((buf[0] & 0x60) != 0) {
                // MEMO: not implements yet
            }

            // Energy Expended Status bit
            if ((buf[0] & 0x10) != 0) {
                Integer v = characteristic.getIntValue(FORMAT_UINT16, offset);
                if (v != null) {
                    energyExpended = v;
                }
                offset += 2;
            }

            // RR-Interval bit
            if ((buf[0] & 0x08) != 0) {
                Integer v = characteristic.getIntValue(FORMAT_UINT16, offset);
                if (v != null) {
                    rrInterval = ((double) v / 1024.0) * 1000.0;
                }
            }
        }

        LogUtil.w("@@@@@@ HEART RATE[" + heartRate + ", "
                + energyExpended + ", " + rrInterval + "]");

        BluetoothDevice device = gatt.getDevice();
        onReceivedData(device, heartRate, energyExpended, rrInterval);
    }

    /**
     * 測定データの通知
     * @param device BluetoothDevice
     * @param heartRate Heart Rate Value
     * @param energyExpended Energy Expended Status
     * @param rrInterval RR-Interval
     */
    private void onReceivedData(final BluetoothDevice device, final int heartRate,
                                final int energyExpended, final double rrInterval) {
        LogUtil.d("ConnectEventListener#onReceivedData: [" + device + "]");
        HealthCareDevice hr = mManager.getRegisteredDevice(device.getAddress());
        if (hr == null) {
            LogUtil.w("device not found. device:[" + device + "]");
            return;
        }
        HeartRateData data = mHRData.get(hr);
        if (data == null) {
            LogUtil.w("data not found. device:[" + device + "]");
            return;
        }

        data.setHeartRate(heartRate);
        data.setEnergyExpended(energyExpended);
        data.setRRInterval(rrInterval);

        mHRData.put(hr, data);

        notifyHeartRateData(hr, data);
    }

    /**
     * Notify the heart rate event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private synchronized void notifyHeartRateData(final HealthCareDevice device, final HeartRateData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getAddress(),
                org.deviceconnect.profile.HealthProfileConstants.PROFILE_NAME,
                null, ATTRIBUTE_HEART_RATE);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            setHeartRate(intent, data.getHeartRate());

            // 見守りアプリ用
            data.setUnit(intent, 0);
            data.setTimeStampString(intent);
            DConnectProfile.setResult(intent, DConnectMessage.RESULT_OK);

            mService.sendEvent(intent, event.getAccessToken());
        }
    }

    /**
     * Gets the {@link HeartRateData} from address.
     * @param address address of ble device
     * @return {@link HeartRateData}, or null on error
     */
    public HeartRateData getHeartRateData(final String address) {
        HealthCareDevice device = mManager.getRegisteredDevice(address);
        return mHRData.get(device);
    }

    /**
     * レスポンスに心拍数を設定する.
     * @param response レスポンス
     * @param heartRate 心拍数
     */
    public static void setHeartRate(final Intent response, final int heartRate) {
        //response.putExtra(PARAM_HEART_RATE, heartRate);
        response.putExtra(PARAM_HEART_RATE_APP, heartRate);     // 地域見守りアプリ用
    }

    /**
     * Shift to the next state on GATT Service.
     * @param gatt GATT Service
     */
    private void next(final BluetoothGatt gatt) {

        HeartRateData data = getHeartRateData(gatt.getDevice().getAddress());
        if (data == null) {
            HealthCareDevice hr = mManager.getRegisteredDevice(gatt.getDevice().getAddress());
            if (hr == null) {
                return;
            }
            data = new HeartRateData();
            data.setLocation(HeartRateData.DeviceState.GET_LOCATION);
            mHRData.put(hr, data);
        }

        HeartRateData.DeviceState state = data.getLocation();
        switch (state) {
            case GET_LOCATION:
                if (!callGetBodySensorLocation(gatt)) {
                    data.setLocation(HeartRateData.DeviceState.REGISTER_NOTIFY);
                    LogUtil.d("gatt.discoverServices()");
                    gatt.discoverServices();
                }
                break;
            case REGISTER_NOTIFY:
                if (!callRegisterHeartRateMeasurement(gatt)) {
                    data.setLocation(HeartRateData.DeviceState.ERROR);
                }
                break;
            case CONNECTED:
                LogUtil.d("@@@@@@ GATT Service is connected.");
                break;
            default:
                LogUtil.w("Illegal state. state=" + state);
                break;
        }
    }

    /**
     * Register notification of HeartRateMeasurement Characteristic.
     * @param gatt GATT Service
     * @return true if successful in notification of registration
     */
    private boolean callRegisterHeartRateMeasurement(final BluetoothGatt gatt) {
        boolean registered = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEART_RATE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_HEART_RATE_MEASUREMENT));
            if (c != null) {
                registered = gatt.setCharacteristicNotification(c, true);
                if (registered) {
                    for (BluetoothGattDescriptor descriptor : c.getDescriptors()) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                    HeartRateData data = getHeartRateData(gatt.getDevice().getAddress());
                    data.setLocation(HeartRateData.DeviceState.REGISTER_NOTIFY);
                }
            }
        }
        return registered;
    }

    /**
     * Get a body sensor location from GATT Service.
     * @param gatt GATT Service
     * @return true if gatt has Generic Access Service, false if gatt has no service.
     */
    private boolean callGetBodySensorLocation(final BluetoothGatt gatt) {
        boolean result = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEART_RATE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_BODY_SENSOR_LOCATION));
            if (c != null) {
                result = gatt.readCharacteristic(c);
            }
        }
        return result;
    }

    /**
     * location情報の設定
     * @param device Bluetooth Device
     * @param location location info
     */
    public void onReadSensorLocation(final BluetoothDevice device, final int location) {
        getHeartRateData(device.getAddress()).setSensorLocation(location);
    }

    /**
     * Checks whether characteristic's uuid and checkUuid is same.
     * @param characteristic uuid
     * @param checkUuid uuid
     * @return true uuid is same, false otherwise
     */
    private static boolean isCharacteristic(final BluetoothGattCharacteristic characteristic,
                                            final String checkUuid) {
        String uuid = characteristic.getUuid().toString();
        return checkUuid.equalsIgnoreCase(uuid);
    }

    /**
     * Checks whether characteristic is body sensor location.
     * @param characteristic uuid
     * @return true uuid is same, false otherwise
     */
    private static boolean isBodySensorLocation(final BluetoothGattCharacteristic characteristic) {
        return isCharacteristic(characteristic, BleUtils.CHAR_BODY_SENSOR_LOCATION);
    }
}

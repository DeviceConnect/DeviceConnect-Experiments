/*
 ThermometerProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.subprofile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Handler;

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

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implement HealthConnectSubProfile.
 * @author NTT DOCOMO, INC.
 */
public class ThermometerProfile extends HealthConnectProfile.HealthConnectSubProfile {

    /**
     * 属性: {@value} .
     */
    public static String ATTRIBUTE_HEALTH_THERMOMETER = "thermometer";

    /**
     * HealthCareDeviceServiceインスタンス.
     */
    private HealthCareDeviceService mService;

    /**
     * HealthCareManagerインスタンス
     */
    private HealthCareManager mManager;

    /**
     * Handler
     */
    Handler mHandler = new Handler();

    /**
     * HealthThermometerの最新情報
     */
    private final Map<HealthCareDevice, ThermometerData> mHTData = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param mgr instance of {@link HealthCareManager}
     * @param service instance of {@link HealthCareDeviceService}
     */
    public ThermometerProfile(final HealthCareManager mgr, HealthCareDeviceService service) {
        mService = service;
        mManager = mgr;
        mManager.addOnBluetoothGattListener(HealthCareDevice.PROFILE_TYPE_HEALTH_THERMOMETER, new GattEventListener());
    }

    /**
     * 体温計 GattEvent 実装
     */
    private class GattEventListener extends HealthCareManager.OnGattEventListener {
        @Override
        public boolean onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                LogUtil.d("  -> gatt.getDevice().getBondState():" + gatt.getDevice().getBondState());
                mManager.disconnectBleDevice(gatt.getDevice().getAddress());
            } else {
                writeIndicationValue(gatt, status);
            }
            return true;
        }
        @Override
        public boolean onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                LogUtil.d("  -> gatt.getDevice().getBondState():" + gatt.getDevice().getBondState());
                requestTemperatureMeasurement(gatt);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                mManager.disconnectBleDevice(gatt.getDevice().getAddress());
            } else {
                requestTemperatureMeasurement(gatt);
            }
            return true;
        }
        @Override
        public boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            onReceiveTemperatureMeasurement(gatt, characteristic);
            return true;
        }
    }

    @Override
    public boolean onGetRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        LogUtil.d("ThermometerProfile.onGetRequest()");
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        LogUtil.d("  -> serviceId:" + serviceId);

        HealthCareDevice device = mManager.getRegisteredDevice(serviceId);
        ThermometerData data = getThermometerData(serviceId);
        if (device == null || data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target device not found.");
            return true;
        }

        // ヘルスケアプラグイン用
        device.setDeviceInfo(response);
        device.setBatteryLevel(response);
        //data.setDeviceInfo(response);

        // 見守りアプリ用
        data.setAppDeviceInfo(response);

        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    public boolean onPutRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        LogUtil.d("ThermometerProfile.onPutRequest()");
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        LogUtil.d("  -> serviceId:" + serviceId);
        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "sessionKey is null.");
            return true;
        }
        LogUtil.d("  -> sessionKey:" + sessionKey);

        HealthCareDevice device = mManager.getRegisteredDevice(serviceId);
        if (device == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target device not found.");
            return true;
        }
        device.setDeviceInfo(response);

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error != EventError.NONE) {
            MessageUtils.setUnknownError(response);
            return true;
        }

        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    public boolean onDeleteRequest(final Intent request, final Intent response,
                                     final String serviceId, final String sessionKey) {
        LogUtil.d("ThermometerProfile.onDeleteRequest()");
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        LogUtil.d("  -> serviceId:" + serviceId);
        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "There is no sessionKey.");
            return true;
        }
        LogUtil.d("  -> sessionKey:" + sessionKey);

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
     * Notification通知の有効化.
     * @param gatt BluetoothGatt
     * @param status Discovered status
     * @return Is used event.
     */
    private boolean writeIndicationValue(final BluetoothGatt gatt, final int status) {
        if (!setIndicationValue(gatt)) {
            LogUtil.d("gatt.discoverServices()");
            gatt.discoverServices();
        }
        return true;
    }

    /**
     * Set ENABLE_INDICATION_VALUE to DESCRIPTOR
     * @param gatt GATT Service
     * @return true if gatt has Generic Access Service, false if gatt has no service.
     */
    private boolean setIndicationValue(final BluetoothGatt gatt) {
        boolean result = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEALTH_THERMOMETER_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_TEMPERATURE_MEASUREMENT));
            if (c != null) {
                BluetoothGattDescriptor descriptor = c.getDescriptor(
                        UUID.fromString(BleUtils.DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                result = gatt.writeDescriptor(descriptor);
                LogUtil.d("  -> gatt.writeDescriptor() result is " + result);
            }
        }
        return result;
    }

    /**
     * 体温計の通知要求
     * @param gatt Gatt Service
     * @return 要求成否
     */
    private boolean requestTemperatureMeasurement(final BluetoothGatt gatt) {
        boolean result = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEALTH_THERMOMETER_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_TEMPERATURE_MEASUREMENT));
            if (c != null) {
                result = gatt.setCharacteristicNotification(c, true);
                LogUtil.d("  -> gatt.setCharacteristicNotification() result is " + result);
            }
        }
        return result;
    }

    /**
     * Received temperature measurement value
     * @param gatt GATT Service
     * @param characteristic BluetoothGattCharacteristic
     */
    private void onReceiveTemperatureMeasurement(final BluetoothGatt gatt,
                                                 final BluetoothGattCharacteristic characteristic) {
        boolean isCelsius = true;
        float temperature = 0;
        long unixTime = 0;
        String typeCode = "";
        int offset = 1;

        byte[] buf = characteristic.getValue();
        if (buf.length > 1) {

            if ((buf[0] & 0x80) != 0) {
                // 華氏
                isCelsius = false;
                temperature = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, offset);
                offset += 4;
            } else {
                // 摂氏
                isCelsius = true;
                temperature = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, offset);
                offset += 4;
            }

            // タイムスタンプフィールド
            if ((buf[0] & 0x40) != 0) {
                // 1582-9999 (0 is unknown)
                Integer year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                offset += 2;
                // 0-12 (0 is unknown)
                Integer month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset++;
                // 1-31
                Integer day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset++;
                // 0-23
                Integer hours = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset++;
                // 0-59
                Integer minutes = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset++;
                // 0-59
                Integer seconds = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset++;

                final Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, hours, minutes, seconds);
                unixTime = calendar.getTimeInMillis();
            } else {
                // 指定がない場合は現在時刻とする.
                final Calendar calendar = Calendar.getInstance();
                unixTime = calendar.getTimeInMillis();
            }

            // 温度計種別
            if ((buf[0] & 0x20) != 0) {
                typeCode = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
            }
        }

        BluetoothDevice device = gatt.getDevice();
        onReceivedThermometerData(device, temperature, isCelsius, unixTime, typeCode);
    }

    /**
     * 測定データの通知
     * @param device Thermometer device
     * @param temperature 体温
     * @param isCelsius deg Cかどうか
     * @param unixTime Unix time
     * @param typeCode 測定場所
     */
    private void onReceivedThermometerData(final BluetoothDevice device, final float temperature, final boolean isCelsius,
                                          final long unixTime, final String typeCode) {
        HealthCareDevice hr = mManager.getRegisteredDevice(device.getAddress());
        if (hr == null) {
            return;
        }

        ThermometerData data = new ThermometerData();
        data.setTemperature(temperature);
        data.setTypeCode(typeCode);
        data.setUnit(isCelsius);
        data.setTimeStamp(unixTime);

        mHTData.put(hr, data);
        notifyThermometerData(hr, data);
    }

    /**
     * Notify the heart rate event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private void notifyThermometerData(final HealthCareDevice device, final ThermometerData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getAddress(),
                org.deviceconnect.profile.HealthProfileConstants.PROFILE_NAME,
                null, ATTRIBUTE_HEALTH_THERMOMETER);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            //device.setBatteryLevel(intent);
            //data.setDeviceInfo(intent);
            // 見守りアプリ用
            data.setAppDeviceInfo(intent);
            DConnectProfile.setResult(intent, DConnectMessage.RESULT_OK);

            mService.sendEvent(intent, event.getAccessToken());
        }
    }

    /**
     * デバイスに対応するThermometerDataの取得.
     * @param address address
     * @return data
     */
    private ThermometerData getThermometerData(final String address) {
        HealthCareDevice device = mManager.getRegisteredDevice(address);
        if (device == null || device.getProfileType() != HealthCareDevice.PROFILE_TYPE_HEALTH_THERMOMETER) {
            return null;
        }
        ThermometerData data = mHTData.get(device);
        if (data == null) {
            data = new ThermometerData();
        }
        return data;
    }
}

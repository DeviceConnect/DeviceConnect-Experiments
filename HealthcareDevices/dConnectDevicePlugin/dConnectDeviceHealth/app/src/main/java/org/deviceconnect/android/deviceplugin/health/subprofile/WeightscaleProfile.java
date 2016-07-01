/*
 ThermometerProfile
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
public class WeightscaleProfile extends HealthConnectProfile.HealthConnectSubProfile {

    /**
     * 属性: {@value} .
     */
    public static String ATTRIBUTE_HEALTH_WEIGHT_SCALE = "weightscale";

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
    private final Map<HealthCareDevice, WeightscaleData> mWSData = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param mgr instance of {@link HealthCareManager}
     * @param service instance of {@link HealthCareDeviceService}
     */
    public WeightscaleProfile(final HealthCareManager mgr, HealthCareDeviceService service) {
        mService = service;
        mManager = mgr;
        mManager.addOnBluetoothGattListener(HealthCareDevice.PROFILE_TYPE_HEALTH_WEIGHT_SCALE, new GattEventListener());
    }

    /**
     * 体重計 GattEvent 実装
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
                requestWeightMeasurement(gatt);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                mManager.disconnectBleDevice(gatt.getDevice().getAddress());
            } else {
                requestWeightMeasurement(gatt);
            }
            return true;
        }
        @Override
        public boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            LogUtil.d("WeightScaleProfile#Characteristic Changed : " + characteristic);
            onReceiveWeightMeasurement(gatt, characteristic);
            return true;
        }
    }

    @Override
    public boolean onGetRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        LogUtil.d("WeightscaleProfile.onGetRequest()");
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        LogUtil.d("  -> serviceId:" + serviceId);

        HealthCareDevice device = mManager.getRegisteredDevice(serviceId);
        WeightscaleData data = getWeightData(serviceId);
        if (device == null || data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target device not found.");
            return true;
        }

        // ヘルスケアプラグイン用
        device.setDeviceInfo(response);
        device.setBatteryLevel(response);
        data.setDeviceInfo(response);

        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    public boolean onPutRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        LogUtil.d("WeightscaleProfile.onPutRequest()");
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
        LogUtil.d("WeightscaleuProfile.onDeleteRequest()");
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
     * Indication通知の有効化.
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
                BleUtils.SERVICE_HEALTH_WEIGHT_SCALE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_WEIGHT_MEASUREMENT));
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
     * 体重計の通知要求
     * @param gatt Gatt Service
     * @return 要求成否
     */
    private boolean requestWeightMeasurement(final BluetoothGatt gatt) {
        boolean result = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEALTH_WEIGHT_SCALE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_WEIGHT_MEASUREMENT));
            if (c != null) {
                result = gatt.setCharacteristicNotification(c, true);
                LogUtil.d("  -> gatt.setCharacteristicNotification() result is " + result);
            }
        }
        return result;
    }

    /**
     * Received weight measurement value
     * @param gatt GATT Service
     * @param characteristic BluetoothGattCharacteristic
     */
    private void onReceiveWeightMeasurement(final BluetoothGatt gatt,
                                                 final BluetoothGattCharacteristic characteristic) {
        float weight = 0;
        boolean isKg = true;
        long unixTime = 0;
        int offset = 1;

        byte[] buf = characteristic.getValue();
        LogUtil.d("WeightscaleProfile#onReceiveWeightMeasurement / Num of Charas = " + buf.length);
        if (buf.length > 1) {

            // Check Flag(8bit) for unit / Read Weight from corresponding device
            LogUtil.d("buf[0] = " + buf[0]);
            if ((buf[0] & 0x80) != 0) {
                // lb
                isKg = false;
                weight = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                weight *= 0.01;
                offset += 2;
            } else {
                // Kg
                isKg = true;
                weight = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                weight *= 0.005;
                offset += 2;
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

        }

        LogUtil.d("WeightscaleProfile#onReceiveWeightMeasurement / Weight : " + weight + ", isKg : " + isKg + ", unixTime : " + unixTime);
        BluetoothDevice device = gatt.getDevice();
        LogUtil.d("WeightscaleProfile#onReceiveWeightMeasurement / BLE Devide Info : " + device.getAddress() + ", " + device.getName());
        onReceivedWeightData(device, weight, isKg, unixTime);
    }

    /**
     * Construct and format a measured data, and notify
     *
     * @param device Weight Scal
     * @param weight measured weight
     * @param isKg a flag whether SI(Kg) basis
     * @param unixTime Unix time
     */
    private void onReceivedWeightData(final BluetoothDevice device, final float weight, final boolean isKg, final long unixTime) {
        HealthCareDevice hr = mManager.getRegisteredDevice(device.getAddress());
        if (hr == null) {
            return;
        }

        WeightscaleData data = new WeightscaleData();
        data.setWeight(weight);
        data.setUnit(isKg);
        data.setTimeStamp(unixTime);

        mWSData.put(hr, data);
        notifyWeightData(hr, data);
    }

    /**
     * Notify the weight scale event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private void notifyWeightData(final HealthCareDevice device, final WeightscaleData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getAddress(),
                org.deviceconnect.profile.HealthProfileConstants.PROFILE_NAME,
                null, ATTRIBUTE_HEALTH_WEIGHT_SCALE);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);

            data.setDeviceInfo(intent);
            DConnectProfile.setResult(intent, DConnectMessage.RESULT_OK);

            mService.sendEvent(intent, event.getAccessToken());
        }
    }

    /**
     * デバイスに対応するWeightscaleDataの取得.
     * @param address address
     * @return data
     */
    private WeightscaleData getWeightData(final String address) {
        HealthCareDevice device = mManager.getRegisteredDevice(address);
        if (device == null || device.getProfileType() != HealthCareDevice.PROFILE_TYPE_HEALTH_WEIGHT_SCALE) {
            return null;
        }
        WeightscaleData data = mWSData.get(device);
        if (data == null) {
            data = new WeightscaleData();
        }
        return data;
    }
}

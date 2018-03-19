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
public class BloodPressureProfile extends HealthConnectProfile.HealthConnectSubProfile {

    /**
     * 属性: {@value} .
     */
    public static String ATTRIBUTE_HEALTH_BLOOD_PRESSURE = "bloodpressure";

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
    private final Map<HealthCareDevice, BloodPressureData> mBPData = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param mgr instance of {@link HealthCareManager}
     * @param service instance of {@link HealthCareDeviceService}
     */
    public BloodPressureProfile(final HealthCareManager mgr, HealthCareDeviceService service) {
        mService = service;
        mManager = mgr;
        mManager.addOnBluetoothGattListener(HealthCareDevice.PROFILE_TYPE_HEALTH_BLOOD_PRESSURE, new GattEventListener());
    }

    /**
     * 血圧計 GattEvent 実装
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
                requestBloodPressureMeasurement(gatt);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                mManager.disconnectBleDevice(gatt.getDevice().getAddress());
            } else {
                requestBloodPressureMeasurement(gatt);
            }
            return true;
        }
        @Override
        public boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            LogUtil.d("BloodPressureProfile#Characteristic has been changed.");
            onReceiveBloodPressureMeasurement(gatt, characteristic);
            return true;
        }
    }

    @Override
    public boolean onGetRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        LogUtil.d("BloodPressureProfile.onGetRequest()");
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        LogUtil.d("  -> serviceId:" + serviceId);

        HealthCareDevice device = mManager.getRegisteredDevice(serviceId);
        BloodPressureData data = getBloodPressureData(serviceId);
        if (device == null || data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target device not found.");
            return true;
        }

        device.setDeviceInfo(response);
        data.setDeviceInfo(response);
        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);

        return true;
    }

    @Override
    public boolean onPutRequest(final Intent request, final Intent response,
                                final String serviceId, final String sessionKey) {
        LogUtil.d("BloodPressureProfile.onPutRequest()");
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
        LogUtil.d("BloodPressureProfile.onDeleteRequest()");
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
                BleUtils.SERVICE_HEALTH_BLOOD_PRESSURE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_BLOOD_PRESSURE_MEASUREMENT));
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
     * 血圧計の通知要求
     * @param gatt Gatt Service
     * @return 要求成否
     */
    private boolean requestBloodPressureMeasurement(final BluetoothGatt gatt) {
        boolean result = false;
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEALTH_BLOOD_PRESSURE_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(
                    UUID.fromString(BleUtils.CHAR_BLOOD_PRESSURE_MEASUREMENT));
            if (c != null) {
                result = gatt.setCharacteristicNotification(c, true);
                LogUtil.d("  -> gatt.setCharacteristicNotification() result is " + result);
            }
        }
        return result;
    }

    /**
     * Received blood pressure measurement value
     * @param gatt GATT Service
     * @param characteristic BluetoothGattCharacteristic
     */
    private void onReceiveBloodPressureMeasurement(final BluetoothGatt gatt,
                                                 final BluetoothGattCharacteristic characteristic) {
        boolean isHg = true;
        float systolic = 0;
        float diastolic = 0;
        float marterial = 0;
        long unixTime = 0;
        int offset = 1;     // Byte

        LogUtil.d("onReceiveBloodPressureMeasurement()");

        byte[] buf = characteristic.getValue();
        if (buf.length > 1) {

            // Check Flag(8bit) for unit / Read Pressure systolic, diastolic and mean arterial from corresponding device
            if ((buf[0] & 0x80) != 0) {
                // kPa
                isHg = false;

                // Systolic in kPa
                offset += 6;
                systolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
                // Diastolic in kPa
                offset += 2;
                diastolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
                // Mean Arterial in kPa
                offset += 2;
                marterial = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
            } else {
                // mmHg
                isHg = true;

                // Systolic in mmHg
                systolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
                // Diastolic in mmHg
                offset += 2;
                diastolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
                // Mean Arterial in mmHg
                offset += 2;
                marterial = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
                offset += 6;      // Skip for kPa values field
            }

            LogUtil.d("isHg = " + isHg);
            LogUtil.d("systolic = " + systolic);
            LogUtil.d("diastolic = " + diastolic);
            LogUtil.d("marterial = " + marterial);

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

            LogUtil.d("unixTime = " + unixTime);

            // Pulse Rate
            if ((buf[0] & 0x20) != 0) {
                // This time, Not used for the system
            }

            // UserID
            if ((buf[0] & 0x10) != 0) {
                // This time, Not used for the system
            }

            // Measurement Status, Order : Little Endian
            if ((buf[0] & 0x08) != 0) {
                // This time, Not used for the system
            }
        }

        BluetoothDevice device = gatt.getDevice();
        onReceivedBloodPressureData(device, systolic, diastolic, marterial, unixTime, isHg);
    }

    /**
     * 血圧測定データの通知
     * @param device Blood Pressure Device
     * @param systolic 最高血圧
     * @param diastolic 最低血圧
     * @param marterial 平均血圧
     * @param unixTime Unix time
     * @param isHg 単位(true = mmHg / false = kPa)
     */
    private void onReceivedBloodPressureData(final BluetoothDevice device, final float systolic, final float diastolic, final float marterial,
                                          final long unixTime, final boolean isHg) {
        HealthCareDevice hr = mManager.getRegisteredDevice(device.getAddress());
        if (hr == null) {
            return;
        }

        BloodPressureData data = new BloodPressureData();
        data.setSystericPressure(systolic);
        data.setDiastolicPressure(diastolic);
        data.setMeanarterial(marterial);
        data.setUnit(isHg);
        data.setTimeStamp(unixTime);

        mBPData.put(hr, data);
        notifyBloodPressureData(hr, data);
    }

    /**
     * Notify the heart rate event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private void notifyBloodPressureData(final HealthCareDevice device, final BloodPressureData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getAddress(),
                org.deviceconnect.profile.HealthProfileConstants.PROFILE_NAME,
                null, ATTRIBUTE_HEALTH_BLOOD_PRESSURE);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);

            data.setDeviceInfo(intent);
            DConnectProfile.setResult(intent, DConnectMessage.RESULT_OK);

            mService.sendEvent(intent, event.getAccessToken());
        }
    }

    /**
     * デバイスに対応するBloodPressureDataの取得.
     * @param address address
     * @return data
     */
    private BloodPressureData getBloodPressureData(final String address) {
        HealthCareDevice device = mManager.getRegisteredDevice(address);
        if (device == null || device.getProfileType() != HealthCareDevice.PROFILE_TYPE_HEALTH_BLOOD_PRESSURE) {
            return null;
        }
        BloodPressureData data = mBPData.get(device);
        if (data == null) {
            data = new BloodPressureData();
        }
        return data;
    }
}

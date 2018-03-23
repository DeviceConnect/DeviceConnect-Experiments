/*
 HealthCareDevice
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.data;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.health.ble.BleUtils;

/**
 * This class is information of a device.
 * @author NTT DOCOMO, INC.
 */
public class HealthCareDevice {
    private int mId = -1;
    private String mName;
    private String mAddress;
    private boolean mRegisterFlag;
    private int mProfileType;

    private static final String DEVICE_PRODUCT_NAME = "deviceProductName";
    private static final String DEVICE_MANUFACTURE_NAME = "deviceManufactureName";
    private static final String DEVICE_MODEL_NUMBER = "deviceModelNumber";
    private static final String DEVICE_FIRMWARE_REVISION = "deviceFirmwareRevision";
    private static final String DEVICE_SERIAL_NUMBER = "deviceSerialNumber";
    private static final String DEVICE_SOFTWARE_REVISION = "deviceSoftwareRevision";
    private static final String DEVICE_HARDWARE_REVISION = "deviceHardwareRevision";
    private static final String DEVICE_PART_NUMBER = "devicePartNumber";
    private static final String DEVICE_PROTOCOL_REVISION = "deviceProtocolRevision";
    private static final String DEVICE_SYSTEM_ID = "deviceSystemId";
    private static final String DEVICE_BATTERY_LEVEL = "deviceBatteryLevel";

    public void setDeviceInfo(Intent response) {
        response.putExtra(DEVICE_PRODUCT_NAME, getName());
        response.putExtra(DEVICE_MANUFACTURE_NAME, getDeviceManufacturerName());
        response.putExtra(DEVICE_MODEL_NUMBER, getDeviceModelNumber());
        response.putExtra(DEVICE_FIRMWARE_REVISION, getDeviceFirmwareRevision());
        response.putExtra(DEVICE_SERIAL_NUMBER, getDeviceSerialNumber());
        response.putExtra(DEVICE_SOFTWARE_REVISION, getDeviceSoftwareRevision());
        response.putExtra(DEVICE_HARDWARE_REVISION, getDeviceHardwareRevision());
        response.putExtra(DEVICE_PART_NUMBER, getDevicePartNumber());
        response.putExtra(DEVICE_PROTOCOL_REVISION, getDeviceProtocolRevision());
        response.putExtra(DEVICE_SYSTEM_ID, getDeviceSystemId());
    }
    public void setBatteryLevel(Intent response) {
        response.putExtra(DEVICE_BATTERY_LEVEL, getDeviceBatteryLevel());
    }

    private String mDeviceManufacturerName = "";
    private String mDeviceModelNumber = "";
    private String mDeviceFirmwareRevision = "";
    private String mDeviceSerialNumber = "";
    private String mDeviceSoftwareRevision = "";
    private String mDeviceHardwareRevision = "";
    private String mDevicePartNumber = "";
    private String mDeviceProtocolRevision = "";
    private String mDeviceSystemId = "";
    private String mDeviceBatteryLevel = "";
    private boolean mDeviceInformationRegistered = false;

    public int getId() {
        return mId;
    }

    public void setId(final int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(final String address) {
        mAddress = address;
    }

    public boolean isRegisterFlag() {
        return mRegisterFlag;
    }

    public void setRegisterFlag(final boolean registerFlag) {
        mRegisterFlag = registerFlag;
    }

    public int getProfileType() { return mProfileType; }

    public void setProfileType(final int deviceType) { mProfileType = deviceType; }

    public String getDeviceManufacturerName() {
        return mDeviceManufacturerName;
    }

    public void setDeviceManufacturerName(final String deviceManufacturerName) {
        mDeviceManufacturerName = deviceManufacturerName;
    }

    public String getDeviceModelNumber() {
        return mDeviceModelNumber;
    }

    public void setDeviceModelNumber(final String deviceModelNumber) {
        mDeviceModelNumber = deviceModelNumber;
    }

    public String getDeviceFirmwareRevision() {
        return mDeviceFirmwareRevision;
    }

    public void setDeviceFirmwareRevision(final String deviceFirmwareRevision) {
        mDeviceFirmwareRevision = deviceFirmwareRevision;
    }

    public String getDeviceSerialNumber() {
        return mDeviceSerialNumber;
    }

    public void setDeviceSerialNumber(final String deviceSerialNumber) {
        mDeviceSerialNumber = deviceSerialNumber;
    }

    public String getDeviceSoftwareRevision() {
        return mDeviceSoftwareRevision;
    }

    public void setDeviceSoftwareRevision(final String deviceSoftwareRevision) {
        mDeviceSoftwareRevision = deviceSoftwareRevision;
    }

    public String getDeviceHardwareRevision() {
        return mDeviceHardwareRevision;
    }

    public void setDeviceHardwareRevision(final String deviceHardwareRevision) {
        mDeviceHardwareRevision = deviceHardwareRevision;
    }

    public String getDevicePartNumber() {
        return mDevicePartNumber;
    }

    public void setDevicePartNumber(final String devicePartNumber) {
        mDevicePartNumber = devicePartNumber;
    }

    public String getDeviceProtocolRevision() {
        return mDeviceProtocolRevision;
    }

    public void setDeviceProtocolRevision(final String deviceProtocolRevision) {
        mDeviceProtocolRevision = deviceProtocolRevision;
    }

    public String getDeviceSystemId() {
        return mDeviceSystemId;
    }

    public void setDeviceSystemId(final String deviceSystemId) {
        mDeviceSystemId = deviceSystemId;
    }

    public String getDeviceBatteryLevel() {
        return mDeviceBatteryLevel;
    }

    public void setDeviceBatteryLevel(final String deviceBatteryLevel) {
        mDeviceBatteryLevel = deviceBatteryLevel;
    }

    public boolean isDeviceInformationRegistered() {
        return mDeviceInformationRegistered;
    }

    public void setDeviceInformationRegistered(final boolean deviceInformationRegistered) {
        mDeviceInformationRegistered = deviceInformationRegistered;
    }

    public String getProfileTypeName() {
        if (mProfileType == PROFILE_TYPE_HEART_RATE) {
            return "heartRate";
        } else if (mProfileType == PROFILE_TYPE_HEALTH_THERMOMETER) {
            return "thermometer";
        } else if (mProfileType == PROFILE_TYPE_HEALTH_BLOOD_PRESSURE) {
            return "bloodpressure";
        } else if (mProfileType == PROFILE_TYPE_HEALTH_WEIGHT_SCALE) {
            return "weightscale";
        }
        return "";
    }

    /**
     * Get the profile type from Bluetooth Gatt
     * @param gatt BluetoothGatt
     * @return profile type
     */
    public static int getProfileType(final BluetoothGatt gatt) {
        if (BleUtils.hasHeartRateService(gatt)) {
            return HealthCareDevice.PROFILE_TYPE_HEART_RATE;
        }
        if (BleUtils.hasHealthThermometerService(gatt)) {
            return HealthCareDevice.PROFILE_TYPE_HEALTH_THERMOMETER;
        }
        if (BleUtils.hasHealthBloodpressureService(gatt)) {
            return HealthCareDevice.PROFILE_TYPE_HEALTH_BLOOD_PRESSURE;
        }
        if (BleUtils.hasHealthWeightScaleService(gatt)) {
            return HealthCareDevice.PROFILE_TYPE_HEALTH_WEIGHT_SCALE;
        }
        return HealthCareDevice.PROFILE_TYPE_UNCONFIRMED;
    }

    public static final int PROFILE_TYPE_NOT_SUPPORTED = -1;
    public static final int PROFILE_TYPE_UNCONFIRMED = 0;
    public static final int PROFILE_TYPE_HEART_RATE = 1;
    public static final int PROFILE_TYPE_HEALTH_THERMOMETER = 2;
    public static final int PROFILE_TYPE_HEALTH_BLOOD_PRESSURE = 3;
    public static final int PROFILE_TYPE_HEALTH_WEIGHT_SCALE = 4;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HealthCareDevice that = (HealthCareDevice) o;

        if (mProfileType != that.mProfileType) {
            return false;
        }
        if (mId != that.mId) {
            return false;
        }
        if (mAddress != null ? !mAddress.equals(that.mAddress) : that.mAddress != null) {
            return false;
        }
        if (mName != null ? !mName.equals(that.mName) : that.mName != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mAddress != null ? mAddress.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"id\": " + mId + ", ");
        builder.append("\"name\": " + mName + ", ");
        builder.append("\"address\": " + mAddress + ", ");
        builder.append("\"registerFlag\": " + mRegisterFlag + ",  ");
        builder.append("\"deviceType\": " + mProfileType + "} ");
        return builder.toString();
    }
}

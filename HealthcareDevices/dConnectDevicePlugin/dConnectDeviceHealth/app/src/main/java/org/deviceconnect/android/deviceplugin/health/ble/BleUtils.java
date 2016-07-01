/*
 BleUtils
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import org.deviceconnect.android.deviceplugin.health.data.HealthCareDevice;

import java.util.UUID;

/**
 * A class containing utility methods related to BLE.
 * @author NTT DOCOMO, INC.
 */
public final class BleUtils {

    // 1800 Generic Access
    public static final String SERVICE_GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PERIPHERAL_PRIVACY_FLAG = "00002a02-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_RECONNECTION_ADDRESS = "00002a03-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = "00002a04-0000-1000-8000-00805f9b34fb";

    // 1801 Generic Attribute
    public static final String SERVICE_GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb";

    // 1802 Immediate Alert
    public static final String SERVICE_IMMEDIATE_ALERT = "00001802-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb";
    // StickNFindではCHAR_ALERT_LEVELに0x01をWriteすると光り、0x02では音が鳴り、0x03では光って鳴る。

    // 180a Device Information
    public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_HARDWARE_REVISION_STRING = "00002a27-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_FIRMWARE_REVISION_STRING = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SOFTWARE_REVISION_STRING = "00002a28-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SYSTEM_ID = "00002a23-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST = "00002a2a-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PNP_ID = "00002a50-0000-1000-8000-00805f9b34fb";

    // 180F Battery Service
    public static final String SERVICE_BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    // 180D Heart Reate Service
    public static final String SERVICE_HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BODY_SENSOR_LOCATION = "00002a38-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_HEART_RATE_CONTROL_POINT = "00002a39-0000-1000-8000-00805f9b34fb";

    // 1809 Health Thermometer Service
    public static final String SERVICE_HEALTH_THERMOMETER_SERVICE = "00001809-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_TEMPERATURE_MEASUREMENT = "00002a1c-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_TEMPERATURE_TYPE = "00002a1c-0000-1000-8000-00805f9b34fb";

    // 1810 Blood Pressure Service
    public static final String SERVICE_HEALTH_BLOOD_PRESSURE_SERVICE = "00001810-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BLOOD_PRESSURE_MEASUREMENT = "00002a35-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BLOOD_PRESSURE_FEATURE = "00002a49-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_INTERMEDIATE_CUFF_PRESSURE = "00002a36-0000-1000-8000-00805f9b34fb";

    // 181D Weight Scale Service
    public static final String SERVICE_HEALTH_WEIGHT_SCALE_SERVICE = "0000181d-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_WEIGHT_MEASUREMENT = "00002a9d-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_WEIGHT_SCALE_FEATURE = "00002a9e-0000-1000-8000-00805f9b34fb";

    // 1808 Glucose Service
    public static final String SERVICE_HEALTH_GLUCOSE_SERVICE = "00001808-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_GLUCOSE_MEASUREMENT = "00002a18-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_GLUCOSE_MEASUREMENT_CONTEXT = "00002a34-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_GLUCOSE_FEATURE = "00002a51-0000-1000-8000-00805f9b34fb";

    public static final String CHAR_CGM_MEASUREMENT = "00002aa7-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_CGM_FEATURE = "00002aa8-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_CGM_SESSION_RUN_TIME = "00002aab-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_CGM_SESSION_START_TIME = "00002aaa-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_CGM_SPECIFIC_OPS_POINT = "00002aac-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_CGM_STATUS = "00002aa9-0000-1000-8000-00805f9b34fb";

    // 1822 Pulse Oximeter Service
    public static final String SERVICE_PULSE_OXIMETER_SERVICE = "00001822-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PLX_SPOT_CHECK_MEASUREMENT = "00002a5e-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PLX_CONTINUOUS_MEASUREMENT = "00002a5f-0000-1000-8000-00805f9b34fb";

    // common
    public static final String DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String SERVICE_BOND_MANAGEMENT_SERVICE = "0000181e-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BOND_MANAGEMENT_CONTROL_POINT = "00002aa4-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BOND_MANAGEMENT_FEATURE = "00002aa5-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_RECORD_ACCESS_CONTROL_POINT = "00002a52-0000-1000-8000-00805f9b34fb";

    private BleUtils() {
    }

    /**
     * Checks whether device(smart phone) supports BLE.
     * @param context context of application
     * @return Returns true if the device supports BLE, else
     * false.
     */
    public static boolean isBLESupported(final Context context) {
        return Build.VERSION.SDK_INT >= 18 &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Gets instance of BluetoothManager.
     * @param context context of application
     * @return Instance of BluetoothManager or null if the BluetoothManager does not exist.
     */
    public static BluetoothManager getManager(final Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    /**
     * Tests whether BLE Device has Heart Rate Service.
     * @param gatt GATT Service
     * @return true BLE device has Heart Rate Service
     */
    public static boolean hasHeartRateService(final BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEART_RATE_SERVICE));
        return service != null;
    }

    /**
     * Tests whether BLE Device has Health Thermometer Service.
     * @param gatt GATT Service
     * @return true BLE device has Health Thermometer Service
     */
    public static boolean hasHealthThermometerService(final BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEALTH_THERMOMETER_SERVICE));
        return service != null;
    }

    /**
     * Tests whether BLE Device has Health Blood Pressure Service.
     * @param gatt GATT Service
     * @return true BLE device has Health Blood Pressure Service
     */
    public static boolean hasHealthBloodpressureService(final BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEALTH_BLOOD_PRESSURE_SERVICE));
        return service != null;
    }

    /**
     * Tests whether BLE Device has Health Weight Scale Service.
     * @param gatt GATT Service
     * @return true BLE device has Health Weight Scale Service
     */
    public static boolean hasHealthWeightScaleService(final BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(
                BleUtils.SERVICE_HEALTH_WEIGHT_SCALE_SERVICE));
        return service != null;
    }
}

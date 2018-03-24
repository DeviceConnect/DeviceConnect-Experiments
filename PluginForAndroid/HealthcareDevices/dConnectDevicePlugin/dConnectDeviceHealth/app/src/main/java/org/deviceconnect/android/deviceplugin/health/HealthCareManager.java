/*
 HealthCareManager
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.health.ble.BleDeviceDetector;
import org.deviceconnect.android.deviceplugin.health.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.health.data.HealthCareDBHelper;
import org.deviceconnect.android.deviceplugin.health.data.HealthCareDevice;
import org.deviceconnect.android.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.deviceconnect.android.deviceplugin.health.HealthCareConnector.ConnectEventListener;
import static org.deviceconnect.android.deviceplugin.health.ble.BleDeviceDetector.BleDeviceDiscoveryListener;

/**
 * This class manages a BLE device and GATT Service.
 * <p>
 * This class provides the following functions:
 * <li>Scan a BLE device</li>
 * <li>Connect a GATT of Health Care Service</li>
 * <li>Get health care</li>
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class HealthCareManager {

    /**
     * Instance of {@link BleDeviceDetector}.
     */
    private BleDeviceDetector mDetector;

    /**
     * Instance of {@link HealthCareConnector}.
     */
    private HealthCareConnector mConnector;

    /**
     * Instance of {@link HealthCareDBHelper}.
     */
    private HealthCareDBHelper mDBHelper;

    /**
     * Define the time to delay first execution.
     */
    private static final int CHK_FIRST_WAIT_PERIOD = 1000;

    /**
     * Define the period between successive executions.
     */
    private static final int CHK_WAIT_PERIOD = 10 * 1000;

    /**
     * Define the period between successive executions (ecomode).
     */
    private static final int CHK_WAIT_PERIOD_ECOMODE = 20 * 1000;

    /**
     * Instance of ScheduledExecutorService.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * ScheduledFuture of automatic connection timer.
     */
    private ScheduledFuture<?> mAutoConnectTimerFuture;

    /**
     * デバイスの検索とデバイス情報の取得用コールバックリスナの保持
     */
    private OnDeviceDiscoveryListener mDeviceDiscoveryListener;

    /**
     * プロファイル種別に対応するGattイベントリスナの保持
     */
    private HashMap<Integer, OnGattEventListener> mGattEventListener = new HashMap<Integer, OnGattEventListener>();

    /**
     * Bluetooth状態（ON/OFF）の変更通知リスナの保持
     */
    private OnBluetoothStateChangedListener mBluetoothStateChangedListener;

    /**
     * アプリケーションコンテキスト
     */
    private Context mContext;

    /**
     * Handler
     */
    private Handler mHandler = new Handler();

    /**
     * Constructor.
     * @param context application context
     */
    public HealthCareManager(final Context context) {

        mContext = context;
        mDetector = new BleDeviceDetector(context, mDiscoveryListener);
        mConnector = new HealthCareConnector(context, mConnectEventListener);
        mDBHelper = new HealthCareDBHelper(context);
        addOnBluetoothGattListener(HealthCareDevice.PROFILE_TYPE_UNCONFIRMED, new GattEventListener());

        if (mDetector.isEnabled()) {
            start();
        }
    }

    /**
     * Starts the HealthCareManager.
     */
    public void start() {
        mDetector.initialize();
        if (mDetector.isScanning()) {
            mDetector.startScan();
        }
        autoConnectionStart();
    }

    /**
     * reInitialize the BleDeviceDetector.
     */
    public void reInitialize() {
        mDetector.reInitialize();
    }

    /**
     * Sets the OnDeviceDiscoveryListener.
     * @param listener The listener to be told when found device or connected device
     */
    public void setOnDeviceDiscoveryListener(OnDeviceDiscoveryListener listener) {
        mDeviceDiscoveryListener = listener;
    }

    /**
     * Sets the OnGattEventListener.
     * @param profileType sub profile type.
     * @param listener The listener to be told when get a data of bluetooth gatt data.
     */
    public void addOnBluetoothGattListener(int profileType, OnGattEventListener listener) {
        mGattEventListener.put(profileType, listener);
    }

    /**
     * Sets the OnBluetoothStateChangedListener.
     * @param listener listener.
     */
    public void setOnBluetoothStateChangedListener(OnBluetoothStateChangedListener listener) {
        mBluetoothStateChangedListener = listener;
    }

    /**
     * Starts BLE scan.
     */
    public void startScanBle() {
        mDetector.startScan();
    }

    /**
     * Stops BLE scan.
     */
    public void stopScanBle() {
        mDetector.stopScan();
    }

    /**
     * Check Scanning.
     * @return is Scanning
     */
    public boolean isScanning() {
        return mDetector.isScanning();
    }

    /**
     * Gets the set of BluetoothDevice that are bonded (paired) to the local adapter.
     * @return set of BluetoothDevice, or null on error
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return mDetector.getBondedDevices();
    }

    /**
     * Starts timer for automatic connection of BLE device.
     * <p>
     *     If timer has already started, this method do nothing.
     * </p>
     * <p>
     *     NOTE: The automatic connection was implemented on one's own,
     *           because the autoConnect flag of BluetoothDevice#connectGatt did not work as expected.
     * </p>
     * @throws IllegalStateException if {@link BleDeviceDetector} has not been set, this exception occur.
     */
    public void autoConnectionStart() {
        autoConnectionStart(CHK_FIRST_WAIT_PERIOD);
    }

    private synchronized void autoConnectionStart(int firstPeriod) {
        if (mAutoConnectTimerFuture != null) {
            // timer has already started.
            return;
        }
        int interval = CHK_WAIT_PERIOD;
        SharedPreferences sp = mContext.getSharedPreferences("common", Context.MODE_PRIVATE);
        if (sp.getBoolean("ecomode", false)) {
            interval = CHK_WAIT_PERIOD_ECOMODE;
        }
        mAutoConnectTimerFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("HealthCareManager.autoConnectionStart().run()");
                boolean foundOfflineDevice = false;
                for (HealthCareDevice device : getRegisteredDevices()) {
                    if (!mConnector.isContainGattMap(device.getAddress())) {
                        // Found the offline device.
                        LogUtil.d("  -> offlineDevice:" + device.getAddress());
                        foundOfflineDevice = true;
                    }
                }
                if (foundOfflineDevice) {
                    LogUtil.d("HealthCareManager.scanLeDeviceOnce()");
                    mDetector.scanLeDeviceOnce(new BleDeviceDetector.BleDeviceDiscoveryListener() {
                        @Override
                        public void onDiscovery(final List<BluetoothDevice> devices) {
                            LogUtil.d("HealthCareManager.onDiscovery() called.");
                            for (HealthCareDevice registeredDevice : getRegisteredDevices()) {
                                if (!mConnector.isContainGattMap(registeredDevice.getAddress())) {
                                    // 登録済みデバイスかつ、接続未の場合だけを対象に以下の判定を行う

                                    // 1. 探索結果の中に登録済みデバイスが存在するか
                                    BluetoothDevice device = getBluetoothDeviceFromDeviceList(devices, registeredDevice.getAddress());
                                    if (device != null) {
                                        LogUtil.d("  -> find device");
                                        connectBleDevice(device.getAddress());
                                        continue;
                                    }

                                    // 2. Bluetooth接続されているか
                                    // ペアリング済みBLEデバイスが一部の端末で検索できないため
                                    // Bluetooth接続されていることを検出した場合は接続を試みる.
                                    BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
                                    List<BluetoothDevice> connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT);
                                    if (connectedDevices != null) {
                                        for (BluetoothDevice connectedDevice : connectedDevices) {
                                            String connectedAddress = connectedDevice.getAddress();
                                            if (connectedAddress != null &&
                                                    connectedAddress.equals(registeredDevice.getAddress())) {
                                                LogUtil.d("  -> connected device");
                                                connectBleDevice(connectedDevice.getAddress());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }, firstPeriod, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * 再接続処理
     */
    public void autoConnectionRestart() {
        if (mAutoConnectTimerFuture != null) {
            mAutoConnectTimerFuture.cancel(true);
            mAutoConnectTimerFuture = null;
            autoConnectionStart(0);
        }
    }
    /**
     * Gets a BluetoothDevice from device list.
     * @param list list
     * @param address address
     * @return Instance of BluetoothDevice, null if not found address
     */
    public BluetoothDevice getBluetoothDeviceFromDeviceList(
            final List<BluetoothDevice> list, final String address) {
        for (BluetoothDevice device : list) {
            if (address.equalsIgnoreCase(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    /**
     * Connect to GATT Server by address.
     * @param address address for ble device
     */
    public void connectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            mConnector.connectDevice(blue);
        }
    }

    /**
     * Disconnect to GATT Server by address.
     * @param address address for ble device
     */
    public void disconnectBleDevice(final String address) {
        BluetoothDevice blue = mDetector.getDevice(address);
        if (blue != null) {
            mConnector.disconnectDevice(blue);
        }
    }

    /**
     * Check that BLE device connected.
     * @return is Connected.
     */
    public boolean isConnectedDevice(String address) {
        return mConnector.isContainGattMap(address);
    }

    /**
     * Stops the HealthCareManager.
     */
    public void stop() {
        mConnector.stop();
        mDetector.stopScan();
        if (mAutoConnectTimerFuture != null) {
            mAutoConnectTimerFuture.cancel(true);
            mAutoConnectTimerFuture = null;
        }
    }

    /**
     * Gets the list of all BLE device.
     * @return list of BLE device
     */
    public List<HealthCareDevice> getAllDevices() {
        return mDBHelper.getAllDevices();
    }

    /**
     * Gets the list of BLE device that was registered to automatic connection.
     * @return list of BLE device
     */
    public List<HealthCareDevice> getRegisteredDevices() {
        return mDBHelper.getRegisteredDevices();
    }

    /**
     * Find the {@link HealthCareDevice} from address.
     * @param address address of ble device
     * @return {@link HealthCareDevice}, or null
     */
    public HealthCareDevice getRegisteredDevice(final String address) {
        return mDBHelper.getRegisteredDevice(address);
    }

    /**
     * Register the {@link HealthCareDevice} to database from BluetoothDevice.
     * @param device Instance of BluetoothDevice
     * @return {@link HealthCareDevice}
     */
    public HealthCareDevice registerHealthCareDevice(final BluetoothDevice device, int profileType) {
        if (getRegisteredDevice(device.getAddress()) != null) {
            return null;
        }
        HealthCareDevice hr = new HealthCareDevice();
        String name = device.getName();
        if (name == null) {
            name = "";
        }
        hr.setName(name);
        hr.setAddress(device.getAddress());
        hr.setRegisterFlag(false);
        hr.setProfileType(profileType);
        mDBHelper.addHealthCareDevice(hr);

        return hr;
    }

    /**
     * 登録有無フラグを更新する
     * @param address デバイスのaddress
     * @param flag フラグ値
     */
    public void setRegisterFlag(String address, boolean flag) {
        HealthCareDevice hr = getRegisteredDevice(address);
        if (hr != null) {
            hr.setRegisterFlag(flag);
            mDBHelper.updateHealthCareDevice(hr);
        }
    }

    /**
     * デバイス情報を更新する
     * @param hr ヘルスケアデバイス情報
     * @return DB更新結果
     */
    public int updateHealthCareDevice(final HealthCareDevice hr) {
        return mDBHelper.updateHealthCareDevice(hr);
    }

    /*
     * Implementation of ConnectEventListener.
     */
    private final ConnectEventListener mConnectEventListener = new ConnectEventListener() {

        @Override
        public boolean onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onConnectionStateChange(gatt, status, newState);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (gatt.getDevice() != null) {
                        if (!mConnector.isContainGattMap(gatt.getDevice().getAddress())) {
                            showToast(gatt.getDevice(), "Connect to ");
                        }
                    }
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (gatt.getDevice() != null) {
                        if (mConnector.isContainGattMap(gatt.getDevice().getAddress())) {
                            showToast(gatt.getDevice(), "Disconnect to ");
                        }
                    }
                }
            }
            return isUsed;
        }

        @Override
        public boolean onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onServicesDiscovered(gatt, status);
            }
            return isUsed;
        }

        @Override
        public boolean onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onCharacteristicRead(gatt, characteristic, status);
            }
            return isUsed;
        }

        @Override
        public boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onCharacteristicChanged(gatt, characteristic);
            }
            return isUsed;
        }

        @Override
        public boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onDescriptorRead(gatt, descriptor, status);
            }
            return isUsed;
        }

        @Override
        public boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onCharacteristicWrite(gatt, characteristic, status);
            }
            return isUsed;
        }

        @Override
        public boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onDescriptorWrite(gatt, descriptor, status);
            }
            return isUsed;
        }

        @Override
        public boolean onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onReliableWriteCompleted(gatt, status);
            }
            return isUsed;
        }

        @Override
        public boolean onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onReadRemoteRssi(gatt, rssi, status);
            }
            return isUsed;
        }

        @Override
        public boolean onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            boolean isUsed = false;
            OnGattEventListener listener = mGattEventListener.get(getRegisteredProfileType(gatt));
            if (listener != null) {
                isUsed = listener.onMtuChanged(gatt, mtu, status);
            }
            return isUsed;
        }

        /**
         * Gattに該当するサブプロファイルをDBのデバイス情報から取得する
         * @param gatt BluetoothGatt.
         * @return デバイス種別
         */
        private int getRegisteredProfileType(final BluetoothGatt gatt) {
            BluetoothDevice device = gatt.getDevice();
            if (device == null) {
                LogUtil.d("  -> RegisteredProfileType is UNCONFIRMED (device==null)");
                return HealthCareDevice.PROFILE_TYPE_UNCONFIRMED;
            }
            final String address = device.getAddress();
            if (address == null) {
                LogUtil.d("  -> RegisteredProfileType is UNCONFIRMED (address==null)");
                return HealthCareDevice.PROFILE_TYPE_UNCONFIRMED;
            }
            HealthCareDevice registedDevice = getRegisteredDevice(address);
            if (registedDevice == null) {
                LogUtil.d("  -> RegisteredProfileType is UNCONFIRMED (registeredDevice==null)");
                return HealthCareDevice.PROFILE_TYPE_UNCONFIRMED;
            }
            if (!registedDevice.isDeviceInformationRegistered()) {
                LogUtil.d("  -> RegisteredProfileType is UNCONFIRMED (isDeviceInformationRegistered() is false)");
                return HealthCareDevice.PROFILE_TYPE_UNCONFIRMED;
            }
            LogUtil.d("  -> RegisteredProfileType is " + registedDevice.getProfileType());
            return registedDevice.getProfileType();
        }
    };

    private class GattEventListener extends HealthCareManager.OnGattEventListener {
        @Override
        public boolean onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS ||
                    newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onDisconnected(gatt.getDevice(), status);
                }
            }
            return true;
        }

        @Override
        public boolean onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                registerHealthCareDevice(gatt.getDevice(), HealthCareDevice.getProfileType(gatt));
                requestDeviceInformation(gatt, null);
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onDisconnected(gatt.getDevice(), status);
                }
            } else {
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onDisconnected(gatt.getDevice(), status);
                }
            }
            return true;
        }
        @Override
        public boolean onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {

            if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onDisconnected(gatt.getDevice(), status);
                }
            } else if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.getUuid().equals(UUID.fromString(BleUtils.CHAR_MANUFACTURER_NAME_STRING))) {
                    HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                    byte[] data = characteristic.getValue();
                    device.setDeviceManufacturerName(new String(data));
                    updateHealthCareDevice(device);
                } else if (characteristic.getUuid().equals(UUID.fromString(BleUtils.CHAR_MODEL_NUMBER_STRING))) {
                    HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                    byte[] data = characteristic.getValue();
                    device.setDeviceModelNumber(new String(data));
                    updateHealthCareDevice(device);
                } else if (characteristic.getUuid().equals(UUID.fromString(BleUtils.CHAR_SERIAL_NUMBER_STRING))) {
                    HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                    byte[] data = characteristic.getValue();
                    device.setDeviceSerialNumber(new String(data));
                    updateHealthCareDevice(device);
                } else if (characteristic.getUuid().equals(UUID.fromString(BleUtils.CHAR_HARDWARE_REVISION_STRING))) {
                    HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                    byte[] data = characteristic.getValue();
                    device.setDeviceHardwareRevision(new String(data));
                    updateHealthCareDevice(device);
                } else if (characteristic.getUuid().equals(UUID.fromString(BleUtils.CHAR_FIRMWARE_REVISION_STRING))) {
                    HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                    byte[] data = characteristic.getValue();
                    device.setDeviceFirmwareRevision(new String(data));
                    updateHealthCareDevice(device);
                } else if (characteristic.getUuid().equals(UUID.fromString(BleUtils.CHAR_SOFTWARE_REVISION_STRING))) {
                    HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                    byte[] data = characteristic.getValue();
                    device.setDeviceSoftwareRevision(new String(data));
                    updateHealthCareDevice(device);
                } else if (characteristic.getUuid().equals(UUID.fromString(BleUtils.CHAR_SYSTEM_ID))) {
                    HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                    byte[] data = characteristic.getValue();
                    device.setDeviceSystemId(new String(data));
                    updateHealthCareDevice(device);
                }

                // 次のデバイス情報の要求.
                requestDeviceInformation(gatt, characteristic);
            }
            return true;
        }

        /**
         * 次のデバイス情報の読み込み要求を行う
         * @param gatt BluetoothGatt
         * @param characteristic 取得したデバイス情報のCharacteristic
         */
        private void requestDeviceInformation(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            BluetoothGattCharacteristic next;
            if (characteristic == null) {
                next = getNextRequestCharacteristic(gatt, null);
            } else {
                next = getNextRequestCharacteristic(gatt, characteristic.getUuid());
            }
            if (next != null) {
                gatt.readCharacteristic(next);
                LogUtil.d("  -> gatt.readCharacteristic() next is " + next.getUuid());
            } else {
                HealthCareDevice device = getRegisteredDevice(gatt.getDevice().getAddress());
                device.setDeviceInformationRegistered(true);
                updateHealthCareDevice(device);
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onRegistered(gatt.getDevice());
                }
                disconnectBleDevice(device.getAddress());
                showToast(gatt.getDevice(), "Disconnect to ");
            }
        }

        /**
         * 次に取得を要求するCharacteristicを決定する
         * @param gatt BluetoothGatt
         * @param current 現在取得したCharacteristic
         * @return 次に要求するCharacteristic
         */
        private BluetoothGattCharacteristic getNextRequestCharacteristic(final BluetoothGatt gatt, UUID current) {
            BluetoothGattCharacteristic next;
            UUID nextUUID;
            if (current == null) {
                nextUUID = UUID.fromString(BleUtils.CHAR_MANUFACTURER_NAME_STRING);
                next = gatt.getService(UUID.fromString(BleUtils.SERVICE_DEVICE_INFORMATION)).getCharacteristic(nextUUID);
            } else if (current.equals(UUID.fromString(BleUtils.CHAR_MANUFACTURER_NAME_STRING))) {
                nextUUID = UUID.fromString(BleUtils.CHAR_MODEL_NUMBER_STRING);
                next = gatt.getService(UUID.fromString(BleUtils.SERVICE_DEVICE_INFORMATION)).getCharacteristic(nextUUID);
            } else if (current.equals(UUID.fromString(BleUtils.CHAR_MODEL_NUMBER_STRING))) {
                nextUUID = UUID.fromString(BleUtils.CHAR_SERIAL_NUMBER_STRING);
                next = gatt.getService(UUID.fromString(BleUtils.SERVICE_DEVICE_INFORMATION)).getCharacteristic(nextUUID);
            } else if (current.equals(UUID.fromString(BleUtils.CHAR_SERIAL_NUMBER_STRING))) {
                nextUUID = UUID.fromString(BleUtils.CHAR_HARDWARE_REVISION_STRING);
                next = gatt.getService(UUID.fromString(BleUtils.SERVICE_DEVICE_INFORMATION)).getCharacteristic(nextUUID);
            } else  if (current.equals(UUID.fromString(BleUtils.CHAR_HARDWARE_REVISION_STRING))) {
                nextUUID = UUID.fromString(BleUtils.CHAR_FIRMWARE_REVISION_STRING);
                next = gatt.getService(UUID.fromString(BleUtils.SERVICE_DEVICE_INFORMATION)).getCharacteristic(nextUUID);
            } else if (current.equals(UUID.fromString(BleUtils.CHAR_FIRMWARE_REVISION_STRING))) {
                nextUUID = UUID.fromString(BleUtils.CHAR_SOFTWARE_REVISION_STRING);
                next = gatt.getService(UUID.fromString(BleUtils.SERVICE_DEVICE_INFORMATION)).getCharacteristic(nextUUID);
            } else if (current.equals(UUID.fromString(BleUtils.CHAR_SOFTWARE_REVISION_STRING))) {
                nextUUID = UUID.fromString(BleUtils.CHAR_SYSTEM_ID);
                next = gatt.getService(UUID.fromString(BleUtils.SERVICE_DEVICE_INFORMATION)).getCharacteristic(nextUUID);
            } else {
                return null;
            }

            if (next == null) {
                next = getNextRequestCharacteristic(gatt, nextUUID);
            }
            return next;
        }
    }

    private void showToast(final BluetoothDevice device, final String text) {
        // DEBUG
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String name = device.getName();
                    if (name == null) {
                        name = device.getAddress();
                    }
                    Toast.makeText(mContext, text + name,
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    LogUtil.w("Exception occurred.");
                }
            }
        });
    }

    /**
     * Implementation of BleDeviceDiscoveryListener.
     */
    private final BleDeviceDiscoveryListener mDiscoveryListener = new BleDeviceDiscoveryListener() {
        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {
            if (mDeviceDiscoveryListener != null) {
                mDeviceDiscoveryListener.onDiscovery(devices);
            }
        }
    };

    /**
     * Bluetoothの状態変更を受ける
     */
    public void onBluetoothStateChanged(int state) {
        if (state == BluetoothAdapter.STATE_ON) {
            start();
        } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
            stop();
        }
        if (mBluetoothStateChangedListener != null) {
            mBluetoothStateChangedListener.onStateChanged(state);
        }
    }

    /**
     * This interface is used to implement {@link HealthCareManager} callbacks.
     */
    public interface OnDeviceDiscoveryListener {
        void onDiscovery(List<BluetoothDevice> devices);
        void onDisconnected(BluetoothDevice device, final int status);
        void onRegistered(BluetoothDevice device);
    }

    /**
     * This interface is used to implement {@link HealthCareManager} callbacks.
     */
    public static class OnGattEventListener {
        public boolean onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            return false;
        }
        public boolean onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            return false;
        }
        public boolean onCharacteristicRead(final BluetoothGatt gatt,final BluetoothGattCharacteristic characteristic, final int status) {
            return false;
        }
        public boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            return false;
        }
        public boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            return false;
        }
        public boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            return false;
        }
        public boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            return false;
        }
        public boolean onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            return false;
        }
        public boolean onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            return false;
        }
        public boolean onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            return false;
        }
    }

    /**
     * This interface is used to implement {@link HealthCareManager} callbacks.
     */
    public interface OnBluetoothStateChangedListener {
        void onStateChanged(int state);
    }
}

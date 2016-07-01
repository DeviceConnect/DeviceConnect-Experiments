/*
 HealthCareConnector
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import org.deviceconnect.android.util.LogUtil;

import java.util.ArrayList;

/**
 * This class manages a BLE device that have Health Care Service.
 * <p>
 * This class provides the following functions:
 * <li>Connect a GATT of Health Care Service</li>
 * <li>Disconnect a GATT of Health Care Service</li>
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class HealthCareConnector {

    /**
     * application context.
     */
    private Context mContext;

    /**
     * Instance of ConnectEventListener.
     */
    private ConnectEventListener mListener;

    /**
     * List of Bluetooth Gatt.
     */
    private final ArrayList<BluetoothGatt> mBLEDevices = new ArrayList();

    /**
     * Constructor.
     * @param context application context
     * @param listener Connect event listener
     */
    public HealthCareConnector(final Context context, final ConnectEventListener listener) {
        mContext = context;
        mListener = listener;
    }

    /**
     * Connect to the bluetooth device.
     * @param device bluetooth device
     */
    public void connectDevice(final BluetoothDevice device) {
        LogUtil.d("## HealthCareConnector.connectDevice()");
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }
        LogUtil.d("  -> address:" + device.getAddress());

        try {
            device.connectGatt(mContext, false, mBluetoothGattCallback);
        } catch (Exception e) {
            // Exception occurred when the BLE state is invalid.
            LogUtil.w("Exception occurred.");
        }
    }

    /**
     * Disconnect to the bluetooth device.
     * @param device bluetooth device
     */
    public void disconnectDevice(final BluetoothDevice device) {
        LogUtil.d("## HealthCareConnector.disconnectDevice()");
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        }
        LogUtil.d("  -> address:" + device.getAddress());

        String address = device.getAddress();
        synchronized (mBLEDevices) {
            for (int i = mBLEDevices.size() - 1; i >= 0; i--) {
                BluetoothGatt gatt = mBLEDevices.get(i);
                if (gatt.getDevice().getAddress().equalsIgnoreCase(address)) {
                    LogUtil.d("  -> gatt.close() address:" + gatt.getDevice().getAddress());
                    gatt.close();
                    mBLEDevices.remove(gatt);
                }
            }
        }
    }

    /**
     * Stops timer for automatic connection of BLE device.
     */
    public synchronized void stop() {
        LogUtil.d("HealthCareConnector.stop()");

        synchronized (mBLEDevices) {
            for (BluetoothGatt gatt : mBLEDevices) {
                LogUtil.d("  -> gatt.close() address:" + gatt.getDevice().getAddress());
                gatt.close();
            }
            mBLEDevices.clear();
        }
    }

    /**
     * Tests whether this mBLEDevices contains address.
     * @param address BLE device address
     * @return true if address is an element of mBLEDevices, false otherwise
     */
    public boolean isContainGattMap(final String address) {
        synchronized (mBLEDevices) {
            for (BluetoothGatt gatt : mBLEDevices) {
                if (gatt.getDevice().getAddress().equalsIgnoreCase(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This class is the implement of BluetoothGattCallback.
     */
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt,
                                            final int status, final int newState) {
            LogUtil.d("HealthCareConnector.onConnectionStateChange()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status + " newState:" + newState);

            // onConnectionStateChange()のタイミングではサービスの情報が取得できないため
            // サブプロファイルへの通知による復帰値は参照しない。
            mListener.onConnectionStateChange(gatt, status, newState);

            if (status != BluetoothGatt.GATT_SUCCESS ||
                        newState == BluetoothProfile.STATE_DISCONNECTED) {
                LogUtil.d("  -> gatt.close() address:" + gatt.getDevice().getAddress());
                gatt.close();
                mBLEDevices.remove(gatt);
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (isContainGattMap(gatt.getDevice().getAddress())) {
                    gatt.close();
                } else {
                    LogUtil.d("  -> gatt.discoverServices()");
                    gatt.discoverServices();
                    mBLEDevices.add(gatt);
                }
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            LogUtil.d("HealthCareConnector.onServicesDiscovered()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onServicesDiscovered(gatt, status);
            if (!isUsed) {
                super.onServicesDiscovered(gatt, status);
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, final int status) {
            LogUtil.d("HealthCareConnector.onCharacteristicRead()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onCharacteristicRead(gatt, characteristic, status);
            if (!isUsed) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
            LogUtil.d("HealthCareConnector.onCharacteristicChanged()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress());

            boolean isUsed = mListener.onCharacteristicChanged(gatt, characteristic);
            if (!isUsed) {
                super.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            LogUtil.d("HealthCareConnector.onDescriptorRead()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onDescriptorRead(gatt, descriptor, status);
            if (!isUsed) {
                super.onDescriptorRead(gatt, descriptor, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.d("HealthCareConnector.onCharacteristicWrite()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onCharacteristicWrite(gatt, characteristic, status);
            if (!isUsed) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            LogUtil.d("HealthCareConnector.onDescriptorWrite()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onDescriptorWrite(gatt, descriptor, status);
            if (!isUsed) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            LogUtil.d("HealthCareConnector.onReliableWriteCompleted()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onReliableWriteCompleted(gatt, status);
            if (!isUsed) {
                super.onReliableWriteCompleted(gatt, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            LogUtil.d("HealthCareConnector.onReadRemoteRssi()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onReadRemoteRssi(gatt, rssi, status);
            if (!isUsed) {
                super.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            LogUtil.d("HealthCareConnector.onMtuChanged()");
            LogUtil.d("  -> address:" + gatt.getDevice().getAddress() + " status:" + status);

            boolean isUsed = mListener.onMtuChanged(gatt, mtu, status);
            if (!isUsed) {
                super.onMtuChanged(gatt, mtu, status);
            }
        }
    };

    /**
     * This interface is used to implement {@link HealthCareConnector} callbacks.
     */
    public interface ConnectEventListener {
        boolean onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState);
        boolean onServicesDiscovered(final BluetoothGatt gatt, final int status);
        boolean onCharacteristicRead(final BluetoothGatt gatt,final BluetoothGattCharacteristic characteristic, final int status);
        boolean onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic);
        boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);
        boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
        boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);
        boolean onReliableWriteCompleted(BluetoothGatt gatt, int status);
        boolean onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);
        boolean onMtuChanged(BluetoothGatt gatt, int mtu, int status);
    }
}

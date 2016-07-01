/*
 HealthCareDeviceSettingsFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.health.HealthCareApplication;
import org.deviceconnect.android.deviceplugin.health.HealthCareManager;
import org.deviceconnect.android.deviceplugin.health.R;
import org.deviceconnect.android.deviceplugin.health.activity.HealthCareDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.health.data.HealthCareDevice;
import org.deviceconnect.android.deviceplugin.health.fragment.dialog.ErrorDialogFragment;
import org.deviceconnect.android.deviceplugin.health.fragment.dialog.ProgressDialogFragment;
import org.deviceconnect.android.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.deviceconnect.android.deviceplugin.health.HealthCareManager.OnDeviceDiscoveryListener;

/**
 * This fragment do setting of the connection to the ble device.
 * @author NTT DOCOMO, INC.
 */
public class HealthCareDeviceSettingsFragment extends Fragment {
    /**
     * Adapter.
     */
    private DeviceAdapter mDeviceAdapter;

    /**
     * Manager.
     */
    private HealthCareManager mManager;

    /**
     * Error Dialog.
     */
    private ErrorDialogFragment mErrorDialogFragment;

    /**
     * Progress Dialog.
     */
    private ProgressDialogFragment mProgressDialogFragment;

    /**
     * Handler.
     */
    private Handler mHandler = new Handler();

    /**
     * Bluetooth ON/OFFを判定する
     */
    BluetoothAdapter mBluetoothAdapter = null;

    View mRootView;

    /**
     * Bluetooth ON/OFFの切り替えスイッチ
     */
    Switch mBluetoothSwitch = null;

    /**
     * スキャン中のプログレス部品
     */
    View mFooterView = null;

    /**
     * スキャン実行中フラグ
     */
    boolean mIsScanning = false;

    /**
     * Supported Product List (Not Used)
     */
    String[] productNames = {
        "PS-100",       // EPSON PULSENSE
        "PS-500",       // EPSON PULSENSE
        "UT201BLE",     // A&D BLE Thermometer
        "UA-651BLE",    // A&D BLE Blood Pressure
        "UC-352BLE"     // A&D BLE Weight Scale
    };


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mDeviceAdapter = new DeviceAdapter(getActivity(), createDeviceContainers());

        mRootView = inflater.inflate(R.layout.fragment_heart_rate_device_settings, null);
        final ListView listView = (ListView) mRootView.findViewById(R.id.device_list_view);
        mFooterView = inflater.inflate(R.layout.bluetooth_list_footer_view, null);

        // デバイスの一覧のヘッダにスキャン要求・停止ボタンを追加する。
        View headerView = inflater.inflate(R.layout.bluetooth_list_header_view, null);
        Button button = (Button) headerView.findViewById(R.id.button_scan);
        if (getManager().isScanning()) {
            button.setText(R.string.heart_rate_setting_button_stop);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickScanButton();
            }
        });
        listView.addHeaderView(headerView);
        listView.setAdapter(mDeviceAdapter);

        // Bluetoothのサポート状況と、ON/OFFを判定する。
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothSwitch = (Switch) mRootView.findViewById(R.id.switch_bluetooth);
            if (mBluetoothSwitch != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothSwitch.setChecked(true);
                    mRootView.findViewById(R.id.text_bluetooth_settings).setVisibility(View.GONE);
                    mRootView.findViewById(R.id.device_list_view).setVisibility(View.VISIBLE);
                } else {
                    mBluetoothSwitch.setChecked(false);
                    mRootView.findViewById(R.id.text_bluetooth_settings).setVisibility(View.VISIBLE);
                    mRootView.findViewById(R.id.device_list_view).setVisibility(View.GONE);
                }
            }
            mBluetoothSwitch.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                mBluetoothAdapter.enable();
                                mBluetoothSwitch.setEnabled(false);
                            } else {
                                mBluetoothAdapter.disable();
                                mBluetoothSwitch.setEnabled(false);
                            }
                        }
                    }
            );

            getManager().setOnBluetoothStateChangedListener(mBLEListener);
        }
        return mRootView;
    }

    private void onClickScanButton() {
        final ListView listView = (ListView) mRootView.findViewById(R.id.device_list_view);
        Button button = (Button) mRootView.findViewById(R.id.button_scan);
        if (mIsScanning) {
            getManager().stopScanBle();
            button.setText(R.string.heart_rate_setting_button_start);
            listView.removeFooterView(mFooterView);
            mIsScanning = false;
        } else {
            getManager().startScanBle();
            button.setText(R.string.heart_rate_setting_button_stop);
            listView.addFooterView(mFooterView);
            mIsScanning = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getManager().setOnDeviceDiscoveryListener(mEvtListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getManager().setOnDeviceDiscoveryListener(null);
        getManager().stopScanBle();
        dismissProgressDialog();
        dismissErrorDialog();
    }

    /**
     * Connect to the BLE device that have heart rate service.
     * @param device BLE device that have heart rate service.
     */
    private void connectDevice(final DeviceContainer device) {
        getManager().connectBleDevice(device.getAddress());
        showProgressDialog(device.getName());

        connectingName = device.getAddress();
    }

    private String connectingName;

    /**
     * Disconnect to the BLE device that have heart rate service.
     * @param device BLE device that have heart rate service.
     */
    private void disconnectDevice(final DeviceContainer device) {
        getManager().disconnectBleDevice(device.getAddress());
        getManager().setRegisterFlag(device.getAddress(), false);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
                if (container != null) {
                    container.setRegisterFlag(false);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * デバイスの登録
     * @param device 登録対象デバイス
     */
    private void registerDevice(final DeviceContainer device) {
        getManager().setRegisterFlag(device.getAddress(), true);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
                if (container != null) {
                    container.setRegisterFlag(true);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Display the dialog of connecting a ble device.
     * @param name device name
     */
    private void showProgressDialog(final String name) {
        dismissProgressDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.heart_rate_setting_connecting_title);
        String message = res.getString(R.string.heart_rate_setting_connecting_message, name);
        mProgressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        mProgressDialogFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Dismiss the dialog of connecting a ble device.
     */
    private void dismissProgressDialog() {
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
            mProgressDialogFragment = null;
        }
    }

    /**
     * Display the error dialog.
     * @param name device name
     * @param status error reason.
     */
    private void showErrorDialog(String name, final int status) {
        dismissErrorDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.heart_rate_setting_dialog_error_title);
        if (name== null) {
            name = getString(R.string.heart_rate_setting_default_name);
        }
        String message;
        if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
            message = res.getString(R.string.heart_rate_setting_dialog_need_auth, name);
        } else {
            message = res.getString(R.string.heart_rate_setting_dialog_error_message, name);
        }
        mErrorDialogFragment = ErrorDialogFragment.newInstance(title, message);
        mErrorDialogFragment.show(getFragmentManager(), "error_dialog");
        mErrorDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mErrorDialogFragment = null;
            }
        });
    }

    /**
     * Dismiss the error dialog.
     */
    private void dismissErrorDialog() {
        if (mErrorDialogFragment != null) {
            mErrorDialogFragment.dismiss();
            mErrorDialogFragment = null;
        }
    }

    /**
     * Gets a instance of HealthCareManager.
     * @return HealthCareManager
     */
    private HealthCareManager getManager() {
        return mManager;
    }

    private OnDeviceDiscoveryListener mEvtListener = new OnDeviceDiscoveryListener() {

        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {
            if (mDeviceAdapter == null) {
                return;
            }
            LogUtil.d("BleDeviceDiscoveryListener#onDiscovery: " + devices.size());
            if (getActivity() == null) {
                return;
            }
            for (BluetoothDevice device : devices) {
                int count = mDeviceAdapter.getCount();
                boolean isMatched = false;
                for (int i = 0; i < count; i++) {
                    DeviceContainer adapterDevice = mDeviceAdapter.getItem(i);
                    if (device.getAddress().equals(adapterDevice.getAddress())) {
                        isMatched = true;
                        break;
                    }
                }
                if (!isMatched) {
                    mDeviceAdapter.add(createContainer(device));
                }
            }
        }

        @Override
        public void onDisconnected(final BluetoothDevice device, final int status) {
            LogUtil.d("ConnectEventListener#onDisconnected: [" + device + "]");
            if (getActivity() == null) {
                return;
            }
            if (connectingName == null || !connectingName.equals(device.getAddress())) {
                return;
            }
            connectingName = null;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
                    if (container != null) {
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                    dismissProgressDialog();
                    showErrorDialog(device.getName(), status);
                }
            });

        }

        @Override
        public void onRegistered(final BluetoothDevice device) {
            LogUtil.d("ConnectEventListener#onRegistered: [" + device + "]");
            if (getActivity() == null) {
                return;
            }
            if (connectingName == null || !connectingName.equals(device.getAddress())) {
                return;
            }
            connectingName = null;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
                    if (container != null) {
                        HealthCareDevice healthCareDevice = getManager().getRegisteredDevice(device.getAddress());
                        if (healthCareDevice == null) {
                            container.setProfileType(HealthCareDevice.PROFILE_TYPE_UNCONFIRMED);
                        } else {
                            container.setProfileType(healthCareDevice.getProfileType());
                            if (healthCareDevice.getProfileType() == HealthCareDevice.PROFILE_TYPE_HEALTH_THERMOMETER ||
                                    healthCareDevice.getProfileType() == HealthCareDevice.PROFILE_TYPE_HEART_RATE ||
                                    healthCareDevice.getProfileType() == HealthCareDevice.PROFILE_TYPE_HEALTH_BLOOD_PRESSURE ||
                                    healthCareDevice.getProfileType() == HealthCareDevice.PROFILE_TYPE_HEALTH_WEIGHT_SCALE ) {
                                // 登録済みとする
                                getManager().setRegisterFlag(device.getAddress(), true);
                                container.setRegisterFlag(true);
                            }
                        }
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                    dismissProgressDialog();
                }
            });
        }

    };

    /**
     * Create a list of device.
     * @return list of device
     */
    private List<DeviceContainer> createDeviceContainers() {
        List<DeviceContainer> containers = new ArrayList<>();
        List<HealthCareDevice> devices = getManager().getAllDevices();

        for (HealthCareDevice device : devices) {
            containers.add(createContainer(device));
        }

        // MEMO: add of device that are paired to smart phone.
        Set<BluetoothDevice> pairing = getManager().getBondedDevices();
        if (pairing != null) {
            for (BluetoothDevice device : pairing) {
                String name = device.getName();
                LogUtil.d("name is " + name);

                if (name != null
                        && (name.contains("PS-100") || name.contains("UT201BLE") || name.contains("UA-651BLE") || name.contains("UC-352BLE"))
                        && !containAddressForList(containers, device.getAddress())) {
                    containers.add(createContainer(device));
                }

                if (name != null && !containAddressForList(containers, device.getAddress())) {
                    containers.add(createContainer(device));
                }
            }
        }

        return containers;
    }

    /**
     * Returns true if this address contains the list of device.
     * @param containers list of device
     * @param address    address of device
     * @return true if address is an element of this List, false otherwise
     */
    private boolean containAddressForList(final List<DeviceContainer> containers, final String address) {
        for (DeviceContainer container : containers) {
            if (container.getAddress().equalsIgnoreCase(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Look for a DeviceContainer with the given address.
     * @param address address of device
     * @return The DeviceContainer that has the given address or null
     */
    private DeviceContainer findDeviceContainerByAddress(final String address) {
        int size = mDeviceAdapter.getCount();
        for (int i = 0; i < size; i++) {
            DeviceContainer container = mDeviceAdapter.getItem(i);
            if (container.getAddress().equalsIgnoreCase(address)) {
                return container;
            }
        }
        return null;
    }

    /**
     * Create a DeviceContainer from BluetoothDevice.
     * @param device Instance of BluetoothDevice
     * @return Instance of DeviceContainer
     */
    private DeviceContainer createContainer(final BluetoothDevice device) {
        DeviceContainer container = new DeviceContainer();
        container.setName(device.getName());
        container.setAddress(device.getAddress());
        return container;
    }

    /**
     * Create a DeviceContainer from HealthCareDevice.
     * @param device   Instance of HealthCareDevice
     * @return Instance of DeviceContainer
     */
    private DeviceContainer createContainer(final HealthCareDevice device) {
        DeviceContainer container = new DeviceContainer();
        container.setName(device.getName());
        container.setAddress(device.getAddress());
        container.setRegisterFlag(device.isRegisterFlag());
        container.setProfileType(device.getProfileType());
        return container;
    }

    /**
     * デバイスリストのアダプタで管理するデバイスコンテナ
     */
    private class DeviceContainer {
        private String mName;
        private String mAddress;
        private int mProfileType;
        private boolean mRegisterFlag;

        public String getName() {
            return mName;
        }

        public void setName(final String name) {
            if (name == null) {
                mName = getActivity().getResources().getString(
                        R.string.heart_rate_setting_default_name);
            } else {
                mName = name;
            }
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

        public void setRegisterFlag(boolean registerFlag) {
            mRegisterFlag = registerFlag;
        }

        public void setProfileType(int type) {
            mProfileType = type;
        }

        public int getProfileType() {
            return mProfileType;
        }
    }

    /**
     * デバイスリストに設定するアダプタ
     */
    private class DeviceAdapter extends ArrayAdapter<DeviceContainer> {
        private LayoutInflater mInflater;

        /**
         * コンストラクタ
         * @param context Context
         * @param objects DeviceContainerのリスト
         */
        public DeviceAdapter(final Context context, final List<DeviceContainer> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_heart_rate_device, null);
            }

            // デバイス種別に対応する画像を設定する
            final DeviceContainer device = getItem(position);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.device_type_image);
            switch (device.getProfileType()) {
                case HealthCareDevice.PROFILE_TYPE_NOT_SUPPORTED:
                    imageView.setImageResource(R.drawable.not_supported);
                    break;
                case HealthCareDevice.PROFILE_TYPE_HEART_RATE:
                    imageView.setImageResource(R.drawable.heart_rate);
                    break;
                case HealthCareDevice.PROFILE_TYPE_HEALTH_THERMOMETER:
                    imageView.setImageResource(R.drawable.health_thermometer);
                    break;
                case HealthCareDevice.PROFILE_TYPE_HEALTH_BLOOD_PRESSURE:
                    imageView.setImageResource(R.drawable.health_thermometer);
                    break;
                case HealthCareDevice.PROFILE_TYPE_HEALTH_WEIGHT_SCALE:
                    imageView.setImageResource(R.drawable.health_thermometer);
                    break;
                default:
                    imageView.setImageResource(R.drawable.unconfirmed);
                    break;
            }

            // デバイス名を設定する.
            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
            nameView.setText(device.getName());

            // デバイスのMAC ADDRESSを設定する
            TextView addressView = (TextView) convertView.findViewById(R.id.device_address);
            addressView.setText(device.getAddress());

            // デバイスへの接続ボタンを設定する
            Button btn = (Button) convertView.findViewById(R.id.btn_connect_device);
            if (device.isRegisterFlag()) {
                btn.setBackgroundResource(R.drawable.button_red);
                btn.setText(R.string.heart_rate_setting_unregister);
            } else {
                btn.setBackgroundResource(R.drawable.button_blue);
                btn.setText(R.string.heart_rate_setting_register);
            }
            if (device.getProfileType() == HealthCareDevice.PROFILE_TYPE_NOT_SUPPORTED) {
                btn.setEnabled(false);
            } else {
                btn.setEnabled(true);
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (device.isRegisterFlag()) {
                        // デバイスの切断、およびデバイスの登録解除状態に変更する
                        disconnectDevice(device);
                    } else {
                        HealthCareDevice hcDevice = getManager().getRegisteredDevice(device.getAddress());
                        if (hcDevice != null && hcDevice.isDeviceInformationRegistered()) {
                            // 既に接続したことがあるデバイスの場合は登録状態に変更するのみ
                            registerDevice(device);
                        } else {
                            // 接続未デバイスの場合は、デバイス情報の取得を行うまで
                            // プログレスダイアログを表示する。
                            connectDevice(device);
                        }
                    }
                }
            });

            return convertView;
        }
    }

    /**
     * Bluetooth 有効/無効 の変更通知を受ける.
     */
    private HealthCareManager.OnBluetoothStateChangedListener mBLEListener =
            new HealthCareManager.OnBluetoothStateChangedListener() {
        @Override
        public void onStateChanged(final int state) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    stateChanged(state);
                }
            });
        }
    };

    /**
     * デバイス一覧・説明画面の切り替えを行う.
     * @param state Bluetoothの状態を示す.
     */
    public void stateChanged(int state) {
        if (mBluetoothSwitch != null) {
            if (state == BluetoothAdapter.STATE_ON) {
                mBluetoothSwitch.setChecked(true);
                mBluetoothSwitch.setEnabled(true);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                mBluetoothSwitch.setChecked(false);
                mBluetoothSwitch.setEnabled(true);
            }
        }
        if (state == BluetoothAdapter.STATE_ON) {
            // デバイス一覧画面に切り替え
            mRootView.findViewById(R.id.text_bluetooth_settings).setVisibility(View.GONE);
            mRootView.findViewById(R.id.device_list_view).setVisibility(View.VISIBLE);
        } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
            if (mIsScanning) {
                onClickScanButton();
            }
            // Bluetooth OFF切り替え中
            mRootView.findViewById(R.id.text_bluetooth_settings).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.device_list_view).setVisibility(View.GONE);
        }
    }

    /**
     * HealthCareManagerを設定する。
     * @param manager
     */
    public void setHealthCareManager(HealthCareManager manager) {
        mManager = manager;
    }
}
/*
 HealthCareDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import org.deviceconnect.android.deviceplugin.health.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.health.subprofile.BloodPressureProfile;
import org.deviceconnect.android.deviceplugin.health.subprofile.ThermometerProfile;
import org.deviceconnect.android.deviceplugin.health.subprofile.HeartRateProfile;
import org.deviceconnect.android.deviceplugin.health.subprofile.WeightscaleProfile;
import org.deviceconnect.android.profile.HealthServiceDiscoveryProfile;
import org.deviceconnect.android.profile.HealthSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.HealthServiceInformationProfile;
import org.deviceconnect.android.profile.HealthConnectProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.util.LogUtil;

/**
 * This service provide Health Profile.
 * @author NTT DOCOMO, INC.
 */
public class HealthCareDeviceService extends DConnectMessageService {

    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                getManager().onBluetoothStateChanged(state);
            }
        }
    };

    /**
     * Instance of handler.
     */
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d("HealthCareDeviceService start.");

        if (!BleUtils.isBLESupported(getContext())) {
            LogUtil.w("BLE is not support.");
            return;
        }

        EventManager.INSTANCE.setController(new MemoryCacheController());

        HealthCareApplication app = (HealthCareApplication) getApplication();
        app.initialize();

        HealthConnectProfile healthConnectProfile = new HealthConnectProfile();

        // 心拍計のサブプロファイル追加
        HeartRateProfile heartRateHealthProfile = new HeartRateProfile(app.getHealthCareManager(), this);
        healthConnectProfile.addSubProfile(HeartRateProfile.ATTRIBUTE_HEART_RATE, heartRateHealthProfile);
        // 体温計のサブプロファイル追加
        ThermometerProfile thermometerProfile = new ThermometerProfile(app.getHealthCareManager(), this);
        healthConnectProfile.addSubProfile(thermometerProfile.ATTRIBUTE_HEALTH_THERMOMETER, thermometerProfile);
        // 血圧計のサブプロファイル追加
        BloodPressureProfile bloodpressureProfile = new BloodPressureProfile(app.getHealthCareManager(), this);
        healthConnectProfile.addSubProfile(bloodpressureProfile.ATTRIBUTE_HEALTH_BLOOD_PRESSURE, bloodpressureProfile);
        // 体重計のサブプロファイル追加
        WeightscaleProfile weightscaleProfile = new WeightscaleProfile(app.getHealthCareManager(), this);
        healthConnectProfile.addSubProfile(weightscaleProfile.ATTRIBUTE_HEALTH_WEIGHT_SCALE, weightscaleProfile);

        addProfile(healthConnectProfile);

        registerBluetoothFilter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBlutoothFilter();
        getManager().stop();
        LogUtil.d("HealthCareDeviceService end.");
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HealthSystemProfile();
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HealthServiceDiscoveryProfile(this);
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new HealthServiceInformationProfile(this);
    }

    /**
     * Register a BroadcastReceiver of Bluetooth event.
     */
    private void registerBluetoothFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mSensorReceiver, filter, null, mHandler);
    }
    /**
     * Unregister a previously registered BroadcastReceiver.
     */
    private void unregisterBlutoothFilter() {
        unregisterReceiver(mSensorReceiver);
    }

    /**
     * Gets a instance of HealthCareManager.
     * @return HealthCareManager
     */
    private HealthCareManager getManager() {
        HealthCareApplication app = (HealthCareApplication) getApplication();
        return app.getHealthCareManager();
    }
}

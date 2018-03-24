/*
 HealthServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.health.HealthCareApplication;
import org.deviceconnect.android.deviceplugin.health.HealthCareDeviceService;
import org.deviceconnect.android.deviceplugin.health.HealthCareManager;
import org.deviceconnect.android.deviceplugin.health.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.health.data.HealthCareDevice;
import org.deviceconnect.android.util.LogUtil;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement ServiceDiscoveryProfile.
 * @author NTT DOCOMO, INC.
 */
public class HealthServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * パラメータ: {@value} .
     */
    String PARAM_MANUFACTURER = "manufacturer";

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public HealthServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }
    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        if (!BleUtils.isBLESupported(getContext())) {
            LogUtil.w("BLE not supported.");
            List<Bundle> services = new ArrayList<>();
            setResult(response, DConnectMessage.RESULT_OK);
            setServices(response, services);
            return true;
        }

        List<Bundle> services = new ArrayList<>();
        List<HealthCareDevice> devices = getManager().getRegisteredDevices();
        synchronized (devices) {
            for (HealthCareDevice device : devices) {
                Bundle service = new Bundle();
                service.putString(PARAM_ID, device.getAddress());
                service.putString(PARAM_NAME, device.getName());
                service.putString(PARAM_MANUFACTURER, device.getDeviceManufacturerName());
                service.putString(PARAM_TYPE, NetworkType.BLE.getValue());
                service.putBoolean(PARAM_ONLINE, true);
                service.putString(PARAM_CONFIG, "");
                ArrayList<String> scopes = new ArrayList<String>();
                scopes.add(device.getProfileTypeName());
                service.putStringArray(PARAM_SCOPES, scopes.toArray(new String[scopes.size()]));
                services.add(service);
            }
        }
        setResult(response, DConnectMessage.RESULT_OK);
        setServices(response, services);
        return true;
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        return super.onPutOnServiceChange(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        return super.onDeleteOnServiceChange(request, response, serviceId, sessionKey);
    }

    /**
     * Gets a instance of HealthCareManager.
     * @return instance of HealthCareManager
     */
    private HealthCareManager getManager() {
        HealthCareDeviceService service = (HealthCareDeviceService) getContext();
        HealthCareApplication app = (HealthCareApplication) service.getApplication();
        return app.getHealthCareManager();
    }
}

/*
 HealthCareApplication
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.health.ble.BleUtils;

/**
 * Implementation of Application.
 * @author NTT DOCOMO, INC.
 */
public class HealthCareApplication extends Application {
    /**
     * Instance of HealthCareManager.
     */
    private HealthCareManager mMgr;

    /**
     * Initialize the HealthCareApplication.
     */
    public void initialize() {
        if (mMgr == null && BleUtils.isBLESupported(getApplicationContext())) {
            mMgr = new HealthCareManager(getApplicationContext());
        }
    }

    /**
     * Gets a instance of HealthCareManager.
     * @return HealthCareManager
     */
    public HealthCareManager getHealthCareManager() {
        return mMgr;
    }
}

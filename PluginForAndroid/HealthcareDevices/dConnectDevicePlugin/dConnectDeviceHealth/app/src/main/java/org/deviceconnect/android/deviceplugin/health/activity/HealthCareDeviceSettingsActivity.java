/*
 HealthCareDeviceSettingsActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.health.HealthCareApplication;
import org.deviceconnect.android.deviceplugin.health.fragment.HealthCareDeviceSettingsFragment;
import org.deviceconnect.android.deviceplugin.health.fragment.InstructionsFragment;
import org.deviceconnect.android.deviceplugin.health.fragment.SummaryFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * This activity is settings screen.
 * @author NTT DOCOMO, INC.
 */
public class HealthCareDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HealthCareApplication app = (HealthCareApplication) getApplication();
        app.initialize();
    }

    @Override
    public int getPageCount() {return 3;}

    @Override
    public Fragment createPage(int position) {
        if (position == 0) {
            return new SummaryFragment();
        } else if (position == 1) {
            return new InstructionsFragment();
        } else {
            HealthCareApplication application = (HealthCareApplication) getApplication();
            HealthCareDeviceSettingsFragment fragment = new HealthCareDeviceSettingsFragment();
            fragment.setHealthCareManager(application.getHealthCareManager());
            return fragment;
        }
    }
}

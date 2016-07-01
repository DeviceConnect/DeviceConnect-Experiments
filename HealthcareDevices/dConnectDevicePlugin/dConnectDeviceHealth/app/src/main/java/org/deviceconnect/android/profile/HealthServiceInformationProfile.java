/*
 HealthServiceInformationProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.health.HealthCareApplication;
import org.deviceconnect.android.deviceplugin.health.HealthCareDeviceService;
import org.deviceconnect.android.deviceplugin.health.data.HealthCareDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement ServiceInformationProfile.
 * @author NTT DOCOMO, INC.
 */
public class HealthServiceInformationProfile extends ServiceInformationProfile {
    /**
     * Constructor.
     * @param provider profile provider
     */
    public HealthServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }
}

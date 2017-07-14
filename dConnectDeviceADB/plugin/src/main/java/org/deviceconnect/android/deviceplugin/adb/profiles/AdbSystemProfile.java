package org.deviceconnect.android.deviceplugin.adb.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.adb.AdbServiceListActivity;
import org.deviceconnect.android.profile.SystemProfile;


public class AdbSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return AdbServiceListActivity.class;
    }
}
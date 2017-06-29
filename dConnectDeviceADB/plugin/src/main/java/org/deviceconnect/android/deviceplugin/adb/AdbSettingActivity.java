package org.deviceconnect.android.deviceplugin.adb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.adb.fragment.AdbSettingFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

import static org.deviceconnect.android.deviceplugin.adb.fragment.AdbSettingFragment.EXTRA_IP_ADDRESS;
import static org.deviceconnect.android.deviceplugin.adb.fragment.AdbSettingFragment.EXTRA_MODE;
import static org.deviceconnect.android.deviceplugin.adb.fragment.AdbSettingFragment.EXTRA_PORT;

public class AdbSettingActivity extends DConnectSettingPageFragmentActivity {

    private AdbSettingFragment mFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = new AdbSettingFragment();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle args = new Bundle();
            args.putSerializable(EXTRA_MODE, intent.getSerializableExtra(EXTRA_MODE));
            args.putString(EXTRA_IP_ADDRESS, intent.getStringExtra(EXTRA_IP_ADDRESS));
            args.putString(EXTRA_PORT, intent.getStringExtra(EXTRA_PORT));
            mFragment.setArguments(args);
        }
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public Fragment createPage(int i) {
        return mFragment;
    }
}
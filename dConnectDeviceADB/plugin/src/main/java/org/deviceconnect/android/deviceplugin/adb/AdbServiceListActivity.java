package org.deviceconnect.android.deviceplugin.adb;

import android.app.Activity;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.adb.fragment.AdbSettingFragment;
import org.deviceconnect.android.deviceplugin.adb.service.AdbService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

import java.util.logging.Logger;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class AdbServiceListActivity extends DConnectServiceListActivity {

    private final Logger mLogger = Logger.getLogger("adb-plugin");

    @Override
    protected boolean enablesItemClick() {
        return true;
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        super.onItemClick(service);
        if (service instanceof AdbService) {
            AdbService adbService = (AdbService) service;
            Intent intent = new Intent(this, AdbSettingActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(AdbSettingFragment.EXTRA_MODE, AdbSettingFragment.Mode.EDIT);
            intent.putExtra(AdbSettingFragment.EXTRA_IP_ADDRESS, adbService.getIpAddress());
            intent.putExtra(AdbSettingFragment.EXTRA_PORT, Integer.toString(adbService.getPortNumber()));
            startActivity(intent);
        }
    }

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return AdbMessageService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return AdbSettingActivity.class;
    }

}

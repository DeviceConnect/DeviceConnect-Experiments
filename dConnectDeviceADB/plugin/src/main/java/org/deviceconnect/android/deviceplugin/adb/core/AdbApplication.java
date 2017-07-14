package org.deviceconnect.android.deviceplugin.adb.core;


import android.app.Application;

import java.util.logging.Logger;


public class AdbApplication extends Application {

    private ConnectionManager mConnectionMgr;

    private final Logger mLogger = Logger.getLogger("adb-plugin");

    @Override
    public void onCreate() {
        super.onCreate();

        mConnectionMgr = new ConnectionManager(getApplicationContext(), mLogger);
    }

    @Override
    public void onTerminate() {
        mConnectionMgr.dispose();

        super.onTerminate();
    }

    public ConnectionManager getConnectionManager() {
        return mConnectionMgr;
    }

}

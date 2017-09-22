package org.deviceconnect.android.deviceplugin.adb.core;


import android.app.Application;

import org.deviceconnect.android.deviceplugin.adb.BuildConfig;
import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class AdbApplication extends Application {

    private ConnectionManager mConnectionMgr;

    private final Logger mLogger = Logger.getLogger("adb-plugin");

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(mLogger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }

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

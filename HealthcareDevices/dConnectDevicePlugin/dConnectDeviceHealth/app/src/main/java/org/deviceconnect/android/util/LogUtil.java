/*
 LogUtil
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.util;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.health.BuildConfig;

import java.util.logging.Logger;

/**
 * LogUtility class
 */
public class LogUtil {
    private static final String TAG = "health";
    private static Logger mLogger = Logger.getLogger("health.dplugin");

    public static void v(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
            mLogger.finest(msg);
        }
    }
    public static void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg);
            mLogger.fine(msg);
        }
    }
    public static void i(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg);
            mLogger.info(msg);
        }
    }
    public static void w(String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, msg);
            mLogger.warning(msg);
        }
    }
    public static void e(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg);
            mLogger.severe(msg);
        }
    }
}

package org.deviceconnect.android.deviceplugin.adb.core;


public interface ConnectionListener {

    void onAdded(Connection connection);

    void onConnected(Connection connection);

    void onPortChanged(Connection connection);

    void onDisconnected(Connection connection);

    void onRemoved(Connection connection);

}

package org.deviceconnect.android.deviceplugin.adb.service;


import org.deviceconnect.android.deviceplugin.adb.core.Connection;
import org.deviceconnect.android.deviceplugin.adb.profiles.AdbProfile;
import org.deviceconnect.android.service.DConnectService;


public class AdbService extends DConnectService {

    private final Connection mConnection;

    public AdbService(final String id, final Connection conn) {
        super(id);
        mConnection = conn;

        addProfile(new AdbProfile());
    }

    public Connection getConnection() {
        return mConnection;
    }

    public String getIpAddress() {
        return mConnection.getIpAddress();
    }

    public int getPortNumber() {
        return mConnection.getPortNumber();
    }
}

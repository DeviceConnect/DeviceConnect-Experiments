package org.deviceconnect.android.deviceplugin.adb;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class AdbMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) AdbMessageService.class;
        return (Class<Service>) clazz;
    }
}
package org.deviceconnect.android.deviceplugin.adb;

import org.deviceconnect.android.deviceplugin.adb.core.AdbApplication;
import org.deviceconnect.android.deviceplugin.adb.core.Connection;
import org.deviceconnect.android.deviceplugin.adb.core.ConnectionListener;
import org.deviceconnect.android.deviceplugin.adb.core.ConnectionManager;
import org.deviceconnect.android.deviceplugin.adb.profiles.AdbSystemProfile;
import org.deviceconnect.android.deviceplugin.adb.service.AdbService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class AdbMessageService extends DConnectMessageService implements ConnectionListener, DConnectServiceListener {

    private static final long CONNECTION_RETRY_INTERVAL = 3000;

    private ConnectionManager mConnectionMgr;

    private final Logger mLogger = Logger.getLogger("adb-plugin");
    private final ScheduledExecutorService mConnectionExecutor = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture> mConnectionFutures = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        getServiceProvider().addServiceListener(this);

        mConnectionMgr = ((AdbApplication) getApplication()).getConnectionManager();
        mConnectionMgr.addListener(this);
        initServices();
    }

    @Override
    public void onDestroy() {
        getServiceProvider().removeServiceListener(this);
        super.onDestroy();
    }

    @Override
    protected void onDevicePluginReset() {
        mConnectionMgr.reset();
        getServiceProvider().removeAllServices();
        initServices();
    }

    private void initServices() {
        for (Connection conn : mConnectionMgr.getConnectionList()) {
            registerService(conn);
            if (!conn.isConnected()) {
                scheduleConnection(conn);
            }
        }
    }

    //---------- DConnectServiceListener ----------

    @Override
    public void onServiceAdded(final DConnectService service) {
        mLogger.info("AdbMessageService: onServiceAdded: " + service);
        // NOP.
    }

    @Override
    public void onServiceRemoved(final DConnectService service) {
        mLogger.info("AdbMessageService: onServiceRemoved: " + service);
        if (service instanceof AdbService) {
            AdbService adbService = (AdbService) service;
            String ipAddress = adbService.getIpAddress();
            int portNum = adbService.getPortNumber();

            mConnectionMgr.removeConnection(ipAddress, portNum);

            // 自動接続タイマー停止
            cancelSchedule(ipAddress);
        }
    }

    @Override
    public void onStatusChange(final DConnectService service) {
        mLogger.info("AdbMessageService: DConnectService: " + service);
        // NOP.
    }

    //---------- ConnectionListener ----------

    @Override
    public void onAdded(final Connection conn) {
        mLogger.info("AdbMessageService: onAdded: " + conn);
        registerService(conn);
        scheduleConnection(conn);
    }

    private static boolean hasSameAddress(final AdbService service, final String ipAddress) {
        return service.getId().startsWith(ipAddress);
    }

    @Override
    public void onConnected(final Connection conn) {
        mLogger.info("AdbMessageService: onConnected: " + conn);
        registerService(conn);
    }

    @Override
    public void onPortChanged(final Connection conn) {
        mLogger.info("AdbMessageService: onPortChanged: " + conn);
        registerService(conn);
        scheduleConnection(conn);
    }

    @Override
    public void onDisconnected(final Connection conn) {
        mLogger.info("AdbMessageService: onDisconnected: " + conn);
        DConnectService service = getServiceProvider().getService(conn.getIpAddress());
        if (service != null) {
            service.setOnline(false);
        }
    }

    @Override
    public void onRemoved(final Connection conn) {
        mLogger.info("AdbMessageService: onRemoved: " + conn);
        // NOP.
    }

    private void scheduleConnection(final Connection conn) {
        scheduleConnection(conn, 0);
    }

    private void scheduleConnection(final Connection conn, final long delay) {
        synchronized (mConnectionFutures) {
            cancelSchedule(conn.getIpAddress());

            final ScheduledFuture future = mConnectionExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        mLogger.info("Connecting to " + conn);
                        mConnectionMgr.connect(conn);
                    } catch (IOException e) {
                        // 接続に失敗したら、3秒後に再実行
                        scheduleConnection(conn, CONNECTION_RETRY_INTERVAL);
                        mLogger.info("Failed to connect to " + conn + ". Scheduled the retry after " + CONNECTION_RETRY_INTERVAL + " milliseconds.");
                    } catch (InterruptedException e) {
                        mLogger.info("Connection schedule is canceled.");
                        // NOP.
                    }
                }
            }, delay, TimeUnit.MILLISECONDS);
            mConnectionFutures.put(conn.getIpAddress(), future);
        }
    }

    private void cancelSchedule(final String key) {
        synchronized (mConnectionFutures) {
            ScheduledFuture cache = mConnectionFutures.get(key);
            if (cache != null) {
                cache.cancel(true);
            }
        }
    }

    private void registerService(final Connection conn) {
        String id = conn.getIpAddress();

        DConnectService service = getServiceProvider().getService(id);
        if (service == null) {
            service = new AdbService(id, conn);
            service.setNetworkType(NetworkType.UNKNOWN);
            getServiceProvider().addService(service);
        }
        service.setName(generateServiceName(conn));
        service.setOnline(conn.isConnected());
    }

    private static String generateServiceName(final Connection conn) {
        String ipAddress = conn.getIpAddress();
        int port = conn.getPortNumber();
        return generateServiceName(ipAddress, port);
    }

    private static String generateServiceName(final String ip, final int port) {
        return "ADB daemon (" + ip + ":" + port + ")";
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new AdbSystemProfile();
    }

}
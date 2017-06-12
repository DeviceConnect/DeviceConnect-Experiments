package org.deviceconnect.android.deviceplugin.adb.core;


import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;


public class ConnectionManager {

    private final Map<String, Connection> mConnections = new HashMap<>();
    private final List<ConnectionListener> mConnectionListeners = new ArrayList<>();
    private final ScheduledExecutorService mListenerExecutor;
    private final ConnectionDBHelper mDBHelper;
    private Logger mLogger;

    ConnectionManager(final Context context, final Logger logger) {
        mDBHelper = new ConnectionDBHelper(context);
        mListenerExecutor = Executors.newScheduledThreadPool(4);
        mLogger = logger;
        initConnections();
    }

    private void initConnections() {
        // データベースからADB接続情報を復元
        for (Connection stored : mDBHelper.getConnectionList()) {
            mConnections.put(createKey(stored), stored);
        }

        Connection local = null;
        for (Connection conn : getConnectionList()) {
            if (conn.isLocal()) {
                local = conn;
                break;
            }
        }
        if (local == null) {
            addConnection(Connection.LOOPBACK_ADDRESS, 5555); // デフォルトのADB接続
        }
    }

    void dispose() {
        mConnectionListeners.clear();
        mListenerExecutor.shutdown();
        disconnectAll();
    }

    public void reset() {
        disconnectAll();
        initConnections();
    }

    public List<Connection> getConnectionList() {
        List<Connection> list = new ArrayList<>();
        synchronized (mConnections) {
            for (Map.Entry<String, Connection> entry : mConnections.entrySet()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public void connect(final Connection conn) throws IOException, InterruptedException {
        connect(conn, Connection.DEFAULT_TIMEOUT);
    }

    public void connect(final Connection conn, final long timeout) throws IOException, InterruptedException {
        conn.connect(timeout);
        notifyOnConnected(conn);
    }

    public void disconnect(final Connection conn) throws IOException {
        conn.disconnect();
        notifyOnDisconnected(conn);
    }

    void disconnectAll() {
        synchronized (mConnections) {
            for (Map.Entry<String, Connection> entry : mConnections.entrySet()) {
                Connection conn = entry.getValue();
                try {
                    disconnect(conn);
                } catch (IOException e) {
                    // NOP
                }
            }
            mConnections.clear();
        }
    }

    public void changePort(final Connection conn, final int newPort) throws IOException {
        conn.disconnect();
        conn.setPortNumber(newPort);
        mDBHelper.updateConnection(conn);
        notifyOnPortChanged(conn);
    }

    public void addListener(final ConnectionListener listener) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                ConnectionListener l = it.next();
                if (l == listener) {
                    return;
                }
            }
            mConnectionListeners.add(listener);
        }
    }

    public void removeListener(final ConnectionListener listener) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                ConnectionListener l = it.next();
                if (l == listener) {
                    it.remove();
                }
            }
        }
    }

    private void notifyOnAdded(final Connection conn) {
        synchronized (mConnectionListeners) {
            for (final ConnectionListener l : mConnectionListeners) {
                mListenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onAdded(conn);
                    }
                });
            }
        }
    }

    private void notifyOnConnected(final Connection conn) {
        synchronized (mConnectionListeners) {
            for (final ConnectionListener l : mConnectionListeners) {
                mListenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onConnected(conn);
                    }
                });
            }
        }
    }

    private void notifyOnPortChanged(final Connection conn) {
        synchronized (mConnectionListeners) {
            for (final ConnectionListener l : mConnectionListeners) {
                mListenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onPortChanged(conn);
                    }
                });
            }
        }
    }

    private void notifyOnDisconnected(final Connection conn) {
        synchronized (mConnectionListeners) {
            for (final ConnectionListener l : mConnectionListeners) {
                mListenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onDisconnected(conn);
                    }
                });
            }
        }
    }

    private void notifyOnRemoved(final Connection conn) {
        synchronized (mConnectionListeners) {
            for (final ConnectionListener l : mConnectionListeners) {
                mListenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        l.onRemoved(conn);
                    }
                });
            }
        }
    }

    public boolean hasConnection(final String ipAddress) {
        return getConnection(ipAddress) != null;
    }

    public Connection getConnection(final String ipAddress) {
        for (Connection conn : getConnectionList()) {
            if (conn.hasIpAddress(ipAddress)) {
                return conn;
            }
        }
        return null;
    }

    public void addConnection(final String ipAddress, final int port) {
        if (hasConnection(ipAddress)) {
            throw new IllegalArgumentException(ipAddress + " is duplicated.");
        }
        Connection conn = new Connection(ipAddress, port);
        mConnections.put(createKey(conn), conn);
        mDBHelper.addConnection(conn);
        notifyOnAdded(conn);
    }

    public void removeConnection(final String ipAddress, final int port) {
        Connection conn = new Connection(ipAddress, port);
        mConnections.remove(createKey(conn));
        mDBHelper.removeConnection(conn);
        notifyOnRemoved(conn);
    }

    public void removeConnection(final Connection conn) {
        removeConnection(conn.getIpAddress(), conn.getPortNumber());
    }

    private static String createKey(final Connection conn) {
        return conn.getIpAddress();
    }
}

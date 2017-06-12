package org.deviceconnect.android.deviceplugin.adb.core;


import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Connection {

    private static final String LOCALHOST = "localhost";
    public static final String LOOPBACK_ADDRESS = "127.0.0.1";
    public static final long DEFAULT_TIMEOUT = 30 * 1000; //ミリ秒

    private final String mIpAddress;
    private int mPortNum;

    private AdbConnection mAdbConnection;
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Object mLock = new Object();
    private Command mPendingCommand;

    public Connection(final String ipAddress, final int portNum) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("ipAddress is null.");
        }
        if (portNum <= 0) {
            throw new IllegalArgumentException("portNum is illegal.");
        }
        mIpAddress = convertIpAddress(ipAddress);
        mPortNum = portNum;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public int getPortNumber() {
        return mPortNum;
    }

    public void setPortNumber(final int portNum) {
        mPortNum = portNum;
    }

    public boolean isConnected() {
        return mAdbConnection != null;
    }

    public boolean isLocal() {
        return LOOPBACK_ADDRESS.equals(mIpAddress);
    }

    public boolean hasIpAddress(final String ipAddress) {
        String ip = convertIpAddress(ipAddress);
        return mIpAddress.equals(ip);
    }

    private String convertIpAddress(final String ipAddress) {
        return LOCALHOST.equals(ipAddress) ? LOOPBACK_ADDRESS : ipAddress;
    }

    void connect(final long timeout) throws IOException, InterruptedException {
        if (mAdbConnection != null) {
            return;
        }
        mAdbConnection = doCommand(new Command<AdbConnection>() {
            @Override
            public AdbConnection execute() throws IOException, InterruptedException {
                try {
                    Socket socket = new Socket(mIpAddress, mPortNum);
                    AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                        @Override
                        public String encodeToString(byte[] data) {
                            return android.util.Base64.encodeToString(data, 16);
                        }
                    });
                    AdbConnection adbConn = AdbConnection.create(socket, crypto);
                    adbConn.connect();
                    return adbConn;
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        }, timeout);
    }

    void disconnect() throws IOException {
        synchronized (mLock) {
            if (mPendingCommand != null) {
                mPendingCommand.cancel();
                mPendingCommand = null;
            }
        }
        if (mAdbConnection != null) {
            mAdbConnection.close();
            mAdbConnection = null;
        }
    }

    public byte[] openAndRead(final String destination, final long timeout)
            throws IOException, InterruptedException {
        if (mAdbConnection == null) {
            throw new IOException("Not connected to " + toString());
        }

        return doCommand(new Command<byte[]>() {
            @Override
            public byte[] execute() throws IOException, InterruptedException {
                return mAdbConnection.openAndRead(destination);
            }
        }, timeout);
    }

    private <T> T doCommand(final Command<T> command, final long timeout)
            throws IOException, InterruptedException {
        synchronized (mLock) {
            if (mPendingCommand != null) {
                throw new IOException(mIpAddress + " is busy.");
            }
            mPendingCommand = command;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    command.mRunningThread = Thread.currentThread();
                    command.success(command.execute());
                } catch (InterruptedException e) {
                    // NOP.
                } catch (IllegalStateException e) {
                    command.error(e);
                } catch (IOException e) {
                    command.error(e);
                } catch (RuntimeException e) {
                    command.error(e);
                } finally {
                    mPendingCommand = null;
                    synchronized (command) {
                        command.notifyAll();
                    }
                }
            }
        });

        synchronized (command) {
            if (!command.isFinished()) {
                command.wait(timeout);
            }
            if (command.isFinished()) {
                if (command.isSuccess()) {
                    return command.getResult();
                } else {
                    command.throwException();
                    return null; // NOTE: Never return in this case.
                }
            } else {
                command.cancel();
                throw new ConnectionTimeoutException();
            }
        }
    }

    @Override
    public String toString() {
        return mIpAddress + ":" + mPortNum;
    }

    private abstract class Command<T> {

        final CommandResult<T> mCommandResult = new CommandResult<>();
        Thread mRunningThread;
        boolean misFinished;

        T getResult() {
            return mCommandResult.mResult;
        }

        boolean isFinished() {
            return misFinished;
        }

        boolean isSuccess() {
            return mCommandResult.isSuccess();
        }

        void success(final T result) {
            mCommandResult.success(result);
            mRunningThread = null;
            misFinished = true;
        }

        void error(final Exception error) {
            mCommandResult.error(error);
            mRunningThread = null;
            misFinished = true;
        }

        void cancel() {
            if (mRunningThread != null) {
                mRunningThread.interrupt();
            }
        }

        void throwException() throws IOException, IllegalStateException {
            mCommandResult.throwException();
        }

        public abstract T execute() throws IOException, InterruptedException;
    }

    private static class CommandResult<T> {
        T mResult;
        Exception mError;

        boolean isSuccess() {
            return mResult != null;
        }

        void success(final T result) {
            mResult = result;
        }

        void error(final Exception error) {
            mError = error;
        }

        void throwException() throws IOException, IllegalStateException {
            if (mError instanceof IOException) {
                throw ((IOException) mError);
            } else if (mError instanceof IllegalStateException) {
                throw ((IllegalStateException) mError);
            } else if (mError instanceof RuntimeException) {
                throw ((RuntimeException) mError);
            } else {
                throw new RuntimeException("Unexpected exception was set. Fix this bug.");
            }
        }
    }

}

package org.deviceconnect.android.deviceplugin.adb.command;

import org.deviceconnect.android.deviceplugin.adb.core.Connection;

import java.io.IOException;

/**
 * ADBコマンドの抽象クラス.
 */
public abstract class AdbCommand {

    public abstract String run(Connection conn, long timeout) throws IOException, InterruptedException;

}

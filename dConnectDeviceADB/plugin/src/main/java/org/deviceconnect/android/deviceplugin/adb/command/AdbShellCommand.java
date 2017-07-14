package org.deviceconnect.android.deviceplugin.adb.command;


import org.deviceconnect.android.deviceplugin.adb.core.Connection;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "shell", description = "")
public class AdbShellCommand extends AdbCommand {

    @Arguments
    private List<String> mShellCommand;

    @Override
    public String run(final Connection conn, final long timeout) throws IOException, InterruptedException {
        String command = getShellCommand();
        if (isInteractive()) {
            throw new UnsupportedOperationException("interactive mode of shell is not supported.");
        }
        return executeShell(conn, command, timeout);
    }

    private String executeShell(final Connection conn, final String command, final long timeout)
            throws IOException, InterruptedException {
        byte[] output = conn.openAndRead("shell:" + command, timeout);
        return new String(output, "UTF-8");
    }

    private boolean isInteractive() {
        return getShellCommand() == null;
    }

    String getShellCommand() {
        if (mShellCommand == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Iterator<String> it = mShellCommand.iterator(); it.hasNext(); ) {
            String arg = it.next();
            builder.append(arg);
            if (it.hasNext()) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}

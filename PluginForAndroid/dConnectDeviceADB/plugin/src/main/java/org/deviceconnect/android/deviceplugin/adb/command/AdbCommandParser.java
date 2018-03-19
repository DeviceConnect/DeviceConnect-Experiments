package org.deviceconnect.android.deviceplugin.adb.command;


import io.airlift.airline.Cli;

public class AdbCommandParser {

    private Cli<AdbCommand> mParser;

    public AdbCommandParser() {
        Cli.CliBuilder<AdbCommand> builder = Cli.<AdbCommand>builder("adb")
                .withCommands(AdbShellCommand.class);
        mParser = builder.build();
    }

    public AdbCommand parse(final String command) {
        String[] args = command.split(" ");
        return mParser.parse(args);
    }
}

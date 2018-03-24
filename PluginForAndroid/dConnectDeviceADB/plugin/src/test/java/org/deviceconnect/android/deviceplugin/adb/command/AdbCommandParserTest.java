package org.deviceconnect.android.deviceplugin.adb.command;


import junit.framework.Assert;

import org.junit.Test;

public class AdbCommandParserTest {

    @Test
    public void testParseNormal001() throws Exception {
        final String shellCommand = "input touchscreen tap 100 200";

        AdbCommandParser parser = new AdbCommandParser();
        AdbCommand command = parser.parse("shell " + shellCommand);
        Assert.assertTrue(command instanceof AdbShellCommand);
        Assert.assertEquals(shellCommand, ((AdbShellCommand) command).getShellCommand());
    }

    @Test
    public void testParseAbnormal001() throws Exception {
        AdbCommandParser parser = new AdbCommandParser();
        AdbCommand command = parser.parse("shell");
        Assert.assertTrue(command instanceof AdbShellCommand);
        Assert.assertEquals(null, ((AdbShellCommand) command).getShellCommand());
    }
}

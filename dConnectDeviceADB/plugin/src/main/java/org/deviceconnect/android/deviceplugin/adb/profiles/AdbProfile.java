package org.deviceconnect.android.deviceplugin.adb.profiles;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.adb.command.AdbCommandParser;
import org.deviceconnect.android.deviceplugin.adb.core.Connection;
import org.deviceconnect.android.deviceplugin.adb.core.ConnectionTimeoutException;
import org.deviceconnect.android.deviceplugin.adb.service.AdbService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;

import io.airlift.airline.ParseCommandUnrecognizedException;

public class AdbProfile extends DConnectProfile {

    private final AdbCommandParser mAdbParser = new AdbCommandParser();

    public AdbProfile() {

        // POST /adb
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String adbCommand = (String) request.getExtras().get("command");
                if (adbCommand.startsWith("adb ")) {
                    adbCommand = adbCommand.substring("adb ".length());
                }

                try {
                    Connection conn = ((AdbService) getService()).getConnection();
                    String output = mAdbParser.parse(adbCommand).run(conn, Connection.DEFAULT_TIMEOUT);

                    response.putExtra("output", output);
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (ParseCommandUnrecognizedException e) {
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                } catch (UnsupportedOperationException e) {
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                } catch (ConnectionTimeoutException e) {
                    MessageUtils.setIllegalDeviceStateError(response, "Connection timeout.");
                } catch (IOException e) {
                    MessageUtils.setIllegalDeviceStateError(response, e.getMessage());
                } catch (InterruptedException e) {
                    MessageUtils.setIllegalServerStateError(response, "Command was canceled.");
                }
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "adb";
    }
}
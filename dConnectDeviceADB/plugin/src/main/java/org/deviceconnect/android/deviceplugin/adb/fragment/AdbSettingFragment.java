package org.deviceconnect.android.deviceplugin.adb.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.deviceconnect.android.deviceplugin.adb.R;
import org.deviceconnect.android.deviceplugin.adb.core.AdbApplication;
import org.deviceconnect.android.deviceplugin.adb.core.Connection;
import org.deviceconnect.android.deviceplugin.adb.core.ConnectionManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Logger;


public class AdbSettingFragment extends Fragment {

    public enum Mode {
        ADD,
        EDIT
    }

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_IP_ADDRESS = "ip_address";
    public static final String EXTRA_PORT = "port";

    private EditText mIpAddressEditor;
    private EditText mPortNumEditor;

    private ConnectionManager mConnectionMgr;
    private boolean mIsEnabledPromptDialog = true;

    private final Logger mLogger = Logger.getLogger("adb-plugin");

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, null);

        mConnectionMgr = getConnectionManager();

        Bundle args = getArguments();
        Mode mode = (Mode) args.getSerializable(EXTRA_MODE);
        if (mode == null) {
            mode = Mode.ADD;
        }
        String ipAddressParam = args.getString(EXTRA_IP_ADDRESS);
        String portParam = args.getString(EXTRA_PORT);

        mIpAddressEditor = (EditText) root.findViewById(R.id.edit_text_ip_address);
        if (mode != Mode.ADD) {
            mIpAddressEditor.setEnabled(false);
        }
        if (ipAddressParam != null) {
            mIpAddressEditor.setText(ipAddressParam);
        }

        mPortNumEditor = (EditText) root.findViewById(R.id.edit_text_port_number);
        if (portParam != null) {
            mPortNumEditor.setText(portParam);
        }

        Button connectionAddButton = (Button) root.findViewById(R.id.button_connection_add);
        connectionAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String ip = getIpAddressParam();
                final int portNum = getPortNumberParam();

                Connection cache = mConnectionMgr.getConnection(ip);
                if (cache != null) {
                    showAlreadyConnectedDialog(ip);
                } else {
                    mConnectionMgr.addConnection(ip, portNum);
                    if (showsPrompt()) {
                        prompt(Mode.ADD, portNum);
                    } else {
                        finish();
                    }
                }
            }
        });
        connectionAddButton.setVisibility(mode == Mode.ADD ? View.VISIBLE : View.GONE);

        Button connectionChangeButton = (Button) root.findViewById(R.id.button_connection_change);
        connectionChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String ip = getIpAddressParam();
                final int portNum = getPortNumberParam();

                Connection cache = mConnectionMgr.getConnection(ip);
                boolean isChanged = false;
                if (cache != null && cache.getPortNumber() != portNum) {
                    try {
                        mConnectionMgr.changePort(cache, portNum);
                        isChanged = true;
                    } catch (IOException e) {
                        // NOP.
                    }
                }

                if (isChanged && showsPrompt()) {
                    prompt(Mode.EDIT, portNum);
                } else {
                    finish();
                }
            }
        });
        connectionChangeButton.setVisibility(mode == Mode.EDIT ? View.VISIBLE : View.GONE);
        return root;
    }

    private String getIpAddressParam() {
        String param = mIpAddressEditor.getText().toString();
        final String ip;
        mLogger.info("Setting: IP address (inputted): " + param);
        if (isLocalAddress(param)) {
            mLogger.info("Setting: " + param + " is local IP address.");
            // ホストデバイスを指すアドレスはすべて下記に統一する.
            ip = Connection.LOOPBACK_ADDRESS;
            mLogger.info("Setting: IP address (converted): " + param);
        } else {
            ip = param;
        }
        return ip;
    }

    private int getPortNumberParam() {
        return Integer.parseInt(mPortNumEditor.getText().toString());
    }

    private boolean showsPrompt() {
        return mIsEnabledPromptDialog;
    }

    private void prompt(final Mode mode, final int port) {
        ConfirmDialogFragment dialog = createPromptDialog(mode, port);
        dialog.setCallback(new ConfirmDialogCallback() {
            @Override
            public void onDialogConfirmed() {
                finish();
            }
        });
        dialog.show(getFragmentManager(), "dialog");
    }

    /**
     * 指定されたIPv4アドレスがホストデバイスのアドレスであるかどうかをチェックする.
     *
     * @return ホストデバイスのアドレスである場合はtrue. そうでない場合はfalse.
     */
    private boolean isLocalAddress(final String ipv4Address) {
        if (Connection.LOOPBACK_ADDRESS.equals(ipv4Address) ||  "0.0.0.0".equals(ipv4Address)) {
            return true;
        }
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface networkInterface = e.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String host = address.getHostAddress();
                    if (ipv4Address.equals(host)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            // NOP.
        }
        return false;
    }

    private ConnectionManager getConnectionManager() {
        Activity activity = getActivity();
        if (activity != null) {
            return ((AdbApplication) activity.getApplication()).getConnectionManager();
        }
        return null;
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void showAlreadyConnectedDialog(final String ipAddress) {
        String message = getString(R.string.setting_error_dialog_message_already_connected);
        message = message.replace("{{target}}", ipAddress);

        ErrorDialogFragment dialog = createErrorDialog(message);
        dialog.show(getFragmentManager(), "dialog");
    }

    private ErrorDialogFragment createErrorDialog(final String message) {
        Bundle args = new Bundle();
        args.putString(AlertDialogFragment.TITLE, getString(R.string.setting_error_dialog_title));
        args.putString(AlertDialogFragment.MESSAGE, message);

        ErrorDialogFragment dialog = new ErrorDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    private ConfirmDialogFragment createPromptDialog(final Mode mode, final int port) {
        String message = getString(R.string.setting_prompt_dialog_message_for_new_service);
        message = message.replace("{{operation}}", mode == Mode.ADD ? getString(R.string.setting_button_connection_add) : getString(R.string.setting_button_connection_change));
        message = message.replace("{{port}}", Integer.toString(port));

        Bundle args = new Bundle();
        args.putString(AlertDialogFragment.TITLE, getString(R.string.setting_prompt_dialog_title));
        args.putString(AlertDialogFragment.MESSAGE, message);

        ConfirmDialogFragment dialog = new ConfirmDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }
}

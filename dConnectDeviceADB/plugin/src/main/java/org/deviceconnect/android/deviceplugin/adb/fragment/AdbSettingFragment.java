package org.deviceconnect.android.deviceplugin.adb.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.adb.R;
import org.deviceconnect.android.deviceplugin.adb.core.AdbApplication;
import org.deviceconnect.android.deviceplugin.adb.core.Connection;
import org.deviceconnect.android.deviceplugin.adb.core.ConnectionListener;
import org.deviceconnect.android.deviceplugin.adb.core.ConnectionManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AdbSettingFragment extends Fragment implements ConnectionListener {

    public enum Mode {
        ADD,
        EDIT
    }

    private enum Ipv4AddressError {
        EMPTY(R.string.setting_error_empty),
        INVALID_FORMAT(R.string.setting_error_ip_address_invalid_format),
        NOT_ALLOWED_ZERO_PADDING(R.string.setting_error_not_allowed_zero_padding),
        ALREADY_ADDED(R.string.setting_error_already_connected);

        private int mMessageId;

        Ipv4AddressError(final int messageId) {
            mMessageId = messageId;
        }

        public String getMessage(final Context context) {
            return context.getString(mMessageId);
        }
    }

    private enum PortNumberError {
        EMPTY(R.string.setting_error_empty),
        OUT_OF_RANGE(R.string.setting_error_port_number_out_of_range);
        // INVALID_FORMAT //NOTE: 数字であることはUIコンポーネント側の設定で保証する

        private int mMessageId;

        PortNumberError(final int messageId) {
            mMessageId = messageId;
        }

        public String getMessage(final Context context) {
            return context.getString(mMessageId);
        }
    }

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_IP_ADDRESS = "ip_address";
    public static final String EXTRA_PORT = "port";

    private static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$");
    private static final String LOCALHOST = "localhost";

    private Mode mMode;
    private EditText mIpAddressEditor;
    private EditText mPortNumEditor;
    private Button mConnectionAddButton;
    private Button mConnectionChangeButton;

    private ConnectionManager mConnectionMgr;
    private Handler mHandler;

    private final Logger mLogger = Logger.getLogger("adb-plugin");

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, null);
        mHandler = new Handler(Looper.getMainLooper());

        mConnectionMgr = getConnectionManager();
        mConnectionMgr.addListener(this);

        Bundle args = getArguments();
        mMode = (Mode) args.getSerializable(EXTRA_MODE);
        if (mMode == null) {
            mMode = Mode.ADD;
        }

        mIpAddressEditor = (EditText) root.findViewById(R.id.edit_text_ip_address);
        if (mMode != Mode.ADD) {
            mIpAddressEditor.setEnabled(false);
        }

        mPortNumEditor = (EditText) root.findViewById(R.id.edit_text_port_number);

        mConnectionAddButton = (Button) root.findViewById(R.id.button_connection_add);
        mConnectionAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String ip = getIpAddressParam();
                final int portNum = getPortNumberParam();
                mConnectionMgr.addConnection(ip, portNum);
                prompt(Mode.ADD, ip, portNum);
            }
        });
        mConnectionAddButton.setVisibility(mMode == Mode.ADD ? View.VISIBLE : View.GONE);

        mConnectionChangeButton = (Button) root.findViewById(R.id.button_connection_change);
        mConnectionChangeButton.setOnClickListener(new View.OnClickListener() {
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

                if (isChanged) {
                    prompt(Mode.EDIT, ip, portNum);
                } else {
                    finish();
                }
            }
        });
        mConnectionChangeButton.setVisibility(mMode == Mode.EDIT ? View.VISIBLE : View.GONE);

        // 指定された設定を表示
        String ipAddressParam = args.getString(EXTRA_IP_ADDRESS);
        String portParam = args.getString(EXTRA_PORT);
        if (ipAddressParam != null) {
            mIpAddressEditor.setText(ipAddressParam);
        }
        if (portParam != null) {
            mPortNumEditor.setText(portParam);
        }

        // 設定変更リスナーの初期化
        mIpAddressEditor.addTextChangedListener(new DefaultTextWatcher() {
            @Override
            public void onTextChanged(final CharSequence text, final int start, final int before, final int count) {
                showInputError();
            }
        });
        mPortNumEditor.addTextChangedListener(new DefaultTextWatcher() {
            @Override
            public void onTextChanged(final CharSequence text, final int start, final int before, final int count) {
                showInputError();
            }
        });

        // エラー表示初期化
        showInputError();

        return root;
    }

    private void showInputError() {
        boolean hasError = showIpv4AddressError(getIpAddressParam()) ||
                           showPortNumberError(mPortNumEditor.getText().toString());
        enableButton(!hasError);
    }

    private void enableButton(final boolean isEnabled) {
        mConnectionAddButton.setEnabled(isEnabled);
        mConnectionChangeButton.setEnabled(isEnabled);
    }

    private boolean showIpv4AddressError(final CharSequence input) {
        Ipv4AddressError error = checkIpv4Address(input);
        if (error != null) {
            mIpAddressEditor.setError(error.getMessage(getContext()));
            return true;
        }
        return false;
    }

    private boolean showPortNumberError(final CharSequence input) {
        PortNumberError error = checkPortNumber(input);
        if (error != null) {
            mPortNumEditor.setError(error.getMessage(getContext()));
            return true;
        }
        return false;
    }

    private Ipv4AddressError checkIpv4Address(final CharSequence input) {
        // 未指定でないこと
        if (TextUtils.isEmpty(input)) {
            return Ipv4AddressError.EMPTY;
        }
        String str = input.toString();

        // 4つの要素で分割されること
        Matcher matcher = IPV4_ADDRESS_PATTERN.matcher(input);
        if (!matcher.matches()) {
            return Ipv4AddressError.INVALID_FORMAT;
        }
        try {
            String[] parts = str.split("\\.");

            // 各要素が 0 以上 255 以下であること
            for (String p : parts) {
                int part = Integer.parseInt(p);
                if (part < 0 || part > 255) {
                    return Ipv4AddressError.INVALID_FORMAT;
                }
            }

            // ゼロパディングされていないこと (曖昧さの回避のため)
            for (String p : parts) {
                if (p.length() > 1 && p.startsWith("0")) {
                    return Ipv4AddressError.NOT_ALLOWED_ZERO_PADDING;
                }
            }
        } catch (NumberFormatException e) {
            return Ipv4AddressError.INVALID_FORMAT;
        }

        // 新規追加の場合は、IPv4アドレスが未登録のものであること
        if (mMode == Mode.ADD) {
            Connection cache = mConnectionMgr.getConnection(str);
            if (cache != null) {
                return Ipv4AddressError.ALREADY_ADDED;
            }
        }

        return null;
    }

    private PortNumberError checkPortNumber(final CharSequence input) {
        // 未指定でないこと
        if (TextUtils.isEmpty(input)) {
            return PortNumberError.EMPTY;
        }

        // 1024 以上 65535 以下であること
        try {
            int portNum = Integer.parseInt(input.toString());
            if (portNum < 1024 || portNum > 65535) {
                return PortNumberError.OUT_OF_RANGE;
            }
        } catch (NumberFormatException e) {
            // NOTE: Integer.MAX_VALUE を超える値が指定された場合に発生
            return PortNumberError.OUT_OF_RANGE;
        }

        return null;
    }

    @Override
    public void onDestroyView() {
        mConnectionMgr.removeListener(this);
        super.onDestroyView();
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

    private void prompt(final Mode mode, final String ipAddress, final int port) {
        ConfirmDialogFragment dialog = createPromptDialog(mode, ipAddress, port);
        dialog.setCallback(new ConfirmDialogCallback() {
            @Override
            public void onDialogConfirmed() {
                finish();
            }
        });
        dialog.show(getFragmentManager(), "prompt");
    }

    /**
     * 指定されたIPv4アドレスがホストデバイスのアドレスであるかどうかをチェックする.
     *
     * @return ホストデバイスのアドレスである場合はtrue. そうでない場合はfalse.
     */
    private boolean isLocalAddress(final String ipv4Address) {
        if (LOCALHOST.equals(ipv4Address) ||
            Connection.LOOPBACK_ADDRESS.equals(ipv4Address) ||
            "0.0.0.0".equals(ipv4Address)) {
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

    private ErrorDialogFragment createErrorDialog(final String message) {
        Bundle args = new Bundle();
        args.putString(AlertDialogFragment.TITLE, getString(R.string.setting_error_dialog_title));
        args.putString(AlertDialogFragment.MESSAGE, message);

        ErrorDialogFragment dialog = new ErrorDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    private ConfirmDialogFragment createPromptDialog(final Mode mode, final String ipAddress, final int port) {
        String message = getString(R.string.setting_prompt_dialog_message_for_new_service);
        message = message.replace("{{operation}}", mode == Mode.ADD ? getString(R.string.setting_button_connection_add) : getString(R.string.setting_button_connection_change));
        message = message.replace("{{port}}", Integer.toString(port));

        Bundle args = new Bundle();
        args.putString(AlertDialogFragment.TITLE, getString(R.string.setting_prompt_dialog_title));
        args.putString(AlertDialogFragment.MESSAGE, message);
        args.putString(ConfirmDialogFragment.IP_ADDRESS, ipAddress);

        ConfirmDialogFragment dialog = new ConfirmDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAdded(final Connection connection) {
        // NOP.
    }

    @Override
    public void onConnected(final Connection connection) {
        mLogger.info("AdbSettingFragment: onConnected: IP = " + connection.getIpAddress());
        showConnectionSuccess(connection);
        finishIfConnected(connection);
    }

    private void showConnectionSuccess(final Connection connection) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String message = getString(R.string.setting_message_adb_connection_success);
                message = message.replace("{{target}}", connection.getIpAddress() + ":" + connection.getPortNumber());
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishIfConnected(final Connection connection) {
        Fragment f = getFragmentManager().findFragmentByTag("prompt");
        mLogger.info("AdbSettingFragment: prompt fragment = " + f);
        if (f != null && f instanceof ConfirmDialogFragment) {
            ConfirmDialogFragment prompt = (ConfirmDialogFragment) f;
            String waitingIp = prompt.getIpAddress();
            if (connection.hasIpAddress(waitingIp)) {
                finish();
            }
        }
    }

    @Override
    public void onPortChanged(final Connection connection) {
        // NOP.
    }

    @Override
    public void onDisconnected(final Connection connection) {
        // NOP.
    }

    @Override
    public void onRemoved(final Connection connection) {
        // NOP.
    }

    private abstract class DefaultTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(final CharSequence text, final int start, final int count, final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence text, final int start, final int before, final int count) {
        }

        @Override
        public void afterTextChanged(final Editable editor) {
        }
    }
}

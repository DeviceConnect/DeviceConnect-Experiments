package org.deviceconnect.android.deviceplugin.adb.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.adb.R;

public class ErrorDialogFragment extends AlertDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(TITLE);
        String message = args.getString(MESSAGE);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.setting_button_ok, null)
                .create();
    }
}
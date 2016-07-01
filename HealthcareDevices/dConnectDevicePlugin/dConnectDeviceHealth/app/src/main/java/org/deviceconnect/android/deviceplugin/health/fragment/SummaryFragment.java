/*
 SummaryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.deviceconnect.android.deviceplugin.health.HealthCareApplication;
import org.deviceconnect.android.deviceplugin.health.R;
import org.deviceconnect.android.deviceplugin.health.activity.HealthCareDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.health.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.health.fragment.dialog.ErrorDialogFragment;

/**
 * This fragment explain summary of this device plug-in.
 * @author NTT DOCOMO, INC.
 */
public class SummaryFragment extends Fragment {
    /**
     * Error Dialog.
     */
    private ErrorDialogFragment mErrorDialogFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, null);
        if (!BleUtils.isBLESupported(getActivity())) {
            showErrorDialog();
        }
        SharedPreferences sp = getActivity().getSharedPreferences("common", Context.MODE_PRIVATE);
        boolean ecomode = sp.getBoolean("ecomode", false);
        CheckBox box = (CheckBox)rootView.findViewById(R.id.checkBox);
        box.setChecked(ecomode);

        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences sp = getActivity().getSharedPreferences("common", Context.MODE_PRIVATE);
                sp.edit().putBoolean("ecomode", b).commit();
                HealthCareApplication app = (HealthCareApplication)getActivity().getApplication();
                app.getHealthCareManager().autoConnectionRestart();
            }
        });
        return rootView;
    }

    /**
     * Display the error dialog.
     */
    private void showErrorDialog() {
        dismissErrorDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.summary_not_support_title);
        String message = res.getString(R.string.summary_not_support_message);
        mErrorDialogFragment = ErrorDialogFragment.newInstance(title, message);
        mErrorDialogFragment.show(getFragmentManager(), "error_dialog");
        mErrorDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mErrorDialogFragment = null;
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    /**
     * Dismiss the error dialog.
     */
    private void dismissErrorDialog() {
        if (mErrorDialogFragment != null) {
            mErrorDialogFragment.dismiss();
            mErrorDialogFragment = null;
        }
    }
}

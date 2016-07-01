/*
 ThermometerData
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.subprofile;

import android.content.Intent;

import java.text.SimpleDateFormat;

// unused import statement
//import org.apache.james.mime4j.field.datetime.DateTime;

/**
 * This class is information of a Blood Pressure.
 * @author NTT DOCOMO, INC.
 */
public class BloodPressureData {
    // 最高血圧
    private float mSystricPressure;
    // 最低血圧
    private float mDiastolicPressure;
    // 平均血圧
    private float mMeanarterial;
    // 単位
    private Integer mPressureUnit;
    // 計測日時
    private long mTimeStamp;

    private static final String SYSTOLIC_PRESSURE = "systolicpressure";
    private static final String DIASTOLIC_PRESSURE = "diastolicpressure";
    private static final String MEANARTERIAL = "meanarterial";
    private static final String UNIT = "pressureunit";
    private static final String TIME_STAMP = "timestamp";

    public void setDeviceInfo(Intent response) {
        response.putExtra(SYSTOLIC_PRESSURE, getSystericPressure());
        response.putExtra(DIASTOLIC_PRESSURE, getDiastolicPressure());
        response.putExtra(MEANARTERIAL, getMeanarterial());
        response.putExtra(UNIT, getUnit());
        response.putExtra(TIME_STAMP, getTimeStampString());
    }

    // Blood Pressure
    public float getSystericPressure() { return mSystricPressure; }
    public float getDiastolicPressure() { return mDiastolicPressure; }
    public float getMeanarterial() { return mMeanarterial; }

    public void setSystericPressure(float sysPressure) {
        mSystricPressure = sysPressure;
    }

    public void setDiastolicPressure(float diaPressure) {
        mDiastolicPressure = diaPressure;
    }

    public void setMeanarterial(float meanPressure) {
        mMeanarterial = meanPressure;
    }

    // Unit
    public Integer getUnit() {
        return mPressureUnit;
    }
    public void setUnit(boolean isHg) {
        if (isHg) {
            mPressureUnit = 0;    // mmHg
        } else {
            mPressureUnit = 1;   // kPa
        }
    }

    // Timestamp
    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }
    public long getTimeStamp() {
        return mTimeStamp;
    }
    public String getTimeStampString() {
        if (mTimeStamp <= 0) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        return formatter.format(mTimeStamp);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{" + SYSTOLIC_PRESSURE + " : " + mSystricPressure +  "} ");
        builder.append("{" + DIASTOLIC_PRESSURE + " : " + mDiastolicPressure +  "} ");
        builder.append("{" + MEANARTERIAL + " : " + mMeanarterial +  "} ");
        builder.append("{" + UNIT + " : " + mPressureUnit +  "} ");
        builder.append("{" + TIME_STAMP + " : " + mTimeStamp +  "} ");
        return builder.toString();
    }
}

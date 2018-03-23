/*
 HeartRateData
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.subprofile;

import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is information of a Heart Rate.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateData {
    private int mId;
    private int mHeartRate;
    private int mEnergyExpended;
    private double mRRInterval;
    private DeviceState mLocation;
    private int mSensorLocation;
    // 見守りアプリ用
    private long mTimeStamp;
    private Integer mUnit;

    private static final String UNIT_APP = "rateunit";         // 地域見守り：UNIT のパラメータ名変更（Integer／0＝C　1＝F）
    private static final String TIME_STAMP_APP = "timestamp";   // 地域見守り：TIME_STAMP のパラメータ名変更

    public int getId() {
        return mId;
    }

    public void setId(final int id) {
        mId = id;
    }

    public int getHeartRate() {
        return mHeartRate;
    }

    public void setHeartRate(final int heartRate) {
        mHeartRate = heartRate;
    }

    public int getEnergyExpended() {
        return mEnergyExpended;
    }

    public void setEnergyExpended(final int energyExpended) {
        mEnergyExpended = energyExpended;
    }

    public double getRRInterval() {
        return mRRInterval;
    }

    public void setRRInterval(final double rrInterval) {
        mRRInterval = rrInterval;
    }

    public DeviceState getLocation() {
        return mLocation;
    }

    public void setLocation(final DeviceState location) {
        mLocation = location;
    }

    public void setSensorLocation(final int location) {
        mSensorLocation = location;
    }
    public int getSensorLocation() {
        return mSensorLocation;
    }

    // 見守りアプリ用
    public Integer getUnit() {
        return mUnit;
    }
    public void setUnit(Integer unitValue) {
        mUnit = unitValue;
    }
    public void setUnit(Intent response, Integer unitValue) {
        response.putExtra(UNIT_APP, unitValue);
    }

    // 見守りアプリ用
    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }
    public void setTimeStampString(Intent response) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        response.putExtra(TIME_STAMP_APP, formatter.format(new Date()));
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


    public enum DeviceState {
        GET_LOCATION,
        REGISTER_NOTIFY,
        CONNECTED,
        DISCONNECT,
        ERROR,
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"id\": " + mId + ", ");
        builder.append("\"heartRate\": " + mHeartRate + ", ");
        builder.append("\"energyExpended\": " + mEnergyExpended + ", ");
        builder.append("\"RRInterval\": " + mRRInterval +  "} ");
        return builder.toString();
    }
}

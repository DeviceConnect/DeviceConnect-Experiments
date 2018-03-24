/*
 ThermometerData
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.subprofile;

import android.content.Intent;

// unused import statement
//import org.apache.james.mime4j.field.datetime.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is information of a Thermometer.
 * @author NTT DOCOMO, INC.
 */
public class ThermometerData {
    private float mTemperature;
    private String mTemperatureMderFloat;
    private String mTypeCode;
    private String mType;
    private String mUnitCode;
    private String mUnit;
    private long mTimeStamp;
    // 見守りアプリ用
    private boolean mIsUnitC;

    private static final String TEMPERATURE = "temperature";    // 地域見守り：TEMPERATURE_MDERFLOAT から値を変換して格納する
    private static final String TEMPERATURE_MDERFLOAT = "temperatureMderFloat";
    private static final String TYPE = "type";
    private static final String TYPE_CODE = "typeCode";
    private static final String UNIT = "unit";
    private static final String UNIT_APP = "tempunit";         // 地域見守り：UNIT のパラメータ名変更（Integer／0＝C　1＝F）
    private static final String UNIT_CODE = "unitCode";
    private static final String TIME_STAMP = "timeStamp";
    private static final String TIME_STAMP_APP = "timestamp";   // 地域見守り：TIME_STAMP のパラメータ名変更
    private static final String TIME_STAMP_STRING = "timeStampString";

    public void setDeviceInfo(Intent response) {
        response.putExtra(TEMPERATURE, getTemperature());
        response.putExtra(TEMPERATURE_MDERFLOAT, getTemperatureMderfloat());
        response.putExtra(TYPE, getType());
        response.putExtra(TYPE_CODE, getTypeCode());
        response.putExtra(UNIT, getUnit());
        response.putExtra(UNIT_CODE, getUnitCode());
        response.putExtra(TIME_STAMP, getTimeStamp());
        response.putExtra(TIME_STAMP_STRING, getTimeStampString());
    }

    public void setAppDeviceInfo(Intent response) { // 見守りアプリ用
        response.putExtra(TEMPERATURE, getTemperature());
        response.putExtra(UNIT_APP, getUnitApp());
        response.putExtra(TIME_STAMP_APP, getTimeStampApp());
    }

    public float getTemperature() {
        return mTemperature;
    }
    public String getTemperatureMderfloat() {
        return mTemperatureMderFloat;
    }
    public void setTemperature(final float temperature) {
        mTemperature = temperature;
        mTemperatureMderFloat = "FF" + String.format("%06X", (int)(temperature*10));
    }

    public String getType() {
        return mType;
    }
    public String getTypeCode() {
        return mTypeCode;
    }
    public void setTypeCode(String typeCode) {
        if (typeCode.isEmpty()) {
            mTypeCode = "";
            mType = "";
            return;
        }
        mTypeCode = typeCode;
        setType(typeCode);
    }
    private void setType(String typeCode) {
    }

    public String getUnit() {
        return mUnit;
    }
    public String getUnitCode() {
        return mUnitCode;
    }
    public void setUnit(boolean isCelsius) {
        if (isCelsius) {
            mUnit = "deg C";
            mIsUnitC = true;    // 見守りアプリ用
            mUnitCode = "268192";
        } else {
            mUnit = "deg F";
            mIsUnitC = false;   // 見守りアプリ用
            mUnitCode = "268193";
        }
    }
    // 見守りアプリ用
    public Integer getUnitApp() {
        if (mIsUnitC) {
            return 0;   // Celsius
        } else {
            return 1;   // Fahrenheit
        }
    }

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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        return formatter.format(mTimeStamp);
    }
    // 見守りアプリ用
    public String getTimeStampApp() {
        if (mTimeStamp <= 0) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        return formatter.format(mTimeStamp);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\"temperature\": " + mTemperature +  "} ");
        builder.append("\"type\": " + mType +  "} ");
        builder.append("\"timestamp\": " + mTimeStamp +  "} ");
        return builder.toString();
    }
}

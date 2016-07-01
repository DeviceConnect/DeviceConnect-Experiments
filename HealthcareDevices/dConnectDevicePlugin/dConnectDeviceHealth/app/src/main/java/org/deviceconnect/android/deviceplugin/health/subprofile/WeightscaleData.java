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
 * This class is information of a Weight Scale.
 * @author NTT DOCOMO, INC.
 */
public class WeightscaleData {
    private float mWeight;
    private Integer mUnit;
    private long mTimeStamp;

    private static final String WEIGHT = "weight";
    private static final String UNIT = "weightunit";     // 0 = Kg / 1 = ib
    private static final String TIME_STAMP = "timestamp";

    public void setDeviceInfo(Intent response) {
        response.putExtra(WEIGHT, getWeight());
        response.putExtra(UNIT, getUnit());
        response.putExtra(TIME_STAMP, getTimeStampString());
    }

    public float getWeight() {
        return mWeight;
    }
    public void setWeight(final float weight) {
        mWeight = weight;
    }

    public Integer getUnit() {
        return mUnit;
    }
    public void setUnit(boolean isKg) {
        if (isKg) {
            mUnit = 0;
        } else {
            mUnit = 1;
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        return formatter.format(mTimeStamp);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(WEIGHT + " : " + mWeight +  "} ");
        builder.append(UNIT + " : " + mUnit +  "} ");
        builder.append(TIME_STAMP + " : " + mTimeStamp +  "} ");
        return builder.toString();
    }
}

/*
 HealthCareDBHelper
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.health.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manage a database.
 * @author NTT DOCOMO, INC.
 */
public class HealthCareDBHelper {

    /**
     * Define the name of the database.
     */
    private static final String DB_NAME = "health_care.db";

    /**
     * Define the version of the database.
     */
    private static final int DB_VERSION = 1;
    private static final String TBL_NAME = "device_tbl";

    private static final String COL_NAME = "name";
    private static final String COL_ADDRESS = "address";
    private static final String COL_REGISTER_FLAG = "register_flag";
    private static final String COL_PROFILE_TYPE = "device_type";

    private static final String COL_DEVICE_MANUFACTURE_NAME = "manufacture_name";
    private static final String COL_DEVICE_MODEL_NUMBER = "model_number";
    private static final String COL_DEVICE_FIRMWARE_REVISION = "firmware_revision";
    private static final String COL_DEVICE_SERIAL_NUMBER = "serial_number";
    private static final String COL_DEVICE_SOFTWARE_REVISION = "software_revision";
    private static final String COL_DEVICE_HARDWARE_REVISION = "hardware_revision";
    private static final String COL_DEVICE_PART_NUMBER = "part_number";
    private static final String COL_DEVICE_PROTOCOL_REVISION = "protocol_revision";
    private static final String COL_DEVICE_SYSTEM_ID = "system_id";
    private static final String COL_DEVICE_BATTERY_LEVEL = "battery_level";
    private static final String COL_INFORMATION_REGISTERED = "information_registered";

    private DBHelper mDBHelper;

    /**
     * Constructor.
     * @param context application context
     */
    public HealthCareDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * Add the device to database.
     * @param device device
     * @return the row ID of the newly added row, or -1 if an error occurred
     */
    public synchronized long addHealthCareDevice(final HealthCareDevice device) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, device.getName());
        values.put(COL_ADDRESS, device.getAddress());
        values.put(COL_REGISTER_FLAG, device.isRegisterFlag() ? 1 : 0);
        values.put(COL_PROFILE_TYPE, device.getProfileType());
        values.put(COL_DEVICE_MANUFACTURE_NAME, device.getDeviceManufacturerName());
        values.put(COL_DEVICE_MODEL_NUMBER, device.getDeviceModelNumber());
        values.put(COL_DEVICE_FIRMWARE_REVISION, device.getDeviceFirmwareRevision());
        values.put(COL_DEVICE_SERIAL_NUMBER, device.getDeviceSerialNumber());
        values.put(COL_DEVICE_SOFTWARE_REVISION, device.getDeviceSoftwareRevision());
        values.put(COL_DEVICE_HARDWARE_REVISION, device.getDeviceHardwareRevision());
        values.put(COL_DEVICE_PART_NUMBER, device.getDevicePartNumber());
        values.put(COL_DEVICE_PROTOCOL_REVISION, device.getDeviceProtocolRevision());
        values.put(COL_DEVICE_SYSTEM_ID, device.getDeviceSystemId());
        values.put(COL_DEVICE_BATTERY_LEVEL, device.getDeviceBatteryLevel());
        values.put(COL_INFORMATION_REGISTERED, device.isDeviceInformationRegistered() ? 1 : 0);

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    /**
     * Update the device in the database.
     * @param device device
     * @return the number of rows updated
     */
    public synchronized int updateHealthCareDevice(final HealthCareDevice device) {
        ContentValues values = new ContentValues();
        values.put(COL_REGISTER_FLAG, device.isRegisterFlag() ? 1 : 0);
        values.put(COL_PROFILE_TYPE, device.getProfileType());
        values.put(COL_DEVICE_MANUFACTURE_NAME, device.getDeviceManufacturerName());
        values.put(COL_DEVICE_MODEL_NUMBER, device.getDeviceModelNumber());
        values.put(COL_DEVICE_FIRMWARE_REVISION, device.getDeviceFirmwareRevision());
        values.put(COL_DEVICE_SERIAL_NUMBER, device.getDeviceSerialNumber());
        values.put(COL_DEVICE_SOFTWARE_REVISION, device.getDeviceSoftwareRevision());
        values.put(COL_DEVICE_HARDWARE_REVISION, device.getDeviceHardwareRevision());
        values.put(COL_DEVICE_PART_NUMBER, device.getDevicePartNumber());
        values.put(COL_DEVICE_PROTOCOL_REVISION, device.getDeviceProtocolRevision());
        values.put(COL_DEVICE_SYSTEM_ID, device.getDeviceSystemId());
        values.put(COL_DEVICE_BATTERY_LEVEL, device.getDeviceBatteryLevel());
        values.put(COL_INFORMATION_REGISTERED, device.isDeviceInformationRegistered() ? 1 : 0);

        String whereClause = COL_ADDRESS + "=?";
        String[] whereArgs = {
                device.getAddress()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * Delete the device in the database.
     * @param device device
     * @return the number of rows deleted, 0 otherwise
     */
    public synchronized int removeHealthCareDevice(final HealthCareDevice device) {
        String whereClause = COL_ADDRESS + "=?";
        String[] whereArgs = {
                device.getAddress()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    public synchronized int removeAllHealthCareDevice() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, "", null);
        } finally {
            db.close();
        }
    }

    public synchronized List<HealthCareDevice> getAllDevices() {
        String whereClause = "";
        String[] whereArgs = {};
        return getDevices(whereClause, whereArgs);
    }

    public synchronized List<HealthCareDevice> getRegisteredDevices() {
        String whereClause = COL_REGISTER_FLAG + "=?";
        String[] whereArgs = {"1"};
        return getDevices(whereClause, whereArgs);
    }

    public synchronized HealthCareDevice getRegisteredDevice(String address) {
        String whereClause = COL_ADDRESS + "=?";
        String[] whereArgs = {address};
        List<HealthCareDevice> data = getDevices(whereClause, whereArgs);
        if (data != null && data.size() > 0) {
            return data.get(0);
        }
        return null;
    }

    /**
     * Get a list of device in the database.
     * @return a list of device
     */
    private synchronized List<HealthCareDevice> getDevices(String whereClause, String[] selectionArgs) {
        String sql = "SELECT * FROM " + TBL_NAME;
        if (whereClause != null && whereClause.length() > 0) {
            sql += " WHERE " + whereClause;
        };

        List<HealthCareDevice> devices = new ArrayList<>();
        Cursor cursor = null;

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(sql, selectionArgs);

            boolean next = cursor.moveToFirst();
            while (next) {
                HealthCareDevice device = new HealthCareDevice();
                device.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
                device.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                device.setAddress(cursor.getString(cursor.getColumnIndex(COL_ADDRESS)));
                device.setRegisterFlag(cursor.getInt(cursor.getColumnIndex(COL_REGISTER_FLAG)) == 1);
                device.setProfileType(cursor.getInt(cursor.getColumnIndex(COL_PROFILE_TYPE)));
                device.setDeviceManufacturerName(cursor.getString(cursor.getColumnIndex(COL_DEVICE_MANUFACTURE_NAME)));
                device.setDeviceModelNumber(cursor.getString(cursor.getColumnIndex(COL_DEVICE_MODEL_NUMBER)));
                device.setDeviceFirmwareRevision(cursor.getString(cursor.getColumnIndex(COL_DEVICE_FIRMWARE_REVISION)));
                device.setDeviceSerialNumber(cursor.getString(cursor.getColumnIndex(COL_DEVICE_SERIAL_NUMBER)));
                device.setDeviceSoftwareRevision(cursor.getString(cursor.getColumnIndex(COL_DEVICE_SOFTWARE_REVISION)));
                device.setDeviceHardwareRevision(cursor.getString(cursor.getColumnIndex(COL_DEVICE_HARDWARE_REVISION)));
                device.setDevicePartNumber(cursor.getString(cursor.getColumnIndex(COL_DEVICE_PART_NUMBER)));
                device.setDeviceProtocolRevision(cursor.getString(cursor.getColumnIndex(COL_DEVICE_PROTOCOL_REVISION)));
                device.setDeviceSystemId(cursor.getString(cursor.getColumnIndex(COL_DEVICE_SYSTEM_ID)));
                device.setDeviceBatteryLevel(cursor.getString(cursor.getColumnIndex(COL_DEVICE_BATTERY_LEVEL)));
                device.setDeviceInformationRegistered(cursor.getInt(cursor.getColumnIndex(COL_INFORMATION_REGISTERED)) == 1);
                devices.add(device);
                next = cursor.moveToNext();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return devices;
    }

    public synchronized void disconnectedDevices() {

    }

    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
            createDB(db);
        }

        private void createDB(final SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TBL_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_ADDRESS + " TEXT NOT NULL, "
                    + COL_REGISTER_FLAG + " INTEGER, "
                    + COL_PROFILE_TYPE + " INTEGER, "
                    + COL_DEVICE_MANUFACTURE_NAME + " TEXT NOT NULL, "
                    + COL_DEVICE_MODEL_NUMBER + " TEXT NOT NULL, "
                    + COL_DEVICE_FIRMWARE_REVISION + " TEXT NOT NULL, "
                    + COL_DEVICE_SERIAL_NUMBER + " TEXT NOT NULL, "
                    + COL_DEVICE_SOFTWARE_REVISION + " TEXT NOT NULL, "
                    + COL_DEVICE_HARDWARE_REVISION + " TEXT NOT NULL, "
                    + COL_DEVICE_PART_NUMBER + " TEXT NOT NULL, "
                    + COL_DEVICE_PROTOCOL_REVISION + " TEXT NOT NULL, "
                    + COL_DEVICE_SYSTEM_ID + " TEXT NOT NULL, "
                    + COL_DEVICE_BATTERY_LEVEL + " TEXT NOT NULL, "
                    + COL_INFORMATION_REGISTERED + " INTEGER"
                    + ");";
            db.execSQL(sql);
        }
    }
}

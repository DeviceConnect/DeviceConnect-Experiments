package org.deviceconnect.android.deviceplugin.adb.core;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

class ConnectionDBHelper {

    private static final String DB_NAME = "adb_daemon.db";
    private static final int DB_VERSION = 1;
    private static final String TBL_NAME = "service_tbl";
    private static final String COL_IP_ADDRESS = "ip_address";
    private static final String COL_PORT_NUMBER = "port_number";

    private final DBHelper mDBHelper;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    ConnectionDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    synchronized long addConnection(final Connection conn) {
        ContentValues values = new ContentValues();
        values.put(COL_IP_ADDRESS, conn.getIpAddress());
        values.put(COL_PORT_NUMBER, conn.getPortNumber());

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    synchronized int updateConnection(final Connection conn) {
        ContentValues values = new ContentValues();
        values.put(COL_PORT_NUMBER, conn.getPortNumber());

        String whereClause = COL_IP_ADDRESS + "=?";
        String[] whereArgs = {
                conn.getIpAddress()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    synchronized int removeConnection(final Connection conn) {
        String whereClause = COL_IP_ADDRESS + "=?";
        String[] whereArgs = {
                conn.getIpAddress()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    List<Connection> getConnectionList() {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<Connection> connections = new ArrayList<>();
        boolean next = cursor.moveToFirst();
        while (next) {
            String ipAddress = cursor.getString(cursor.getColumnIndex(COL_IP_ADDRESS));
            int port = cursor.getInt(cursor.getColumnIndex(COL_PORT_NUMBER));

            Connection conn = new Connection(ipAddress, port);
            connections.add(conn);
            next = cursor.moveToNext();
        }
        cursor.close();
        return connections;
    }

    private static class DBHelper extends SQLiteOpenHelper {

        DBHelper(final Context context) {
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
                    + COL_IP_ADDRESS + " TEXT,"
                    + COL_PORT_NUMBER + " INTEGER"
                    + ");";
            db.execSQL(sql);
        }
    }
}

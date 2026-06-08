package com.locationtracker.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "location_tracker.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_LOCATIONS = "locations";
    private static final String COL_ID = "_id";
    private static final String COL_LAT = "latitude";
    private static final String COL_LNG = "longitude";
    private static final String COL_ACCURACY = "accuracy";
    private static final String COL_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_LOCATIONS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_LAT + " REAL NOT NULL, " +
                    COL_LNG + " REAL NOT NULL, " +
                    COL_ACCURACY + " REAL, " +
                    COL_TIMESTAMP + " INTEGER NOT NULL" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
    }

    public void insertLocation(LocationEntry entry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LAT, entry.getLatitude());
        values.put(COL_LNG, entry.getLongitude());
        values.put(COL_ACCURACY, entry.getAccuracy());
        values.put(COL_TIMESTAMP, entry.getTimestamp());
        long id = db.insert(TABLE_LOCATIONS, null, values);
        entry.setId(id);
        db.close();
    }

    public List<LocationEntry> getAllLocations() {
        List<LocationEntry> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATIONS, null, null, null, null, null,
                COL_TIMESTAMP + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LNG));
                float acc = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_ACCURACY));
                long ts = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                list.add(new LocationEntry(id, lat, lng, acc, ts));
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    public List<LocationEntry> getLocationsForMap() {
        // Returns all, ordered oldest-first for map polyline drawing
        List<LocationEntry> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATIONS, null, null, null, null, null,
                COL_TIMESTAMP + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LNG));
                float acc = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_ACCURACY));
                long ts = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                list.add(new LocationEntry(id, lat, lng, acc, ts));
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    public int getTotalCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LOCATIONS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOCATIONS, null, null);
        db.close();
    }

    /**
     * Export all location data as CSV string.
     */
    public String exportToCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("id,latitude,longitude,accuracy_m,timestamp,datetime\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        List<LocationEntry> locations = getAllLocations();
        for (LocationEntry e : locations) {
            csv.append(e.getId()).append(",")
                    .append(e.getLatitude()).append(",")
                    .append(e.getLongitude()).append(",")
                    .append(e.getAccuracy()).append(",")
                    .append(e.getTimestamp()).append(",")
                    .append(sdf.format(new Date(e.getTimestamp())))
                    .append("\n");
        }
        return csv.toString();
    }
}

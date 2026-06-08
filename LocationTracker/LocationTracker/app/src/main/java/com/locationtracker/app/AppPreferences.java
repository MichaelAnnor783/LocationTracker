package com.locationtracker.app;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREFS_NAME = "location_tracker_prefs";
    private static final String KEY_UPDATE_INTERVAL = "update_interval_ms";
    private static final long DEFAULT_INTERVAL = 10_000L; // 10 seconds

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public long getUpdateIntervalMs() {
        return prefs.getLong(KEY_UPDATE_INTERVAL, DEFAULT_INTERVAL);
    }

    public void setUpdateIntervalMs(long intervalMs) {
        prefs.edit().putLong(KEY_UPDATE_INTERVAL, intervalMs).apply();
    }
}

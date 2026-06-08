package com.locationtracker.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationTrackingService extends Service {

    public static final String ACTION_START = "com.locationtracker.app.START";
    public static final String ACTION_STOP = "com.locationtracker.app.STOP";
    public static final String ACTION_LOCATION_UPDATE = "com.locationtracker.app.LOCATION_UPDATE";

    private static final String CHANNEL_ID = "LocationTrackerChannel";
    private static final String CHANNEL_NAME = "Location Tracking";
    private static final int NOTIFICATION_ID = 1001;

    public static volatile boolean isRunning = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseHelper dbHelper;

    // Default interval: 10 seconds
    private long updateInterval = 10_000L;
    private long fastestInterval = 5_000L;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        setupLocationCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            // Read interval from preferences
            AppPreferences prefs = new AppPreferences(this);
            updateInterval = prefs.getUpdateIntervalMs();
            fastestInterval = updateInterval / 2;

            startForeground(NOTIFICATION_ID, buildNotification("Initializing...", 0, 0));
            requestLocationUpdates();
            isRunning = true;
        } else if (ACTION_STOP.equals(action)) {
            stopLocationUpdates();
            stopForeground(true);
            stopSelf();
            isRunning = false;
        }
        return START_STICKY;
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                for (Location location : result.getLocations()) {
                    handleNewLocation(location);
                }
            }
        };
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, updateInterval)
                .setMinUpdateIntervalMillis(fastestInterval)
                .setWaitForAccurateLocation(false)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void handleNewLocation(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        float accuracy = location.getAccuracy();
        long timestamp = location.getTime();

        // Save to DB
        LocationEntry entry = new LocationEntry(lat, lng, accuracy, timestamp);
        dbHelper.insertLocation(entry);

        // Broadcast to UI
        Intent broadcast = new Intent(ACTION_LOCATION_UPDATE);
        broadcast.putExtra("latitude", lat);
        broadcast.putExtra("longitude", lng);
        broadcast.putExtra("accuracy", accuracy);
        broadcast.putExtra("timestamp", timestamp);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        // Update notification
        updateNotification(lat, lng, timestamp);
    }

    private void updateNotification(double lat, double lng, long timestamp) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, buildNotification(
                String.format(Locale.getDefault(), "%.5f, %.5f", lat, lng),
                lat, lng));
    }

    private Notification buildNotification(String coordinates, double lat, double lng) {
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notifIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, LocationTrackingService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 1,
                stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Location Tracker Running")
                .setContentText(coordinates)
                .setSubText("Last update: " + sdf.format(new Date()))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_delete, "Stop", stopPending)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows while location tracking is active");
            channel.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        isRunning = false;
    }
}

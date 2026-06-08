package com.locationtracker.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.locationtracker.app.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private LocationAdapter adapter;
    private List<LocationEntry> locationEntries;
    private DatabaseHelper dbHelper;
    private boolean isTracking = false;

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra("latitude", 0);
            double lng = intent.getDoubleExtra("longitude", 0);
            float accuracy = intent.getFloatExtra("accuracy", 0);
            long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());

            LocationEntry entry = new LocationEntry(lat, lng, accuracy, timestamp);
            locationEntries.add(0, entry);
            adapter.notifyItemInserted(0);
            binding.recyclerView.scrollToPosition(0);

            // Update status card
            updateStatusCard(entry);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dbHelper = new DatabaseHelper(this);
        locationEntries = new ArrayList<>(dbHelper.getAllLocations());

        adapter = new LocationAdapter(locationEntries, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabToggle.setOnClickListener(v -> toggleTracking());
        binding.btnViewMap.setOnClickListener(v -> openMap());
        binding.btnExport.setOnClickListener(v -> exportData());
        binding.btnClear.setOnClickListener(v -> confirmClear());

        updateTrackingUI(false);
    }

    private void toggleTracking() {
        if (!isTracking) {
            checkPermissionsAndStart();
        } else {
            stopTracking();
        }
    }

    private void checkPermissionsAndStart() {
        List<String> missingPerms = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            missingPerms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPerms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if (missingPerms.isEmpty()) {
            startTracking();
        } else {
            ActivityCompat.requestPermissions(this,
                    missingPerms.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void startTracking() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        serviceIntent.setAction(LocationTrackingService.ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        isTracking = true;
        updateTrackingUI(true);
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show();
    }

    private void stopTracking() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        serviceIntent.setAction(LocationTrackingService.ACTION_STOP);
        startService(serviceIntent);
        isTracking = false;
        updateTrackingUI(false);
        Toast.makeText(this, "Location tracking stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateTrackingUI(boolean tracking) {
        isTracking = tracking;
        if (tracking) {
            binding.fabToggle.setImageResource(android.R.drawable.ic_media_pause);
            binding.statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.status_active));
            binding.tvStatus.setText("Tracking Active");
        } else {
            binding.fabToggle.setImageResource(android.R.drawable.ic_media_play);
            binding.statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.status_inactive));
            binding.tvStatus.setText("Tracking Paused");
        }
    }

    private void updateStatusCard(LocationEntry entry) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        binding.tvLastUpdate.setText("Last update: " + sdf.format(new Date(entry.getTimestamp())));
        binding.tvCurrentCoords.setText(String.format(Locale.getDefault(),
                "%.6f, %.6f", entry.getLatitude(), entry.getLongitude()));
        binding.tvAccuracy.setText(String.format(Locale.getDefault(),
                "Accuracy: %.1f m", entry.getAccuracy()));
        binding.tvTotalPoints.setText("Total points: " + locationEntries.size());
    }

    private void openMap() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void exportData() {
        String csvData = dbHelper.exportToCSV();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_TEXT, csvData);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Location Tracker Data Export");
        startActivity(Intent.createChooser(shareIntent, "Export Location Data"));
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will permanently delete all recorded location points. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.clearAll();
                    locationEntries.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startTracking();
            } else {
                showPermissionRationale();
            }
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("This app needs location permission to track your position. " +
                        "Please grant location permissions in Settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter(LocationTrackingService.ACTION_LOCATION_UPDATE));
        // Check if service is running
        isTracking = LocationTrackingService.isRunning;
        updateTrackingUI(isTracking);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

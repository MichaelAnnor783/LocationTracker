package com.locationtracker.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerInterval;
    private AppPreferences prefs;

    private static final long[] INTERVALS_MS = {5_000L, 10_000L, 30_000L, 60_000L, 300_000L};
    private static final String[] INTERVAL_LABELS = {"5 seconds", "10 seconds", "30 seconds", "1 minute", "5 minutes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = new AppPreferences(this);
        spinnerInterval = findViewById(R.id.spinner_interval);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, INTERVAL_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInterval.setAdapter(adapter);

        // Set current selection
        long currentInterval = prefs.getUpdateIntervalMs();
        for (int i = 0; i < INTERVALS_MS.length; i++) {
            if (INTERVALS_MS[i] == currentInterval) {
                spinnerInterval.setSelection(i);
                break;
            }
        }

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> {
            int pos = spinnerInterval.getSelectedItemPosition();
            prefs.setUpdateIntervalMs(INTERVALS_MS[pos]);
            Toast.makeText(this, "Settings saved. Restart tracking to apply.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

package com.locationtracker.app;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private DatabaseHelper dbHelper;
    private List<LocationEntry> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Location History Map");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        locations = dbHelper.getLocationsForMap();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        TextView tvCount = findViewById(R.id.tv_point_count);
        tvCount.setText(locations.size() + " location points");
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        if (locations == null || locations.isEmpty()) {
            Toast.makeText(this, "No location data to display", Toast.LENGTH_SHORT).show();
            return;
        }

        List<LatLng> points = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault());

        for (int i = 0; i < locations.size(); i++) {
            LocationEntry e = locations.get(i);
            LatLng latLng = new LatLng(e.getLatitude(), e.getLongitude());
            points.add(latLng);

            boolean isFirst = i == 0;
            boolean isLast = i == locations.size() - 1;

            if (isFirst || isLast) {
                MarkerOptions marker = new MarkerOptions()
                        .position(latLng)
                        .title(isFirst ? "Start" : "Latest")
                        .snippet(sdf.format(new Date(e.getTimestamp()))
                                + "\n±" + String.format(Locale.getDefault(), "%.1f", e.getAccuracy()) + "m")
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                isLast ? BitmapDescriptorFactory.HUE_RED
                                       : BitmapDescriptorFactory.HUE_GREEN));
                googleMap.addMarker(marker);
            }
        }

        // Draw path polyline
        googleMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(6f)
                .color(Color.parseColor("#2196F3"))
                .geodesic(true));

        // Fit bounds to show all points
        if (points.size() > 1) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng p : points) boundsBuilder.include(p);
            LatLngBounds bounds = boundsBuilder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 15f));
        }

        // Map type toggle
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

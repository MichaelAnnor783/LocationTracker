package com.locationtracker.app;

public class LocationEntry {
    private long id;
    private double latitude;
    private double longitude;
    private float accuracy;
    private long timestamp;

    public LocationEntry(double latitude, double longitude, float accuracy, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }

    public LocationEntry(long id, double latitude, double longitude, float accuracy, long timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getAccuracy() { return accuracy; }
    public long getTimestamp() { return timestamp; }

    public void setId(long id) { this.id = id; }
}

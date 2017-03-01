package com.ottamotta.pavement.location;


import android.location.Location;

public class Loc {

    public final double lat;
    public final double lon;
    public final double speed;
    public final double altitude;
    public final long timestamp;

    public static Loc fromLocation(Location location) {
        return new Loc(location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getAltitude(), location.getTime());
    }

    public Loc(double lat, double lon, double speed, double altitude, long timestamp) {
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.altitude = altitude;
        this.timestamp = timestamp;
    }

    public long getTime() {
        return timestamp;
    }
}

package com.ottamotta.pavement.tracking;

import android.location.Location;

import com.ottamotta.pavement.accelerometer.Acceleration;

public class AccelerationWithLocation {

    private final Location location;
    private final Acceleration acceleration;

    public AccelerationWithLocation(Location location, Acceleration acceleration) {
        this.location = location;
        this.acceleration = acceleration;
    }

    public Location getLocation() {
        return location;
    }

    public Acceleration getAcceleration() {
        return acceleration;
    }
}

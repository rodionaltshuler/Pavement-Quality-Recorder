package com.ottamotta.pavement.tracking;

import com.ottamotta.pavement.accelerometer.Acceleration;
import com.ottamotta.pavement.location.Loc;

public class AccelerationWithLocation {

    private final Loc location;
    private final Acceleration acceleration;

    public AccelerationWithLocation(Loc location, Acceleration acceleration) {
        this.location = location;
        this.acceleration = acceleration;
    }

    public Loc getLocation() {
        return location;
    }

    public Acceleration getAcceleration() {
        return acceleration;
    }
}

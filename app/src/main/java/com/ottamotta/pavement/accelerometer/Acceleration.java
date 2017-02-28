package com.ottamotta.pavement.accelerometer;

import java.util.Arrays;

public class Acceleration {

    private final float[] values;

    private final long timestamp;

    public Acceleration(float[] values, long timestamp) {
        this.timestamp = timestamp;
        this.values = values;
    }

    @Override
    public String toString() {
        return "Acceleration{" + Arrays.toString(values);
    }

    public float[] getValues() {
        return values;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

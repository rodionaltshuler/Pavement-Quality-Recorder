package com.ottamotta.pavement.tracking;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.ottamotta.pavement.accelerometer.Acceleration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccelerationsByLocation {

    private final Map<Location, List<Acceleration>> accelerationsByLocation = new LinkedHashMap<>();

    public void addAll(List<AccelerationWithLocation> points) {
        try {
            long lastLocationTimestamp = 0;
            List<Acceleration> accelerations = null;
            for (AccelerationWithLocation point : points) {
                if (lastLocationTimestamp != point.getLocation().getTime()) {
                    //new location point
                    accelerations = new ArrayList<>();
                    accelerationsByLocation.put(point.getLocation(), accelerations);
                }
                lastLocationTimestamp = point.getLocation().getTime();
                accelerations.add(point.getAcceleration());
            }
        } catch (Exception e) {
            Log.e("Tracking", "Error while constucting AccelerationsByLocation: " + e.getMessage());
        }
    }

    public Map<Location, List<Acceleration>> getAccelerationsByLocation() {
        return Collections.unmodifiableMap(accelerationsByLocation);
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Location, List<Acceleration>> entry : accelerationsByLocation.entrySet()) {
                sb.append(new Date(entry.getKey().getTime()))
                        .append(": ")
                        .append(TextUtils.join(",", entry.getValue()))
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Empty Acceleration!";
        }
    }

}

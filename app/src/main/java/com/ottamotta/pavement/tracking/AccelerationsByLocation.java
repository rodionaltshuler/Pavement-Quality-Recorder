package com.ottamotta.pavement.tracking;

import android.text.TextUtils;
import android.util.Log;

import com.ottamotta.pavement.accelerometer.Acceleration;
import com.ottamotta.pavement.location.Loc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class AccelerationsByLocation {

    private final LinkedHashMap<Loc, List<Acceleration>> accelerationsByLocation = new LinkedHashMap<>();

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

    public Map<Loc, List<Acceleration>> getAccelerationsByLocation() {
        return Collections.unmodifiableMap(accelerationsByLocation);
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Loc, List<Acceleration>> entry : accelerationsByLocation.entrySet()) {
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

    public String getKey() {
        return String.valueOf(hashCode());
    }

    public int getPointsCount() {
        return accelerationsByLocation.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccelerationsByLocation that = (AccelerationsByLocation) o;

        return this.hashCode() == that.hashCode(); //we can live with it
    }

    @Override
    public int hashCode() {
        return Observable.from(accelerationsByLocation.keySet())
                .reduce(0, (sum, location) -> sum + location.hashCode())
                .toBlocking()
                .single();
    }

    List<String> toCSV() {
        List<String> csv = new ArrayList<>();
        for (Map.Entry<Loc, List<Acceleration>> entry : accelerationsByLocation.entrySet()) {
            StringBuilder sb = new StringBuilder();
            Loc loc = entry.getKey();
            sb.append(loc.lat).append(",");
            sb.append(loc.lon).append(",");
            sb.append(loc.speed).append(",");
            sb.append(loc.altitude).append(",");
            List<Acceleration> accelerationList = entry.getValue();
            sb.append("[");
            sb.append(TextUtils.join(",", accelerationList));
            sb.append("]");
            sb.append("\n");
            csv.add(sb.toString());
        }
        return csv;
    }
}

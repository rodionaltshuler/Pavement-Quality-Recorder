package com.ottamotta.pavement.tracking;


import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

public class Tracker {

    private static Tracker instance;

    private Context context;

    private static final String TRACKING_STATUS = "tracking_status";

    private BehaviorSubject<Boolean> trackingStatusSubject;

    private BehaviorSubject<AccelerationsByLocation> accelerationsByLocation;

    public static synchronized Tracker getInstance(Context context) {
        if (null == instance) {
            instance = new Tracker(context);
        }
        return instance;
    }

    private Tracker(Context context) {
        this.context = context;
        trackingStatusSubject = BehaviorSubject.create();
        accelerationsByLocation = BehaviorSubject.create();
        restoreState();
        trackingStatusSubject.subscribe(tracking -> {
                    Intent intent = new Intent(context, TrackingService.class);
                    if (tracking) {
                        context.startService(intent);
                    } else {
                        context.stopService(intent);
                    }
                    saveTrackingStatus(tracking);
                },
                error -> {
                    error.printStackTrace();
                    Log.e("Tracking", "Error: " + error.getMessage());
                });
    }

    private void restoreState() {
        trackingStatusSubject.onNext(getPreviousStatus());
    }

    public void start() {
        trackingStatusSubject.onNext(true);
    }

    public void stop() {
        trackingStatusSubject.onNext(false);
    }

    private void saveTrackingStatus(boolean isTracking) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(TRACKING_STATUS, isTracking)
                .apply();
    }

    private boolean getPreviousStatus() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(TRACKING_STATUS, false);
    }

    Observable<Boolean> getTrackingStatusObservable() {
        return trackingStatusSubject
                .asObservable()
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    public Observable<AccelerationsByLocation> getAccelerationsByLocationObservable() {
        return accelerationsByLocation
                .asObservable()
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    void onNextAccelerationsByLocation(AccelerationsByLocation next) {
        accelerationsByLocation.onNext(next);
    }
}

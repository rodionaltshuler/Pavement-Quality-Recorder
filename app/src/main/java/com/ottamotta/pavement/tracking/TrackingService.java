package com.ottamotta.pavement.tracking;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ottamotta.pavement.accelerometer.AccelerometerSubject;
import com.ottamotta.pavement.location.Loc;
import com.ottamotta.pavement.location.LocationSubject;
import com.ottamotta.pavement.location.PromptUserActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;

public class TrackingService extends Service {

    private Tracker tracker;

    private AccelerometerSubject accelerometerSubject;
    private LocationSubject locationSubject;

    @Override
    public void onCreate() {
        super.onCreate();
        accelerometerSubject = AccelerometerSubject.getInstance(getApplicationContext());
        locationSubject = LocationSubject.getInstance(getApplicationContext());

        tracker = Tracker.getInstance(getApplicationContext());
        tracker.getTrackingStatusObservable().subscribe(tracking -> {
            try {
                if (!tracking) {
                    Log.d("Tracking", "TrackingService stopping self");
                    stopSelf();
                    locationSubject.stop();
                    accelerometerSubject.stop();
                } else {
                    startTracking();
                }
            } catch (Exception e) {
                Log.e("Tracking", "Error while starting tracking: " + e.getMessage());
            }
        }, error -> {
            Log.e("Tracking", "Error: " + error.getMessage());
        });

        //UPLOAD DATA
        tracker.getAccelerationsByLocationObservable().subscribe(accelerationsByLocation -> {
            UploadIntentService.start(getApplicationContext(), accelerationsByLocation);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTracking() {
        Log.d("Tracking", "TrackingService.startTracking()");
        Observable<AccelerationWithLocation> accelerationWithLocationObservable = Observable.combineLatest(
                locationSubject.start().map(Loc::fromLocation),
                accelerometerSubject.start(),
                AccelerationWithLocation::new);

        Observable<AccelerationsByLocation> accelerationsByLocationObservable = accelerationWithLocationObservable
                .buffer(20, TimeUnit.SECONDS)
                .map(accelerationWithLocations -> {
                    AccelerationsByLocation accelerationsByLocation = new AccelerationsByLocation();
                    accelerationsByLocation.addAll(accelerationWithLocations);
                    return accelerationsByLocation;
                })
                .doOnNext(accelerationsByLocation -> {
                    Log.d("Tracking", accelerationsByLocation.toString());
                });

        accelerationsByLocationObservable.subscribe(accelerationsByLocation -> {
                    tracker.onNextAccelerationsByLocation(accelerationsByLocation);
                },
                error -> {
                    Log.e("Tracking", "Error: " + error.getMessage());
                }
        );

    }
}

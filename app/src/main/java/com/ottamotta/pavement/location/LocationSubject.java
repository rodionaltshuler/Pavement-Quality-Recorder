package com.ottamotta.pavement.location;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ottamotta.pavement.googleapiclient.GoogleApiClientObservable;
import com.ottamotta.pavement.location.exceptions.LocationDisabledException;
import com.ottamotta.pavement.location.exceptions.MissingPermissionException;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LocationSubject {

    private static final String TAG = LocationSubject.class.getSimpleName();

    private static final int MIN_INTERVAL = 1000;
    private static final int FASTEST_INTERVAL = 500;

    @Nullable
    private LocationListener locationListener;

    @Nullable
    private GoogleApiClient connectedGoogleApiClient;

    private final LocationRequest locationRequest = new LocationRequest();

    {
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(MIN_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    private Context context;

    private static LocationSubject instance;

    public static synchronized LocationSubject getInstance(Context context) {
        if (null == instance) {
            instance = new LocationSubject(context);
        }
        return instance;
    }

    private final PublishSubject<Location> publishSubjectLocation = PublishSubject.create();

    private LocationSubject(Context context) {
        this.context = context;
    }

    public Observable<Location> getLocationObservable() {
        return publishSubjectLocation.asObservable();
    }

    public void stop() {
        locationSubscription.unsubscribe();
        /*if (connectedGoogleApiClient != null && locationListener != null) {
            Log.d("Tracking", "Stopping locaiton updates");
            LocationServices.FusedLocationApi.removeLocationUpdates(connectedGoogleApiClient, locationListener);
        }*/
    }

    public Observable<Location> start() {
        restart();
        return getLocationObservable();
    }

    private CompositeSubscription locationSubscription = new CompositeSubscription();

    private void restart() {
        Observable<Location> observable =
                restartableLocationObservable();

        locationSubscription.add(
                observable.subscribe(
                location -> publishSubjectLocation.onNext(location),
                error -> {
                    //todo need to handle exceptions here
                    publishSubjectLocation.onError(error);
                }
        ));
    }

    private Observable<Location> restartableLocationObservable() {
        return checkLocationEnabled()
                .flatMap(locationEnabled -> checkMissedPermissions())
                .flatMap(permissionsGranted -> new GoogleApiClientObservable(context).toObservable())
                .flatMap(this::requestLocationObservable);
    }

    private Observable<Location> requestLocationObservable(GoogleApiClient connectedGoogleApiClient) {
        this.connectedGoogleApiClient = connectedGoogleApiClient;
        return Observable.<Location>create(locationSubsrciber -> {
            LocationListener locationListener = location -> {
                Log.d(TAG, "location changed: " + location.toString());
                locationSubsrciber.onNext(location);
            };
            LocationSubject.this.locationListener = locationListener;
            Log.d(TAG, "Requesting location updates");
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(connectedGoogleApiClient, locationRequest, locationListener);
            } catch (SecurityException e) {
                locationSubsrciber.onError(e);
            }

        })
                .doOnError(throwable -> Log.e(TAG, throwable.getMessage()));
    }

    private Observable<Boolean> checkLocationEnabled() {
        return Observable.create(subscriber -> {
            try {
                if (isLocationEnabled()) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onError(new LocationDisabledException());
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }

    private Observable<Boolean> checkMissedPermissions() {
        return Observable.create(subscriber -> {
            List<String> missingPermissions = missingPermissions();
            if (missingPermissions.isEmpty()) {
                subscriber.onNext(true);
            } else {
                subscriber.onError(new MissingPermissionException(missingPermissions));
            }
        });
    }

    private List<String> missingPermissions() {
        Log.d(TAG, "Checking missing permissions");
        List<String> missing = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return missing;
    }

    private boolean isLocationEnabled() throws Settings.SettingNotFoundException {
        Log.d(TAG, "Checking whether location is enabled");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            android.location.LocationManager systemLocationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = systemLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
            boolean networkEnabled = systemLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
            return gpsEnabled || networkEnabled;
        }
    }

}

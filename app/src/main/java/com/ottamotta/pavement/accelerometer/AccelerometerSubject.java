package com.ottamotta.pavement.accelerometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;


public class AccelerometerSubject {

    private static final String TAG = AccelerometerSubject.class.getSimpleName();

    private Context context;

    private SensorManager sensorManager;

    private static AccelerometerSubject instance;

    public static synchronized AccelerometerSubject getInstance(Context context) {
        if (null == instance) {
            instance = new AccelerometerSubject(context);
        }
        return instance;
    }

    private PublishSubject<Acceleration> publishSubjectAcceleration;

    private CompositeSubscription sensorsSubscription = new CompositeSubscription();

    private AccelerometerSubject(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        publishSubjectAcceleration = PublishSubject.create();
    }

    public void stop() {
        Log.d("Tracking", "Stopping accelerometer updates");
        sensorManager.unregisterListener(sensorEventListener);
        sensorsSubscription.unsubscribe();
    }

    public Observable<Acceleration> start() {
        startSensors();
        return publishSubjectAcceleration.asObservable();
    }

    public void start(Action1<Acceleration> onNextAction) {
        startSensors();
        sensorsSubscription.add(publishSubjectAcceleration.subscribe(
                onNextAction,
                error -> {
                    Log.e("Tracking", error.getMessage());
                }));
    }

    private void startSensors() {
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME);

    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            publishSubjectAcceleration.onNext(new Acceleration(sensorEvent.values, sensorEvent.timestamp));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
}

package com.ottamotta.pavement.tracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.widget.TextView;

import com.ottamotta.pavement.R;
import com.ottamotta.pavement.login.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class TrackingStatusActivity extends BaseActivity {

    Button recordButton;

    TextView textView;

    Tracker tracker;

    CompositeSubscription subscriptions = new CompositeSubscription();

    private final String[] permissions = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int GET_PERMISSIONS_REQUEST_CODE = 201;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        recordButton = (Button) findViewById(R.id.btn_start_stop);
        textView = (TextView) findViewById(R.id.data);
        tracker = Tracker.getInstance(getApplicationContext());
        List<String> missingPermissions = getMissingPermissions();
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, GET_PERMISSIONS_REQUEST_CODE);
        }
    }

    private List<String> getMissingPermissions() {
        List<String> missing = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }
        return missing;
    }

    private Action1<Boolean> recordingStatusAction = recording -> {
        recordButton.setText(recording ? R.string.stop : R.string.start);
        recordButton.setOnClickListener(view -> {
            if (recording)
                tracker.stop();
            else
                tracker.start();
        });
    };

    private Action1<AccelerationsByLocation> locationUpdateAction = accelerationsByLocation -> {
        textView.setText(accelerationsByLocation.toString());
    };

    @Override
    protected void onResume() {
        super.onResume();
        subscriptions.add(tracker.getTrackingStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recordingStatusAction));
        subscriptions.add(tracker.getAccelerationsByLocationObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locationUpdateAction));
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }
}

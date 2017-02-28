package com.ottamotta.pavement.tracking;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import com.ottamotta.pavement.R;
import com.ottamotta.pavement.login.BaseActivity;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class TrackingStatusActivity extends BaseActivity {

    Button recordButton;

    TextView textView;

    Tracker tracker;

    CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        recordButton = (Button) findViewById(R.id.btn_start_stop);
        textView = (TextView) findViewById(R.id.data);
        tracker = Tracker.getInstance(getApplicationContext());
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

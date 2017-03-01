package com.ottamotta.pavement.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.ottamotta.pavement.tools.GsonProvider;

import java.util.List;
import java.util.Map;

import rx.Observable;

public class UploadIntentService extends IntentService {

    private static final String EXTRA_UPLOAD_DATA = "extra_upload_data";

    private static final String PREFS_FILE_NAME = "data_not_uploaded";

    private static final Gson gson = GsonProvider.provideGson();

    public static void start(Context src, AccelerationsByLocation accelerationsByLocation) {
        Intent intent = new Intent(src, UploadIntentService.class);
        String extra = gson.toJson(accelerationsByLocation);
        intent.putExtra(EXTRA_UPLOAD_DATA, extra);
        src.startService(intent);
    }

    public UploadIntentService() {
        super("UploadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String jsonToUpload = intent.getExtras().getString(EXTRA_UPLOAD_DATA);
        try {
            Log.d("Tracking", "Uploading data: " + jsonToUpload);
            AccelerationsByLocation accelerationsByLocation = gson.fromJson(jsonToUpload, AccelerationsByLocation.class);

            Log.d("Tracking", "New accelerationsByLocation contains " + accelerationsByLocation.getPointsCount() + " points");

            List<AccelerationsByLocation> listToUpload = getUnsent();
            Log.d("Tracking", "Have " + listToUpload.size() + " chunks with data not sent");
            listToUpload.add(accelerationsByLocation);

            save(accelerationsByLocation.getKey(), jsonToUpload);

            Observable.from(listToUpload)
                    .flatMap(this::upload)
                    .subscribe(uploadResult -> {
                        if (uploadResult.success) removeFromUnsent(uploadResult.uploadedDataKey);
                    });
        } catch (Exception e) {
            Log.e("Tracking", "Error handling upload intent: " + e.getMessage());
        }
    }

    private void save(String key, String value) {
        Log.d("Tracking", "Saving new acceleretionsByLocation with key " + key);
        getApplicationContext().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    private void removeFromUnsent(String key) {
        Log.d("Tracking", "Removing as sent key " + key);
        getApplicationContext().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply();
    }

    private List<AccelerationsByLocation> getUnsent() {
        Map<String, ?> unsent =
                getApplicationContext().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                        .getAll();

        return Observable.from(unsent.entrySet())
                .map(entry -> gson.fromJson((String) entry.getValue(), AccelerationsByLocation.class))
                .toList()
                .toBlocking()
                .single();
    }

    private Observable<Uploader.UploadResult> upload(AccelerationsByLocation data) {
        return new LocalFileUploader().upload(data);
    }

}

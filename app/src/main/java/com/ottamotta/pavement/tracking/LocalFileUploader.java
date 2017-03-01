package com.ottamotta.pavement.tracking;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

import rx.Observable;


public class LocalFileUploader implements Uploader {

    private static final String FILE_NAME = "pavement.csv";
    private static final String HEADER = "lat,lon,speed,altitude,acceleration_array\n";

    @Override
    public Observable<UploadResult> upload(AccelerationsByLocation data) {
        return Observable.create(subscriber -> {
            try {
                File file = getFile();
                writeStrings(data.toCSV(), file);
                subscriber.onNext(new UploadResult(true, data.getKey()));
            } catch (IOException e) {
                subscriber.onNext(new UploadResult(false, null));
            }
        });
    }

    private File getFile() throws IOException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dataFile = new File(downloadsDir, FILE_NAME);
        if (!dataFile.exists()) {
            dataFile.createNewFile();
            writeStrings(Collections.singletonList(HEADER), dataFile);
        }
        return dataFile;
    }

    private void writeStrings(List<String> strings, File file) throws IOException {
        FileOutputStream os = new FileOutputStream(file, true);
        OutputStreamWriter writer = new OutputStreamWriter(os);
        try {
            for (String s : strings) {
                writer.append(s);
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            try {
                writer.close();
            } catch (Exception e2) {
                //ignore
            }
            throw e;
        }
    }
}

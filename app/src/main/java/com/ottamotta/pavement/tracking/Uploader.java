package com.ottamotta.pavement.tracking;

import rx.Observable;

public interface Uploader {

    public static class UploadResult {
        final boolean success;
        final String uploadedDataKey;

        public UploadResult(boolean success, String uploadedDataKey) {
            this.success = success;
            this.uploadedDataKey = uploadedDataKey;
        }
    }

    public Observable<UploadResult> upload(AccelerationsByLocation data);

}

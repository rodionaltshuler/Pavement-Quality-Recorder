package com.ottamotta.pavement.api.events;

public class LoadingStatusChangeEvent implements BaseEvent {

    private final boolean isLoading;

    public LoadingStatusChangeEvent(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isLoading() {
        return isLoading;
    }
}

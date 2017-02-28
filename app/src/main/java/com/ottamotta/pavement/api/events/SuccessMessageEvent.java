package com.ottamotta.pavement.api.events;


public class SuccessMessageEvent implements BaseEvent {

    private final String message;

    public SuccessMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

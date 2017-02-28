package com.ottamotta.pavement.api.events;


public class ErrorMessageEvent implements BaseEvent {

    private final String message;

    public ErrorMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

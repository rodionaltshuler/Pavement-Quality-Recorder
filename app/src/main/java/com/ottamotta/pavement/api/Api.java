package com.ottamotta.pavement.api;

import com.ottamotta.pavement.api.events.BaseEvent;

import rx.Observable;

public interface Api {

    void createAccount(String email, String password);

    void login(String email, String password);

    void sendEmailVerification();

    void signOut();

    boolean isLoggedIn();

    Observable<BaseEvent> eventsObservable();
}

package com.ottamotta.pavement.api;

import android.content.Context;
import android.support.annotation.StringRes;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ottamotta.pavement.R;
import com.ottamotta.pavement.api.events.BaseEvent;
import com.ottamotta.pavement.api.events.CreateAccountEvent;
import com.ottamotta.pavement.api.events.ErrorMessageEvent;
import com.ottamotta.pavement.api.events.LoadingStatusChangeEvent;
import com.ottamotta.pavement.api.events.SuccessMessageEvent;
import com.ottamotta.pavement.api.events.UserUpdateEvent;

import rx.Observable;
import rx.subjects.PublishSubject;

public class FirebaseApi implements Api {

    private Context context;

    private FirebaseApp app;

    private FirebaseAuth auth;

    private FirebaseDatabase database;

    private static FirebaseApi instance;

    private PublishSubject<BaseEvent> subject;

    public static synchronized FirebaseApi getInstance(Context context) {
        if (null == instance) {
            instance = new FirebaseApi(context);
        }
        return instance;
    }

    private FirebaseApi(Context context) {
        this.context = context;
        subject = PublishSubject.create();
        init();
    }

    @Override
    public Observable<BaseEvent> eventsObservable() {
        return subject;
    }

    private FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        notifyAuthStateChanged(user);
    };

    private void init() {
        app = FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(authStateListener);
    }

    private void notifyAuthStateChanged(FirebaseUser user) {
        subject.onNext(new UserUpdateEvent(new FirebaseUserWrapper(user)));
    }

    @Override
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    @Override
    public void createAccount(String email, String password) {
        notifyLoading(true);
        Task<AuthResult> authResultTask = auth.createUserWithEmailAndPassword(email, password);
        authResultTask.addOnCompleteListener(task -> {
            notifyLoading(false);
            if (!task.isSuccessful()) {
                notifyError(R.string.auth_failed);
            } else {
                subject.onNext(new CreateAccountEvent());
                login(email, password);
            }
        });
    }


    private void notifyError(@StringRes int resError) {
        notifyError(context.getString(resError));
    }

    private void notifySuccess(String message) {
        subject.onNext(new SuccessMessageEvent(message));
    }

    private void notifyError(String message) {
        subject.onNext(new ErrorMessageEvent(message));
    }

    @Override
    public void signOut() {
        auth.signOut();
    }

    @Override
    public void login(String email, String password) {
        notifyLoading(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    DatabaseReference ref = database.getReference("login");
                    ref.setValue(task.getResult().getUser().getEmail() + ":" + System.currentTimeMillis());
                    if (!task.isSuccessful()) {
                        notifyError(R.string.auth_failed);
                    }
                });
    }

    @Override
    public void sendEmailVerification() {
        if (auth.getCurrentUser() != null) {
            notifyLoading(true);
            auth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            notifySuccess("Verification email sent to " + auth.getCurrentUser().getEmail());
                        } else {
                            notifyError("Failed to send verification email.");
                        }
                    });
        } else {
            notifyError(R.string.auth_failed);
        }
    }

    private void notifyLoading(boolean loading) {
        subject.onNext(new LoadingStatusChangeEvent(loading));
    }


}

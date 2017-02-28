package com.ottamotta.pavement.api;

import com.google.firebase.auth.FirebaseUser;

public class FirebaseUserWrapper implements BaseUser {

    private final FirebaseUser firebaseUser;

    public FirebaseUserWrapper(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    @Override
    public String getName() {
        return firebaseUser.getDisplayName();
    }
}

package com.ottamotta.pavement.api.events;

import com.ottamotta.pavement.api.BaseUser;

public class UserUpdateEvent implements BaseEvent {

    private final BaseUser user;

    public UserUpdateEvent(BaseUser user) {
        this.user = user;
    }

    public BaseUser getUser() {
        return user;
    }
}

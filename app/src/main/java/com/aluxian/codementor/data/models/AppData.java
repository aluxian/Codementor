package com.aluxian.codementor.data.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class AppData {

    @JsonField(name = "firebase") FirebaseConfig firebaseConfig;
    @JsonField User user;

    public FirebaseConfig getFirebaseConfig() {
        return firebaseConfig;
    }

    public User getUser() {
        return user;
    }

}

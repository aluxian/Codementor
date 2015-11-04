package com.aluxian.codementor.data.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class FirebaseServerMessage {

    @JsonField String name;
    @JsonField FirebaseMessage message;

    public FirebaseServerMessage() {}

    public FirebaseServerMessage(String name, FirebaseMessage message) {
        this.name = name;
        this.message = message;
    }

}

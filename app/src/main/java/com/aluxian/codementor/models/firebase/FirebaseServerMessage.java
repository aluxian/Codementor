package com.aluxian.codementor.models.firebase;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FirebaseServerMessage {

    private String name;
    private FirebaseMessage message;

    public FirebaseServerMessage(String name, FirebaseMessage message) {
        this.name = name;
        this.message = message;
    }

}

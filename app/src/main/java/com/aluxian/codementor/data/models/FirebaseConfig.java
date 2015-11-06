package com.aluxian.codementor.data.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class FirebaseConfig {

    @JsonField String path;
    @JsonField String token;

    public String getPath() {
        return path;
    }

    public String getToken() {
        return token;
    }

}

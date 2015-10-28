package com.aluxian.codementor.data.models;

import com.google.common.base.Objects;

public class FirebaseServerMessage {

    private String name;
    private FirebaseMessage message;

    public FirebaseServerMessage(String name, FirebaseMessage message) {
        this.name = name;
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirebaseServerMessage)) return false;
        FirebaseServerMessage that = (FirebaseServerMessage) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, message);
    }

}

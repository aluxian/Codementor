package com.aluxian.codementor.data.models.firebase;

import com.aluxian.codementor.data.annotations.GsonModel;
import com.aluxian.codementor.data.annotations.JacksonModel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

@GsonModel
@JacksonModel
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
        return !(name != null ? !name.equals(that.name) : that.name != null) && !(message != null ? !message.equals
                (that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

}

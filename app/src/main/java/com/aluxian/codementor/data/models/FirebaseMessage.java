package com.aluxian.codementor.data.models;

import com.aluxian.codementor.data.annotations.GsonModel;
import com.aluxian.codementor.data.annotations.JacksonModel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@GsonModel
@JacksonModel
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FirebaseMessage {

    private @JsonProperty("chatroom_id") String chatroomId;
    private @JsonProperty("created_at") @SuppressWarnings("unused") Map<String, String> createdAt;
    private @JsonProperty("read_at") String readAt = null;

    private String id;
    private String type;
    private User sender;
    private User receiver;
    private String content;
    private Request request;

    public FirebaseMessage(String chatroomId, Message.Type type, String content,
                           Request request, User sender, User receiver) {
        Map<String, String> createdAt = new HashMap<>();
        createdAt.put(".sv", "timestamp");

        this.chatroomId = chatroomId;
        this.type = type.name().toLowerCase();

        this.createdAt = createdAt;
        this.content = content;
        this.request = request;

        this.sender = sender;
        this.receiver = receiver;

        this.id = UUID.randomUUID().toString();
    }

    public String getReadAt() {
        return readAt;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public Request getRequest() {
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirebaseMessage)) return false;

        FirebaseMessage that = (FirebaseMessage) o;
        return !(chatroomId != null ? !chatroomId.equals(that.chatroomId) : that.chatroomId != null) && !(id != null
                ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        int result = chatroomId != null ? chatroomId.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

}

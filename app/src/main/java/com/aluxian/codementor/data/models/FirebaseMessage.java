package com.aluxian.codementor.data.models;

import com.aluxian.codementor.data.types.MessageType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FirebaseMessage {

    private @JsonProperty("chatroom_id") String chatroomId;
    private @JsonProperty("created_at") Map<String, String> createdAt;
    private @JsonProperty("read_at") String readAt = null;

    private String id;
    private String type;
    private String content;
    private Request request;
    private User sender;
    private User receiver;

    public FirebaseMessage(String chatroomId, MessageType type, String content,
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

    public User getReceiver() {
        return receiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirebaseMessage)) return false;
        FirebaseMessage that = (FirebaseMessage) o;
        return Objects.equal(chatroomId, that.chatroomId) &&
                Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chatroomId, id);
    }

}

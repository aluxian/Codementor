package com.aluxian.codementor.data.models;

import com.aluxian.codementor.data.types.MessageType;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonObject
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class FirebaseMessage {

    @JsonField(name = "chatroom_id") @JsonProperty("chatroom_id") String chatroomId;
    @JsonField(name = "created_at") @JsonProperty("created_at") Map<String, String> createdAt;
    @JsonField(name = "read_at") @JsonProperty("read_at") String readAt;

    @JsonField String id;
    @JsonField String type;
    @JsonField String content;
    @JsonField Request request;
    @JsonField User sender;
    @JsonField User receiver;

    public FirebaseMessage() {}

    public FirebaseMessage(String chatroomId, MessageType type, String content,
                           Request request, User sender, User receiver) {
        Map<String, String> createdAt = new HashMap<>();
        createdAt.put(".sv", "timestamp");

        this.chatroomId = chatroomId;
        this.createdAt = createdAt;
        this.readAt = null;

        this.content = content;
        this.request = request;

        this.sender = sender;
        this.receiver = receiver;

        this.id = UUID.randomUUID().toString();
        this.type = type.name().toLowerCase();
    }

    public User getReceiver() {
        return receiver;
    }

}

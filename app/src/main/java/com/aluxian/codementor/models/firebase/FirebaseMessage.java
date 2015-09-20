package com.aluxian.codementor.models.firebase;

import com.aluxian.codementor.models.Message;
import com.aluxian.codementor.models.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FirebaseMessage {

    private String id = UUID.randomUUID().toString();
    @JsonProperty("chatroom_id")
    private String chatroomId;
    private String type;
    @JsonProperty("created_at")
    private Map<String, String> createdAt;
    @JsonProperty("read_at")
    private String readAt = null;
    private User sender;
    private User receiver;
    private String content;

    public FirebaseMessage(String chatroomId, Message.Type type, String content, User sender, User receiver) {
        Map<String, String> createdAt = new HashMap<>();
        createdAt.put(".sv", "timestamp");

        this.chatroomId = chatroomId;
        this.type = type.name().toLowerCase();

        this.createdAt = createdAt;
        this.content = content;

        this.sender = sender;
        this.receiver = receiver;
    }
}

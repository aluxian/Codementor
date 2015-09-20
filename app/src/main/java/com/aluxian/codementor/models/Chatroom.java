package com.aluxian.codementor.models;

import java.util.Date;

public class Chatroom {

    private String type;
    private float created_at;
    private float read_at;
    private String id;
    private String content;
    private User sender;
    private User receiver;
    private String chatroom_id;
    private String chatroom_firebase_id;

    public Chatroom() {}

    public String getType() {
        return type;
    }

    public Date getCreatedAt() {
        return new Date((long) created_at);
    }

    public Date getReadAt() {
        return new Date((long) read_at);
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getChatroomId() {
        return chatroom_id;
    }

    public String getChatroomFirebaseId() {
        return chatroom_firebase_id;
    }

    public User getOtherUser(String loggedInUsername) {
        if (sender.getUsername().equals(loggedInUsername)) {
            return receiver;
        } else {
            return sender;
        }
    }

}

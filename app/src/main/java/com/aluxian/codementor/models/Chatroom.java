package com.aluxian.codementor.models;

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

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public String getChatroomId() {
        return chatroom_id;
    }

    public User getOtherUser(String loggedInUsername) {
        if (sender.getUsername().equals(loggedInUsername)) {
            return receiver;
        } else {
            return sender;
        }
    }

    public String getFirebasePath() {
        return "chatrooms/" + chatroom_firebase_id + "/" + chatroom_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chatroom)) return false;

        Chatroom chatroom = (Chatroom) o;

        return id.equals(chatroom.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}

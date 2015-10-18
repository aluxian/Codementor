package com.aluxian.codementor.data.models;

import java.io.Serializable;

public class Chatroom implements Serializable {

    private ChatroomData chatroomData;
    private String loggedInUsername;

    public Chatroom(ChatroomData chatroomData, String loggedInUsername) {
        this.chatroomData = chatroomData;
        this.loggedInUsername = loggedInUsername;
    }

    public String getId() {
        return chatroomData.id;
    }

    public String getContent() {
        return chatroomData.content;
    }

    public User getSender() {
        return chatroomData.sender;
    }

    public User getReceiver() {
        return chatroomData.receiver;
    }

    public String getChatroomId() {
        return chatroomData.chatroom_id;
    }

    public User getCurrentUser() {
        if (getSender().getUsername().equals(loggedInUsername)) {
            return getSender();
        } else {
            return getReceiver();
        }
    }

    public User getOtherUser() {
        if (!getSender().getUsername().equals(loggedInUsername)) {
            return getSender();
        } else {
            return getReceiver();
        }
    }

    public boolean sentByCurrentUser() {
        return getSender().equals(getCurrentUser());
    }

    public String getFirebasePath() {
        return "chatrooms/" + chatroomData.chatroom_firebase_id + "/" + chatroomData.chatroom_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chatroom)) return false;

        Chatroom chatroom = (Chatroom) o;
        return !(chatroomData != null ? !chatroomData.equals(chatroom.chatroomData) : chatroom.chatroomData != null)
                && !(loggedInUsername != null ? !loggedInUsername.equals(chatroom.loggedInUsername) : chatroom
                .loggedInUsername != null);

    }

    @Override
    public int hashCode() {
        int result = chatroomData != null ? chatroomData.hashCode() : 0;
        result = 31 * result + (loggedInUsername != null ? loggedInUsername.hashCode() : 0);
        return result;
    }

}
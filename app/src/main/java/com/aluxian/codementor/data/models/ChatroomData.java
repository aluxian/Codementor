package com.aluxian.codementor.data.models;

import com.google.common.base.Objects;

import java.io.Serializable;

public class ChatroomData implements Serializable {

    private String id;
    private String type;
    private String content;
    private Request request;

    private User sender;
    private User receiver;

    private String chatroom_id;
    private String chatroom_firebase_id;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Request getRequest() {
        return request;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatroomData)) return false;
        ChatroomData that = (ChatroomData) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}

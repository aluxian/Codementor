package com.aluxian.codementor.data.models;

import java.io.Serializable;

public class ChatroomData implements Serializable {

    public final String type;
    public final float created_at;
    public final float read_at;
    public final String id;
    public final String content;
    public final User sender;
    public final User receiver;
    public final String chatroom_id;
    public final String chatroom_firebase_id;

    public ChatroomData(String type, float created_at, float read_at, String id, String content, User sender,
                        User receiver, String chatroom_id, String chatroom_firebase_id) {
        this.type = type;
        this.created_at = created_at;
        this.read_at = read_at;
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.chatroom_id = chatroom_id;
        this.chatroom_firebase_id = chatroom_firebase_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatroomData)) return false;

        ChatroomData data = (ChatroomData) o;
        return !(id != null ? !id.equals(data.id) : data.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}

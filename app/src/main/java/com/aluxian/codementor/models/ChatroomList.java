package com.aluxian.codementor.models;

import java.util.List;

public class ChatroomList {

    private List<Chatroom> recent_chats;

    public ChatroomList() {}

    public List<Chatroom> getRecentChats() {
        return recent_chats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatroomList)) return false;

        ChatroomList that = (ChatroomList) o;

        return recent_chats.equals(that.recent_chats);

    }

    @Override
    public int hashCode() {
        return recent_chats.hashCode();
    }

}

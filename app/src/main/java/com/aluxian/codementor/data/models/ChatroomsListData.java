package com.aluxian.codementor.data.models;

import com.google.common.base.Objects;

import java.util.List;

public class ChatroomsListData {

    private List<ChatroomData> recent_chats;

    public ChatroomsListData(List<ChatroomData> recent_chats) {
        this.recent_chats = recent_chats;
    }

    public List<ChatroomData> getRecentChats() {
        return recent_chats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatroomsListData)) return false;
        ChatroomsListData that = (ChatroomsListData) o;
        return Objects.equal(recent_chats, that.recent_chats);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(recent_chats);
    }

}

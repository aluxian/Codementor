package com.aluxian.codementor.data.models;

import java.util.List;

public class ChatroomsListData {

    public final List<ChatroomData> recent_chats;

    public ChatroomsListData(List<ChatroomData> recent_chats) {
        this.recent_chats = recent_chats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatroomsListData)) return false;

        ChatroomsListData chatroomsListData = (ChatroomsListData) o;
        return !(recent_chats != null ? !recent_chats.equals(chatroomsListData.recent_chats) : chatroomsListData
                .recent_chats != null);

    }

    @Override
    public int hashCode() {
        return recent_chats != null ? recent_chats.hashCode() : 0;
    }

}

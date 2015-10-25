package com.aluxian.codementor.data.models;

import java.util.ArrayList;
import java.util.List;

public class ChatroomsList {

    private ChatroomsListData chatroomsListData;
    private List<Chatroom> recentChats;
    private String loggedInUsername;

    public ChatroomsList(ChatroomsListData chatroomsListData, String loggedInUsername) {
        this.chatroomsListData = chatroomsListData;
        this.loggedInUsername = loggedInUsername;
        recentChats = dataToChatrooms(chatroomsListData.recent_chats);
    }

    public List<Chatroom> getRecentChats() {
        return recentChats;
    }

    private List<Chatroom> dataToChatrooms(List<ChatroomData> dataList) {
        List<Chatroom> chatrooms = new ArrayList<>();

        //noinspection Convert2streamapi
        for (ChatroomData data : dataList) {
            chatrooms.add(new Chatroom(data, loggedInUsername));
        }

        return chatrooms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatroomsList)) return false;

        ChatroomsList that = (ChatroomsList) o;
        return !(chatroomsListData != null ? !chatroomsListData.equals(that.chatroomsListData) : that
                .chatroomsListData != null) && !(loggedInUsername != null ?
                !loggedInUsername.equals(that.loggedInUsername) : that.loggedInUsername != null);

    }

    @Override
    public int hashCode() {
        int result = chatroomsListData != null ? chatroomsListData.hashCode() : 0;
        result = 31 * result + (loggedInUsername != null ? loggedInUsername.hashCode() : 0);
        return result;
    }

}

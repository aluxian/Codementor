package com.aluxian.codementor.data.models;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;

public class ChatroomsList {

    private List<Chatroom> recentChats;
    private String loggedInUsername;

    public ChatroomsList(ChatroomsListData chatroomsListData, String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        this.recentChats = Lists.transform(
                chatroomsListData.getRecentChats(),
                data -> new Chatroom(data, loggedInUsername)
        );
    }

    public List<Chatroom> getRecentChats() {
        return recentChats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatroomsList)) return false;
        ChatroomsList that = (ChatroomsList) o;
        return Objects.equal(recentChats, that.recentChats) &&
                Objects.equal(loggedInUsername, that.loggedInUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(recentChats, loggedInUsername);
    }

}

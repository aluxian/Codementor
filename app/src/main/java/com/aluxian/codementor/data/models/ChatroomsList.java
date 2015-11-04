package com.aluxian.codementor.data.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

@JsonObject
public class ChatroomsList {

    @JsonField(name = "recent_chats") List<Chatroom> recentChats;

    public List<Chatroom> getRecentChats() {
        return recentChats;
    }

}

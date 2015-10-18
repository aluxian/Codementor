package com.aluxian.codementor.data.events;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.Message;

public class NewMessageEvent {

    private Chatroom chatroom;
    private Message message;

    public NewMessageEvent(Chatroom chatroom, Message message) {
        this.chatroom = chatroom;
        this.message = message;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public Message getMessage() {
        return message;
    }

}

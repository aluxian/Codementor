package com.aluxian.codementor.presentation.listeners;

import com.aluxian.codementor.data.models.Chatroom;

public interface ChatroomSelectedListener {

    /**
     * Called when a Chatroom entry is selected by the user.
     *
     * @param chatroom The Chatroom object.
     */
    void onChatroomSelected(Chatroom chatroom);

}

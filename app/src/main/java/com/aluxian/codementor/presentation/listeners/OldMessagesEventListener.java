package com.aluxian.codementor.presentation.listeners;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.services.CoreServices;
import com.firebase.client.Firebase;
import com.firebase.client.Query;

import java.util.TreeSet;

import static com.aluxian.codementor.presentation.presenters.ConversationPresenter.BATCH_SIZE;
import static com.aluxian.codementor.presentation.presenters.ConversationPresenter.CREATED_AT;

public class OldMessagesEventListener extends MessagesEventListener {

    private ConversationView conversationView;
    private ConversationAdapter conversationAdapter;
    private Chatroom chatroom;

    public OldMessagesEventListener(ConversationView conversationView, ConversationAdapter conversationAdapter,
                                    Chatroom chatroom, CoreServices coreServices) {
        super(coreServices);
        this.conversationView = conversationView;
        this.conversationAdapter = conversationAdapter;
        this.chatroom = chatroom;
    }

    @Override
    protected Query createQuery(Firebase firebase) {
        return firebase.child(chatroom.getFirebasePath())
                .orderByChild(CREATED_AT).limitToLast(BATCH_SIZE)
                .endAt(conversationAdapter.getOldestMessage().getTimestamp() - 1);
    }

    @Override
    protected void onMessages(TreeSet<Message> messages) {
        conversationAdapter.addOldMessages(messages);
        conversationView.setRefreshing(false);

        if (messages.size() < BATCH_SIZE) {
            conversationView.setAllMessagesLoaded(true);
        }
    }

}

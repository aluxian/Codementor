package com.aluxian.codementor.presentation.listeners;

import android.content.Intent;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.Message;
import com.aluxian.codementor.presentation.adapters.ConversationAdapter;
import com.aluxian.codementor.presentation.presenters.ChatroomsPresenter;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.services.CoreServices;
import com.firebase.client.Firebase;
import com.firebase.client.Query;

import java.util.TreeSet;

import static com.aluxian.codementor.presentation.presenters.ConversationPresenter.BATCH_SIZE_INITIAL;

public class NewMessagesEventListener extends MessagesEventListener {

    private ConversationView conversationView;
    private ConversationAdapter conversationAdapter;
    private Chatroom chatroom;

    public NewMessagesEventListener(ConversationView conversationView, ConversationAdapter conversationAdapter,
                                    Chatroom chatroom, CoreServices coreServices) {
        super(coreServices);
        this.conversationView = conversationView;
        this.conversationAdapter = conversationAdapter;
        this.chatroom = chatroom;
    }

    @Override
    protected Query createQuery(Firebase firebase) {
        return firebase.child(chatroom.getFirebasePath()).limitToLast(BATCH_SIZE_INITIAL);
    }

    @Override
    protected void set(Query query) {
        query.addValueEventListener(this);
    }

    @Override
    protected void unset(Query query) {
        query.removeEventListener(this);
    }

    @Override
    protected void onMessages(TreeSet<Message> messages) {
        if (messages.size() > 0) {
            if (conversationAdapter.getItemCount() > 0) {
                Intent intent = new Intent(ChatroomsPresenter.ACTION_NEW_MESSAGE);
                intent.putExtra(ChatroomsPresenter.EXTRA_CHATROOM, new Chatroom(chatroom));
                intent.putExtra(ChatroomsPresenter.EXTRA_MESSAGE, messages.last());
                localBroadcastManager.sendBroadcast(intent);
            }

            conversationAdapter.addNewMessages(messages);
        }

        if (messages.size() < BATCH_SIZE_INITIAL) {
            conversationView.setAllMessagesLoaded(true);
        }

        conversationView.setRefreshing(false);
        conversationView.showEmptyState(conversationAdapter.getItemCount() == 0);
    }

}

package com.aluxian.codementor.presentation.listeners;

import android.support.annotation.Nullable;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.presentation.views.ConversationView;
import com.aluxian.codementor.services.CoreServices;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

public class PresenceEventListener extends QueryEventListener implements ValueEventListener {

    private ConversationView conversationView;
    private Chatroom chatroom;

    public PresenceEventListener(ConversationView conversationView, Chatroom chatroom, CoreServices coreServices) {
        super(coreServices);
        this.conversationView = conversationView;
        this.chatroom = chatroom;
    }

    @Override
    protected Query createQuery(Firebase firebase) {
        return firebase.child(chatroom.getOtherUser().getPresencePath());
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
    public void onDataChange(DataSnapshot dataSnapshot) {
        updateStatus(dataSnapshot.getValue(String.class));
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        super.onCancelled(firebaseError);
        updateStatus(null);
    }

    private void updateStatus(@Nullable String status) {
        if (status == null) {
            conversationView.setSubtitle(null);
            return;
        }

        conversationView.setSubtitle(PresenceType.parse(status).statusResId);
    }

}

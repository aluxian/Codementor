package com.aluxian.codementor.presentation.holders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.User;
import com.aluxian.codementor.data.types.PresenceType;
import com.aluxian.codementor.presentation.listeners.QueryEventListener;
import com.aluxian.codementor.services.CoreServices;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatroomItemViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.img_avatar) SimpleDraweeView avatarImageView;
    @Bind(R.id.tv_subtitle) TextView subtitleTextView;
    @Bind(R.id.tv_title) TextView titleTextView;
    @Bind(R.id.view_presence) View presenceView;

    private Chatroom currentChatroom;
    private PresenceEventListener presenceListener;
    private CoreServices coreServices;

    public ChatroomItemViewHolder(View itemView, CoreServices coreServices) {
        super(itemView);
        this.coreServices = coreServices;
        ButterKnife.bind(this, itemView);
    }

    public void bindChatroom(Chatroom chatroom) {
        if (currentChatroom != null && currentChatroom.contentEquals(chatroom)) {
            return;
        }

        setText(chatroom);
        setAvatar(chatroom);
        setPresenceListener(chatroom);

        currentChatroom = chatroom;
    }

    public void recycle() {
        currentChatroom = null;
        if (presenceListener != null) {
            presenceListener.stop();
            presenceListener = null;
        }
    }

    private void setText(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser();
        titleTextView.setText(otherUser.getName());
        subtitleTextView.setText(Html.fromHtml(chatroom.getContentDescription()));
    }

    private void setAvatar(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser();
        Uri avatarUri = Uri.parse(otherUser.getAvatarUrl());
        avatarImageView.setImageURI(avatarUri);
    }

    private void setPresenceListener(Chatroom chatroom) {
        if (currentChatroom != null && currentChatroom.getOtherUser().equals(chatroom.getOtherUser())) {
            return;
        }

        if (presenceListener != null) {
            presenceListener.stop();
        }

        presenceView.setBackgroundResource(PresenceType.OFFLINE.backgroundResId);
        presenceListener = new PresenceEventListener(presenceView, chatroom, coreServices);
        presenceListener.start();
    }

    private static class PresenceEventListener extends QueryEventListener implements ValueEventListener {

        private View presenceView;
        private Chatroom chatroom;

        private PresenceEventListener(View presenceView, Chatroom chatroom, CoreServices coreServices) {
            super(coreServices);
            this.presenceView = presenceView;
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
            PresenceType presenceType = PresenceType.parse(dataSnapshot.getValue(String.class));
            presenceView.setBackgroundResource(presenceType.backgroundResId);
            chatroom.getOtherUser().setPresenceType(presenceType);
        }

    }

}

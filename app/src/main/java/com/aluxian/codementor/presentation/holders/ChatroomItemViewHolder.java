package com.aluxian.codementor.presentation.holders;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
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

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatroomItemViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.img_avatar) SimpleDraweeView avatarImageView;
    @Bind(R.id.tv_subtitle) TextView subtitleTextView;
    @Bind(R.id.tv_title) TextView titleTextView;
    @Bind(R.id.view_presence) View presenceView;

    private Context context;
    private Chatroom currentChatroom;
    private CoreServices coreServices;
    private PresenceEventListener presenceListener;
    private ValueAnimator presenceAnimator;
    private int presenceColour;

    public ChatroomItemViewHolder(View itemView, CoreServices coreServices) {
        super(itemView);
        this.context = itemView.getContext();
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

        itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v) {
                itemView.removeOnAttachStateChangeListener(this);
                recycle();
            }
        });
    }

    public void recycle() {
        if (presenceListener != null) {
            presenceListener.stop();
            presenceListener = null;
        }

        if (presenceAnimator != null) {
            presenceAnimator.cancel();
            presenceAnimator = null;
        }

        currentChatroom = null;
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

        recycle();
        currentChatroom = chatroom;

        presenceColour = PresenceType.OFFLINE.getColor(context);
        presenceView.setBackgroundColor(presenceColour);

        presenceListener = new PresenceEventListener(chatroom, this::onPresenceType, coreServices);
        presenceListener.start();
    }

    private void onPresenceType(PresenceType presenceType) {
        if (currentChatroom != null) {
            currentChatroom.getOtherUser().setPresenceType(presenceType);
        }

        if (presenceAnimator != null) {
            presenceAnimator.cancel();
        }

        Integer colorFrom = presenceColour;
        Integer colorTo = presenceType.getColor(context);

        presenceAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        presenceAnimator.addUpdateListener(this::onPresenceColor);
        presenceAnimator.setDuration(250);
        presenceAnimator.start();
    }

    private void onPresenceColor(ValueAnimator animator) {
        presenceView.setBackgroundColor((Integer) animator.getAnimatedValue());
    }

    private static class PresenceEventListener extends QueryEventListener {

        private Chatroom chatroom;
        private PresenceCallback callback;

        private PresenceEventListener(Chatroom chatroom, PresenceCallback callback, CoreServices coreServices) {
            super(coreServices);
            this.chatroom = chatroom;
            this.callback = callback;
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
            callback.update(PresenceType.parse(dataSnapshot.getValue(String.class)));
        }

        public interface PresenceCallback {

            void update(PresenceType presenceType);

        }

    }

}

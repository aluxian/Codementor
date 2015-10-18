package com.aluxian.codementor.presentation.holders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.User;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatroomViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.avatar) SimpleDraweeView avatarImageView;
    @Bind(R.id.subtitle) TextView subtitleTextView;
    @Bind(R.id.title) TextView titleTextView;

    public ChatroomViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void loadChatroom(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser();
        titleTextView.setText(otherUser.getName());

        if (chatroom.sentByCurrentUser()) {
            String content = "You: " + chatroom.getContent();
            subtitleTextView.setText(content);
        } else {
            subtitleTextView.setText(chatroom.getContent());
        }

        Uri avatarUri = Uri.parse(otherUser.getAvatarUrl());
        avatarImageView.setImageURI(avatarUri);
    }

}

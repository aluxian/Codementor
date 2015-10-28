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
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatroomItemViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.img_avatar) SimpleDraweeView avatarImageView;
    @Bind(R.id.tv_subtitle) TextView subtitleTextView;
    @Bind(R.id.tv_title) TextView titleTextView;
    @Bind(R.id.view_presence) View presenceView;

    public ChatroomItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindChatroom(Chatroom chatroom) {
        setText(chatroom);
        setAvatar(chatroom);
        setPresence(chatroom);
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

    private void setPresence(Chatroom chatroom) {
        PresenceType presenceType = chatroom.getOtherUser().getPresenceType();
        presenceView.setBackgroundResource(presenceType.backgroundResId);
    }

}

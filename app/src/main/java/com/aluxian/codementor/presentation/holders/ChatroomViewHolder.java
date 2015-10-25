package com.aluxian.codementor.presentation.holders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.data.models.User;
import com.aluxian.codementor.data.tasks.FirebaseTasks;
import com.aluxian.codementor.data.tasks.TaskContinuations;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatroomViewHolder extends RecyclerView.ViewHolder {

    private FirebaseTasks firebaseTasks;
    private TaskContinuations taskContinuations;
    private User currentUser;

    @Bind(R.id.img_avatar) SimpleDraweeView avatarImageView;
    @Bind(R.id.tv_subtitle) TextView subtitleTextView;
    @Bind(R.id.tv_title) TextView titleTextView;
    @Bind(R.id.view_presence) View presenceView;

    public ChatroomViewHolder(View itemView, CoreServices coreServices) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        firebaseTasks = coreServices.getFirebaseTasks();
        taskContinuations = coreServices.getTaskContinuations();
    }

    public void loadChatroom(Chatroom chatroom) {
        currentUser = chatroom.getOtherUser();
        setText(chatroom);
        setAvatar(chatroom);
        updatePresence(chatroom.getOtherUser());
    }

    private void setText(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser();
        titleTextView.setText(otherUser.getName());

        if (chatroom.sentByCurrentUser()) {
            String content = "You: " + chatroom.getContent();
            subtitleTextView.setText(content);
        } else {
            subtitleTextView.setText(chatroom.getContent());
        }
    }

    private void setAvatar(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser();
        Uri avatarUri = Uri.parse(otherUser.getAvatarUrl());
        avatarImageView.setImageURI(avatarUri);
    }

    private void updatePresence(User forUser) {
        presenceView.setBackgroundResource(R.drawable.presence_offline);
        firebaseTasks.getPresence(forUser.getUsername())
                .onSuccess(task -> {
                    if (!forUser.equals(currentUser)) {
                        return null;
                    }

                    int presenceResId = task.getResult().presenceResId;
                    presenceView.setBackgroundResource(presenceResId);

                    return null;
                })
                .continueWith(taskContinuations.logAndToastError());
    }

}

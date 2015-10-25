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
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.utils.Constants;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatroomViewHolder extends RecyclerView.ViewHolder {

    private ErrorHandler errorHandler;
    private Firebase firebaseRef;
    private Firebase presenceRef;

    @Bind(R.id.img_avatar) SimpleDraweeView avatarImageView;
    @Bind(R.id.tv_subtitle) TextView subtitleTextView;
    @Bind(R.id.tv_title) TextView titleTextView;
    @Bind(R.id.view_presence) View presenceView;

    public ChatroomViewHolder(View itemView, CoreServices coreServices) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        errorHandler = coreServices.getErrorHandler();
        firebaseRef = coreServices.getFirebaseRef();
    }

    public void loadChatroom(Chatroom chatroom) {
        setText(chatroom);
        setAvatar(chatroom);
        setPresenceListener(chatroom);
    }

    private void setText(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser();
        titleTextView.setText(otherUser.getName());
        subtitleTextView.setText(Html.fromHtml(chatroom.getTypeContent()));
    }

    private void setAvatar(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser();
        Uri avatarUri = Uri.parse(otherUser.getAvatarUrl());
        avatarImageView.setImageURI(avatarUri);
    }

    private void setPresenceListener(Chatroom chatroom) {
        if (presenceRef != null) {
            presenceRef.removeEventListener(presenceListener);
        }

        String otherUsername = chatroom.getOtherUser().getUsername();
        presenceRef = firebaseRef.child(Constants.getPresencePath(otherUsername));
        presenceRef.addValueEventListener(presenceListener);
    }

    private ValueEventListener presenceListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String status = dataSnapshot.getValue(String.class);
            PresenceType presenceType = PresenceType.parse(status);
            presenceView.setBackgroundResource(presenceType.backgroundResId);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            errorHandler.logAndToast(firebaseError.toException());
        }
    };

}

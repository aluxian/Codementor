package com.aluxian.codementor.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.models.User;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ChatroomEntryView extends LinearLayout {

    private App app;

    @Bind(R.id.avatar) SimpleDraweeView avatarImageView;
    @Bind(R.id.subtitle) TextView subtitleTextView;
    @Bind(R.id.title) TextView titleTextView;

    public ChatroomEntryView(Context context) {
        super(context);
        init();
    }

    public ChatroomEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatroomEntryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        initLayout();
        inflate(getContext(), R.layout.view_chatroom_entry, this);
        ButterKnife.bind(this);
    }

    private void initLayout() {
        setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        int dp12Padding = dpToPx(12);
        int dp16Padding = dpToPx(16);
        setPadding(dp16Padding, dp12Padding, dp16Padding, dp12Padding);

        int[] attrsRef = new int[]{android.R.attr.selectableItemBackground};
        TypedArray attrs = getContext().obtainStyledAttributes(attrsRef);
        setBackgroundResource(attrs.getResourceId(0, 0));
        attrs.recycle();
    }

    public void setApp(App app) {
        this.app = app;
    }

    public void loadChatroom(Chatroom chatroom) {
        User otherUser = chatroom.getOtherUser(app.getUserManager().getUsername());

        titleTextView.setText(otherUser.getName());

        if (chatroom.getSender().getUsername().equals(app.getUserManager().getUsername())) {
            String content = "You: " + chatroom.getContent();
            subtitleTextView.setText(content);
        } else {
            subtitleTextView.setText(chatroom.getContent());
        }

        Uri avatarUri = Uri.parse(otherUser.getAvatarUrl());
        avatarImageView.setImageURI(avatarUri);
    }

    private int dpToPx(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
        return Math.round(pixels);
    }

}

package com.aluxian.codementor.presentation.holders;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Message;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.card) CardView cardView;
    @Bind(R.id.container) LinearLayout linearLayout;
    @Bind(R.id.message) TextView messageTextView;

    protected int senderChatTextColor;
    protected int senderChatBackgroundColor;
    protected int defaultDarkTextColor;

    public MessageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, itemView);

        senderChatTextColor = ContextCompat.getColor(itemView.getContext(), R.color.sender_text);
        senderChatBackgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.sender_background);
        defaultDarkTextColor = messageTextView.getCurrentTextColor();
    }

    public void loadMessage(Message message) {
        boolean alignRight = message.sentByCurrentUser();
        String body = message.getTypeContent();

        adjustGravity(alignRight);
        adjustColors(alignRight);

        messageTextView.setText(body);
        messageTextView.requestLayout();
    }

    protected void adjustGravity(boolean alignRight) {
        int offset = itemView.getResources().getDimensionPixelSize(R.dimen.message_offset);
        linearLayout.setGravity(alignRight ? Gravity.END : Gravity.START);

        if (alignRight) {
            messageTextView.setGravity(Gravity.END);
            linearLayout.setPadding(offset, 0, 0, 0);
        } else {
            messageTextView.setGravity(Gravity.START);
            linearLayout.setPadding(0, 0, offset, 0);
        }
    }

    protected void adjustColors(boolean alignRight) {
        View cardChild = cardView.getChildAt(0);

        if (alignRight) {
            cardChild.setBackgroundColor(senderChatBackgroundColor);
            messageTextView.setTextColor(senderChatTextColor);
        } else {
            cardChild.setBackgroundColor(Color.WHITE);
            messageTextView.setTextColor(defaultDarkTextColor);
        }
    }

}

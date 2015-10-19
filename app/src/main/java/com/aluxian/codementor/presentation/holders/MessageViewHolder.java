package com.aluxian.codementor.presentation.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Message;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_message) TextView messageTextView;

    public MessageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, itemView);
    }

    public void loadMessage(Message message) {
        messageTextView.setText(message.getTypeContent());
        messageTextView.requestLayout();
    }

}

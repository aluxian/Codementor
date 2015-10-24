package com.aluxian.codementor.presentation.holders;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Message;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.text.format.DateUtils.FORMAT_SHOW_TIME;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_message) TextView messageTextView;
    @Bind(R.id.tv_subtext) TextView subtextView;

    public MessageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, itemView);
    }

    public void loadMessage(Message message) {
        String time = DateUtils.formatDateTime(itemView.getContext(), message.getCreatedAt(), FORMAT_SHOW_TIME);
        subtextView.setText(time);
        messageTextView.setText(message.getTypeContent());
        messageTextView.requestLayout();
    }

}

package com.aluxian.codementor.presentation.holders;

import android.text.Html;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Message;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FileMessageViewHolder extends MessageViewHolder {

    @Bind(R.id.tv_subtext) TextView subtextView;

    public FileMessageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, itemView);
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void loadMessage(Message message) {
        boolean alignRight = message.sentByCurrentUser();
        String body = message.getTypeContent();

        adjustGravity(alignRight);
        adjustColors(alignRight);

        bindFileMessage(message, body, alignRight);
        messageTextView.requestLayout();
    }

    private void bindFileMessage(Message message, String body, boolean alignRight) {
        Spanned htmlBody = Html.fromHtml(body);
        messageTextView.setText(htmlBody);

        String size = Formatter.formatShortFileSize(itemView.getContext(), message.getRequest().getSize());
        subtextView.setText(size);

        if (alignRight) {
            subtextView.setGravity(Gravity.END);
            subtextView.setTextColor(senderChatTextColor);
        } else {
            subtextView.setGravity(Gravity.START);
            subtextView.setTextColor(defaultDarkTextColor);
        }
    }

}

package com.aluxian.codementor.presentation.holders;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Message;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.text.format.DateUtils.FORMAT_SHOW_TIME;

public class HtmlMessageViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_message) TextView messageTextView;
    @Bind(R.id.tv_subtext) TextView subtextView;

    public HtmlMessageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, itemView);
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void loadMessage(Message message, boolean lastSentMessage) {
        setText(message);
        setSubtext(message, lastSentMessage);
        itemView.requestLayout();
    }

    private void setText(Message message) {
        Spanned htmlBody = Html.fromHtml(message.getTypeContent());
        messageTextView.setText(htmlBody);
    }

    private void setSubtext(Message message, boolean lastSentMessage) {
        String time = DateUtils.formatDateTime(itemView.getContext(), message.getCreatedAt(), FORMAT_SHOW_TIME);
        String sizeText = "";

        long size = message.getRequest().getSize();
        if (size > 0) {
            sizeText = " " + Formatter.formatShortFileSize(itemView.getContext(), size);
        }

        String subtext = time + sizeText;

        if (lastSentMessage && message.sentByCurrentUser() && message.hasBeenRead()) {
            subtext += "  SEEN";
        }

        subtextView.setText(subtext);
    }

}

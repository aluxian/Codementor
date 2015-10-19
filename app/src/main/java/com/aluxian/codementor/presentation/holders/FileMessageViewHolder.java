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

public class FileMessageViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_message) TextView messageTextView;
    @Bind(R.id.tv_timeSubText) TextView timeSubtextView;
    @Bind(R.id.tv_sizeSubText) TextView sizeSubtextView;

    public FileMessageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, itemView);
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void loadMessage(Message message) {
        int flags = DateUtils.FORMAT_SHOW_DATE;
        String time = DateUtils.formatDateTime(itemView.getContext(), message.getCreatedAt(), flags);
        timeSubtextView.setText(time);

        String size = Formatter.formatShortFileSize(itemView.getContext(), message.getRequest().getSize());
        sizeSubtextView.setText(size);

        Spanned htmlBody = Html.fromHtml(message.getTypeContent());
        messageTextView.setText(htmlBody);
        messageTextView.requestLayout();
    }

}

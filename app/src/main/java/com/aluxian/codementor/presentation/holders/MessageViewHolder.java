package com.aluxian.codementor.presentation.holders;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.ConversationItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessageViewHolder extends ConversationItemViewHolder {

    @Bind(R.id.tv_message) TextView messageTextView;
    @Bind(R.id.tv_subtext) TextView subtextView;

    public MessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(ConversationItem item, boolean newest) {
        if (item.isHtmlText()) {
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
            messageTextView.setText(Html.fromHtml(item.getText()));
        } else {
            messageTextView.setMovementMethod(null);
            messageTextView.setText(item.getText());
        }

        subtextView.setText(item.getSubtext(itemView.getContext(), newest));
        itemView.requestLayout();
    }

}

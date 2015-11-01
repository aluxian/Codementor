package com.aluxian.codementor.presentation.holders;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.ConversationItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessageViewHolder extends ConversationItemViewHolder {

    @Bind(R.id.tv_message) TextView messageTextView;
    @Bind(R.id.tv_subtext) TextView subtextView;
    @Nullable @Bind(R.id.img_check_2) ImageView checkMark2;

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

        if (checkMark2 != null) {
            checkMark2.setVisibility(item.showSeen() ? View.VISIBLE : View.INVISIBLE);
        }

        subtextView.setText(item.getSubtext(itemView.getContext()));
        itemView.requestLayout();
    }

}

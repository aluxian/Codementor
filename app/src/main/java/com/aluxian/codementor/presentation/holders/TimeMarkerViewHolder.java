package com.aluxian.codementor.presentation.holders;

import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.ConversationItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimeMarkerViewHolder extends ConversationItemViewHolder {

    @Bind(R.id.tv_timestamp) TextView timestampTextView;

    public TimeMarkerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(ConversationItem item, boolean newest) {
        timestampTextView.setText(item.getSubtext());
    }

}

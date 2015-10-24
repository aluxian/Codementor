package com.aluxian.codementor.presentation.holders;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.TimeMarker;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimeMarkerViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_timestamp) TextView timestampTextView;

    public TimeMarkerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void loadTimeMarker(TimeMarker timeMarker) {
        int flags = DateUtils.FORMAT_SHOW_DATE;
        String date = DateUtils.formatDateTime(itemView.getContext(), timeMarker.getTimestamp(), flags);
        timestampTextView.setText(date);
    }

}

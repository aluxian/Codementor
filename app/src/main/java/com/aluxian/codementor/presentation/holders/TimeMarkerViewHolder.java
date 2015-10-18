package com.aluxian.codementor.presentation.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.TimeMarker;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimeMarkerViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.timestamp) RelativeTimeTextView timestampTextView;

    public TimeMarkerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void loadTimeMarker(TimeMarker timeMarker) {
        timestampTextView.setReferenceTime(timeMarker.getTimestamp());
    }

}

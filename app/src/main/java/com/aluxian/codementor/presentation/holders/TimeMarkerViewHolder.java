package com.aluxian.codementor.presentation.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aluxian.codementor.data.models.TimeMarker;

import butterknife.ButterKnife;

public class TimeMarkerViewHolder extends RecyclerView.ViewHolder {

//    @Bind(R.id.tv_timestamp) TextView timestampTextView;

    public TimeMarkerViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void loadTimeMarker(TimeMarker timeMarker) {
        //timestampTextView.setReferenceTime(timeMarker.getTimestamp());
    }

}

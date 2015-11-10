package com.aluxian.codementor.data.models;

import com.aluxian.codementor.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeMarker extends ConversationItem {

    private final long timestamp;
    private String subtext;

    public TimeMarker(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getId() {
        return timestamp;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_time_marker;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getSubtext() {
        if (subtext == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.US);
            subtext = dateFormat.format(new Date(timestamp));
        }

        return subtext;
    }

}

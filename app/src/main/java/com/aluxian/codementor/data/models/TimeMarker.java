package com.aluxian.codementor.data.models;

import android.content.Context;
import android.support.annotation.Nullable;

import com.aluxian.codementor.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeMarker extends ConversationItem {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM", Locale.US);

    private final long timestamp;

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
    protected String generateSubtext(@Nullable Context context) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

}

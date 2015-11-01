package com.aluxian.codementor.data.models;

import android.content.Context;
import android.text.format.DateUtils;

import com.aluxian.codementor.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeMarker extends ConversationItem {

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
    protected String generateSubtext() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timestamp));

        String day = calendar.getDisplayName(Calendar.DAY_OF_MONTH, Calendar.SHORT, Locale.US);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);

        return day + " " + month;
    }

    @Override
    protected String generateSubtext(Context context) {
        return DateUtils.formatDateTime(context, getTimestamp(), DateUtils.FORMAT_SHOW_DATE);
    }

}

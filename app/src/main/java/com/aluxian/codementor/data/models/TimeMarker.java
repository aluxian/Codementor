package com.aluxian.codementor.data.models;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.aluxian.codementor.R;

import java.util.Calendar;
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
    public String getSubtext(@Nullable Context context, boolean showSeen) {
        if (subtext == null) {
            subtext = super.getSubtext(context, showSeen);
        }

        return subtext;
    }

    @Override
    protected String generateSubtext(Context context, boolean showSeen) {
        return DateUtils.formatDateTime(context, getTimestamp(), DateUtils.FORMAT_SHOW_DATE);
    }

    @Override
    protected String generateSubtext(boolean showSeen) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timestamp));

        String day = calendar.getDisplayName(Calendar.DAY_OF_MONTH, Calendar.SHORT, Locale.US);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);

        return day + " " + month;
    }

}

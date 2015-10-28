package com.aluxian.codementor.data.models;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.aluxian.codementor.R;
import com.google.common.base.Objects;

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
    public String getSubtext(@Nullable Context context, boolean showSeen) {
        if (context != null) {
            return DateUtils.formatDateTime(context, getTimestamp(), DateUtils.FORMAT_SHOW_DATE);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(timestamp));

            String day = calendar.getDisplayName(Calendar.DAY_OF_MONTH, Calendar.SHORT, Locale.US);
            String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);

            return day + " " + month;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeMarker)) return false;
        TimeMarker that = (TimeMarker) o;
        return Objects.equal(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(timestamp);
    }

}

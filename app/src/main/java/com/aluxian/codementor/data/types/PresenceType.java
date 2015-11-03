package com.aluxian.codementor.data.types;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.aluxian.codementor.R;

public enum PresenceType {

    AVAILABLE(R.string.status_available, R.color.presence_online),
    ONLINE(R.string.status_online, R.color.presence_online),
    AWAY(R.string.status_away, R.color.presence_away),
    SESSION(R.string.status_session, R.color.presence_busy),
    OFFLINE(R.string.status_offline, android.R.color.white);

    public final int statusResId;
    public final int colorResId;

    PresenceType(int statusResId, int colorResId) {
        this.statusResId = statusResId;
        this.colorResId = colorResId;
    }

    public int getColor(Context context) {
        return ContextCompat.getColor(context, colorResId);
    }

    public static PresenceType parse(String status) {
        if (TextUtils.isEmpty(status)) {
            return OFFLINE;
        }

        try {
            return valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OFFLINE;
        }
    }

}

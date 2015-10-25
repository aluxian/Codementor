package com.aluxian.codementor;

import android.text.TextUtils;

public enum Presence {

    AVAILABLE(R.string.status_available, R.drawable.presence_online),

    ONLINE(R.string.status_online, R.drawable.presence_online),

    AWAY(R.string.status_away, R.drawable.presence_away),

    SESSION(R.string.status_session, R.drawable.presence_busy),

    OFFLINE(R.string.status_offline, R.drawable.presence_offline);

    public final int statusResId;
    public final int backgroundResId;

    Presence(int statusResId, int backgroundResId) {
        this.statusResId = statusResId;
        this.backgroundResId = backgroundResId;
    }

    public static Presence parse(String status) {
        if (TextUtils.isEmpty(status)) {
            return OFFLINE;
        }

        return valueOf(status.toUpperCase());
    }

}

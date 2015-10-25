package com.aluxian.codementor;

public enum Presence {

    AVAILABLE(R.string.status_available, R.drawable.presence_online),

    ONLINE(R.string.status_online, R.drawable.presence_online),

    AWAY(R.string.status_away, R.drawable.presence_away),

    SESSION(R.string.status_session, R.drawable.presence_busy),

    OFFLINE(R.string.status_offline, R.drawable.presence_offline);

    public final int statusResId;
    public final int presenceResId;

    Presence(int statusResId, int presenceResId) {
        this.statusResId = statusResId;
        this.presenceResId = presenceResId;
    }

}

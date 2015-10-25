package com.aluxian.codementor;

public enum Presence {

    AVAILABLE(R.string.status_available),

    ONLINE(R.string.status_online),

    AWAY(R.string.status_away),

    SESSION(R.string.status_session),

    OFFLINE(R.string.status_offline);

    public final int status;

    Presence(int status) {
        this.status = status;
    }

}

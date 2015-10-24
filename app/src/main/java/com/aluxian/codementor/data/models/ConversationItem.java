package com.aluxian.codementor.data.models;

import java.util.UUID;

public class ConversationItem {

    public static final int TYPE_ALIGN_RIGHT = 0x1000000;
    public static final int TYPE_TIME_MARKER = 0x0000001;
    public static final int TYPE_MESSAGE = 0x0000010;
    public static final int TYPE_CONNECT = 0x0000100;
    public static final int TYPE_FILE = 0x0001000;
    public static final int TYPE_SIGNATURE = 0x0010000;
    public static final int TYPE_OTHER = 0x0100000;

    private Message message;
    private TimeMarker timeMarker;

    public ConversationItem(Message message) {
        this.message = message;
    }

    public ConversationItem(TimeMarker timeMarker) {
        this.timeMarker = timeMarker;
    }

    public Message getMessage() {
        return message;
    }

    public TimeMarker getTimeMarker() {
        return timeMarker;
    }

    public int getViewType() {
        int viewType;

        if (isMessage()) {
            viewType = getMessage().getType().typeFlag;

            if (getMessage().sentByCurrentUser()) {
                viewType = viewType | TYPE_ALIGN_RIGHT;
            }
        } else {
            viewType = TYPE_TIME_MARKER;
        }

        return viewType;
    }

    public long getId() {
        if (isMessage()) {
            return UUID.fromString(getMessage().getId()).getMostSignificantBits();
        } else {
            return getTimeMarker().getTimestamp();
        }
    }

    public boolean isMessage() {
        return message != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationItem)) return false;

        ConversationItem that = (ConversationItem) o;
        return !(message != null ? !message.equals(that.message) : that.message != null) && !(timeMarker != null ?
                !timeMarker.equals(that.timeMarker) : that.timeMarker != null);

    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (timeMarker != null ? timeMarker.hashCode() : 0);
        return result;
    }

}

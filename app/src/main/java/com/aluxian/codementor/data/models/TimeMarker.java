package com.aluxian.codementor.data.models;

public class TimeMarker {

    private long timestamp;

    public TimeMarker(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeMarker)) return false;

        TimeMarker that = (TimeMarker) o;
        return timestamp == that.timestamp;

    }

    @Override
    public int hashCode() {
        return (int) (timestamp ^ (timestamp >>> 32));
    }

}

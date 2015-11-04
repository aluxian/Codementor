package com.aluxian.codementor.data.models;

import android.support.annotation.NonNull;

import com.aluxian.codementor.utils.ContentComparable;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.primitives.Longs;

public abstract class ConversationItem implements Comparable<ConversationItem>, ContentComparable<ConversationItem> {

    /**
     * @return The unique ID of this item.
     */
    public abstract long getId();

    /**
     * @return The type of view this item should be bound to.
     */
    public abstract int getLayoutId();

    /**
     * @return A timestamp associated with this item.
     */
    public abstract long getTimestamp();

    /**
     * @return The main text displayed for this item.
     */
    public String getText() {
        return null;
    }

    /**
     * @return An optional, additional text.
     */
    public String getSubtext() {
        return null;
    }

    /**
     * @return Whether the main text should be treated as HTML.
     */
    public boolean isHtmlText() {
        return false;
    }

    /**
     * @return Whether this item has been read by its receiver.
     */
    public boolean isRead() {
        return false;
    }

    /**
     * @return Whether this item has been sent by the currently logged in user.
     */
    public boolean sentByMe() {
        return false;
    }

    @Override
    public int compareTo(@NonNull ConversationItem another) {
        return Longs.compare(getTimestamp(), another.getTimestamp());
    }

    @Override
    public boolean contentEquals(ConversationItem another) {
        return Objects.equal(getSubtext(), another.getSubtext())
                && Objects.equal(getText(), another.getText())
                && Objects.equal(isRead(), another.isRead());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationItem)) return false;
        ConversationItem that = (ConversationItem) o;
        return Objects.equal(getId(), that.getId())
                && Objects.equal(isRead(), that.isRead());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), isRead());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("text", getText())
                .add("subtext", getSubtext())
                .add("timestamp", getTimestamp())
                .toString();
    }

}

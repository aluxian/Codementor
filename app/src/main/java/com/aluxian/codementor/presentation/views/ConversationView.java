package com.aluxian.codementor.presentation.views;

import android.support.annotation.Nullable;

public interface ConversationView extends BaseView {

    /**
     * Change whether the SwipeRefreshLayout should be refreshing or not.
     *
     * @param refreshing True to show the refreshing state, false to hide it.
     */
    void setRefreshing(boolean refreshing);

    /**
     * Change the visibility of the empty state view.
     *
     * @param show Whether to show it or not.
     */
    void showEmptyState(boolean show);

    /**
     * Update the ActionBar subtitle.
     *
     * @param subtitle The new subtitle to set. Can be null, in which case the subtitle will be hidden.
     */
    void setSubtitle(@Nullable String subtitle);

    /**
     * Update the ActionBar subtitle which shows the user's status.
     *
     * @param stringId The resource id of a string to show.
     */
    void setSubtitle(int stringId);

    /**
     * @param text The new text to set.
     */
    void setMessageFieldText(String text);

    /**
     * Mark the conversation as fully loaded.
     *
     * @param loaded Whether all the messages in the conversation have been loaded.
     */
    void setAllMessagesLoaded(boolean loaded);

}

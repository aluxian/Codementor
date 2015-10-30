package com.aluxian.codementor.presentation.views;

import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.adapters.ChatroomsAdapter;

public interface ChatroomsView extends BaseView {

    /**
     * Set the adapter this view should use.
     *
     * @param adapter The adapter to set.
     */
    void setAdapter(ChatroomsAdapter adapter);

    /**
     * Called when a Chatroom entry is selected by the user.
     *
     * @param chatroom The Chatroom object.
     */
    void onChatroomSelected(Chatroom chatroom);

    /**
     * Open the drawer.
     */
    void openDrawer();

    /**
     * Close the drawer.
     */
    void closeDrawer();

    /**
     * Toggle the drawer's state.
     */
    void toggleDrawer();

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
     * Scroll the list to the top.
     */
    void scrollToTop();

}

package com.aluxian.codementor.presentation.listeners;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_ITEMS_THRESHOLD = 10;
    private LinearLayoutManager linearLayoutManager;

    public EndlessRecyclerScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

        int invisibleCount = totalItemCount - visibleItemCount;
        int thresholdPosition = firstVisibleItem + VISIBLE_ITEMS_THRESHOLD;

        boolean shouldLoadMore = invisibleCount <= thresholdPosition;
        if (shouldLoadMore && !isLoading() && !isFullyLoaded()) {
            loadMore();
        }
    }

    /**
     * Start loading more items.
     */
    public abstract void loadMore();

    /**
     * @return Whether all the items have been loaded.
     */
    public abstract boolean isFullyLoaded();

    /**
     * @return Whether the items are currently being loaded.
     */
    public abstract boolean isLoading();

}

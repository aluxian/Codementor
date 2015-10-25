package com.aluxian.codementor.presentation.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_ITEMS_THRESHOLD = 5;
    private LinearLayoutManager linearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

        boolean shouldLoadMore = totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_ITEMS_THRESHOLD;

        if (shouldLoadMore && !isLoading() && !isFullyLoaded()) {
            loadMore();
        }
    }

    public abstract void loadMore();

    public abstract boolean isFullyLoaded();

    public abstract boolean isLoading();

}

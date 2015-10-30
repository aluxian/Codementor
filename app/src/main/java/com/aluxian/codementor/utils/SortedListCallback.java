package com.aluxian.codementor.utils;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;

public class SortedListCallback<T extends ContentComparable<T>> extends SortedListAdapterCallback<T> {

    private boolean reverseOrder;

    public SortedListCallback(RecyclerView.Adapter adapter, boolean reverseOrder) {
        super(adapter);
        this.reverseOrder = reverseOrder;
    }

    @Override
    public int compare(T o1, T o2) {
        int result = o1.compareTo(o2);
        return reverseOrder ? -result : result;
    }

    @Override
    public boolean areContentsTheSame(T oldItem, T newItem) {
        return oldItem.compareContentTo(newItem);
    }

    @Override
    public boolean areItemsTheSame(T item1, T item2) {
        return item1.equals(item2);
    }

}

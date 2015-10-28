package com.aluxian.codementor.utils;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;

public class SortedListCallback<T extends ContentComparable<T>> extends SortedListAdapterCallback<T> {

    public SortedListCallback(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    public int compare(T o1, T o2) {
        return o1.compareTo(o2);
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
